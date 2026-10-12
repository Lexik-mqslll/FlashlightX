package dev.lexik.light;

import dev.lexik.config.FlashlightConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/** Computes the positions of the light cone cast in front of a player. */
public class LightCone {

    /** Sub-block step used while marching a ray. */
    private static final double STEP = 0.25;
    /** Distance skipped at the start of a ray so the holder is not self-lit. */
    private static final double RAY_START = 0.3;

    /** Config values for a single pass, read once instead of per ray. */
    private record Beam(int maxDistance, int minBrightness, float falloff, float pointBlank) {

        static Beam current() {
            FlashlightConfig.Client config = FlashlightConfig.CLIENT;

            return new Beam(config.maxDistance.get(), config.minBrightness.get(),
                    config.falloffStrength.get().floatValue(),
                    config.pointBlankScale.get().floatValue());
        }

        float falloffAt(double dist) {
            return 1.0f - falloff * (float) (dist / maxDistance);
        }
    }

    /**
     * Compute bright spots for the current frame.
     * @return map of BlockPos → light level (0-15)
     */
    public static Map<BlockPos, Integer> compute(Player player) {
        Map<BlockPos, Integer> positions = new LinkedHashMap<>();
        Level level = player.level();

        FlashlightConfig.Client config = FlashlightConfig.CLIENT;
        Beam beam = Beam.current();

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        BlockPos fallback = findFallback(level, eye);

        // center
        addRay(eye, look, config.centerBrightness.get(), positions, level, fallback, beam);

        // cone
        Vec3 right = new Vec3(look.z, 0, -look.x).normalize();
        Vec3 up = right.cross(look).normalize();
        float halfRad = config.coneHalfAngle.get().floatValue() * (Mth.PI / 180.0f);

        int[] ringBrightness = {config.innerRingBrightness.get(),
                config.middleRingBrightness.get(), config.outerRingBrightness.get()};
        double[] ringSpread = {0.35, 0.7, 1.0};
        double[] ringPhase = {0.0, 0.5, 1.0};

        for (int ring = 0; ring < 3; ring++) {
            for (int i = 0; i < 3; i++) {
                double angle = (2.0 * Math.PI * i) / 3.0 + ringPhase[ring];
                Vec3 dir = offsetDirection(look, right, up, angle, halfRad * ringSpread[ring]);
                addRay(eye, dir, ringBrightness[ring], positions, level, fallback, beam);
            }
        }

        return positions;
    }

    private static Vec3 offsetDirection(Vec3 look, Vec3 right, Vec3 up,
                                         double angle, double spreadRad) {
        double hOff = Math.cos(angle) * spreadRad;
        double vOff = Math.sin(angle) * spreadRad;
        return look.add(right.scale(hOff)).add(up.scale(vOff)).normalize();
    }

    /** Marches a ray, lighting open blocks until something blocks the light. */
    private static void addRay(Vec3 eye, Vec3 direction, int baseLevel,
                                Map<BlockPos, Integer> positions, Level level,
                                BlockPos fallback, Beam beam) {
        BlockPos sampled = null;
        BlockPos lastLit = null;
        int steps = Mth.ceil((beam.maxDistance() - RAY_START) / STEP);

        for (int i = 0; i <= steps; i++) {
            double dist = Math.min(RAY_START + i * STEP, beam.maxDistance());
            BlockPos pos = BlockPos.containing(eye.add(direction.scale(dist)));

            // same block as the previous sample - nothing new to test
            if (pos.equals(sampled)) continue;
            sampled = pos;

            BlockState state = level.getBlockState(pos);

            if (blocksBeam(state, level, pos)) {
                // Point-blank wall: light a spot beside the player instead.
                if (lastLit == null && fallback != null) {
                    put(positions, fallback,
                            Math.round(baseLevel * beam.pointBlank()), beam);
                }
                return;
            }

            // Blocks that pass light but cannot hold one are stepped over.
            if (canHoldLight(state)) {
                put(positions, pos, Math.round(baseLevel * beam.falloffAt(dist)), beam);
                lastLit = pos;
            }
        }
    }

    /** Nearest position to the eye that can hold the point-blank light. */
    private static BlockPos findFallback(Level level, Vec3 eye) {
        BlockPos eyePos = BlockPos.containing(eye);

        if (canHoldLight(level.getBlockState(eyePos))) return eyePos;

        // Inside a door or trapdoor: borrow a neighbouring block.
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = eyePos.relative(direction);

            if (canHoldLight(level.getBlockState(neighbour))) return neighbour;
        }

        return null;
    }

    /** Whether the block stops the beam instead of letting it pass. */
    private static boolean blocksBeam(BlockState state, Level level, BlockPos pos) {
        return state.getLightBlock(level, pos) >= 15;
    }

    /**
     * Blocks that may be replaced by a light block: air, water sources and the
     * light blocks placed by earlier passes. Replacing anything else would
     * destroy it client-side.
     */
    public static boolean canHoldLight(BlockState state) {
        return state.isAir() || state.is(Blocks.LIGHT) || isWaterSource(state);
    }

    /** A waterlogged light always draws a full block, so only sources qualify. */
    private static boolean isWaterSource(BlockState state) {
        return state.is(Blocks.WATER) && state.getFluidState().isSource();
    }

    private static void put(Map<BlockPos, Integer> positions, BlockPos pos, int value, Beam beam) {
        if (value < beam.minBrightness()) return;

        int existing = positions.getOrDefault(pos, 0);
        positions.put(pos, Math.max(existing, Mth.clamp(value, 0, 15)));
    }
}

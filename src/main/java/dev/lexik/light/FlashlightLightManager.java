package dev.lexik.light;

import dev.lexik.config.FlashlightConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.*;

/**
 * Places and removes the temporary light blocks. Client-side only; the server
 * never sees these blocks.
 */
public class FlashlightLightManager {

    /** Currently active light positions + their levels */
    private final Set<BlockPos> activeLights = new HashSet<>();
    private final Map<BlockPos, BlockState> originalStates = new HashMap<>();

    /** Tick counter for throttling */
    private int tickCounter = 0;

    /** Applies the light field for this frame, throttled by the config. */
    public void update(net.minecraft.client.multiplayer.ClientLevel level,
                       Map<BlockPos, Integer> newPositions) {
        tickCounter++;

        // Throttled: every tick is smoothest but costs the most.
        if (tickCounter % FlashlightConfig.CLIENT.updateInterval.get() != 0) return;

        // --- 1. Remove lights that are no longer needed ---
        Set<BlockPos> toRemove = new HashSet<>(activeLights);
        toRemove.removeAll(newPositions.keySet());

        for (BlockPos pos : toRemove) {
            restoreBlock(level, pos);
        }

        // --- 2. Update existing lights where level changed ---
        for (Map.Entry<BlockPos, Integer> entry : newPositions.entrySet()) {
            BlockPos pos = entry.getKey();
            int targetLevel = entry.getValue();

            if (pos == null) continue;
            if (!level.isLoaded(pos)) continue;

            BlockState existing = level.getBlockState(pos);

            if (existing.is(Blocks.LIGHT)) {
                int currentLevel = existing.getValue(LightBlock.LEVEL);
                if (currentLevel != targetLevel) {
                    // Reuse the existing state so WATERLOGGED is not lost.
                    level.setBlock(pos, existing.setValue(LightBlock.LEVEL, targetLevel),
                            3); // flag 3: client-side update, no neighbours
                }
            } else if (LightCone.canHoldLight(existing)) {
                // Place a new light block
                originalStates.put(pos, existing);
                level.setBlock(pos,
                        Blocks.LIGHT.defaultBlockState()
                                .setValue(LightBlock.LEVEL, targetLevel)
                                .setValue(LightBlock.WATERLOGGED,
                                        existing.getFluidState().is(Fluids.WATER)),
                        3);
            }
        }

        // --- 3. Track active lights ---
        activeLights.clear();
        activeLights.addAll(newPositions.keySet());
    }

    /** Restores a position to the state recorded before the light was placed. */
    private void restoreBlock(net.minecraft.client.multiplayer.ClientLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) return;

        BlockState current = level.getBlockState(pos);
        if (current.is(Blocks.LIGHT)) {
            BlockState original = originalStates.getOrDefault(pos, Blocks.AIR.defaultBlockState());
            level.setBlock(pos, original, 3);
            originalStates.remove(pos);
        }
    }

    /** Removes every placed light and restores the original blocks. */
    public void clearAll(net.minecraft.client.multiplayer.ClientLevel level) {
        for (BlockPos pos : new HashSet<>(activeLights)) {
            restoreBlock(level, pos);
        }
        activeLights.clear();
    }

    public boolean hasActiveLights() {
        return !activeLights.isEmpty();
    }
}
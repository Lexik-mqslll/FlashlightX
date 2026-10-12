package dev.lexik.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Mod configuration. The beam is drawn client-side and sits in the client
 * config; battery drain runs server-side and sits in the common config.
 */
public class FlashlightConfig {

    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;

    static {
        Pair<Client, ModConfigSpec> client = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = client.getLeft();
        CLIENT_SPEC = client.getRight();

        Pair<Common, ModConfigSpec> common = new ModConfigSpec.Builder().configure(Common::new);
        COMMON = common.getLeft();
        COMMON_SPEC = common.getRight();
    }

    public static class Client {

        // --- beam shape ---
        public final ModConfigSpec.IntValue maxDistance;
        public final ModConfigSpec.DoubleValue coneHalfAngle;
        public final ModConfigSpec.IntValue centerBrightness;
        public final ModConfigSpec.IntValue innerRingBrightness;
        public final ModConfigSpec.IntValue middleRingBrightness;
        public final ModConfigSpec.IntValue outerRingBrightness;
        public final ModConfigSpec.IntValue minBrightness;
        public final ModConfigSpec.DoubleValue falloffStrength;
        public final ModConfigSpec.DoubleValue pointBlankScale;

        // --- performance ---
        public final ModConfigSpec.IntValue updateInterval;
        public final ModConfigSpec.BooleanValue showOtherPlayers;
        public final ModConfigSpec.IntValue maxSources;
        public final ModConfigSpec.DoubleValue maxSourceDistance;

        // --- look ---
        public final ModConfigSpec.BooleanValue glowingLens;
        public final ModConfigSpec.BooleanValue raiseArm;
        public final ModConfigSpec.DoubleValue armAngle;

        Client(ModConfigSpec.Builder builder) {
            builder.comment("How the light beam is shaped.").push("beam");
            maxDistance = builder
                    .comment("How far the beam reaches, in blocks.")
                    .defineInRange("maxDistance", 14, 1, 48);
            coneHalfAngle = builder
                    .comment("Half-angle of the cone in degrees. Larger spreads the light wider.")
                    .defineInRange("coneHalfAngle", 5.5, 0.5, 45.0);
            centerBrightness = builder
                    .comment("Light level of the central ray (0-15).")
                    .defineInRange("centerBrightness", 15, 0, 15);
            innerRingBrightness = builder
                    .comment("Light level of the inner ring of rays.")
                    .defineInRange("innerRingBrightness", 13, 0, 15);
            middleRingBrightness = builder
                    .comment("Light level of the middle ring of rays.")
                    .defineInRange("middleRingBrightness", 11, 0, 15);
            outerRingBrightness = builder
                    .comment("Light level of the outer ring of rays.")
                    .defineInRange("outerRingBrightness", 8, 0, 15);
            minBrightness = builder
                    .comment("Positions dimmer than this are skipped entirely.")
                    .defineInRange("minBrightness", 3, 0, 15);
            falloffStrength = builder
                    .comment("Share of brightness lost across the full beam length.")
                    .defineInRange("falloffStrength", 0.88, 0.0, 1.0);
            pointBlankScale = builder
                    .comment("Brightness multiplier when the beam is pressed against a wall.")
                    .defineInRange("pointBlankScale", 0.85, 0.0, 1.0);
            builder.pop();

            builder.comment("Lighting cost.").push("performance");
            updateInterval = builder
                    .comment("Ticks between light updates. Lower is smoother, higher is cheaper.")
                    .defineInRange("updateInterval", 2, 1, 20);
            showOtherPlayers = builder
                    .comment("Draw the beams of other players holding a lit flashlight.")
                    .define("showOtherPlayers", true);
            maxSources = builder
                    .comment("Maximum number of flashlights drawn at once, nearest first.")
                    .defineInRange("maxSources", 4, 1, 16);
            maxSourceDistance = builder
                    .comment("Ignore flashlights further away than this, in blocks.")
                    .defineInRange("maxSourceDistance", 48.0, 8.0, 128.0);
            builder.pop();

            builder.comment("Cosmetic details.").push("appearance");
            glowingLens = builder
                    .comment("Draw the lens full-bright while the flashlight is on.")
                    .define("glowingLens", true);
            raiseArm = builder
                    .comment("Raise the arm that holds the flashlight instead of letting it hang.")
                    .define("raiseArm", true);
            armAngle = builder
                    .comment("Angle of the raised arm, in degrees.")
                    .defineInRange("armAngle", -171.0, -180.0, 180.0);
            builder.pop();
        }
    }

    public static class Common {

        public final ModConfigSpec.IntValue batterySeconds;
        public final ModConfigSpec.IntValue drainInterval;
        public final ModConfigSpec.BooleanValue autoSwapBattery;
        public final ModConfigSpec.BooleanValue turnOffWhenStowed;
        public final ModConfigSpec.BooleanValue consumeInCreative;
        public final ModConfigSpec.BooleanValue playSounds;
        public final ModConfigSpec.DoubleValue soundVolume;

        Common(ModConfigSpec.Builder builder) {
            builder.comment("Battery life and handling.").push("battery");
            batterySeconds = builder
                    .comment("Seconds of light one battery provides.")
                    .defineInRange("batterySeconds", 180, 1, 36000);
            drainInterval = builder
                    .comment("Ticks between charge updates.",
                            "Each update re-syncs the item to everyone tracking the player.")
                    .defineInRange("drainInterval", 20, 1, 200);
            autoSwapBattery = builder
                    .comment("Pull a fresh battery from the inventory when the current one dies.")
                    .define("autoSwapBattery", true);
            turnOffWhenStowed = builder
                    .comment("Switch the flashlight off when it leaves your hands.")
                    .define("turnOffWhenStowed", true);
            consumeInCreative = builder
                    .comment("Use up batteries in creative mode as well.")
                    .define("consumeInCreative", true);
            builder.pop();

            builder.comment("Sound played when toggling.").push("sound");
            playSounds = builder
                    .comment("Play the click when switching the flashlight on or off.")
                    .define("playSounds", true);
            soundVolume = builder
                    .comment("Volume of that click.")
                    .defineInRange("soundVolume", 0.7, 0.0, 1.0);
            builder.pop();
        }
    }
}

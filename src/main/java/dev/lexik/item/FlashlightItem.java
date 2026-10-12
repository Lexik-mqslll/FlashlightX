package dev.lexik.item;

import dev.lexik.config.FlashlightConfig;
import dev.lexik.sound.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FlashlightItem extends Item implements GeoItem {

    /** How long a single battery lasts while switched on, in ticks. */
    public static int batteryDuration() {
        return FlashlightConfig.COMMON.batterySeconds.get() * 20;
    }

    /** Button press down + held-down idle. */
    private static final RawAnimation TURN_ON = RawAnimation.begin()
            .thenPlay("flashlight_on")
            .thenLoop("flashlight_on_idle");

    /** Button release + resting idle. */
    private static final RawAnimation TURN_OFF = RawAnimation.begin()
            .thenPlay("flashlight_off")
            .thenLoop("flashlight_off_idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FlashlightItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (isOn(stack)) {
                setOn(stack, false);
                playClick(level, player, ModSounds.FLASHLIGHT_OFF.get());
            } else if (getCharge(stack) > 0 || loadBattery(stack, player)) {
                setOn(stack, true);
                playClick(level, player, ModSounds.FLASHLIGHT_ON.get());
            } else {
                // No charge and no spare battery: a dry click.
                playClick(level, player, ModSounds.FLASHLIGHT_OFF.get());
            }
        }

        // CONSUME keeps the interaction without the vanilla arm swing.
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Each stack needs its own id, or they share one animation instance.
        GeoItem.getOrAssignId(stack, serverLevel);

        if (!isOn(stack) || !(entity instanceof Player player)) return;

        // Switch off once the flashlight leaves the hands.
        if (FlashlightConfig.COMMON.turnOffWhenStowed.get() && !isHeld(stack, player)) {
            setOn(stack, false);
            playClick(level, player, ModSounds.FLASHLIGHT_OFF.get());
            return;
        }

        drain(stack, player, level);
    }

    /** Whether this exact stack is held in one of the hands. */
    private static boolean isHeld(ItemStack stack, LivingEntity holder) {
        return holder.getMainHandItem() == stack || holder.getOffhandItem() == stack;
    }

    /** Drains the loaded battery, swapping in a spare when it runs out. */
    private static void drain(ItemStack stack, Player player, Level level) {
        // Writing the state re-syncs the stack, so drain on an interval.
        int interval = FlashlightConfig.COMMON.drainInterval.get();
        if (level.getGameTime() % interval != 0) return;

        int charge = getCharge(stack) - interval;

        if (charge > 0) {
            setCharge(stack, charge);
            return;
        }

        if (FlashlightConfig.COMMON.autoSwapBattery.get() && loadBattery(stack, player)) {
            return;
        }

        setCharge(stack, 0);
        setOn(stack, false);
        playClick(level, player, ModSounds.FLASHLIGHT_OFF.get());
    }

    /** Loads one battery from the inventory and refills the charge. */
    private static boolean loadBattery(ItemStack stack, Player player) {
        Inventory inventory = player.getInventory();
        boolean consume = FlashlightConfig.COMMON.consumeInCreative.get()
                || !player.hasInfiniteMaterials();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);

            if (candidate.is(ModItems.BATTERY.get())) {
                if (consume) {
                    candidate.shrink(1);
                }
                setCharge(stack, batteryDuration());
                return true;
            }
        }

        return false;
    }

    private static void playClick(Level level, Player player, SoundEvent sound) {
        if (!FlashlightConfig.COMMON.playSounds.get()) return;

        level.playSound(null, player.getX(), player.getY(), player.getZ(), sound,
                SoundSource.PLAYERS,
                FlashlightConfig.COMMON.soundVolume.get().floatValue(), 1.0f);
    }

    // --- State ---

    public static boolean isOn(ItemStack stack) {
        return Boolean.TRUE.equals(stack.get(ModItems.FLASHLIGHT_ON.value()));
    }

    private static void setOn(ItemStack stack, boolean on) {
        stack.set(ModItems.FLASHLIGHT_ON.value(), on);
    }

    /** Ticks of light left in the loaded battery. */
    public static int getCharge(ItemStack stack) {
        Integer charge = stack.get(ModItems.CHARGE.value());
        return charge == null ? 0 : charge;
    }

    private static void setCharge(ItemStack stack, int charge) {
        stack.set(ModItems.CHARGE.value(), Mth.clamp(charge, 0, batteryDuration()));
    }

    /**
     * The charge changes while the light burns; without this vanilla treats each
     * update as an item swap and keeps the arm lowered.
     */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !oldStack.is(newStack.getItem());
    }

    // --- Charge bar ---

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCharge(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Mth.clamp(Math.round(13.0f * getCharge(stack) / batteryDuration()), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // Green when full, red when nearly empty.
        float remaining = Mth.clamp((float) getCharge(stack) / batteryDuration(), 0.0f, 1.0f);
        return Mth.hsvToRgb(remaining / 3.0f, 1.0f, 1.0f);
    }

    // --- GeckoLib ---

    /**
     * Gives each render perspective its own animation instance; otherwise they
     * poll one controller several times per frame and the animation stutters.
     */
    @Override
    public boolean isPerspectiveAware() {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // No transition: the animations share their first and last keyframe.
        controllers.add(new AnimationController<>(this, "flashlight", 0, this::handleAnimation));
    }

    private PlayState handleAnimation(AnimationState<FlashlightItem> state) {
        ItemStack stack = state.getData(DataTickets.ITEMSTACK);
        boolean on = stack != null && isOn(stack);

        return state.setAndContinue(on ? TURN_ON : TURN_OFF);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}

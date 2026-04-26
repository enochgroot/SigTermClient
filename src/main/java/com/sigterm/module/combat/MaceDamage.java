package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import java.util.Comparator;

public class MaceDamage extends Module {
    private final Setting fallDist;
    private final Setting range;
    private final Setting mode; // 0 = spoof fallDistance, 1 = auto jump+attack

    public MaceDamage() {
        super("MaceDamage", "Boost mace damage via fall distance", Category.COMBAT, GLFW.GLFW_KEY_G);
        fallDist = addSetting("FallDist", 10, 3, 100, 1, " blocks");
        range = addSetting("Range", 5.0, 3.0, 6.0, 0.5, "m");
        mode = addSetting("Mode", 0, 0, 1, 1, ""); // 0=spoof, 1=jump
    }

    private int jumpTicks = 0;

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null || mc().gameMode == null) return;
        if (!mc().player.getMainHandItem().is(Items.MACE)) return;

        float cooldown = mc().player.getAttackStrengthScale(0f);
        if (cooldown < 1.0f) return;

        double r = range.value;
        Entity target = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(r), e ->
                e instanceof LivingEntity le && le != mc().player && le.isAlive()
                && le.distanceTo(mc().player) <= r
        ).stream().min(Comparator.comparingDouble(e -> e.distanceTo(mc().player))).orElse(null);

        if (target == null) { jumpTicks = 0; return; }

        if (mode.value < 0.5) {
            // Mode 0: Spoof — set fallDistance right before attacking
            float savedFall = mc().player.fallDistance;
            mc().player.fallDistance = (float) fallDist.value;
            mc().player.swing(mc().player.getUsedItemHand());
            mc().gameMode.attack(mc().player, target);
            mc().player.resetAttackStrengthTicker();
            mc().player.fallDistance = savedFall;
        } else {
            // Mode 1: Jump and attack on the way down
            if (mc().player.onGround()) {
                mc().player.jumpFromGround();
                jumpTicks = 0;
            }
            jumpTicks++;
            // Attack when falling and past the peak
            if (jumpTicks > 6 && mc().player.getDeltaMovement().y < -0.1) {
                mc().player.swing(mc().player.getUsedItemHand());
                mc().gameMode.attack(mc().player, target);
                mc().player.resetAttackStrengthTicker();
                jumpTicks = 0;
            }
        }
    }
}

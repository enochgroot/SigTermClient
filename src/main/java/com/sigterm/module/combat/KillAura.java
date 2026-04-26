package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;
import java.util.Comparator;

public class KillAura extends Module {
    private final Setting range;
    private final Setting tickDelaySetting;
    private int tickDelay = 0;

    public KillAura() {
        super("KillAura", "Attacks nearby entities", Category.COMBAT, GLFW.GLFW_KEY_R);
        range = addSetting("Range", 4.5, 2.0, 6.0, 0.5, "m");
        tickDelaySetting = addSetting("Delay", 1, 0, 10, 1, " ticks");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null) return;
        if (tickDelay > 0) { tickDelay--; return; }
        float cooldown = mc().player.getAttackStrengthScale(0f);
        if (cooldown < 0.9f) return;

        double r = range.value;
        Entity closest = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(r), e ->
                e instanceof LivingEntity le && le != mc().player
                && le.isAlive() && le.distanceTo(mc().player) <= r
        ).stream().min(Comparator.comparingDouble(e -> e.distanceTo(mc().player))).orElse(null);

        if (closest != null) {
            mc().player.swing(mc().player.getUsedItemHand());
            mc().gameMode.attack(mc().player, closest);
            mc().player.resetAttackStrengthTicker();
            tickDelay = (int) tickDelaySetting.value;
        }
    }
}

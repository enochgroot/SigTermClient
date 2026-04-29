package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import java.util.Comparator;

public class Reach extends Module {
    private final Setting range;
    private int tickTimer = 0;

    public Reach() {
        super("Reach", "Extended attack range with auto-rotation", Category.COMBAT, 0);
        range = addSetting("Range", 4.5, 3.0, 6.0, 0.5, "m");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null) return;
        tickTimer++;
        if (tickTimer < 6) return;
        double r = range.value;
        Entity target = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(r), e ->
                e instanceof LivingEntity le && le != mc().player
                && le.isAlive() && le.distanceTo(mc().player) <= r
        ).stream().min(Comparator.comparingDouble(e -> e.distanceTo(mc().player))).orElse(null);
        if (target == null) return;
        double dx = target.getX() - mc().player.getX();
        double dy = target.getEyeY() - mc().player.getEyeY();
        double dz = target.getZ() - mc().player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        mc().player.setYRot(yaw);
        mc().player.setXRot(Math.max(-90f, Math.min(90f, pitch)));
        mc().player.swing(mc().player.getUsedItemHand());
        mc().gameMode.attack(mc().player, target);
        mc().player.resetAttackStrengthTicker();
        tickTimer = 0;
    }

    @Override
    public void onEnable() { tickTimer = 0; }
}

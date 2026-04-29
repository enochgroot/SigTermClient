package com.sigterm.module.player;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.phys.Vec3;

public class KnockbackDelay extends Module {
    private final Setting cancelKnockback;
    private Vec3 prevVelocity = null;

    public KnockbackDelay() {
        super("KnockbackDelay", "Cancel knockback by detecting velocity spikes", Category.PLAYER, 0);
        cancelKnockback = addSetting("Cancel", true, false, true, "");
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        Vec3 current = mc().player.getDeltaMovement();
        if (prevVelocity != null) {
            double prevH = Math.sqrt(prevVelocity.x * prevVelocity.x + prevVelocity.z * prevVelocity.z);
            double currH = Math.sqrt(current.x * current.x + current.z * current.z);
            if (currH > prevH + 0.15 && currH > 0.3) {
                if (cancelKnockback.value) {
                    mc().player.setDeltaMovement(prevVelocity);
                } else {
                    double kbDiff = currH - prevH;
                    Vec3 dir = current.normalize();
                    double newH = prevH + kbDiff * 0.5;
                    mc().player.setDeltaMovement(dir.x * newH, current.y, dir.z * newH);
                }
            }
        }
        prevVelocity = mc().player.getDeltaMovement();
    }

    @Override
    public void onDisable() { prevVelocity = null; }
}

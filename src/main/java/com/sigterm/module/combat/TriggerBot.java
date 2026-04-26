package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;

public class TriggerBot extends Module {
    private final Setting range;
    private final Setting delayTicks;
    private int tickTimer = 0;

    public TriggerBot() {
        super("TriggerBot", "Auto-attack when crosshair is on an entity", Category.COMBAT, 0);
        range = addSetting("Range", 4.5, 2.0, 6.0, 0.5, "m");
        delayTicks = addSetting("Delay", 3, 0, 20, 1, " ticks");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null) return;

        tickTimer++;
        if (tickTimer < (int) delayTicks.value) return;

        // Check if crosshair is targeting an entity
        Entity target = mc().player.getTarget(6.0, 1.0f);
        
        if (target instanceof LivingEntity living && living.isAlive() 
            && target != mc().player && target.distanceTo(mc().player) <= range.value) {
            
            float cooldown = mc().player.getAttackStrengthScale(0f);
            if (cooldown < 1.0f) return;

            mc().player.swing(mc().player.getUsedItemHand());
            mc().gameMode.attack(mc().player, target);
            mc().player.resetAttackStrengthTicker();
            tickTimer = 0;
        }
    }

    @Override
    public void onEnable() { tickTimer = 0; }
}

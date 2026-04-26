package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import java.util.Comparator;

public class MaceDamage extends Module {
    private final Setting spoofHeight;
    private final Setting range;
    private int state = 0; // 0=ready, 1=spoofing up, 2=spoofing down, 3=attack
    private int spoofTick = 0;

    public MaceDamage() {
        super("MaceDamage", "Spoof fall distance for massive mace hits", Category.COMBAT, GLFW.GLFW_KEY_G);
        spoofHeight = addSetting("Height", 10, 3, 100, 1, " blocks");
        range = addSetting("Range", 5.0, 3.0, 6.0, 0.5, "m");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null || mc().gameMode == null) return;
        if (!mc().player.getMainHandItem().is(Items.MACE)) return;

        double r = range.value;
        Entity target = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(r), e ->
                e instanceof LivingEntity le && le != mc().player && le.isAlive()
                && le.distanceTo(mc().player) <= r
        ).stream().min(Comparator.comparingDouble(e -> e.distanceTo(mc().player))).orElse(null);

        if (target == null) { state = 0; spoofTick = 0; return; }

        float cooldown = mc().player.getAttackStrengthScale(0f);
        if (cooldown < 1.0f) return;

        double px = mc().player.getX();
        double py = mc().player.getY();
        double pz = mc().player.getZ();
        double height = spoofHeight.value;

        switch (state) {
            case 0 -> {
                // Send packets going UP (server thinks we jumped high)
                int steps = (int) Math.min(height, 20);
                for (int i = 1; i <= steps; i++) {
                    mc().player.connection.send(new ServerboundMovePlayerPacket.Pos(
                        px, py + (height * i / steps), pz, false, mc().player.horizontalCollision));
                }
                state = 1;
                spoofTick = 0;
            }
            case 1 -> {
                // Send packets coming DOWN (server calculates fall distance)
                int steps = (int) Math.min(height, 20);
                for (int i = steps; i >= 0; i--) {
                    mc().player.connection.send(new ServerboundMovePlayerPacket.Pos(
                        px, py + (height * i / steps), pz, false, mc().player.horizontalCollision));
                }
                // Final position = on ground
                mc().player.connection.send(new ServerboundMovePlayerPacket.Pos(
                    px, py, pz, true, mc().player.horizontalCollision));
                state = 2;
            }
            case 2 -> {
                // Attack with spoofed fall distance
                mc().player.swing(mc().player.getUsedItemHand());
                mc().gameMode.attack(mc().player, target);
                mc().player.resetAttackStrengthTicker();
                state = 0;
            }
        }
    }
}

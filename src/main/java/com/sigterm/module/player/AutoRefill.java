package com.sigterm.module.player;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class AutoRefill extends Module {
    private final Setting threshold;
    private final Setting delay;
    private int cooldown = 0;

    public AutoRefill() {
        super("AutoRefill", "Refill hand from inventory when stack runs low", Category.PLAYER, 0);
        threshold = addSetting("Threshold", 8, 1, 32, 1, " items");
        delay = addSetting("Delay", 2, 0, 10, 1, " ticks");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().gameMode == null) return;
        if (cooldown > 0) { cooldown--; return; }

        ItemStack mainHand = mc().player.getMainHandItem();
        if (mainHand.isEmpty()) return;
        if (!mainHand.isStackable()) return;
        if (mainHand.getCount() > (int) threshold.value) return;

        // Find same item in inventory to refill
        var container = mc().player.containerMenu;
        int heldSlot = mc().player.getInventory().selected + 36; // hotbar slots are 36-44 in container

        for (int i = 9; i < 36; i++) { // main inventory slots (skip hotbar + armor)
            ItemStack stack = container.slots.get(i).getItem();
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, mainHand)) {
                // Swap: pick up inventory stack, place on hotbar, put remainder back
                int containerId = container.containerId;
                mc().gameMode.handleInventoryMouseClick(containerId, i, 0, ClickType.PICKUP, mc().player);
                mc().gameMode.handleInventoryMouseClick(containerId, heldSlot, 0, ClickType.PICKUP, mc().player);
                mc().gameMode.handleInventoryMouseClick(containerId, i, 0, ClickType.PICKUP, mc().player);
                cooldown = (int) delay.value;
                return;
            }
        }
    }
}

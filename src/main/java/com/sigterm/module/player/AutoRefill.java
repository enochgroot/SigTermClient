package com.sigterm.module.player;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class AutoRefill extends Module {
    private final Setting threshold;
    private final Setting delay;
    private int cooldown = 0;

    public AutoRefill() {
        super("AutoRefill", "Refill hand from inventory when low", Category.PLAYER, 0);
        threshold = addSetting("Threshold", 8, 1, 32, 1, " items");
        delay = addSetting("Delay", 2, 0, 10, 1, " ticks");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().gameMode == null) return;
        if (cooldown > 0) { cooldown--; return; }

        ItemStack mainHand = mc().player.getMainHandItem();
        if (mainHand.isEmpty() || !mainHand.isStackable()) return;
        if (mainHand.getCount() > (int) threshold.value) return;

        var container = mc().player.containerMenu;

        // Find which hotbar slot holds our item (slots 36-44 in container)
        int heldSlot = -1;
        for (int i = 36; i <= 44; i++) {
            ItemStack hs = container.slots.get(i).getItem();
            if (ItemStack.isSameItemSameComponents(hs, mainHand) && hs.getCount() == mainHand.getCount()) {
                heldSlot = i;
                break;
            }
        }
        if (heldSlot == -1) return;

        // Find matching stack in main inventory (slots 9-35)
        for (int i = 9; i < 36; i++) {
            ItemStack stack = container.slots.get(i).getItem();
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, mainHand)) {
                int cid = container.containerId;
                mc().gameMode.handleInventoryMouseClick(cid, i, 0, ClickType.PICKUP, mc().player);
                mc().gameMode.handleInventoryMouseClick(cid, heldSlot, 0, ClickType.PICKUP, mc().player);
                mc().gameMode.handleInventoryMouseClick(cid, i, 0, ClickType.PICKUP, mc().player);
                cooldown = (int) delay.value;
                return;
            }
        }
    }
}

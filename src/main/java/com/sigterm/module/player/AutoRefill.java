package com.sigterm.module.player;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class AutoRefill extends Module {
    private final Setting threshold;
    private int cooldown = 0;

    public AutoRefill() {
        super("AutoRefill", "Refill hand from inventory when low (shift-click)", Category.PLAYER, 0);
        threshold = addSetting("Threshold", 8, 1, 32, 1, " items");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().gameMode == null) return;
        if (cooldown > 0) { cooldown--; return; }
        ItemStack mainHand = mc().player.getMainHandItem();
        if (mainHand.isEmpty() || !mainHand.isStackable()) return;
        if (mainHand.getCount() > (int) threshold.value) return;
        var container = mc().player.containerMenu;
        int cid = container.containerId;
        for (int i = 9; i < 36 && i < container.slots.size(); i++) {
            ItemStack stack = container.slots.get(i).getItem();
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, mainHand)) {
                mc().gameMode.handleInventoryMouseClick(cid, i, 1, ClickType.SHIFT, mc().player);
                cooldown = 3;
                return;
            }
        }
    }
}

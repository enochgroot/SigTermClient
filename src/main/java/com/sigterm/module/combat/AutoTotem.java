package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class AutoTotem extends Module {
    private int state = 0; // 0=idle, 1=opened inv, 2=clicking, 3=closing
    private int totemSlot = -1;
    private int cooldown = 0;

    public AutoTotem() {
        super("AutoTotem", "Auto-swaps totem to offhand via inventory", Category.COMBAT, GLFW.GLFW_KEY_T);
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().gameMode == null) return;
        if (cooldown > 0) { cooldown--; return; }

        ItemStack offhand = mc().player.getOffhandItem();
        if (offhand.is(Items.TOTEM_OF_UNDYING)) {
            state = 0;
            return;
        }

        switch (state) {
            case 0 -> {
                totemSlot = findTotemInInventory();
                if (totemSlot == -1) return;
                // Send open inventory packet — simulate pressing E
                mc().execute(() -> mc().setScreen(new InventoryScreen(mc().player)));
                state = 1;
                cooldown = 2;
            }
            case 1 -> {
                if (!(mc().screen instanceof InventoryScreen)) {
                    state = 0; return;
                }
                // Pick up totem from its slot
                mc().gameMode.handleInventoryMouseClick(
                    mc().player.containerMenu.containerId,
                    totemSlot, 0, ClickType.PICKUP, mc().player);
                state = 2;
                cooldown = 1;
            }
            case 2 -> {
                if (!(mc().screen instanceof InventoryScreen)) {
                    state = 0; return;
                }
                // Place in offhand slot (slot 45 in player container)
                mc().gameMode.handleInventoryMouseClick(
                    mc().player.containerMenu.containerId,
                    45, 0, ClickType.PICKUP, mc().player);
                state = 3;
                cooldown = 1;
            }
            case 3 -> {
                // Close inventory
                mc().player.closeContainer();
                mc().setScreen(null);
                state = 0;
                cooldown = 5;
            }
        }
    }

    private int findTotemInInventory() {
        if (mc().player == null) return -1;
        var container = mc().player.containerMenu;
        for (int i = 0; i < container.slots.size(); i++) {
            ItemStack stack = container.slots.get(i).getItem();
            if (stack.is(Items.TOTEM_OF_UNDYING)) return i;
        }
        return -1;
    }
}

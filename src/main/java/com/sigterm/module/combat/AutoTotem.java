package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class AutoTotem extends Module {
    private final Setting delay;
    private int cooldown = 0;

    public AutoTotem() {
        super("AutoTotem", "Auto-swaps totem to offhand", Category.COMBAT, GLFW.GLFW_KEY_T);
        delay = addSetting("Delay", 1, 0, 10, 1, " ticks");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().gameMode == null) return;
        if (cooldown > 0) { cooldown--; return; }

        ItemStack offhand = mc().player.getOffhandItem();
        if (offhand.is(Items.TOTEM_OF_UNDYING)) return;

        int totemSlot = findTotemSlot();
        if (totemSlot == -1) return;

        // Direct inventory click — no screen needed
        // Pick up totem
        int containerId = mc().player.containerMenu.containerId;
        mc().gameMode.handleInventoryMouseClick(containerId, totemSlot, 0, ClickType.PICKUP, mc().player);
        // Place in offhand (slot 45)
        mc().gameMode.handleInventoryMouseClick(containerId, 45, 0, ClickType.PICKUP, mc().player);
        // If something was in offhand, put it back where totem was
        mc().gameMode.handleInventoryMouseClick(containerId, totemSlot, 0, ClickType.PICKUP, mc().player);

        cooldown = (int) delay.value;
    }

    private int findTotemSlot() {
        var container = mc().player.containerMenu;
        for (int i = 0; i < container.slots.size(); i++) {
            if (i == 45) continue; // skip offhand slot itself
            if (container.slots.get(i).getItem().is(Items.TOTEM_OF_UNDYING)) return i;
        }
        return -1;
    }
}

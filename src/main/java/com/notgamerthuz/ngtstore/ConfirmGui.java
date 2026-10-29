package com.notgamerthuz.ngtstore;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ConfirmGui extends Gui {
    public ConfirmGui(String title, ItemStack display, Runnable yes, Runnable no) {
        super(Brand.title(title), 3);
        set(13, display);
        set(11, Items.make(Material.LIME_CONCRETE, 1, "<green><bold>CONFIRM", "<gray>Click to confirm"), e -> yes.run());
        set(15, Items.make(Material.RED_CONCRETE, 1, "<red><bold>CANCEL", "<gray>Click to go back"), e -> no.run());
        fillEmpty();
    }
}

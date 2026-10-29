package com.notgamerthuz.ngtstore;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class StoreGui extends Gui {
    public StoreGui(NGT plugin, Player p) {
        super(Brand.title("Store"), 5);
        int idx = 0;
        for (StoreManager.Category c : plugin.store().categories()) {
            int slot = 10 + idx++;
            if (slot > 16) slot += 2; // second row of categories
            set(slot, Items.make(c.icon, 1, c.name, "<gray>" + c.items.size() + " items", "", "<yellow>Click to browse"),
                    e -> new CategoryGui(plugin, p, c).open(p));
        }
        set(36, Items.make(Material.HOPPER, 1, "<green><bold>Sell All", "<gray>Sell every sellable item", "<gray>in your inventory", "", "<yellow>Click to sell"),
                e -> { plugin.store().sellAll(p); p.closeInventory(); });
        set(40, Items.make(Material.GOLD_INGOT, 1, "<gold><bold>Balance", "<white>" + plugin.eco().fmt(plugin.eco().balance(p))));
        set(44, Items.make(Material.BARRIER, 1, "<red>Close"), e -> p.closeInventory());
        fillEmpty();
    }
}

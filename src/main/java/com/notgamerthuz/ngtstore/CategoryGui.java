package com.notgamerthuz.ngtstore;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public final class CategoryGui extends PagedGui<StoreManager.Entry> {
    private final NGT plugin;

    public CategoryGui(NGT plugin, Player p, StoreManager.Category c) {
        super(p, Brand.title(c.id.substring(0, 1).toUpperCase() + c.id.substring(1)), c.items);
        this.plugin = plugin;
    }

    @Override
    protected ItemStack icon(StoreManager.Entry e) {
        String buy = e.buy() > 0 ? "<green>Buy: <gold>" + plugin.eco().fmt(e.buy()) : "<dark_gray>Not buyable";
        String sell = e.sell() > 0 ? "<red>Sell: <gold>" + plugin.eco().fmt(e.sell()) : "<dark_gray>Not sellable";
        return Items.make(e.mat(), 1, "<white>" + Items.pretty(e.mat()),
                buy, sell, "",
                "<yellow>Left-click <gray>buy 1  <dark_gray>|  <yellow>Shift+Left <gray>buy 64",
                "<yellow>Right-click <gray>sell 1  <dark_gray>|  <yellow>Shift+Right <gray>sell 64");
    }

    @Override
    protected void click(StoreManager.Entry en, InventoryClickEvent e) {
        switch (e.getClick()) {
            case LEFT -> plugin.store().buy(viewer, en, 1);
            case SHIFT_LEFT -> plugin.store().buy(viewer, en, 64);
            case RIGHT -> plugin.store().sell(viewer, en, 1);
            case SHIFT_RIGHT -> plugin.store().sell(viewer, en, 64);
            default -> { return; }
        }
        render();
    }

    @Override
    protected void extras() {
        set(48, Items.make(Material.OAK_DOOR, 1, "<yellow>« Back to Store"), e -> new StoreGui(plugin, viewer).open(viewer));
        set(50, Items.make(Material.GOLD_INGOT, 1, "<gold><bold>Balance", "<white>" + plugin.eco().fmt(plugin.eco().balance(viewer))));
    }
}

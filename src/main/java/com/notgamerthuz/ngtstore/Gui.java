package com.notgamerthuz.ngtstore;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Gui implements InventoryHolder {
    protected final Inventory inv;
    private final Map<Integer, Consumer<InventoryClickEvent>> actions = new HashMap<>();

    protected Gui(Component title, int rows) {
        this.inv = Bukkit.createInventory(this, rows * 9, title);
    }

    protected void set(int slot, ItemStack item) { inv.setItem(slot, item); actions.remove(slot); }

    protected void set(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        inv.setItem(slot, item);
        actions.put(slot, action);
    }

    protected void clear() { inv.clear(); actions.clear(); }

    protected void fillEmpty() {
        for (int i = 0; i < inv.getSize(); i++) if (inv.getItem(i) == null) inv.setItem(i, Items.filler());
    }

    public void handle(InventoryClickEvent e) {
        Consumer<InventoryClickEvent> a = actions.get(e.getRawSlot());
        if (a == null) return;
        Player p = (Player) e.getWhoClicked();
        p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1f);
        a.accept(e);
    }

    public void open(Player p) { p.openInventory(inv); }

    @Override
    public Inventory getInventory() { return inv; }
}

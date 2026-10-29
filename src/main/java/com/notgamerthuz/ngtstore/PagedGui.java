package com.notgamerthuz.ngtstore;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class PagedGui<T> extends Gui {
    protected final Player viewer;
    protected final List<T> data;
    protected int page = 0;

    protected PagedGui(Player viewer, Component title, List<T> data) {
        super(title, 6);
        this.viewer = viewer;
        this.data = data;
    }

    protected abstract ItemStack icon(T t);
    protected abstract void click(T t, InventoryClickEvent e);
    protected void extras() {}

    protected int pages() { return Math.max(1, (int) Math.ceil(data.size() / 45.0)); }

    protected void render() {
        clear();
        int start = page * 45;
        for (int i = 0; i < 45 && start + i < data.size(); i++) {
            T t = data.get(start + i);
            set(i, icon(t), e -> click(t, e));
        }
        for (int i = 45; i < 54; i++) set(i, Items.filler());
        if (page > 0) set(45, Items.make(Material.ARROW, 1, "<yellow>« Previous"), e -> { page--; render(); });
        if (page < pages() - 1) set(53, Items.make(Material.ARROW, 1, "<yellow>Next »"), e -> { page++; render(); });
        set(49, Items.make(Material.PAPER, 1, "<gray>Page <white>" + (page + 1) + "<gray>/<white>" + pages()));
        extras();
    }

    @Override
    public void open(Player p) { render(); super.open(p); }
}

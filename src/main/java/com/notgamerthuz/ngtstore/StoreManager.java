package com.notgamerthuz.ngtstore;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class StoreManager {
    public record Entry(Material mat, double buy, double sell) {}

    public static final class Category {
        public final String id, name;
        public final Material icon;
        public final List<Entry> items = new ArrayList<>();
        Category(String id, String name, Material icon) { this.id = id; this.name = name; this.icon = icon; }
    }

    private final NGT plugin;
    private final Map<String, Category> categories = new LinkedHashMap<>();

    public StoreManager(NGT plugin) { this.plugin = plugin; load(); }

    public void load() {
        categories.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("store.categories");
        if (root == null) return;
        for (String key : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(key);
            if (sec == null) continue;
            Material icon = Material.matchMaterial(sec.getString("icon", "CHEST"));
            Category c = new Category(key, sec.getString("name", key), icon == null ? Material.CHEST : icon);
            ConfigurationSection items = sec.getConfigurationSection("items");
            if (items != null) {
                for (String ik : items.getKeys(false)) {
                    Material m = Material.matchMaterial(ik);
                    if (m == null) { plugin.getLogger().warning("Unknown material in store: " + ik); continue; }
                    c.items.add(new Entry(m, items.getDouble(ik + ".buy", -1), items.getDouble(ik + ".sell", -1)));
                }
            }
            categories.put(key, c);
        }
    }

    public Collection<Category> categories() { return categories.values(); }

    public void buy(Player p, Entry e, int amount) {
        if (e.buy() <= 0) { Brand.send(p, "<red>This item can't be bought."); return; }
        double total = e.buy() * amount;
        if (!plugin.eco().withdraw(p, total)) {
            Brand.send(p, "<red>Not enough money. Need <gold>" + plugin.eco().fmt(total));
            return;
        }
        Items.giveOrDrop(p, new ItemStack(e.mat(), amount));
        p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
        Brand.send(p, "<green>Bought <white>" + amount + "x " + Items.pretty(e.mat()) + " <green>for <gold>" + plugin.eco().fmt(total));
    }

    public void sell(Player p, Entry e, int amount) {
        if (e.sell() <= 0) { Brand.send(p, "<red>This item can't be sold."); return; }
        int n = Math.min(amount, count(p, e.mat()));
        if (n <= 0) { Brand.send(p, "<red>You don't have that item."); return; }
        remove(p, e.mat(), n);
        double total = e.sell() * n;
        plugin.eco().deposit(p, total);
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        Brand.send(p, "<green>Sold <white>" + n + "x " + Items.pretty(e.mat()) + " <green>for <gold>" + plugin.eco().fmt(total));
    }

    public void sellAll(Player p) {
        double total = 0;
        int items = 0;
        for (Category c : categories.values()) {
            for (Entry e : c.items) {
                if (e.sell() <= 0) continue;
                int n = count(p, e.mat());
                if (n <= 0) continue;
                remove(p, e.mat(), n);
                total += n * e.sell();
                items += n;
            }
        }
        if (items == 0) { Brand.send(p, "<red>Nothing sellable in your inventory."); return; }
        plugin.eco().deposit(p, total);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        Brand.send(p, "<green>Sold <white>" + items + " <green>items for <gold>" + plugin.eco().fmt(total));
    }

    private int count(Player p, Material m) {
        int n = 0;
        for (ItemStack it : p.getInventory().getStorageContents())
            if (it != null && it.getType() == m && !it.hasItemMeta()) n += it.getAmount();
        return n;
    }

    private void remove(Player p, Material m, int amount) {
        ItemStack[] contents = p.getInventory().getStorageContents();
        for (int i = 0; i < contents.length && amount > 0; i++) {
            ItemStack it = contents[i];
            if (it == null || it.getType() != m || it.hasItemMeta()) continue;
            int take = Math.min(amount, it.getAmount());
            it.setAmount(it.getAmount() - take);
            if (it.getAmount() <= 0) contents[i] = null;
            amount -= take;
        }
        p.getInventory().setStorageContents(contents);
    }
}

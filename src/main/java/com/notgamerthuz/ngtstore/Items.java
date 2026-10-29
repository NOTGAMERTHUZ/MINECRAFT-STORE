package com.notgamerthuz.ngtstore;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;

public final class Items {
    private Items() {}

    public static Component c(String s) { return Brand.MM.deserialize("<!italic>" + s); }

    public static ItemStack make(Material m, int amount, String name, String... lore) {
        ItemStack it = new ItemStack(m, Math.max(1, amount));
        it.editMeta(meta -> {
            meta.displayName(c(name));
            if (lore.length > 0) meta.lore(Arrays.stream(lore).map(Items::c).toList());
        });
        return it;
    }

    public static ItemStack filler() { return make(Material.BLACK_STAINED_GLASS_PANE, 1, " "); }

    public static String pretty(Material m) {
        StringBuilder sb = new StringBuilder();
        for (String part : m.name().toLowerCase().split("_")) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    public static void giveOrDrop(Player p, ItemStack item) {
        Map<Integer, ItemStack> left = p.getInventory().addItem(item);
        left.values().forEach(i -> p.getWorld().dropItemNaturally(p.getLocation(), i));
    }

    public static String duration(long millis) {
        long s = Math.max(0, millis / 1000);
        long h = s / 3600, m = (s % 3600) / 60;
        return h > 0 ? h + "h " + m + "m" : m + "m";
    }
}

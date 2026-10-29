package com.notgamerthuz.ngtstore;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class AuctionManager {
    public record Listing(int id, UUID seller, String sellerName, ItemStack item, double price, long expires) {}

    private final NGT plugin;
    private final File file;
    private final Map<Integer, Listing> listings = new LinkedHashMap<>();
    private final Map<UUID, List<ItemStack>> claims = new HashMap<>();
    private int nextId = 1;

    public AuctionManager(NGT plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "auctions.yml");
        load();
    }

    private void load() {
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        nextId = y.getInt("next", 1);
        ConfigurationSection ls = y.getConfigurationSection("listings");
        if (ls != null) {
            for (String k : ls.getKeys(false)) {
                String p = "listings." + k + ".";
                ItemStack item = y.getItemStack(p + "item");
                if (item == null) continue;
                int id = Integer.parseInt(k);
                listings.put(id, new Listing(id, UUID.fromString(y.getString(p + "seller", "")),
                        y.getString(p + "name", "?"), item, y.getDouble(p + "price"), y.getLong(p + "expires")));
            }
        }
        ConfigurationSection cs = y.getConfigurationSection("claims");
        if (cs != null) {
            for (String k : cs.getKeys(false)) {
                List<ItemStack> list = new ArrayList<>();
                for (Object o : y.getList("claims." + k, List.of())) if (o instanceof ItemStack i) list.add(i);
                if (!list.isEmpty()) claims.put(UUID.fromString(k), list);
            }
        }
    }

    public void save() {
        YamlConfiguration y = new YamlConfiguration();
        y.set("next", nextId);
        for (Listing l : listings.values()) {
            String p = "listings." + l.id() + ".";
            y.set(p + "seller", l.seller().toString());
            y.set(p + "name", l.sellerName());
            y.set(p + "item", l.item());
            y.set(p + "price", l.price());
            y.set(p + "expires", l.expires());
        }
        claims.forEach((u, list) -> y.set("claims." + u, list));
        try { y.save(file); } catch (IOException ex) { plugin.getLogger().warning("Could not save auctions: " + ex.getMessage()); }
    }

    /** All listings, newest first. Pass a UUID to filter by seller, or null for all. */
    public List<Listing> all(UUID seller) {
        List<Listing> out = new ArrayList<>();
        for (Listing l : listings.values()) if (seller == null || l.seller().equals(seller)) out.add(l);
        out.sort(Comparator.comparingInt(Listing::id).reversed());
        return out;
    }

    public int claimCount(UUID u) { return claims.getOrDefault(u, List.of()).size(); }

    public void sell(Player p, double price) {
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) { Brand.send(p, "<red>Hold the item you want to sell."); return; }
        double min = plugin.getConfig().getDouble("auction.min-price", 1);
        if (price < min) { Brand.send(p, "<red>Minimum price is <gold>" + plugin.eco().fmt(min)); return; }
        int max = plugin.getConfig().getInt("auction.max-listings", 10);
        if (all(p.getUniqueId()).size() >= max) { Brand.send(p, "<red>You can only have <white>" + max + " <red>listings."); return; }
        long expires = System.currentTimeMillis() + plugin.getConfig().getLong("auction.duration-hours", 48) * 3_600_000L;
        Listing l = new Listing(nextId++, p.getUniqueId(), p.getName(), hand.clone(), price, expires);
        listings.put(l.id(), l);
        p.getInventory().setItemInMainHand(null);
        save();
        Brand.send(p, "<green>Listed <white>" + l.item().getAmount() + "x " + Items.pretty(l.item().getType()) + " <green>for <gold>" + plugin.eco().fmt(price));
    }

    public void cancel(Player p, Listing l) {
        if (listings.remove(l.id()) == null) { Brand.send(p, "<red>Listing no longer exists."); return; }
        Items.giveOrDrop(p, l.item());
        save();
        Brand.send(p, "<yellow>Listing cancelled, item returned.");
    }

    public void buy(Player buyer, int id) {
        Listing l = listings.get(id);
        if (l == null) { Brand.send(buyer, "<red>That listing is no longer available."); return; }
        if (l.seller().equals(buyer.getUniqueId())) { Brand.send(buyer, "<red>You can't buy your own listing."); return; }
        if (!plugin.eco().withdraw(buyer, l.price())) {
            Brand.send(buyer, "<red>Not enough money. Need <gold>" + plugin.eco().fmt(l.price()));
            return;
        }
        double tax = plugin.getConfig().getDouble("auction.tax-percent", 5);
        double net = l.price() * (1 - tax / 100.0);
        plugin.eco().deposit(Bukkit.getOfflinePlayer(l.seller()), net);
        listings.remove(id);
        save();
        Items.giveOrDrop(buyer, l.item());
        buyer.playSound(buyer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        Brand.send(buyer, "<green>Purchased <white>" + l.item().getAmount() + "x " + Items.pretty(l.item().getType()) + " <green>for <gold>" + plugin.eco().fmt(l.price()));
        Player seller = Bukkit.getPlayer(l.seller());
        if (seller != null) Brand.send(seller, "<green>" + buyer.getName() + " bought your <white>" + Items.pretty(l.item().getType()) + "<green>! You received <gold>" + plugin.eco().fmt(net) + " <gray>(" + tax + "% tax)");
    }

    public void claim(Player p) {
        List<ItemStack> list = claims.remove(p.getUniqueId());
        if (list == null || list.isEmpty()) { Brand.send(p, "<red>Nothing to claim."); return; }
        list.forEach(i -> Items.giveOrDrop(p, i));
        save();
        Brand.send(p, "<green>Claimed <white>" + list.size() + " <green>expired item stack(s).");
    }

    /** Called every minute: moves expired listings into the seller's claim box. */
    public void tick() {
        long now = System.currentTimeMillis();
        boolean changed = false;
        for (Iterator<Listing> it = listings.values().iterator(); it.hasNext(); ) {
            Listing l = it.next();
            if (now < l.expires()) continue;
            it.remove();
            claims.computeIfAbsent(l.seller(), k -> new ArrayList<>()).add(l.item());
            changed = true;
            Player s = Bukkit.getPlayer(l.seller());
            if (s != null) Brand.send(s, "<yellow>Your listing expired. Use <white>/ah claim <yellow>to get the item back.");
        }
        if (changed) save();
    }
}

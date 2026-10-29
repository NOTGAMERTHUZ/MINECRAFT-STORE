package com.notgamerthuz.ngtstore;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class BountyManager {
    private final NGT plugin;
    private final File file;
    private final Map<UUID, Double> amounts = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();

    public BountyManager(NGT plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "bounties.yml");
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection s = y.getConfigurationSection("bounties");
        if (s != null) for (String k : s.getKeys(false)) {
            UUID u = UUID.fromString(k);
            amounts.put(u, s.getDouble(k + ".amount"));
            names.put(u, s.getString(k + ".name", "?"));
        }
    }

    public void save() {
        YamlConfiguration y = new YamlConfiguration();
        amounts.forEach((u, a) -> { y.set("bounties." + u + ".amount", a); y.set("bounties." + u + ".name", names.get(u)); });
        try { y.save(file); } catch (IOException ex) { plugin.getLogger().warning("Could not save bounties: " + ex.getMessage()); }
    }

    public double get(UUID u) { return amounts.getOrDefault(u, 0.0); }
    public String name(UUID u) { return names.getOrDefault(u, "?"); }

    /** Bounties sorted by highest amount first. */
    public List<UUID> sorted() {
        List<UUID> l = new ArrayList<>(amounts.keySet());
        l.sort((a, b) -> Double.compare(amounts.get(b), amounts.get(a)));
        return l;
    }

    public void place(Player setter, String targetName, double amount) {
        Player online = Bukkit.getPlayerExact(targetName);
        OfflinePlayer target = online != null ? online : Bukkit.getOfflinePlayerIfCached(targetName);
        if (target == null || target.getName() == null) { Brand.send(setter, "<red>Player not found."); return; }
        if (target.getUniqueId().equals(setter.getUniqueId())) { Brand.send(setter, "<red>You can't place a bounty on yourself."); return; }
        double min = plugin.getConfig().getDouble("bounty.min-amount", 100);
        if (amount < min) { Brand.send(setter, "<red>Minimum bounty is <gold>" + plugin.eco().fmt(min)); return; }
        if (!plugin.eco().withdraw(setter, amount)) { Brand.send(setter, "<red>You don't have enough money."); return; }
        amounts.merge(target.getUniqueId(), amount, Double::sum);
        names.put(target.getUniqueId(), target.getName());
        save();
        Bukkit.broadcast(Brand.parse(Brand.PREFIX + "<red>" + setter.getName() + " <gray>placed a <gold>" + plugin.eco().fmt(amount)
                + " <gray>bounty on <red>" + target.getName() + "<gray>! Total: <gold>" + plugin.eco().fmt(get(target.getUniqueId()))));
    }

    public void claim(Player killer, Player victim) {
        Double amt = amounts.remove(victim.getUniqueId());
        if (amt == null) return;
        names.remove(victim.getUniqueId());
        plugin.eco().deposit(killer, amt);
        save();
        Bukkit.broadcast(Brand.parse(Brand.PREFIX + "<green>" + killer.getName() + " <gray>collected the <gold>" + plugin.eco().fmt(amt)
                + " <gray>bounty on <red>" + victim.getName() + "<gray>!"));
    }
}

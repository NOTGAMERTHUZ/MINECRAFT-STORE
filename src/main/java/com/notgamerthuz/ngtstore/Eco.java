package com.notgamerthuz.ngtstore;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/** Uses Vault if present, otherwise a built-in balance file. */
public final class Eco {
    private final NGT plugin;
    private VaultHook vault;
    private final File file;
    private final YamlConfiguration data;

    public Eco(NGT plugin) {
        this.plugin = plugin;
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            try { vault = VaultHook.create(); } catch (Throwable ignored) {}
        }
        file = new File(plugin.getDataFolder(), "balances.yml");
        data = YamlConfiguration.loadConfiguration(file);
    }

    public boolean usingVault() { return vault != null; }

    public double balance(OfflinePlayer p) {
        if (vault != null) return vault.balance(p);
        return data.getDouble(p.getUniqueId().toString(), plugin.getConfig().getDouble("economy.starting-balance", 1000));
    }

    public boolean withdraw(OfflinePlayer p, double amount) {
        if (amount < 0 || balance(p) < amount) return false;
        if (vault != null) return vault.withdraw(p, amount);
        set(p, balance(p) - amount);
        return true;
    }

    public void deposit(OfflinePlayer p, double amount) {
        if (vault != null) vault.deposit(p, amount);
        else set(p, balance(p) + amount);
    }

    private void set(OfflinePlayer p, double v) {
        data.set(p.getUniqueId().toString(), v);
        try { data.save(file); } catch (IOException ex) { plugin.getLogger().warning("Could not save balances: " + ex.getMessage()); }
    }

    public String fmt(double a) {
        return plugin.getConfig().getString("economy.symbol", "$") + String.format(Locale.US, "%,.2f", a);
    }
}

package com.notgamerthuz.ngtstore;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

final class VaultHook {
    private final Economy eco;

    private VaultHook(Economy eco) { this.eco = eco; }

    static VaultHook create() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        return rsp == null ? null : new VaultHook(rsp.getProvider());
    }

    double balance(OfflinePlayer p) { return eco.getBalance(p); }
    boolean withdraw(OfflinePlayer p, double a) { return eco.withdrawPlayer(p, a).transactionSuccess(); }
    void deposit(OfflinePlayer p, double a) { eco.depositPlayer(p, a); }
}

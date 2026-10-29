package com.notgamerthuz.ngtstore;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class Commands implements CommandExecutor, TabCompleter {
    private final NGT plugin;

    public Commands(NGT plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        switch (cmd.getName().toLowerCase()) {
            case "store" -> { Player p = player(s); if (p != null) new StoreGui(plugin, p).open(p); }
            case "sellall" -> { Player p = player(s); if (p != null) plugin.store().sellAll(p); }
            case "balance" -> { Player p = player(s); if (p != null) Brand.send(p, "<gray>Balance: <gold>" + plugin.eco().fmt(plugin.eco().balance(p))); }
            case "ah" -> auction(s, a);
            case "bounty" -> bounty(s, a);
            case "ngt" -> admin(s, a);
        }
        return true;
    }

    private void auction(CommandSender s, String[] a) {
        Player p = player(s);
        if (p == null) return;
        if (a.length == 0) { new AuctionGui(plugin, p, false).open(p); return; }
        switch (a[0].toLowerCase()) {
            case "sell" -> {
                if (a.length < 2) { Brand.send(p, "<red>Usage: /ah sell <price>"); return; }
                Double price = amount(p, a[1]);
                if (price != null) plugin.auction().sell(p, price);
            }
            case "mine" -> new AuctionGui(plugin, p, true).open(p);
            case "claim" -> plugin.auction().claim(p);
            default -> Brand.send(p, "<gray>/ah <white>| sell <price> | mine | claim");
        }
    }

    private void bounty(CommandSender s, String[] a) {
        Player p = player(s);
        if (p == null) return;
        if (a.length == 0 || a[0].equalsIgnoreCase("list")) { new BountyGui(plugin, p).open(p); return; }
        switch (a[0].toLowerCase()) {
            case "set" -> {
                if (a.length < 3) { Brand.send(p, "<red>Usage: /bounty set <player> <amount>"); return; }
                Double amt = amount(p, a[2]);
                if (amt != null) plugin.bounty().place(p, a[1], amt);
            }
            case "check" -> {
                if (a.length < 2) { Brand.send(p, "<red>Usage: /bounty check <player>"); return; }
                Player t = Bukkit.getPlayerExact(a[1]);
                double b = t == null ? 0 : plugin.bounty().get(t.getUniqueId());
                Brand.send(p, "<gray>Bounty on <white>" + a[1] + "<gray>: <gold>" + plugin.eco().fmt(b));
            }
            default -> Brand.send(p, "<gray>/bounty <white>| list | set <player> <amount> | check <player>");
        }
    }

    private void admin(CommandSender s, String[] a) {
        if (a.length == 0) {
            Brand.send(s, "<white>NGTStore v" + plugin.getPluginMeta().getVersion() + " <gray>by <white>" + Brand.NAME);
            return;
        }
        if (!s.hasPermission("ngtstore.admin")) { Brand.send(s, "<red>No permission."); return; }
        switch (a[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.store().load();
                Brand.send(s, "<green>Config reloaded.");
            }
            case "eco" -> {
                if (a.length < 4) { Brand.send(s, "<red>Usage: /ngt eco <give|take> <player> <amount>"); return; }
                Player online = Bukkit.getPlayerExact(a[2]);
                OfflinePlayer t = online != null ? online : Bukkit.getOfflinePlayerIfCached(a[2]);
                Double amt = amount(s, a[3]);
                if (t == null || amt == null) { if (t == null) Brand.send(s, "<red>Player not found."); return; }
                if (a[1].equalsIgnoreCase("give")) plugin.eco().deposit(t, amt);
                else if (!plugin.eco().withdraw(t, amt)) { Brand.send(s, "<red>Player lacks that amount."); return; }
                Brand.send(s, "<green>Done.");
            }
            default -> Brand.send(s, "<gray>/ngt <white>reload | eco <give|take> <player> <amount>");
        }
    }

    private Player player(CommandSender s) {
        if (s instanceof Player p) return p;
        Brand.send(s, "<red>Players only.");
        return null;
    }

    private Double amount(CommandSender s, String str) {
        try {
            double d = Double.parseDouble(str);
            if (Double.isNaN(d) || Double.isInfinite(d) || d <= 0) throw new NumberFormatException();
            return d;
        } catch (NumberFormatException ex) {
            Brand.send(s, "<red>Invalid amount.");
            return null;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] a) {
        List<String> out = new ArrayList<>();
        String name = cmd.getName().toLowerCase();
        if (a.length == 1) {
            switch (name) {
                case "ah" -> out.addAll(List.of("sell", "mine", "claim"));
                case "bounty" -> out.addAll(List.of("list", "set", "check"));
                case "ngt" -> out.addAll(List.of("reload", "eco"));
            }
        } else if (a.length == 2 && (name.equals("bounty") || (name.equals("ngt") && a[0].equalsIgnoreCase("eco")))) {
            if (name.equals("ngt")) out.addAll(List.of("give", "take"));
            else Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
        } else if (a.length == 3 && name.equals("ngt")) {
            Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
        }
        String last = a[a.length - 1].toLowerCase();
        out.removeIf(x -> !x.toLowerCase().startsWith(last));
        return out;
    }
}

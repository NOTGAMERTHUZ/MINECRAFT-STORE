package com.notgamerthuz.ngtstore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

public final class Brand {
    public static final String NAME = "NOTGAMERTHUZ";
    public static final String GRADIENT = "<gradient:#ff512f:#dd2476><bold>NOTGAMERTHUZ</bold></gradient>";
    public static final String PREFIX = GRADIENT + " <dark_gray>» <gray>";
    public static final MiniMessage MM = MiniMessage.miniMessage();

    private Brand() {}

    public static Component parse(String s) { return MM.deserialize(s); }

    public static void send(CommandSender s, String msg) { s.sendMessage(MM.deserialize(PREFIX + msg)); }

    public static Component title(String t) {
        return MM.deserialize(GRADIENT + " <dark_gray>• <white>" + t);
    }
}

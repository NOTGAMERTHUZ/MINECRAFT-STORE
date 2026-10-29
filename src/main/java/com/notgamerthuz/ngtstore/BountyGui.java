package com.notgamerthuz.ngtstore;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public final class BountyGui extends PagedGui<UUID> {
    private final NGT plugin;

    public BountyGui(NGT plugin, Player p) {
        super(p, Brand.title("Bounties"), plugin.bounty().sorted());
        this.plugin = plugin;
    }

    @Override
    protected ItemStack icon(UUID u) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        String name = plugin.bounty().name(u);
        head.editMeta(SkullMeta.class, m -> {
            m.setOwningPlayer(Bukkit.getOfflinePlayer(u));
            m.displayName(Items.c("<red><bold>" + name));
            m.lore(java.util.List.of(
                    Items.c("<gray>Bounty: <gold>" + plugin.eco().fmt(plugin.bounty().get(u))),
                    Items.c(""),
                    Items.c("<yellow>Click to add to this bounty")));
        });
        return head;
    }

    @Override
    protected void click(UUID u, InventoryClickEvent e) {
        viewer.closeInventory();
        Brand.send(viewer, "<gray>Use <white>/bounty set " + plugin.bounty().name(u) + " <amount> <gray>to add to the bounty.");
    }

    @Override
    protected void extras() {
        set(51, Items.make(Material.WRITABLE_BOOK, 1, "<green>Place a bounty", "<gray>Use <white>/bounty set <player> <amount>"));
    }
}

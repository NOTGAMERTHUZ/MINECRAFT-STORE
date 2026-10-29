package com.notgamerthuz.ngtstore;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class AuctionGui extends PagedGui<AuctionManager.Listing> {
    private final NGT plugin;
    private final boolean mine;

    public AuctionGui(NGT plugin, Player p, boolean mine) {
        super(p, Brand.title(mine ? "My Listings" : "Auction House"), plugin.auction().all(mine ? p.getUniqueId() : null));
        this.plugin = plugin;
        this.mine = mine;
    }

    @Override
    protected ItemStack icon(AuctionManager.Listing l) {
        ItemStack it = l.item().clone();
        List<Component> lore = it.lore() == null ? new ArrayList<>() : new ArrayList<>(it.lore());
        lore.add(Items.c(""));
        lore.add(Items.c("<gray>Price: <gold>" + plugin.eco().fmt(l.price())));
        lore.add(Items.c("<gray>Seller: <white>" + l.sellerName()));
        lore.add(Items.c("<gray>Expires in: <white>" + Items.duration(l.expires() - System.currentTimeMillis())));
        lore.add(Items.c(""));
        lore.add(Items.c(l.seller().equals(viewer.getUniqueId()) ? "<red>Click to cancel listing" : "<yellow>Click to buy"));
        it.editMeta(m -> m.lore(lore));
        return it;
    }

    @Override
    protected void click(AuctionManager.Listing l, InventoryClickEvent e) {
        if (l.seller().equals(viewer.getUniqueId())) {
            plugin.auction().cancel(viewer, l);
            new AuctionGui(plugin, viewer, mine).open(viewer);
            return;
        }
        ItemStack display = icon(l);
        new ConfirmGui("Confirm Purchase", display,
                () -> { plugin.auction().buy(viewer, l.id()); new AuctionGui(plugin, viewer, mine).open(viewer); },
                () -> new AuctionGui(plugin, viewer, mine).open(viewer)).open(viewer);
    }

    @Override
    protected void extras() {
        set(47, Items.make(mine ? Material.CHEST : Material.PLAYER_HEAD, 1,
                        mine ? "<yellow>« All Listings" : "<aqua>My Listings", "<gray>Click to switch view"),
                e -> new AuctionGui(plugin, viewer, !mine).open(viewer));
        int claims = plugin.auction().claimCount(viewer.getUniqueId());
        set(51, Items.make(Material.ENDER_CHEST, 1, "<light_purple>Claim Expired Items",
                "<gray>Waiting: <white>" + claims, "", "<yellow>Click to claim"), e -> {
            plugin.auction().claim(viewer);
            new AuctionGui(plugin, viewer, mine).open(viewer);
        });
        set(52, Items.make(Material.OAK_SIGN, 1, "<green>How to sell", "<gray>Hold an item and run", "<white>/ah sell <price>"));
    }
}

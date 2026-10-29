package com.notgamerthuz.ngtstore;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class NGT extends JavaPlugin implements Listener {
    private Eco eco;
    private StoreManager store;
    private AuctionManager auction;
    private BountyManager bounty;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        eco = new Eco(this);
        store = new StoreManager(this);
        auction = new AuctionManager(this);
        bounty = new BountyManager(this);

        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        getServer().getPluginManager().registerEvents(this, this);

        Commands cmds = new Commands(this);
        for (String c : new String[]{"store", "sellall", "ah", "bounty", "balance", "ngt"}) {
            var pc = getCommand(c);
            if (pc != null) { pc.setExecutor(cmds); pc.setTabCompleter(cmds); }
        }

        getServer().getScheduler().runTaskTimer(this, auction::tick, 1200L, 1200L);

        var log = getComponentLogger();
        log.info(Brand.parse("<gradient:#ff512f:#dd2476>=== " + Brand.NAME + " | NGTStore v" + getPluginMeta().getVersion() + " ==="));
        log.info(Brand.parse("<gray>Economy: " + (eco.usingVault() ? "Vault" : "built-in (install Vault for shared economy)")));
    }

    @Override
    public void onDisable() {
        if (auction != null) auction.save();
        if (bounty != null) bounty.save();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        if (killer != null && !killer.equals(victim)) bounty.claim(killer, victim);
    }

    public Eco eco() { return eco; }
    public StoreManager store() { return store; }
    public AuctionManager auction() { return auction; }
    public BountyManager bounty() { return bounty; }
}

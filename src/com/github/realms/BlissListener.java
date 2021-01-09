package com.github.realms;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BlissListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        new Sleeper(event.getPlayer());
        event.setQuitMessage("§3☽§b " + event.getPlayer().getName() + " has gone to sleep §3☽");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (Bliss.getInstance().cache.containsKey(event.getPlayer().getUniqueId()) ||
                Bliss.getInstance().isPlayerDead(event.getPlayer().getUniqueId())) {
            event.setJoinMessage("§6☀§e " + event.getPlayer().getName() + " has woken up §6☀");

            //if (Bliss.getInstance().cache.containsKey(event.getPlayer().getUniqueId())) {
            //    Bliss.getInstance().cache.get(event.getPlayer().getUniqueId()).entity.remove();
            //    Bliss.getInstance().cache.remove(event.getPlayer().getUniqueId());
            //  }

            // Manage dead players.
            if (Bliss.getInstance().isPlayerDead(event.getPlayer().getUniqueId())) {
                Bliss.getInstance().removeDeadPlayer(event.getPlayer().getUniqueId());
                event.getPlayer().getInventory().clear();
                event.getPlayer().setHealth(0);
                event.getPlayer().sendMessage("§4⚔§c You died while asleep.");
                event.getPlayer().sendMessage("§4Cause of Death: " +
                        Bliss.getInstance().deadplayers.get(event.getPlayer().getUniqueId()));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!event.isCancelled()) {
            if (Bliss.getInstance().EntitytoSleeper(event.getEntity()) != null) {
                Sleeper sleeper = Bliss.getInstance().EntitytoSleeper(event.getEntity());
                SleeperDamageEvent damageEvent = new SleeperDamageEvent(sleeper, event, event.getDamage());
                Bukkit.getPluginManager().callEvent(damageEvent);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        if (Bliss.getInstance().EntitytoSleeper(event.getEntity()) != null) {
            Bukkit.getPluginManager().callEvent(new SleeperDeathEvent(Bliss.getInstance().EntitytoSleeper(event.getEntity()), event));
        }
    }
}

package com.github.realms;

import com.github.realms.object.Sleeper;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BlissListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {

        new BukkitRunnable() {
            @Override
            public void run() {
                Sleeper sleeper = new Sleeper(event.getPlayer());
                Bukkit.broadcastMessage(new Sleeper(sleeper.toJson()).getHands()[0].getType().toString());
            }
        }.runTask(Bliss.getInstance());

    }
}

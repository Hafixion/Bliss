package com.github.realms;

import com.github.realms.object.BlissPlayer;
import com.github.realms.object.CraftConnection;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class BlissListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {

    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
                WorldServer nmsWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
                BlissPlayer npc = new BlissPlayer(nmsServer, nmsWorld, new GameProfile(UUID.randomUUID(), "TrafficConeGod"), new PlayerInteractManager(nmsWorld));
                CraftConnection.swapConnection(npc);
                npc.f(event.getPlayer().getLocation().getBlockX(), event.getPlayer().getLocation().getBlockY(), event.getPlayer().getLocation().getBlockZ());
                npc.setInvisible(false);
                npc.setNoGravity(false);
                npc.fauxSleeping = true;
                nmsWorld.addEntity(npc);

                PlayerConnection connection = ((CraftPlayer) event.getPlayer()).getHandle().playerConnection;

                connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
                connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
            }
        }.runTask(Bliss.plugin);
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Bukkit.broadcastMessage("ae");
    }
}

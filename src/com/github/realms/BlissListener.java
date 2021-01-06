package com.github.realms;

import com.github.realms.object.BlissPlayer;
import com.github.realms.object.BlissConnection;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlissListener implements Listener {
/*
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        Location logout = event.getPlayer().getLocation();
        PlayerInventory inv = event.getPlayer().getInventory();

        // Create the FakePlayer
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) event.getPlayer().getWorld()).getHandle();
        BlissPlayer npc = new BlissPlayer(nmsServer, nmsWorld, makeProfile(event.getPlayer().getUniqueId(),
                event.getPlayer().getDisplayName()), new PlayerInteractManager(nmsWorld));
        BlissConnection.swapConnection(npc);


    }*/

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
                WorldServer nmsWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
                BlissPlayer npc = new BlissPlayer(nmsServer, nmsWorld, makeProfile(UUID.fromString("d1a44aa9-5eb7-4069-8b2f-db8080c2a685"), "Traffic"), new PlayerInteractManager(nmsWorld));
                BlissConnection.swapConnection(npc);
                npc.f(event.getPlayer().getLocation().getBlockX(), event.getPlayer().getLocation().getBlockY(), event.getPlayer().getLocation().getBlockZ());
                npc.setInvisible(false);
                npc.setNoGravity(false);
                nmsWorld.addEntity(npc);
                npc.getBukkitEntity().getInventory().setContents(event.getPlayer().getInventory().getContents());
                npc.getBukkitEntity().getInventory().setArmorContents(event.getPlayer().getInventory().getArmorContents());

                PlayerConnection connection = ((CraftPlayer) event.getPlayer()).getHandle().playerConnection;

                connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
                connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));

                npc.entitySleep(new BlockPosition(event.getPlayer().getLocation().getBlockX(), event.getPlayer().getLocation().getBlockY(), event.getPlayer().getLocation().getBlockZ()));

                // todo add complete armor, check out the gravity issue.
                connection.sendPacket(new PacketPlayOutEntityMetadata(npc.getId(), npc.getDataWatcher(), false));

                // Remove from Tablist
                nmsServer.getPlayerList().players.removeIf(entityPlayer -> entityPlayer.getUniqueIDString().equalsIgnoreCase(npc.getUniqueIDString()));
                connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc));

                List<Pair<EnumItemSlot, ItemStack>> itemList = new ArrayList<>();
                itemList.add(Pair.of(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(event.getPlayer().getItemInHand())));
                connection.sendPacket(new PacketPlayOutEntityEquipment(npc.getId(), itemList));
            }
        }.runTask(Bliss.plugin);
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Bukkit.broadcastMessage("ae");
    }

    // Used to get Skins.
    private GameProfile makeProfile(UUID skinId, String name) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        if (skinId != null) {
            GameProfile skin = new GameProfile(skinId, null);
            skin = ((CraftServer) Bukkit.getServer()).getServer().getMinecraftSessionService().fillProfileProperties(skin, true);
            if (skin.getProperties().get("textures") != null) {
                Property textures = skin.getProperties().get("textures").iterator().next();
                profile.getProperties().put("textures", textures);
            }
        }
        return profile;
    }
}

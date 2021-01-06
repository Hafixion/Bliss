package com.github.realms.object;

import net.minecraft.server.v1_16_R2.EntityPlayer;
import net.minecraft.server.v1_16_R2.EnumProtocolDirection;
import net.minecraft.server.v1_16_R2.MinecraftServer;
import net.minecraft.server.v1_16_R2.NetworkManager;
import net.minecraft.server.v1_16_R2.Packet;
import net.minecraft.server.v1_16_R2.PlayerConnection;

public class CraftConnection extends PlayerConnection {

    // default constructor
    public CraftConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        // This *is* an npc so there's no need to process sending packets.
    }

    // Easy way to swap a connection
    public static void swapConnection(EntityPlayer player) {
        player.playerConnection = new CraftConnection(player.server, new NetworkManager(EnumProtocolDirection.SERVERBOUND), player);
    }
}

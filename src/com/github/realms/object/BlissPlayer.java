package com.github.realms.object;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.Location;
import org.bukkit.Material;

public class BlissPlayer extends EntityPlayer {

    public BlissPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
    }

    @Override
    public void tick() {
        super.tick();
        Location location = this.getBukkitEntity().getLocation();
        location.setY(location.getBlockY() - 1);

        if (location.getBlock().getType() == Material.AIR) {
            this.f(Math.floor(locX()) + 0.5, Math.floor(locY()) - 1, Math.floor(locZ()) + 0.5);
        }

        if (location.getBlock().getType() == Material.WATER) {
            this.f(Math.floor(locX()) + 0.5, locY() - 0.05, Math.floor(locZ()) + 0.5);
        }

        if (this.getBukkitEntity().getLocation().getBlock().getType() == Material.WATER) {
            this.f(Math.floor(locX()) + 0.5, locY() - 0.05, Math.floor(locZ()) + 0.5);
        }

        location.setY(getHeadY());
        if (location.getBlock().getType() == Material.WATER) {
            damageEntity(DamageSource.DROWN, 0.5f);

        }
    }
}

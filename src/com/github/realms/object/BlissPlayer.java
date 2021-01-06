package com.github.realms.object;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.Location;
import org.bukkit.Material;

public class BlissPlayer extends EntityPlayer {
    private int timeSinceNoDamage;

    public BlissPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
    }

    @Override
    public void tick() {
        super.tick();
        boolean disturbed = false;

        Location location = this.getBukkitEntity().getLocation();
        if (getPose() == EntityPose.STANDING)
            location.setY(locY() - 2);

        if (location.getBlock().getType() == Material.AIR) {
            this.f(locX(), Math.floor(locY()) - 1, locZ());
            disturbed = true;
            noDamageTicks = 20;
        }

        if (location.getBlock().getType() == Material.WATER) {
            this.f(ZeroPointFive(locX()), locY() - 0.1, ZeroPointFive(locZ()));
            disturbed = true;
            noDamageTicks = 20;
        }

        location.setY(getHeadY());
        if (location.getBlock().getType() == Material.WATER) {
            damageEntity(DamageSource.DROWN, 0.5f);
            disturbed = true;
            noDamageTicks = 20;
        }

        if (noDamageTicks == 0)
            timeSinceNoDamage++;
        else timeSinceNoDamage = 0;

        if (timeSinceNoDamage < 200 || disturbed) disturbed = true;
        else disturbed = false;

        if (disturbed) this.setPose(EntityPose.STANDING);
        else this.setPose(EntityPose.SLEEPING);
    }

    public static double ZeroPointFive(double n) {
        if (Math.floor(n) == n || Math.ceil(n) == n) return n + 0.5;

        double difference = n - Math.floor(n) + 0.5;

        if (difference == 0) return n;
        if (difference > 0) {
            return n + difference;
        } else {
            return n - difference;
        }
    }
}

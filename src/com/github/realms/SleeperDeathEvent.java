package com.github.realms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class SleeperDeathEvent extends Event {
    private final Sleeper sleeper;
    private final EntityDeathEvent deathEvent;

    @Override
    public HandlerList getHandlers() {
        return new HandlerList();
    }

    public static HandlerList getHandlerList() {
        return new HandlerList();
    }

    public SleeperDeathEvent(Sleeper sleeper, EntityDeathEvent event) {
        this.sleeper = sleeper;
        this.deathEvent = event;

        Bliss.getInstance().removeCache(sleeper);
        Location loc = event.getEntity().getLocation();
        for (ItemStack stack : sleeper.getDrops()) if (stack != null && !(stack.getType() == Material.AIR)) loc.getWorld().dropItemNaturally(loc, stack);

        Bliss.getInstance().addDeadPlayer(getSleeper().getUuid(), getDeathEvent().getEntity().getLastDamageCause().getCause().name());

        Bukkit.broadcastMessage("§c" + sleeper.getDisplay() + " died while asleep.");
    }

    public Sleeper getSleeper() {
        return sleeper;
    }

    public EntityDeathEvent getDeathEvent() {
        return deathEvent;
    }
}

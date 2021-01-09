package com.github.realms;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

public class SleeperDamageEvent extends Event implements Cancellable {
    private final Sleeper sleeper;
    private final EntityDamageEvent damageEvent;
    private final double damage;
    private boolean cancel = false;

    @Override
    public HandlerList getHandlers() {
        return new HandlerList();
    }

    public static HandlerList getHandlerList() {
        return new HandlerList();
    }

    public SleeperDamageEvent(Sleeper sleeper, EntityDamageEvent event, double damage) {
        this.sleeper = sleeper;
        this.damageEvent = event;
        this.damage = damage;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean b) {
        cancel = b;
        damageEvent.setCancelled(b);
    }

    public Sleeper getSleeper() {
        return sleeper;
    }

    public EntityDamageEvent getDamageEvent() {
        return damageEvent;
    }

    public double getDamage() {
        return damage;
    }
}

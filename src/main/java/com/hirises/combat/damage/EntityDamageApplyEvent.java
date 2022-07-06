package com.hirises.combat.damage;

import com.hirises.combat.damage.impl.SimpleDamageApplier;
import com.hirises.combat.item.ItemChangeEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class EntityDamageApplyEvent extends Event {
    public static HandlerList handlerlist = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerlist;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerlist;
    }

    private boolean canceled = false;
    private LivingEntity entity;
    private SimpleDamageApplier applier;

    public EntityDamageApplyEvent(LivingEntity entity, SimpleDamageApplier applier) {
        this.entity = entity;
        this.applier = applier;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public SimpleDamageApplier getApplier() {
        return applier;
    }
}

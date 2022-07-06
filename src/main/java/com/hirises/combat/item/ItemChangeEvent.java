package com.hirises.combat.item;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class ItemChangeEvent extends Event {
    enum Cause{
        Enchant,
        Anvil,
        Use,
        ;
    }

    public static HandlerList handlerlist = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerlist;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerlist;
    }

    private boolean canceled = false;
    private ItemStack itemStack;
    private Cause cause;

    public ItemChangeEvent(ItemStack itemStack, Cause cause) {
        this.itemStack = itemStack;
        this.cause = cause;
    }

    public Cause getCause(){
        return cause;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}

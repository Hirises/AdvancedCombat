package com.hirises.combat.item;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

//아이템이 새로 등록 (= 생성) 되었을 때 호출되는 이벤트 (ex 몬스터 드롭, 제작, 거래등)
public class ItemRegisteredEvent extends Event {
    enum Cause{
        Chest,
        Drop,
        Craft,
        Trade,
        Water,
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

    public ItemRegisteredEvent(ItemStack itemStack, Cause cause) {
        this.itemStack = itemStack;
        this.cause = cause;
    }

    public Cause getCause() {
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

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}

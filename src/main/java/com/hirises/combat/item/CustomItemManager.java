package com.hirises.combat.item;

import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CustomItemManager implements Listener {
    protected static Map<Material, ItemStack> replacement = new HashMap<>();
    protected static Set<Material> remove = EnumSet.noneOf(Material.class);

    @EventHandler
    public void onItemRegistered(ItemRegisteredEvent event){
        if(event.isCanceled()){
            return;
        }
        ItemStack item = event.getItemStack();
        if(!ItemUtil.isExist(item)){
            return;
        }

        if(hasRegisteredItemReplacement(item.getType())){
            event.setItemStack(ItemUtil.setAmount(replacement.get(item.getType()), item.getAmount()));
            return;
        }else if(hasRegisteredItemRemove(item.getType())){
            event.setCanceled(true);
            return;
        }
    }

    public static void registerItemReplacement(Material origin, ItemStack replace){
        replacement.put(origin, replace);
    }

    public static ItemStack getRegisteredItemReplacement(Material origin){
        return replacement.get(origin);
    }

    public static boolean hasRegisteredItemReplacement(Material origin){
        return replacement.containsKey(origin);
    }

    public static void removeRegisteredItemReplacement(Material origin){
        replacement.remove(origin);
    }

    public static void registerItemRemove(Material target){
        remove.add(target);
    }

    public static boolean hasRegisteredItemRemove(Material target){
        return remove.contains(target);
    }

    public static void removeRegisteredItemRemove(Material target){
        remove.remove(target);
    }
}

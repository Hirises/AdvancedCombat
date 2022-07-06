package com.hirises.combat.item;

import com.hirises.combat.config.Keys;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;

public class ItemSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event){
        Item item = event.getEntity();
        ItemStack itemStack = item.getItemStack();

        if(NBTTagStore.containKey(itemStack, Keys.Item_Checked.toString())){
            return;
        }

        ItemRegisteredEvent event1 = new ItemRegisteredEvent(itemStack, ItemRegisteredEvent.Cause.Drop);
        Bukkit.getPluginManager().callEvent(event1);

        if(event1.isCanceled()){
            item.remove();
        }
        item.setItemStack(NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemCraft(CraftItemEvent event){
        ItemStack item = event.getCurrentItem();

        ItemRegisteredEvent event1 = new ItemRegisteredEvent(item, ItemRegisteredEvent.Cause.Craft);
        Bukkit.getPluginManager().callEvent(event1);

        if(event1.isCanceled()){
            event1.setCanceled(true);
        }
        event.setCurrentItem(NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemMelt(FurnaceSmeltEvent event){
        ItemStack item = event.getResult();

        ItemRegisteredEvent event1 = new ItemRegisteredEvent(item, ItemRegisteredEvent.Cause.Craft);
        Bukkit.getPluginManager().callEvent(event1);

        if(event1.isCanceled()){
            event1.setCanceled(true);
        }
        event.setResult(NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPotionBrew(BrewEvent event){
        List<ItemStack> items = event.getResults();

        for(int index = 0; index < items.size(); index++){
            ItemRegisteredEvent event1 = new ItemRegisteredEvent(items.get(index), ItemRegisteredEvent.Cause.Craft);
            Bukkit.getPluginManager().callEvent(event1);

            if(event1.isCanceled()){
                items.set(index, NBTTagStore.set(new ItemStack(Material.AIR), Keys.Item_Checked.toString(), true));
            }else{
                items.set(index, NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent event){
        Inventory inventory = event.getInventory();
        if(inventory.getType() != InventoryType.CHEST){
            return;
        }

        ItemStack[] contents = inventory.getContents();

        for(int index = 0; index < contents.length; index++){
            if(NBTTagStore.containKey(contents[index], Keys.Item_Checked.toString())){
                continue;
            }

            ItemRegisteredEvent event1 = new ItemRegisteredEvent(contents[index], ItemRegisteredEvent.Cause.Chest);
            Bukkit.getPluginManager().callEvent(event1);

            if(event1.isCanceled()){
                contents[index] = NBTTagStore.set(new ItemStack(Material.AIR), Keys.Item_Checked.toString(), true);
            }else{
                contents[index] = NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true);
            }
        }

        inventory.setContents(contents);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTraderInteract(PlayerInteractAtEntityEvent event){
        Entity entity = event.getRightClicked();
        List<MerchantRecipe> recipes = null;
        if(entity.getType().equals(EntityType.WANDERING_TRADER)){
            WanderingTrader trader = (WanderingTrader) entity;

            recipes = trader.getRecipes();
            trader.setRecipes(checkRecipe(recipes));
        }else if(entity.getType().equals(EntityType.VILLAGER)) {
            Villager villager = (Villager) entity;

            recipes = villager.getRecipes();
            villager.setRecipes(checkRecipe(recipes));
        }
    }

    private List<MerchantRecipe> checkRecipe(List<MerchantRecipe> recipes){
        int nullFlag = 0;
        List<ItemStack> ingredients;
        List<MerchantRecipe> copy = new ArrayList<>();
        for(MerchantRecipe recipe : recipes){
            if(NBTTagStore.containKey(recipe.getResult(), Keys.Item_Checked.toString())){
                copy.add(recipe);
                continue;
            }

            ingredients = recipe.getIngredients();

            for(int index = 0; index < ingredients.size(); index++){
                if(!ItemUtil.isExist(ingredients.get(index))){
                    nullFlag++;
                    continue;
                }
                if(NBTTagStore.containKey(ingredients.get(index), Keys.Item_Checked.toString())){
                    continue;
                }

                ItemRegisteredEvent event1 = new ItemRegisteredEvent(ingredients.get(index), ItemRegisteredEvent.Cause.Trade);
                Bukkit.getPluginManager().callEvent(event1);

                if(event1.isCanceled()){
                    ingredients.set(index, NBTTagStore.set(new ItemStack(Material.AIR), Keys.Item_Checked.toString(), true));
                    nullFlag++;
                }else{
                    ingredients.set(index, NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true));
                }
            }

            if(nullFlag == ingredients.size()){
                continue;
            }

            ItemRegisteredEvent event1 = new ItemRegisteredEvent(recipe.getResult(), ItemRegisteredEvent.Cause.Trade);
            Bukkit.getPluginManager().callEvent(event1);

            if(event1.isCanceled()){
                continue;
            }else{
                MerchantRecipe newRecipe = new MerchantRecipe(NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true), recipe.getUses(), recipe.getMaxUses(), recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier(), recipe.getDemand(), recipe.getSpecialPrice());
                newRecipe.setIngredients(ingredients);
                copy.add(newRecipe);
            }
        }

        return copy;
    }

    @EventHandler
    public void onWaterBucketEmpty(PlayerBucketEmptyEvent event){
        ItemStack item = event.getItemStack();

        if(NBTTagStore.containKey(item, Keys.Item_Checked.toString())){
            return;
        }

        ItemRegisteredEvent event1 = new ItemRegisteredEvent(item, ItemRegisteredEvent.Cause.Chest);
        Bukkit.getPluginManager().callEvent(event1);

        if(event1.isCanceled()){
            event.setItemStack(new ItemStack(Material.AIR));
        }else{
            event.setItemStack(NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true));
        }
    }

    @EventHandler
    public void onWaterBucketFill(PlayerBucketFillEvent event){
        ItemStack item = event.getItemStack();

        if(NBTTagStore.containKey(item, Keys.Item_Checked.toString())){
            return;
        }

        ItemRegisteredEvent event1 = new ItemRegisteredEvent(item, ItemRegisteredEvent.Cause.Chest);
        Bukkit.getPluginManager().callEvent(event1);

        if(event1.isCanceled()){
            event.setItemStack(new ItemStack(Material.AIR));
        }else{
            event.setItemStack(NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true));
        }
    }
}

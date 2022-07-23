package com.hirises.combat.item;

import com.hirises.combat.config.Keys;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.List;

//아이템 교체를 위한 이벤트 처리
public class ItemListener implements Listener {

    //<editor-fold desc="아이템 생성 검사">

    //<editor-fold desc="아이템 제작 검사">
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemCraft(CraftItemEvent event){  //제작대
        ItemStack item = event.getCurrentItem();

        ItemRegisteredEvent event1 = new ItemRegisteredEvent(item, ItemRegisteredEvent.Cause.Craft);
        Bukkit.getPluginManager().callEvent(event1);

        if(event1.isCanceled()){
            event1.setCanceled(true);
        }
        event.setCurrentItem(NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemMelt(FurnaceSmeltEvent event){    //화로
        ItemStack item = event.getResult();

        ItemRegisteredEvent event1 = new ItemRegisteredEvent(item, ItemRegisteredEvent.Cause.Craft);
        Bukkit.getPluginManager().callEvent(event1);

        if(event1.isCanceled()){
            event1.setCanceled(true);
        }
        event.setResult(NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPotionBrew(BrewEvent event){  //포션
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
    //</editor-fold>

    //<editor-fold desc="주민 거래 검사">
    @EventHandler(priority = EventPriority.LOWEST)
    public void onTraderInteract(PlayerInteractAtEntityEvent event){    //주민 거래 검사
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

    //거래 내역 확인 & 업데이트
    private List<MerchantRecipe> checkRecipe(List<MerchantRecipe> recipes){
        int nullFlag = 0;
        List<ItemStack> ingredients;
        List<MerchantRecipe> copy = new ArrayList<>();
        for(MerchantRecipe recipe : recipes){
            if(NBTTagStore.containKey(recipe.getResult(), Keys.Item_Checked.toString())){
                copy.add(recipe);
                continue;
            }

            //거래 요구 재료 확인
            ingredients = recipe.getIngredients();
            for(int index = 0; index < ingredients.size(); index++){
                if(!ItemUtil.isExist(ingredients.get(index))){
                    nullFlag++;
                    continue;
                }
                if(NBTTagStore.containKey(ingredients.get(index), Keys.Item_Checked.toString())){
                    continue;
                }

                if(!ItemUtil.isExist(ingredients.get(index))){
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

            //모든 재료가 삭제 되었다면 거래도 삭제
            if(nullFlag == ingredients.size()){
                continue;
            }

            ItemRegisteredEvent event1 = new ItemRegisteredEvent(recipe.getResult(), ItemRegisteredEvent.Cause.Trade);
            Bukkit.getPluginManager().callEvent(event1);

            if(event1.isCanceled()){
                //거래 결과물이 삭제되었다면 거래도 삭제
                continue;
            }else{
                MerchantRecipe newRecipe = new MerchantRecipe(NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true), recipe.getUses(), recipe.getMaxUses(), recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier(), recipe.getDemand(), recipe.getSpecialPrice());
                newRecipe.setIngredients(ingredients);
                copy.add(newRecipe);
            }
        }

        return copy;
    }
    //</editor-fold>

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event){  //아이템 생성시 (= 블럭 파괴, 몬스터 드롭)
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
    public void onInventoryOpen(InventoryOpenEvent event){  //상자 검사
        Inventory inventory = event.getInventory();
        if(inventory.getType() != InventoryType.CHEST){
            return;
        }

        ItemStack[] contents = inventory.getContents();

        for(int index = 0; index < contents.length; index++){
            if(NBTTagStore.containKey(contents[index], Keys.Item_Checked.toString())){
                continue;
            }

            if(!ItemUtil.isExist(contents[index])){
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
    public void onEntitySpawn(EntitySpawnEvent event){  //엔티티 검사
        Entity entity = event.getEntity();
        if(entity instanceof LivingEntity){
            LivingEntity livingEntity = (LivingEntity) entity;

            //장비 슬롯 확인
            for(EquipmentSlot slot : EquipmentSlot.values()){
                ItemRegisteredEvent event1 = new ItemRegisteredEvent(livingEntity.getEquipment().getItem(slot), ItemRegisteredEvent.Cause.Chest);
                Bukkit.getPluginManager().callEvent(event1);

                if(event1.isCanceled()){
                    livingEntity.getEquipment().setItem(slot, NBTTagStore.set(new ItemStack(Material.AIR), Keys.Item_Checked.toString(), true));
                }else{
                    livingEntity.getEquipment().setItem(slot, NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true));
                }
            }
        }
        if(entity instanceof InventoryHolder){
            InventoryHolder holder = (InventoryHolder) entity;

            //인벤토리 확인
            ItemStack[] contents = holder.getInventory().getContents();
            for(int index = 0; index < contents.length; index++){
                if(NBTTagStore.containKey(contents[index], Keys.Item_Checked.toString())){
                    continue;
                }

                if(!ItemUtil.isExist(contents[index])){
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
            holder.getInventory().setContents(contents);
        }
    }

    @EventHandler
    public void onWaterBucketEmpty(PlayerBucketEmptyEvent event){   //양동이 비우기
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE){
            return;
        }
        ItemStack item = event.getItemStack();

        if(NBTTagStore.containKey(item, Keys.Item_Checked.toString())){
            return;
        }

        ItemRegisteredEvent event1 = new ItemRegisteredEvent(item, ItemRegisteredEvent.Cause.Water);
        Bukkit.getPluginManager().callEvent(event1);

        if(event1.isCanceled()){
            event.setItemStack(new ItemStack(Material.AIR));
        }else{
            event.setItemStack(NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true));
        }
    }

    @EventHandler
    public void onWaterBucketFill(PlayerBucketFillEvent event){     //양동이 채우기
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE){
            return;
        }
        ItemStack item = event.getItemStack();

        if(NBTTagStore.containKey(item, Keys.Item_Checked.toString())){
            return;
        }

        ItemRegisteredEvent event1 = new ItemRegisteredEvent(item, ItemRegisteredEvent.Cause.Water);
        Bukkit.getPluginManager().callEvent(event1);

        if(event1.isCanceled()){
            event.setItemStack(new ItemStack(Material.AIR));
        }else{
            event.setItemStack(NBTTagStore.set(event1.getItemStack(), Keys.Item_Checked.toString(), true));
        }
    }
    //</editor-fold>

    //<editor-fold desc="아이템 변경 검사">
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemEnchant(PrepareAnvilEvent event){     //아이템 인첸트
        ItemChangeEvent event1 = new ItemChangeEvent(event.getResult(), ItemChangeEvent.Cause.Enchant);
        Bukkit.getPluginManager().callEvent(event1);

        if(event1.isCanceled()){
            event.setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemRepair(PlayerItemMendEvent event){     //아이템 수리
        ItemChangeEvent event1 = new ItemChangeEvent(event.getItem(), ItemChangeEvent.Cause.Anvil);
        Bukkit.getPluginManager().callEvent(event1);

        if(event1.isCanceled()){
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDamage(PlayerItemDamageEvent event){      //아이템 사용
        ItemChangeEvent event1 = new ItemChangeEvent(event.getItem(), ItemChangeEvent.Cause.Anvil);
        Bukkit.getPluginManager().callEvent(event1);

        if(event1.isCanceled()){
            event.setCancelled(true);
        }
    }
    //</editor-fold>
}

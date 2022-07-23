package com.hirises.combat.command;

import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.config.Keys;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

//편의성 커맨드
public class CommandManager implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1){
            return false;
        }
        switch (args[0]){
            case "reload":
                //컨피그 리로드
                ConfigManager.init();
                return true;
            case "lore":{
                //아이템 로어 업데이트
                if(!(sender instanceof Player)){
                    return false;
                }
                ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
                if(!ItemUtil.isExist(item)){
                    return false;
                }
                ItemMeta meta = item.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.setLore(ConfigManager.itemLoreData.getItemLore(item));
                item.setItemMeta(meta);
                return true;
            }
            case "lore-all":{
                //인벤에 있는 모든 아이템 로어 업데이트
                if(!(sender instanceof Player)){
                    return false;
                }
                for(ItemStack item : ((Player) sender).getInventory().getContents()){
                    if(!ItemUtil.isExist(item)){
                        continue;
                    }
                    ItemMeta meta = item.getItemMeta();
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    meta.setLore(ConfigManager.itemLoreData.getItemLore(item));
                    item.setItemMeta(meta);
                }
                return true;
            }
            case "attribute":{
                //아이템 로어 재적용 (인첸트등 재계산)
                if(!(sender instanceof Player)){
                    return false;
                }
                ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
                if(!ItemUtil.isExist(item)){
                    return false;
                }
                if(ConfigManager.hasArmorData(item)){
                    NBTTagStore.set(item, Keys.Armor_Data.toString(), ConfigManager.getNewArmorData(item));
                }
                if(ConfigManager.hasProjectileData(item)){
                    NBTTagStore.set(item, Keys.Projectile_Data.toString(), ConfigManager.getNewProjectileData(item));
                }
                if(ConfigManager.hasWeaponData(item)){
                    NBTTagStore.set(item, Keys.Weapon_Data.toString(), ConfigManager.getNewWeaponData(item));
                }
                if(ConfigManager.useItemLore){
                    ItemMeta meta = item.getItemMeta();
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    meta.setLore(ConfigManager.itemLoreData.getItemLore(item));
                    item.setItemMeta(meta);
                }
                return true;
            }
            case "attribute-all":{
                //인벤에 있는 모든 아이템 로어 재적용 (인첸트등 재계산)
                if(!(sender instanceof Player)){
                    return false;
                }
                for(ItemStack item : ((Player) sender).getInventory().getContents()){
                    if(!ItemUtil.isExist(item)){
                        continue;
                    }
                    if(ConfigManager.hasArmorData(item)){
                        NBTTagStore.set(item, Keys.Armor_Data.toString(), ConfigManager.getNewArmorData(item));
                    }
                    if(ConfigManager.hasProjectileData(item)){
                        NBTTagStore.set(item, Keys.Projectile_Data.toString(), ConfigManager.getNewProjectileData(item));
                    }
                    if(ConfigManager.hasWeaponData(item)){
                        NBTTagStore.set(item, Keys.Weapon_Data.toString(), ConfigManager.getNewWeaponData(item));
                    }
                    if(ConfigManager.useItemLore){
                        ItemMeta meta = item.getItemMeta();
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        meta.setLore(ConfigManager.itemLoreData.getItemLore(item));
                        item.setItemMeta(meta);
                    }
                }
                return true;
            }
            case "attribute-chest":{
                //바라보고 있는 상자에 있는 모든 아이템 로어 재적용 (인첸트등 재계산)
                if(!(sender instanceof Player)){
                    return false;
                }
                Block block = ((Player) sender).getTargetBlock(EnumSet.of(Material.AIR), 30);
                if(block.getState() instanceof Container){
                    Container container = (Container) block.getState();
                    for(ItemStack item : container.getInventory().getContents()){
                        if(!ItemUtil.isExist(item)){
                            continue;
                        }
                        if(ConfigManager.hasArmorData(item)){
                            NBTTagStore.set(item, Keys.Armor_Data.toString(), ConfigManager.getNewArmorData(item));
                        }
                        if(ConfigManager.hasProjectileData(item)){
                            NBTTagStore.set(item, Keys.Projectile_Data.toString(), ConfigManager.getNewProjectileData(item));
                        }
                        if(ConfigManager.hasWeaponData(item)){
                            NBTTagStore.set(item, Keys.Weapon_Data.toString(), ConfigManager.getNewWeaponData(item));
                        }
                        if(ConfigManager.useItemLore){
                            ItemMeta meta = item.getItemMeta();
                            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            meta.setLore(ConfigManager.itemLoreData.getItemLore(item));
                            item.setItemMeta(meta);
                        }
                    }
                    return true;
                }
                return false;
            }
        }
        return false;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length){
            case 0:{
                List<String> output = Arrays.asList("reload", "lore", "lore-all", "attribute", "attribute-all", "attribute-chest");
                return output;
            }
            case 1:{
                List<String> output = Arrays.asList("reload", "lore", "lore-all", "attribute", "attribute-all", "attribute-chest");
                return output.stream().filter(value -> value.startsWith(args[0])).collect(Collectors.toList());
            }
        }
        return null;
    }
}

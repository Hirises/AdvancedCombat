package com.hirises.combat.command;

import com.hirises.combat.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandManager implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length < 1){
            return false;
        }
        switch (args[0]){
            case "reload":
                ConfigManager.init();
                return true;
            case "lore":
                if(!(sender instanceof Player)){
                    return false;
                }
                ItemStack item = ((Player) sender).getInventory().getItemInMainHand();
                ItemMeta meta = item.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.setLore(ConfigManager.itemLoreData.getItemLore(item));
                item.setItemMeta(meta);
                return true;
        }
        return false;
    }
}

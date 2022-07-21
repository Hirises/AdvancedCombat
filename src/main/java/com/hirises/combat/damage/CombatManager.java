package com.hirises.combat.damage;

import com.google.common.util.concurrent.AtomicDouble;
import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.config.Keys;
import com.hirises.combat.damage.data.*;
import com.hirises.core.armorstand.ArmorStandWrapper;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.task.CancelableTask;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CombatManager {
    public final static int DAMAGE_MODIFIER = 10;
    public final static Map<LivingEntity, FoodTask> foodMap = new HashMap<>();
    private static class FoodTask {
        private LivingEntity entity;
        private AtomicDouble heal;
        private CancelableTask task;

        public FoodTask(LivingEntity entity){
            this.entity = entity;
            this.heal = new AtomicDouble();
            this.task = null;
        }

        public void addHeal(double value){
            this.heal.addAndGet(value);
            if(this.entity == null){
                heal = null;
                task = null;
                entity = null;
                foodMap.remove(this);
                return;
            }
            synchronized (entity){
                if(this.task == null){
                    this.task = new CancelableTask(AdvancedCombat.getInst(), ConfigManager.foodDelay, ConfigManager.foodDelay) {
                        @Override
                        public void run() {
                            double amount = heal.get();
                            if(amount <= 0.01){
                                cancel();
                                task = null;
                                heal.set(0);
                                return;
                            }
                            if(!entity.isValid()){
                                cancel();
                                heal = null;
                                task = null;
                                entity = null;
                                foodMap.remove(this);
                                return;
                            }
                            heal(entity, amount);
                        }
                    };
                }
            }
        }

        public void removeHeal(double value){
            if(this.entity == null){
                heal = null;
                task = null;
                entity = null;
                foodMap.remove(this);
                return;
            }
            this.heal.addAndGet(-value);
        }

        public void cancelTask(){
            this.task.cancel();
            this.task = null;
        }
    }

    public static void damage(LivingEntity entity, double damage) {
        if(!NBTTagStore.containKey(entity, Keys.Current_Health.toString())){
            NBTTagStore.set(entity, Keys.Current_Health.toString(), getMaxHealth(entity));
        }
        double health = NBTTagStore.get(entity, Keys.Current_Health.toString(), Double.class);
        health -= damage;
        NBTTagStore.set(entity, Keys.Current_Health.toString(), health);
        applyHealth(entity);
    }

    public static void heal(LivingEntity entity, double heal) {
        if(!NBTTagStore.containKey(entity, Keys.Current_Health.toString())){
            return;
        }
        double health = NBTTagStore.get(entity, Keys.Current_Health.toString(), Double.class);
        if(health + heal > getMaxHealth(entity)){
            heal = getMaxHealth(entity) - health;
        }
        health += heal;
        NBTTagStore.set(entity, Keys.Current_Health.toString(), health);
        applyHealth(entity);
        if(ConfigManager.useDamageMeter && heal > 0){
            spawnDamageMeter(entity.getEyeLocation(), ConfigManager.damageMeterData.getHealMeterString(heal));
        }
    }

    public static void startHealGradually(LivingEntity entity, double heal){
        foodMap.computeIfAbsent(entity, value -> new FoodTask(value)).addHeal(heal);
    }

    public static void endHealGradually(LivingEntity entity, double heal){
        if(!foodMap.containsKey(entity)){
            return;
        }
        foodMap.get(entity).removeHeal(heal);
    }

    public static void applyHealth(LivingEntity entity){
        if(entity == null){
            return;
        }
        double health = NBTTagStore.get(entity, Keys.Current_Health.toString(), Double.class) / DAMAGE_MODIFIER;
        if(health <= 0){
            if(entity instanceof Player){
                Player player = (Player) entity;
                if(player.getInventory().contains(Material.TOTEM_OF_UNDYING)){
                    if(player.getCooldown(Material.TOTEM_OF_UNDYING) == 0){
                        player.getInventory().removeItem(new ItemStack(Material.TOTEM_OF_UNDYING));
                        //토템 적용
                        applyTotemOfUndying(entity);
                        return;
                    }
                }else if(entity.getEquipment().getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING)){
                    entity.getEquipment().setItemInOffHand(new ItemStack(Material.AIR));
                    //토템 적용
                    applyTotemOfUndying(entity);
                    return;
                }
            }else if(entity instanceof InventoryHolder){
                InventoryHolder holder = (InventoryHolder) entity;
                if(holder.getInventory().contains(Material.TOTEM_OF_UNDYING)){
                    holder.getInventory().removeItem(new ItemStack(Material.TOTEM_OF_UNDYING));
                    //토템 적용
                    applyTotemOfUndying(entity);
                    return;
                }else if(entity.getEquipment().getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING)){
                    entity.getEquipment().setItemInOffHand(new ItemStack(Material.AIR));
                    //토템 적용
                    applyTotemOfUndying(entity);
                    return;
                }
            }else{
                if(entity.getEquipment().getItemInMainHand().getType().equals(Material.TOTEM_OF_UNDYING)){
                    entity.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
                    //토템 적용
                    applyTotemOfUndying(entity);
                    return;
                }else if(entity.getEquipment().getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING)){
                    entity.getEquipment().setItemInOffHand(new ItemStack(Material.AIR));
                    //토템 적용
                    applyTotemOfUndying(entity);
                    return;
                }
            }
            health = 0;
        }
        entity.setHealth(health);
    }

    public static void applyTotemOfUndying(LivingEntity entity){
        NBTTagStore.set(entity, Keys.Current_Health.toString(), 0);
        ConfigManager.undyingTotem.eat(entity, 1);
    }

    public static double getMaxHealth(LivingEntity entity){
        return entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * DAMAGE_MODIFIER;
    }

    public static double getDefence(LivingEntity entity, DamageTag tag, List<DefencePenetrate> penetrates){
        EntityEquipment equipment = entity.getEquipment();
        double defence = 0;
        if(ItemUtil.isExist(equipment.getHelmet())){
            defence += getNewArmorData(equipment.getHelmet()).getFinalDefence(tag, penetrates);
        }
        if(ItemUtil.isExist(equipment.getChestplate())){
            defence += getNewArmorData(equipment.getChestplate()).getFinalDefence(tag, penetrates);
        }
        if(ItemUtil.isExist(equipment.getLeggings())){
            defence += getNewArmorData(equipment.getLeggings()).getFinalDefence(tag, penetrates);
        }
        if(ItemUtil.isExist(equipment.getBoots())){;
            defence += getNewArmorData(equipment.getBoots()).getFinalDefence(tag, penetrates);
        }
        if(entity instanceof Player){
            Player player = (Player) entity;
            if(player.isBlocking()){
                defence += getNewArmorData(new ItemStack(Material.SHIELD)).getFinalDefence(tag, penetrates);
            }
        }
        return defence;
    }

    public static int getWeight(LivingEntity entity){
        EntityEquipment equipment = entity.getEquipment();
        return getWeight(equipment.getItemInMainHand(), equipment.getItemInOffHand(), equipment.getHelmet(),
                equipment.getChestplate(), equipment.getLeggings(), equipment.getBoots());
    }

    public static int getWeight(ItemStack main, ItemStack off, ItemStack helmet, ItemStack chest, ItemStack leggings, ItemStack boots){
        int weight = 0;
        if(ItemUtil.isExist(main)){
            weight += getWeaponData(main).getWeight();
        }
        if(ItemUtil.isExist(off)){
            weight += getWeaponData(off).getWeight();
        }
        if(ItemUtil.isExist(helmet)){
            weight += getNewArmorData(helmet).getWeight();
        }
        if(ItemUtil.isExist(chest)){
            weight += getNewArmorData(chest).getWeight();
        }
        if(ItemUtil.isExist(leggings)){
            weight += getNewArmorData(leggings).getWeight();
        }
        if(ItemUtil.isExist(boots)){
            weight += getNewArmorData(boots).getWeight();
        }
        return weight;
    }

    public static void spawnDamageMeter(Location loc, String string){
        ArmorStandWrapper meter = ArmorStandWrapper.getInstance(
                loc.add(Vector.getRandom().add(new Vector(-0.5, 0, -0.5)))
        );
        meter.asMark();
        meter.get().setCustomName(string);
        meter.get().setCustomNameVisible(true);
        NBTTagStore.set(meter.get(), Keys.DamageMeter.toString(), true);
        Bukkit.getScheduler().runTaskLater(AdvancedCombat.getInst(), () -> {
            meter.get().remove();
        }, ConfigManager.damageMeterData.duration());
    }

    public static boolean hasWeaponData(ItemStack weapon){
        return ConfigManager.weaponDataMap.containsKey(weapon.getType());
    }

    public static boolean hasArmorData(ItemStack armor){
        return ConfigManager.armorDataMap.containsKey(armor.getType());
    }

    public static boolean hasProjectileData(LivingEntity entity){
        ItemStack weapon = entity.getEquipment().getItemInMainHand();
        if(!ItemUtil.isExist(weapon) || !hasProjectileData(weapon)){
            weapon = entity.getEquipment().getItemInOffHand();
            return hasProjectileData(weapon);
        }
        return hasProjectileData(weapon);
    }

    public static boolean hasProjectileData(ItemStack armor){
        return ConfigManager.projectileDataMap.containsKey(armor.getType());
    }

    public static boolean hasEntityData(EntityType type){
        return ConfigManager.entityDataMap.containsKey(type);
    }

    public static DamageApplier getEntityData(EntityType type){
        DamageApplier output = ConfigManager.entityDataMap.get(type);
        if(output == null){
            return new DamageApplier();
        }
        return output;
    }

    public static boolean hasFoodData(ItemStack item){
        return ConfigManager.foodDataMap.containsKey(item.getType());
    }

    public static FoodData getFoodData(ItemStack item){
        FoodData output = ConfigManager.foodDataMap.get(item.getType());
        if(output == null){
            return new FoodData();
        }
        return output;
    }

    public static ProjectileData getProjectileData(LivingEntity entity){
        ItemStack weapon = entity.getEquipment().getItemInMainHand();
        if(!ItemUtil.isExist(weapon) || !hasProjectileData(weapon)){
            weapon = entity.getEquipment().getItemInOffHand();
            return getProjectileData(weapon);
        }
        return getProjectileData(weapon);
    }

    public static ProjectileData getProjectileData(ItemStack weapon){
        if(NBTTagStore.containKey(weapon, Keys.Projectile_Data.toString())){
            return NBTTagStore.get(weapon, Keys.Projectile_Data.toString(), ProjectileData.class);
        }
        return getNewProjectileData(weapon);
    }

    public static ProjectileData getNewProjectileData(ItemStack weapon){
        ProjectileData output = ConfigManager.projectileDataMap.get(weapon.getType());
        if(output == null){
            return ConfigManager.normalArrow;
        }
        Map<Enchantment, Integer> enchants = weapon.getItemMeta().getEnchants();
        if(enchants != null && enchants.size() > 0){
            for(Enchantment enchant : enchants.keySet()){
                if(hasProjectileEnchantData(enchant)){
                    DamageEnchantData data = getProjectileEnchantData(enchant);
                    output = output.merge(data.getEnchant(enchants.get(enchant)));
                }
            }
        }
        return output;
    }

    public static WeaponData getWeaponData(LivingEntity entity){
        ItemStack weapon = entity.getEquipment().getItemInMainHand();
        if(!ItemUtil.isExist(weapon)){
            return ConfigManager.bearHand;
        }
        return getWeaponData(weapon);
    }

    public static WeaponData getWeaponData(ItemStack weapon){
        if(NBTTagStore.containKey(weapon, Keys.Weapon_Data.toString())){
            return NBTTagStore.get(weapon, Keys.Weapon_Data.toString(), WeaponData.class);
        }
        return getNewWeaponData(weapon);
    }

    public static WeaponData getNewWeaponData(ItemStack weapon){
        WeaponData output = ConfigManager.weaponDataMap.get(weapon.getType());
        if(output == null){
            return ConfigManager.bearHand;
        }
        Map<Enchantment, Integer> enchants = weapon.getItemMeta().getEnchants();
        if(enchants != null && enchants.size() > 0){
            for(Enchantment enchant : enchants.keySet()){
                if(hasWeaponEnchantData(enchant)){
                    DamageEnchantData data = getWeaponEnchantData(enchant);
                    output = output.merge(data.getEnchant(enchants.get(enchant)));
                }
            }
        }
        return output;
    }

    public static ArmorData getArmorData(ItemStack armor){
        if(NBTTagStore.containKey(armor, Keys.Armor_Data.toString())){
            return NBTTagStore.get(armor, Keys.Armor_Data.toString(), ArmorData.class);
        }
        return getNewArmorData(armor);
    }

    public static boolean hasProjectileEnchantData(Enchantment enchantment){
        return ConfigManager.projectileEnchantDataMap.containsKey(enchantment);
    }

    public static boolean hasArmorEnchantData(Enchantment enchantment){
        return ConfigManager.armorEnchantDataMap.containsKey(enchantment);
    }

    public static boolean hasWeaponEnchantData(Enchantment enchantment){
        return ConfigManager.weaponEnchantDataMap.containsKey(enchantment);
    }

    public static DamageEnchantData getProjectileEnchantData(Enchantment enchantment){
        if(hasProjectileEnchantData(enchantment)){
            return ConfigManager.projectileEnchantDataMap.get(enchantment);
        }
        return new DamageEnchantData();
    }

    public static DamageEnchantData getWeaponEnchantData(Enchantment enchantment){
        if(hasWeaponEnchantData(enchantment)){
            return ConfigManager.weaponEnchantDataMap.get(enchantment);
        }
        return new DamageEnchantData();
    }

    public static ArmorEnchantData getArmorEnchantData(Enchantment enchantment){
        if(hasArmorEnchantData(enchantment)){
            return ConfigManager.armorEnchantDataMap.get(enchantment);
        }
        return new ArmorEnchantData();
    }

    public static ArmorData getNewArmorData(ItemStack armor){
        ArmorData output = ConfigManager.armorDataMap.get(armor.getType());
        if(output == null){
            return new ArmorData();
        }
        Map<Enchantment, Integer> enchants = armor.getItemMeta().getEnchants();
        if(enchants != null && enchants.size() > 0){
            for(Enchantment enchant : enchants.keySet()){
                if(hasArmorEnchantData(enchant)){
                    ArmorEnchantData data = getArmorEnchantData(enchant);
                    output = output.merge(data.getEnchant(enchants.get(enchant)));
                }
            }
        }
        return output;
    }
}

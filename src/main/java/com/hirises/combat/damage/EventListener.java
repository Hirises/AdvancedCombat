package com.hirises.combat.damage;

import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.config.Keys;
import com.hirises.combat.damage.data.*;
import com.hirises.combat.item.CustomItemManager;
import com.hirises.combat.item.ItemRegisteredEvent;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EventListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(ConfigManager.bearHand.getAttackSpeed());
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(
                (ConfigManager.weightData.normalSpeedRate() / 1000.0)
        );
        assertPlayerSettings(player);
    }

    private void assertPlayerSettings(Player player) {
        if(player.getFoodLevel() > 19){
            player.setFoodLevel(19);
        }
        for(String key : NBTTagStore.getKeys(player, Keys.MaterialCoolDown_NoDot.toString())){
            Material mat = Material.valueOf(key);
            int restTick = (int) (NBTTagStore.get(player, Keys.MaterialCoolDown + key, Long.class) - Util.getCurrentTick());
            if(restTick > 0){
                player.setCooldown(mat, restTick);
            }
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        EntityEquipment equipment = player.getEquipment();
        double speedRate = ConfigManager.weightData.getSpeedRate(
                CombatManager
                        .getWeight(player.getInventory().getItem(event.getNewSlot()), player.getInventory().getItemInOffHand(),
                                equipment.getHelmet(), equipment.getChestplate(),
                                equipment.getLeggings(), equipment.getBoots())
        );
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedRate / 1000.0);
    }

    @EventHandler
    public void onUndying(EntityResurrectEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();
        double speedRate = ConfigManager.weightData.getSpeedRate(CombatManager.getWeight(player));
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedRate / 1000.0);
        NBTTagStore.set(player, Keys.Current_Health.toString(), player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * CombatManager.DAMAGE_MODIFIER);
        CombatManager.applyHealth(player);
        Bukkit.getScheduler().runTaskLater(AdvancedCombat.getInst(), () -> {
            assertPlayerSettings(player);
        }, 1);
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent event){
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(AdvancedCombat.getInst(), () -> {
            double speedRate = ConfigManager.weightData.getSpeedRate(CombatManager.getWeight(player));
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedRate / 1000.0);
        }, 1);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(AdvancedCombat.getInst(), () -> {
            double speedRate = ConfigManager.weightData.getSpeedRate(CombatManager.getWeight(player));
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedRate / 1000.0);
        }, 1);
    }

    @EventHandler
    public void onItemPickUp(EntityPickupItemEvent event){
        if(event.getEntity() instanceof Player){
            Player player = (Player) event.getEntity();
            Bukkit.getScheduler().runTaskLater(AdvancedCombat.getInst(), () -> {
                double speedRate = ConfigManager.weightData.getSpeedRate(CombatManager.getWeight(player));
                player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedRate / 1000.0);
            }, 1);
        }
    }

    @EventHandler
    public void armorCheck(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        double speedRate = ConfigManager.weightData.getSpeedRate(CombatManager.getWeight(player));
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedRate / 1000.0);
    }

    @EventHandler
    public void armorCheck(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(ItemUtil.isExist(event.getItem()) && CombatManager.hasArmorData(event.getItem())){
                Bukkit.getScheduler().runTaskLater(AdvancedCombat.getInst(), () -> {
                    double speedRate = ConfigManager.weightData.getSpeedRate(CombatManager.getWeight(player));
                    player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedRate / 1000.0);
                }, 1);
            }
        }
    }

    @EventHandler
    public void onEatFood(PlayerItemConsumeEvent event){
        ItemStack food = event.getItem();
        if(ItemUtil.isExist(food) && CombatManager.hasFoodData(food)){
            FoodData data = CombatManager.getFoodData(food);
            Player player = event.getPlayer();
            event.setCancelled(true);
            ItemStack item;
            if(player.getInventory().getItemInMainHand().getType().equals(food.getType())){
                item = player.getInventory().getItemInMainHand();
            }else{
                item = player.getInventory().getItemInOffHand();
            }
            if(item.getAmount() > data.getMaxConsumeAmount()){
                data.eat(player, data.getMaxConsumeAmount());
                if(player.getGameMode() != GameMode.CREATIVE){
                    ItemUtil.operateAmount(item, -1 * data.getMaxConsumeAmount());
                }
            }else{
                data.eat(player, item.getAmount());
                if(player.getGameMode() != GameMode.CREATIVE) {
                    if (player.getInventory().getItemInMainHand().equals(food)) {
                        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    } else {
                        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event){
        Player player = (Player) event.getEntity();
        if(event.getFoodLevel() > 19){
            event.setCancelled(true);
            player.setFoodLevel(19);
        }
    }

    @EventHandler
    public void onEntityHeal(EntityRegainHealthEvent event){
        if(event.getEntity() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity) event.getEntity();
            switch (event.getRegainReason()){
                case EATING:{
                    event.setAmount(0);
                    return;
                }
                default: {
                    break;
                }
            }
            CombatManager.heal(entity, event.getAmount() * CombatManager.DAMAGE_MODIFIER);
            event.setAmount(0);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event){
        Projectile projectile = event.getEntity();
        if(projectile.getShooter() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity) projectile.getShooter();
            if(CombatManager.hasProjectileData(entity)){
                ProjectileData data = CombatManager.getProjectileData(entity);
                NBTTagStore.set(projectile, Keys.Projectile_Damage.toString(), data.getDamage());
            }else if(CombatManager.hasEntityData(entity.getType())){
                DamageApplier data = CombatManager.getEntityData(entity.getType());
                NBTTagStore.set(projectile, Keys.Projectile_Damage.toString(), data);
            }
        }else{
            NBTTagStore.set(projectile, Keys.Projectile_Damage.toString(), ConfigManager.normalArrow);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(event.getEntity() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity) event.getEntity();
            DamageApplier applier = null;
            switch (event.getCause()){
                case ENTITY_ATTACK: //플레이어 공격
                case ENTITY_SWEEP_ATTACK: //휘칼
                case PROJECTILE: //화살
                    //데미지 따로 계산
                    event.setDamage(0);
                    return;
                case THORNS: //가시
                    event.setCancelled(true);
                    return;
                case FALLING_BLOCK:
                case CONTACT:
                    //물리 데미지
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Physics));
                    break;
                case ENTITY_EXPLOSION:
                case BLOCK_EXPLOSION:
                    //물리, 폭발 데미지
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Physics, DamageTag.DamageType.Explosion));
                    break;
                case FIRE:
                case FIRE_TICK:
                case HOT_FLOOR:
                case LAVA:
                    //일반, 화염 데미지
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Normal, DamageTag.DamageType.Fire));
                    break;
                case FREEZE:
                    //일반 데미지
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Normal));
                    break;
                case SONIC_BOOM:
                    //마법, 원거리 데미지
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Magic, DamageTag.DamageType.Projectile));
                    break;
                case DRAGON_BREATH:
                case MAGIC:
                    //마법 데미지
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Magic));
                    break;
                case CUSTOM:
                    //스텍 오버플로우 방지
                    return;
                case FALL:
                    //낙하
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Const, DamageTag.DamageType.Fall));
                    break;
                default:
                    //고정 데미지
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Const));
                    break;
            }
            applier.apply(entity, CombatManager.DAMAGE_MODIFIER);
            event.setDamage(0);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event){
        if(event.getEntity() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity) event.getEntity();
            double damageRate = 1;
            if(entity.getType().equals(EntityType.PLAYER)){
                damageRate = ConfigManager.playerDamageRate;
            }else{
                damageRate = ConfigManager.etcDamageRate;
            }
            PartialDamageApplier finalApplier = new PartialDamageApplier();
            if(event.getDamager() instanceof Projectile){
                Projectile projectile = (Projectile) event.getDamager();
                DamageApplier data = NBTTagStore.get(projectile, Keys.Projectile_Damage.toString(), DamageApplier.class);
                if(data == null){
                    return;
                }
                finalApplier.merge(data);
            }else if(event.getDamager() instanceof LivingEntity){
                LivingEntity damager = (LivingEntity) event.getDamager();
                WeaponData weapon = CombatManager.getWeaponData(damager);
                if(event.getDamager() instanceof Player){
                    Player player = (Player) event.getDamager();
                    if(player.getAttackCooldown() <= 0.9){
                        event.setCancelled(true);
                        return;
                    }
                    if(entity.getLocation().distanceSquared(player.getLocation()) > weapon.getAttackDistance() * weapon.getAttackDistance()){
                        event.setCancelled(true);
                        return;
                    }
                }else{
                    EntityType entityType = damager.getType();
                    if(CombatManager.hasEntityData(entityType)){
                        DamageApplier monsterDamage = CombatManager.getEntityData(entityType);
                        finalApplier.merge(monsterDamage);
                    }
                }
                DamageApplier applier = weapon.getDamage();
                finalApplier.merge(applier);
            }
            finalApplier.apply(entity, damageRate);
        }
    }
}

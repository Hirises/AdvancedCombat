package com.hirises.combat.damage;

import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.config.Keys;
import com.hirises.combat.damage.data.DamageApplier;
import com.hirises.combat.damage.data.DamageTag;
import com.hirises.combat.damage.data.ProjectileData;
import com.hirises.combat.damage.data.WeaponData;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
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

public class EventListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(ConfigManager.bearHand.getAttackSpeed());
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(
                (ConfigManager.weightData.normalSpeedRate() / 1000.0)
        );
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
    public void onRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();
        double speedRate = ConfigManager.weightData.getSpeedRate(CombatManager.getWeight(player));
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedRate / 1000.0);
        NBTTagStore.set(player, Keys.Current_Health.toString(), player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * CombatManager.DAMAGE_MODIFIER);
        CombatManager.applyHealth(player);
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
    public void onEntityHeal(EntityRegainHealthEvent event){
        if(event.getEntity() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity) event.getEntity();
            CombatManager.heal(entity, event.getAmount() * CombatManager.DAMAGE_MODIFIER);
            event.setAmount(0);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event){
        Projectile projectile = event.getEntity();
        if(projectile.getShooter() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity) projectile.getShooter();
            ProjectileData data = CombatManager.getProjectileData(entity);
            NBTTagStore.set(projectile, Keys.Projectile_Damage.toString(), data);
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
                case FALL:
                    //낙하
                case CUSTOM:
                    //스텍 오버플로우 방지
                    return;
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
            if(event.getDamager() instanceof Projectile){
                event.setDamage(0);
                Projectile projectile = (Projectile) event.getDamager();
                ProjectileData data = NBTTagStore.get(projectile, Keys.Projectile_Damage.toString(), ProjectileData.class);
                data.getDamage().apply(entity);
            }else if(event.getDamager() instanceof LivingEntity){
                event.setDamage(0);
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
                }
                DamageApplier applier = weapon.getDamage();
                applier.apply(entity);
            }
        }
    }
}

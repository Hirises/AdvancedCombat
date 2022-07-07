package com.hirises.combat.damage;

import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.damage.impl.SimpleCombatManager;
import com.hirises.combat.damage.impl.SimpleDamageApplier;
import com.hirises.combat.damage.impl.SimpleDamageTag;
import com.hirises.core.util.Util;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onEntityHeal(EntityRegainHealthEvent event){
        if(event.getEntity() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity) event.getEntity();
            AdvancedCombat.getCombatManager().heal(entity, event.getAmount() * SimpleCombatManager.DAMAGE_MODIFIER);
            event.setAmount(0);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(event.getEntity() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity) event.getEntity();
            SimpleDamageApplier applier = null;
            switch (event.getCause()){
                case ENTITY_ATTACK: //플레이어 공격
                case ENTITY_SWEEP_ATTACK: //휘칼
                case PROJECTILE: //화살
                    //데미지 따로 계산
                    applier = new SimpleDamageApplier(event.getDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Physics));
                    applier.apply(entity, AbstractCombatManager.DAMAGE_MODIFIER);
                    return;
                case THORNS: //가시
                    event.setCancelled(true);
                    return;
                case FALL:
                    //낙하
                case FALLING_BLOCK:
                case CONTACT:
                    //물리 데미지
                    applier = new SimpleDamageApplier(event.getDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Physics));
                    break;
                case ENTITY_EXPLOSION:
                case BLOCK_EXPLOSION:
                    //물리, 폭발 데미지
                    applier = new SimpleDamageApplier(event.getDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Physics, SimpleDamageTag.DamageType.Explosion));
                    break;
                case FIRE:
                case FIRE_TICK:
                case HOT_FLOOR:
                case LAVA:
                    //일반, 화염 데미지
                    applier = new SimpleDamageApplier(event.getDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Normal, SimpleDamageTag.DamageType.Fire));
                    break;
                case FREEZE:
                    //일반 데미지
                    applier = new SimpleDamageApplier(event.getDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Normal));
                    break;
                case SONIC_BOOM:
                    //마법, 원거리 데미지
                    applier = new SimpleDamageApplier(event.getDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Magic, SimpleDamageTag.DamageType.Projectile));
                    break;
                case DRAGON_BREATH:
                case MAGIC:
                    //마법 데미지
                    applier = new SimpleDamageApplier(event.getDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Magic));
                    break;
                default:
                    //고정 데미지
                    applier = new SimpleDamageApplier(event.getDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Const));
                    break;
            }
            applier.apply(entity, AbstractCombatManager.DAMAGE_MODIFIER);
            event.setDamage(0);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event){
        if(event.getEntity().getType().equals(EntityType.PLAYER)){

        }
    }
}

package com.hirises.combat.damage;

import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.damage.impl.SimpleDamage;
import com.hirises.combat.damage.impl.SimpleDamageApplier;
import com.hirises.combat.damage.impl.SimpleDamageTag;
import com.hirises.core.util.Util;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(event.getEntity() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity) event.getEntity();
            SimpleDamageApplier applier = null;
            switch (event.getCause()){
                case ENTITY_ATTACK:
                case ENTITY_SWEEP_ATTACK:
                case PROJECTILE:
                case THORNS: //가시
                    //데미지 따로 계산
                    return;
                case FALL:
                    //낙하
                case FALLING_BLOCK:
                case CONTACT:
                    //물리 데미지
                    applier = new SimpleDamageApplier(event.getFinalDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Physics));
                    break;
                case ENTITY_EXPLOSION:
                case BLOCK_EXPLOSION:
                    //물리, 폭발 데미지
                    applier = new SimpleDamageApplier(event.getFinalDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Physics, SimpleDamageTag.DamageType.Explosion));
                    break;
                case FIRE:
                case FIRE_TICK:
                case HOT_FLOOR:
                case LAVA:
                    //일반, 화염 데미지
                    applier = new SimpleDamageApplier(event.getFinalDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Normal, SimpleDamageTag.DamageType.Fire));
                    break;
                case FREEZE:
                    //일반 데미지
                    applier = new SimpleDamageApplier(event.getFinalDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Normal));
                    break;
                case SONIC_BOOM:
                    //마법, 원거리 데미지
                    applier = new SimpleDamageApplier(event.getFinalDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Magic, SimpleDamageTag.DamageType.Projectile));
                    break;
                case DRAGON_BREATH:
                case MAGIC:
                    //마법 데미지
                    applier = new SimpleDamageApplier(event.getFinalDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Magic));
                    break;
                default:
                    //고정 데미지
                    applier = new SimpleDamageApplier(event.getFinalDamage(), new SimpleDamageTag(SimpleDamageTag.AttackType.Const));
                    break;
            }
            applier.apply(entity);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event){
        if(event.getEntity().getType().equals(EntityType.PLAYER)){
            Util.logging("hit entity");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByBlockEvent event){
        if(event.getEntity().getType().equals(EntityType.PLAYER)){
            Util.logging("hit block");
        }
    }
}

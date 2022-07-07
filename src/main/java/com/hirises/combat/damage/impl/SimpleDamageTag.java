package com.hirises.combat.damage.impl;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

import java.util.EnumSet;

public class SimpleDamageTag implements DataUnit {
    public enum AttackType{
        Normal,
        Physics,
        Magic,
        Const,
        ;
    }
    public enum DamageType{
        Fire,
        Projectile,
        Explosion,
        ;
    }

    private AttackType attackType;
    private EnumSet<DamageType> damageTypes;

    public SimpleDamageTag(){
        this(AttackType.Normal);
    }

    public SimpleDamageTag(AttackType attackType){
        this(attackType, EnumSet.noneOf(DamageType.class));
    }

    public SimpleDamageTag(AttackType attackType, DamageType damageType){
        this(attackType, EnumSet.of(damageType));
    }

    public SimpleDamageTag(AttackType attackType, EnumSet<DamageType> damageTypes){
        this.attackType = attackType;
        this.damageTypes = damageTypes;
    }

    public AttackType getAttackType() {
        return attackType;
    }

    public EnumSet<DamageType> getDamageTypes() {
        return damageTypes;
    }

    public boolean hasDamageType(DamageType type){
        return damageTypes.contains(type);
    }

    public void addDamageType(DamageType type){
        damageTypes.add(type);
    }

    public void removeDamageType(DamageType type){
        damageTypes.remove(type);
    }

    public void setAttackType(AttackType attackType) {
        this.attackType = attackType;
    }

    public boolean equalAttackType(AttackType attackType){
        return this.attackType == attackType;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.attackType = AttackType.valueOf(yml.getToString(root + ".속성"));
        this.damageTypes = EnumSet.noneOf(DamageType.class);
        for(String element : yml.getConfig().getStringList(root + ".타입")){
            damageTypes.add(DamageType.valueOf(element));
        }
    }

    @Override
    public void save(YamlStore yml, String root) {
        //No Use
    }
}
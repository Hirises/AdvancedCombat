package com.hirises.combat.damage.impl;

import java.util.EnumSet;

public class SimpleDamageTag{
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
}
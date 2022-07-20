package com.hirises.combat.damage.data;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

import java.util.EnumSet;
import java.util.Objects;

public class DamageTag implements DataUnit {
    public enum AttackType{
        Normal,
        Physics,
        Magic,
        Const,  //방깍시 전부 해당, 방어시 전부 해당 X
        ;
    }
    public enum DamageType{
        Fire,
        Projectile,
        Explosion,
        Fall,
        ;
    }

    private AttackType attackType;
    private EnumSet<DamageType> damageTypes;

    public DamageTag(){
        this(AttackType.Normal);
    }

    public DamageTag(AttackType attackType){
        this(attackType, EnumSet.noneOf(DamageType.class));
    }

    public DamageTag(AttackType attackType, DamageType damageType){
        this(attackType, EnumSet.of(damageType));
    }

    public DamageTag(AttackType attackType, EnumSet<DamageType> damageTypes){
        this.attackType = attackType;
        this.damageTypes = damageTypes;
    }

    public int getDamageTypeFlag(){
        int flag = 0;
        for(DamageType type : damageTypes){
            flag |= (1 << type.ordinal());
        }
        return flag;
    }

    public static EnumSet<DamageType> getDamageTypeFromFlag(int flag){
        EnumSet<DamageType> output = EnumSet.noneOf(DamageType.class);
        for(DamageType type : DamageType.values()){
            if((flag & (1 << type.ordinal())) != 0){
                output.add(type);
            }
        }
        return output;
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

    public boolean checkDefenceType(DamageTag damageTag){
        if(damageTag.equalAttackType(AttackType.Const)){
            if(damageTag.hasDamageType(DamageType.Fall) && this.attackType == AttackType.Normal){
                return this.damageTypes.contains(DamageType.Fall);
            }
            return false;
        }
        if(damageTag.equalAttackType(this.attackType) || this.attackType == AttackType.Normal){
            if(this.damageTypes.size() > 0){
                return damageTag.getDamageTypes().containsAll(this.damageTypes);
            }else {
                return true;
            }
        }else{
            return false;
        }
    }

    public boolean checkDefencePenetrateType(DamageTag damageTag){
        if(damageTag.getAttackType().equals(DamageTag.AttackType.Const)
                || damageTag.getAttackType().equals(this.attackType)){
            if(damageTag.getDamageTypes().size() > 0){
                return this.damageTypes.containsAll(damageTag.getDamageTypes());
            }else {
                return true;
            }
        }else{
            return false;
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DamageTag damageTag = (DamageTag) o;
        return attackType == damageTag.attackType && Objects.equals(damageTypes, damageTag.damageTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attackType, damageTypes);
    }
}
package com.hirises.combat.damage.calculate;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;

//데미지 타입
//꼭 데미지 뿐만 아니라 방어력, 방어관통력등에서 사용된다
@Immutable
@ThreadSafe
public class DamageTag implements DataUnit {
    //데미지 타입
    //1종류를 가진다
    public enum AttackType{
        Normal,
        Physics,
        Magic,
        Const,  //방어관통 처리시 전부 포함, 방어력 처리시 전부 미포함
        ;
    }
    //데미지 속성
    //0개 이상을 포함한다.
    public enum DamageAttribute {
        Fire,
        Projectile,
        Explosion,
        Fall,
        ;
    }

    private AttackType attackType;
    private EnumSet<DamageAttribute> damageAttributes;

    public DamageTag(){
        this(AttackType.Normal);
    }

    public DamageTag(AttackType attackType){
        this(attackType, EnumSet.noneOf(DamageAttribute.class));
    }

    public DamageTag(AttackType attackType, DamageAttribute damageAttribute){
        this(attackType, EnumSet.of(damageAttribute));
    }

    public DamageTag(AttackType attackType, EnumSet<DamageAttribute> damageAttributes){
        this.attackType = attackType;
        this.damageAttributes = damageAttributes;
    }

    //데미지 속성을 플래그로 반환
    public int getDamageAttributeFlag(){
        int flag = 0;
        for(DamageAttribute type : damageAttributes){
            flag |= (1 << type.ordinal());
        }
        return flag;
    }

    //플래그에서 데미지 속성을 복원
    public static EnumSet<DamageAttribute> getDamageAttributeFromFlag(int flag){
        EnumSet<DamageAttribute> output = EnumSet.noneOf(DamageAttribute.class);
        for(DamageAttribute type : DamageAttribute.values()){
            if((flag & (1 << type.ordinal())) != 0){
                output.add(type);
            }
        }
        return output;
    }

    //입력된 공격 속성을 이 방어력 속성이 방어할 수 있는가 여부
    public boolean checkDefenceType(DamageTag damageTag){
        if(damageTag.equalAttackType(AttackType.Const)){
            if(damageTag.hasDamageAttribute(DamageAttribute.Fall) && this.attackType == AttackType.Normal){
                return this.damageAttributes.contains(DamageAttribute.Fall);
            }
            return false;
        }
        if(damageTag.equalAttackType(this.attackType) || this.attackType == AttackType.Normal){
            if(this.damageAttributes.size() > 0){
                return damageTag.getDamageAttributes().containsAll(this.damageAttributes);
            }else {
                return true;
            }
        }else{
            return false;
        }
    }

    //입력된 방어관통 속성이 이 방어력 속성을 관통할 수 있는가 여부
    public boolean checkDefencePenetrateType(DamageTag damageTag){
        if(damageTag.getAttackType().equals(DamageTag.AttackType.Const)
                || damageTag.getAttackType().equals(this.attackType)){
            if(damageTag.getDamageAttributes().size() > 0){
                return this.damageAttributes.containsAll(damageTag.getDamageAttributes());
            }else {
                return true;
            }
        }else{
            return false;
        }
    }

    public AttackType getAttackType() {
        return attackType;
    }

    public EnumSet<DamageAttribute> getDamageAttributes() {
        return damageAttributes.clone();
    }

    public boolean hasDamageAttribute(DamageAttribute type){
        return damageAttributes.contains(type);
    }

    public boolean equalAttackType(AttackType attackType){
        return this.attackType == attackType;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.attackType = AttackType.valueOf(yml.getToString(root + ".속성"));
        this.damageAttributes = EnumSet.noneOf(DamageAttribute.class);
        for(String element : yml.getConfig().getStringList(root + ".타입")){
            damageAttributes.add(DamageAttribute.valueOf(element));
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
        return attackType == damageTag.attackType && Objects.equals(damageAttributes, damageTag.damageAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attackType, damageAttributes);
    }
}
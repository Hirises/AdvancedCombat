package com.hirises.combat.damage.calculate;

import com.hirises.combat.damage.manager.CombatManager;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import org.bukkit.entity.LivingEntity;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

//단일 데미지 객체
@Immutable
@ThreadSafe
public class Damage implements DataUnit, IHasDamageTagValue {
    private double damage;
    private DamageTag damageTag;    //이 데미지의 속성

    public Damage(){
        this.damage = 0;
        this.damageTag = new DamageTag();
    }

    public Damage(double damage, DamageTag damageTag){
        this.damage = damage;
        this.damageTag = damageTag;
    }

    //N배한 데미지를 반환 (원본 보존)
    public Damage multiply(double value){
        return new Damage(damage * value, damageTag);
    }

    //해당 엔티티에 적용될 최종 데미지를 반환
    public double getFinalDamage(LivingEntity entity, List<DefencePenetrate> penetrates){
        if(damageTag.equalAttackType(DamageTag.AttackType.Const) && !damageTag.hasDamageAttribute(DamageTag.DamageAttribute.Fall)){
            //고정 데미지일 경우 방어력 계산 생략
            return damage;
        }

        double defence = (CombatManager.getDefence(entity, damageTag, penetrates) + 100);   //적용될 방어력 구하기
        if(defence < 34){  //방어력 최소치 검사
            defence = 34;
        }
        return (damage * 100) / defence;    //LoL의 방어력 공식 사용
    }

    @Override
    public DamageTag getDamageTag() {
        return damageTag;
    }

    @Override
    public double getValue() {
        return damage;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.damage = yml.getToNumber(root + ".데미지");
        this.damageTag = yml.getOrDefault(new DamageTag(), root);
    }

    @Override
    public void save(YamlStore yml, String root) {
        //No Use
    }

    @Override
    public String toString() {
        return "SimpleDamage{" +
                "damage=" + damage +
                ", type=" + damageTag.getAttackType() +
                ", tags=" + damageTag.getDamageAttributes().toArray() +
                '}';
    }
}

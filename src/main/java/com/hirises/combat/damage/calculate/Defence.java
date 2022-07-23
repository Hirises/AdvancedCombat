package com.hirises.combat.damage.calculate;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

//단일 방어력 객체
@Immutable
@ThreadSafe
public class Defence implements DataUnit, IHasDamageTagValue {
    private double defence;
    private DamageTag damageTag;

    public Defence(){
        this.defence = 0;
        damageTag = new DamageTag(DamageTag.AttackType.Normal);
    }

    public Defence(double defence, DamageTag damageTag){
        this.defence = defence;
        this.damageTag = damageTag;
    }

    //해당 데미지 종류에 대한 최종적으로 적용되는 방어력을 반환
    public double getFinalDefence(DamageTag damageTag, List<DefencePenetrate> penetrates){
        if(this.damageTag.checkDefenceType(damageTag)){  //방어 가능한가?
            double output = defence;
            for(DefencePenetrate penetrate : penetrates){   //방어관통 처리
                output = penetrate.reduceDefence(output, this.damageTag);
            }
            return output;
        }
        return 0;
    }

    public double getDefence() {
        return defence;
    }

    @Override
    public DamageTag getDamageTag() {
        return damageTag;
    }

    @Override
    public double getValue() {
        return defence;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.defence = yml.getToNumber(root + ".방어");
        this.damageTag = yml.getOrDefault(new DamageTag(), root);
    }

    @Override
    public void save(YamlStore yamlStore, String s) {
        //No Use
    }

    @Override
    public String toString() {
        return "Defence{" +
                "defence=" + defence +
                ", damageTag=" + damageTag +
                '}';
    }
}

package com.hirises.combat.damage.calculate;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

//단일 방어 관통력 객체
@Immutable
@ThreadSafe
public class DefencePenetrate implements DataUnit, IHasDamageTagValue {
    private double rate;    //0.2, 0.3 같은 형식. '깍을' 수치이다. 실제 적용시 '1-rate'로 사용
    private DamageTag damageTag;

    public DefencePenetrate(){
        this.rate = 0;
        this.damageTag = new DamageTag(DamageTag.AttackType.Const);
    }

    public DefencePenetrate(double rate, DamageTag damageTag){
        this.rate = rate;
        this.damageTag = damageTag;
    }

    //입력된 방어력을 이 방어 관통력 객체에 의해 감소시켜서 반환
    public double reduceDefence(double defence, DamageTag damageTag){
        if(damageTag.checkDefencePenetrateType(this.damageTag)){    //관통할 수 있는 속성인가?
            if(defence > 0){    //방어력이 음수인가?
                //양수면 절댓값 감소
                return defence * (1 - rate);
            }else{
                //음수면 절댓값 증가
                return defence * (1 + rate);
            }
        }
        return defence;
    }

    public double getRate() {
        return rate;
    }

    @Override
    public DamageTag getDamageTag() {
        return damageTag;
    }

    @Override
    public double getValue() {
        return rate * 100;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.rate = yml.getToNumber(root + ".관통률");
        this.damageTag = yml.getOrDefault(new DamageTag(), root);
    }

    @Override
    public void save(YamlStore yamlStore, String s) {
        //No Use
    }

    @Override
    public String toString() {
        return "SimpleDefencePenetrate{" +
                "rate=" + rate +
                ", damageTag=" + damageTag.toString() +
                '}';
    }
}

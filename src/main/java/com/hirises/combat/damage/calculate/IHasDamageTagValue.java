package com.hirises.combat.damage.calculate;

//데미지 테그를 보유한 객체들
//데미지 테그 관련한 처리를 일괄적으로 처리하기 위해 묶었다
public interface IHasDamageTagValue {
    DamageTag getDamageTag();

    double getValue();
}

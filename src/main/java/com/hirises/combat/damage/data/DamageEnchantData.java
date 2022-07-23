package com.hirises.combat.damage.data;

import com.hirises.combat.damage.calculate.Damage;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//데미지를 증가시키는 형식의 인첸트 데이터 (무기, 발사체)
@Immutable
@ThreadSafe
public class DamageEnchantData implements DataUnit {
    private List<Damage> initialDamage;     //기본 데미지 증가량
    private List<Damage> increaseDamage;    //레벨당 데미지 증가량

    public DamageEnchantData(){
        initialDamage = new ArrayList<>();
        increaseDamage = new ArrayList<>();
    }

    //해당 인첸트 레벨에 대한 데미지 증가량을 반환
    public List<Damage> getEnchant(int level){
        List<Damage> result = new ArrayList<>();
        result.addAll(initialDamage);
        for(int i = 1; i < level; i++){
            result.addAll(increaseDamage);
        }
        return result;
    }

    public List<Damage> getIncreaseDamage() {
        return Collections.unmodifiableList(increaseDamage);
    }

    public List<Damage> getInitialDamage() {
        return Collections.unmodifiableList(initialDamage);
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.initialDamage = new ArrayList<>();
        for(String key : yml.getKeys(root + ".기본")){
            initialDamage.add(yml.getOrDefault(new Damage(), root + ".기본." + key));
        }
        this.increaseDamage = new ArrayList<>();
        for(String key : yml.getKeys(root + ".레벨")){
            increaseDamage.add(yml.getOrDefault(new Damage(), root + ".레벨." + key));
        }
    }

    @Override
    public void save(YamlStore yamlStore, String s) {
        //No Use
    }

    @Override
    public String toString() {
        return "DamageEnchantData{" +
                "initialDamage=" + initialDamage +
                ", increaseDamage=" + increaseDamage +
                '}';
    }
}

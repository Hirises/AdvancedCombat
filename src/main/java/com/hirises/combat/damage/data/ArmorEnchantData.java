package com.hirises.combat.damage.data;

import com.hirises.combat.damage.calculate.Defence;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//갑옷 인첸트 데이터
@Immutable
@ThreadSafe
public class ArmorEnchantData implements DataUnit {
    private List<Defence> initialDefence;   //기본 방어력 증가량
    private List<Defence> increaseDefence;  //레벨당 방어력 증가량

    public ArmorEnchantData(){
        initialDefence = new ArrayList<>();
        increaseDefence = new ArrayList<>();
    }

    public List<Defence> getIncreaseDefence() {
        return Collections.unmodifiableList(increaseDefence);
    }

    public List<Defence> getInitialDefence() {
        return Collections.unmodifiableList(initialDefence);
    }

    //해당 인첸트 레벨에 따른 방어력 증가량을 반환
    public List<Defence> getEnchant(int level){
        List<Defence> result = new ArrayList<>();
        result.addAll(initialDefence);
        for(int i = 1; i < level; i++){
            result.addAll(increaseDefence);
        }
        return result;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.initialDefence = new ArrayList<>();
        for(String key : yml.getKeys(root + ".기본")){
            initialDefence.add(yml.getOrDefault(new Defence(), root + ".기본." + key));
        }
        this.increaseDefence = new ArrayList<>();
        for(String key : yml.getKeys(root + ".레벨")){
            increaseDefence.add(yml.getOrDefault(new Defence(), root + ".레벨." + key));
        }
    }

    @Override
    public void save(YamlStore yamlStore, String s) {
        //No Use
    }
}

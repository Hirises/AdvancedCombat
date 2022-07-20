package com.hirises.combat.damage.data;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DamageEnchantData implements DataUnit {
    private List<Defence> initialDamage;
    private List<Defence> increaseDamage;

    public DamageEnchantData(){
        initialDamage = new ArrayList<>();
        increaseDamage = new ArrayList<>();
    }

    public List<Defence> getIncreaseDamage() {
        return Collections.unmodifiableList(increaseDamage);
    }

    public List<Defence> getInitialDamage() {
        return Collections.unmodifiableList(initialDamage);
    }

    public List<Defence> getEnchant(int level){
        List<Defence> result = new ArrayList<>();
        result.addAll(initialDamage);
        for(int i = 1; i < level; i++){
            result.addAll(increaseDamage);
        }
        return result;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.initialDamage = new ArrayList<>();
        for(String key : yml.getKeys(root + ".기본")){
            initialDamage.add(yml.getOrDefault(new Defence(), root + ".기본." + key));
        }
        this.increaseDamage = new ArrayList<>();
        for(String key : yml.getKeys(root + ".레벨")){
            increaseDamage.add(yml.getOrDefault(new Defence(), root + ".레벨." + key));
        }
    }

    @Override
    public void save(YamlStore yamlStore, String s) {
        //No Use
    }
}

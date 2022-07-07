package com.hirises.combat.damage.data;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

import java.util.ArrayList;
import java.util.List;

public class ArmorData implements DataUnit {
    private List<Defence> defences;
    private int weight;

    public ArmorData(){
        this.defences = new ArrayList<>();
        this.weight = 0;
    }

    public List<Defence> getDefences() {
        return defences;
    }

    public double getFinalDefence(DamageApplier damageApplier){
        return 0;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public void load(YamlStore yml, String root) {

    }

    @Override
    public void save(YamlStore yamlStore, String s) {

    }
}

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

    public ArmorData(List<Defence> defences, int weight){
        this.defences = defences;
        this.weight = weight;
    }

    public List<Defence> getDefences() {
        return defences;
    }

    public double getFinalDefence(DamageTag damageTag, List<DefencePenetrate> penetrates){
        double output = 0;
        for(Defence defence : defences){
            output += defence.getFinalDefence(damageTag, penetrates);
        }
        return output;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.weight = yml.get(Integer.class, root + ".무게");
        this.defences = new ArrayList<>();
        for(String key : yml.getKeys(root + ".방어")){
            defences.add(yml.getOrDefault(new Defence(), root + ".방어." + key));
        }
    }

    @Override
    public void save(YamlStore yamlStore, String s) {
        //No Use
    }
}

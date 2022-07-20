package com.hirises.combat.damage.data;

import com.hirises.core.store.YamlStore;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WeaponEnchantData extends DamageEnchantData {
    private List<EntityType> target;

    public WeaponEnchantData(){
        super();
        target = null;
    }

    public List<EntityType> getTarget() {
        return Collections.unmodifiableList(target);
    }

    public boolean checkTarget(EntityType type){
        if(this.target == null || this.target.size() == 0){
            return true;
        }
        return this.target.contains(type);
    }

    @Override
    public void load(YamlStore yml, String root) {
        super.load(yml, root);
        if(yml.containKey(root + ".대상")){
            this.target = yml.getConfig().getStringList(root + ".대상").stream().map(value -> EntityType.valueOf(value)).collect(Collectors.toList());
        }
    }

    @Override
    public void save(YamlStore yamlStore, String s) {
        //No Use
    }
}

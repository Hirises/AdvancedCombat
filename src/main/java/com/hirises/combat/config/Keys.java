package com.hirises.combat.config;

public enum Keys {
    Item_Checked("combat_item_checked"),
    Current_Health("combat_current_health"),
    Attribute_Modifier("combat_attribute"),
    Projectile_Damage("combat_projectile_damage"),
    DamageMeter("combat_damagerMeter"),
    MaterialCoolDown("combat_matCool."),
    MaterialCoolDown_NoDot("combat_matCool"), //MaterialCoolDown의 점 없는 버전
    ;

    private String key;

    Keys(String key){
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}

package com.hirises.combat.config;

public enum Keys {
    Item_Checked("combat_item_checked"),
    Current_Health("combat_current_health"),
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

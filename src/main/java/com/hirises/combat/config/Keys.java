package com.hirises.combat.config;

//플러그인 내부에서 사용하는 String 키들의 통일성을 위한 Enum 객체
public enum Keys {
    Item_Checked("combat_item_checked"),    //변경, 제거 여부가 확인된 아이템
    Current_Health("combat_current_health"),    //엔티티 현재 체력
    Attribute_Modifier("combat_attribute"),     //아이템에 적용된 Attribute Modifier의 이름
    Projectile_Damage("combat_projectile_damage"),  //발사체 데미지 객체
    DamageMeter("combat_damagerMeter"),             //데미지 미터임을 나타내는 NBT tag
    MaterialCoolDown("combat_matCool."),            //대상 Material의 쿨다운 (Util.getCurrentTick()의 반환값 사용)
    MaterialCoolDown_NoDot("combat_matCool"), //MaterialCoolDown의 점 없는 버전
    Armor_Data("conbat_armorData"),             //갑옷 데이터가 저장된 키
    Weapon_Data("conbat_weaponData"),           //무기 데이터가 저장된 키
    Food_Data("conbat_foodData"),               //음식 데이터가 저장된 키
    Projectile_Data("conbat_projectileData"),   //발사체 데이터가 저장된 키
    Potion_Effect_Applied("combat_potionEffectApplied"),    //커스텀 이펙트가 적용된 AreaEffectCloud
    Potion_Live("combat_potionLive"),           //AreaEffectCloud에 이 Tag가 없으면 플러그인에 의해 삭제된다.
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

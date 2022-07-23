package com.hirises.combat.config;

import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.damage.calculate.DamageApplier;
import com.hirises.combat.damage.calculate.DamageTag;
import com.hirises.combat.damage.calculate.IHasDamageTagValue;
import com.hirises.combat.damage.data.*;
import com.hirises.combat.item.CustomItemManager;
import com.hirises.core.data.TimeUnit;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigManager {
    public final static YamlStore config = new YamlStore(AdvancedCombat.getInst(), "config.yml");
    public static YamlStore settings = new YamlStore(AdvancedCombat.getInst(), "settings.yml");

    //<editor-fold desc="Record 구조체 데이터">
    public static boolean useDamageMeter = false;
    //아머스텐드 데미지 미터
    public record DamageMeterData(
      String format,
      int duration,

      String normalColor,
      String physicsColor,
      String magicColor,
      String constColor,
      String healColor,

      String fireSymbol,
      String projectileSymbol,
      String explosionSymbol,
      String fallSymbol
    )
    {
        public String getHealMeterString(double heal){
            return Util.remapStrings(format, Util.toRemap(
                    "color",  healColor,
                    "symbol", "",
                    "damage", Util.safeToString((int)Math.floor(heal))
            ));
        }

        public String getDamageMeterString(DamageTag tag, double damage){
            String color = "";
            String symbol = "";

            switch (tag.getAttackType()){
                case Normal -> color = normalColor;
                case Physics -> color = physicsColor;
                case Magic -> color = magicColor;
                case Const -> color = constColor;
            }

            for(DamageTag.DamageAttribute type : tag.getDamageAttributes()){
                switch (type){
                    case Fire -> symbol += fireSymbol;
                    case Projectile -> symbol += projectileSymbol;
                    case Explosion -> symbol += explosionSymbol;
                    case Fall -> symbol += fallSymbol;
                }
            }

            return Util.remapStrings(format, Util.toRemap(
                    "color",  color,
                    "symbol", symbol,
                    "damage", Util.safeToString((int)Math.floor(damage))
            ));
        }
    }
    //무게 관련 데이터
    public record WeightData(
            int normalSpeedRate,
            int maxWeight,
            double speedRatePerWeight,
            double fallDamageRatePerWeight
    )
    {
        public double getSpeedRate(int weight){
            if(weight > maxWeight){
                weight = maxWeight;
            }
            return normalSpeedRate - (speedRatePerWeight * weight);
        }

        public double getFallDamageRate(int weight){
            if(weight > maxWeight){
                weight = maxWeight;
            }
            return 1 + fallDamageRatePerWeight * weight;
        }
    }
    public static WeightData weightData;
    //아이템 로어 데이터
    public static boolean useItemLore = false;
    public record ItemLoreData(
            List<String> loreFormat,

            String attackDamageLine,
            String penetrateLine,
            String projectileDamageLine,
            String defenceLine,
            String foodLine,
            String attributeLine,

            String attackSpeedFormat,
            String attackDistanceFormat,
            String weightFormat,

            String maxConsumeAmountFormat,
            String hungerFormat,
            String healFormat,
            String graduallyHealFormat,
            String healDurationFormat,
            String coolDownFormat,

            String damagePropertiesFormat,
            String fireTag,
            String projectileTag,
            String explosionTag,
            String fallTag,
            String tagSeparator,
            String tagPrefix,
            String tagSuffix,

            String normalFormat,
            String physicsFormat,
            String magicFormat,
            String constFormat,
            String attackTypeSeparator
    )
    {
        public List<String> getItemLore(ItemStack item){
            int weight = 0;
            double attackSpeed = -1;
            double attackDistance = -1;
            StringBuilder builder = new StringBuilder();
            if(hasArmorData(item)){
                ArmorData data = getArmorData(item);
                builder.append(defenceLine);
                builder.append("/n");
                builder.append(getIHasDamageTagValueLore(data.getDefences()));
                builder.append("/n");

                weight += data.getWeight();
            }
            if(hasWeaponData(item) && !item.getType().equals(Material.SHIELD)){
                WeaponData data = getWeaponData(item);
                builder.append(attackDamageLine);
                builder.append("/n");
                builder.append(getIHasDamageTagValueLore(data.getDamage().getDamages()));
                if(data.getDamage().getPenetrates().size() > 0){
                    builder.append(penetrateLine);
                    builder.append("/n");
                    builder.append(getIHasDamageTagValueLore(data.getDamage().getPenetrates()));
                    builder.append("/n");
                }

                weight += data.getWeight();
                attackSpeed = data.getAttackSpeed();
                attackDistance = data.getAttackDistance();
            }
            if(hasProjectileData(item)){
                ProjectileData data = getProjectileData(item);
                builder.append(projectileDamageLine);
                builder.append("/n");
                builder.append(getIHasDamageTagValueLore(data.getDamage().getDamages()));
                if(data.getDamage().getPenetrates().size() > 0){
                    builder.append(penetrateLine);
                    builder.append("/n");
                    builder.append(getIHasDamageTagValueLore(data.getDamage().getPenetrates()));
                    builder.append("/n");
                }
            }
            if(hasFoodData(item)){
                builder.append(foodLine);
                builder.append("/n");
                FoodData data = getFoodData(item);
                if(data.getMaxConsumeAmount() > 1){
                    builder.append(Util.remapString(maxConsumeAmountFormat, "value", Util.safeToString(data.getMaxConsumeAmount())));
                    builder.append("/n");
                }
                if(data.getMaxConsumeAmount() > 1){
                    builder.append(Util.remapString(hungerFormat, "value", String.format("%.2f", data.getInstantHunger())));
                    builder.append("/n");
                }
                if(data.getInstantHeal() > 0){
                    builder.append(Util.remapString(healFormat, "value", String.format("%.2f", data.getInstantHeal())));
                    builder.append("/n");
                }
                if(data.getHealPerSecond() > 0){
                    builder.append(Util.remapString(graduallyHealFormat, "value", String.format("%.2f", data.getHealPerSecond())));
                    builder.append("/n");
                    builder.append(Util.remapString(healDurationFormat, "value", String.format("%.1f초", new TimeUnit(data.getHealDuration()).getToSecond())));
                    builder.append("/n");
                }
                if(data.getCoolDown() > 0){
                    builder.append(Util.remapString(coolDownFormat, "value", String.format("%.1f초", new TimeUnit(data.getCoolDown()).getToSecond())));
                    builder.append("/n");
                }
            }
            if(weight > 0 || attackSpeed > 0 || attackDistance > 0){
                builder.append(attributeLine);
                builder.append("/n");
                if(attackSpeed > 0){
                    builder.append(Util.remapString(attackSpeedFormat, "value", String.format("%.2f", attackSpeed)));
                    builder.append("/n");
                }
                if(attackDistance > 0){
                    builder.append(Util.remapString(attackDistanceFormat, "value", String.format("%.2f", attackDistance)));
                    builder.append("/n");
                }
                builder.append(Util.remapString(weightFormat, "value", Util.safeToString(weight)));
                builder.append("/n");
            }
            List<String> output = Util.trimAllLine(Util.stringToList(builder.toString()));
            output.add(0, "");
            return output;
        }

        public String getIHasDamageTagValueLore(List<? extends IHasDamageTagValue> values){
            Map<Integer, List<IHasDamageTagValue>> tagMap = new HashMap<>();
            StringBuilder builder = new StringBuilder();
            for(IHasDamageTagValue data : values) {
                tagMap.compute(data.getDamageTag().getDamageAttributeFlag(), (key, value) -> {
                    if (value == null) {
                        value = new ArrayList<>();
                    }
                    value.add(data);
                    return value;
                });
            }
            for(int key : tagMap.keySet().stream().sorted(Integer::compareTo).collect(Collectors.toList())){
                EnumSet<DamageTag.DamageAttribute> tags = DamageTag.getDamageAttributeFromFlag(key);
                StringBuilder innerBuilder = new StringBuilder();
                if(tags.size() > 0){
                    innerBuilder.append(tags.stream().map((value) -> {
                        switch (value){
                            case Explosion -> {
                                return explosionTag;
                            }
                            case Fire -> {
                                return fireTag;
                            }
                            case Projectile -> {
                                return projectileTag;
                            }
                            case Fall -> {
                                return fallTag;
                            }
                            default -> {
                                return "";
                            }
                        }
                    }).collect(Collectors.joining(tagSeparator, tagPrefix, tagSuffix)));
                }
                List<String> valueForType = new ArrayList<>();
                for(DamageTag.AttackType attackType : DamageTag.AttackType.values()){
                    double value = 0;
                    for(IHasDamageTagValue data : tagMap.get(key)){
                        if(data.getDamageTag().equalAttackType(attackType)){
                            value += data.getValue();
                        }
                    }
                    if(value != 0){
                        switch (attackType){
                            case Normal -> valueForType.add(Util.remapString(normalFormat, "value", String.format("%d", (int)Math.floor(value))));
                            case Physics -> valueForType.add(Util.remapString(physicsFormat, "value", String.format("%d", (int)Math.floor(value))));
                            case Magic -> valueForType.add(Util.remapString(magicFormat, "value", String.format("%d", (int)Math.floor(value))));
                            case Const -> valueForType.add(Util.remapString(constFormat, "value", String.format("%d", (int)Math.floor(value))));
                        }
                    }
                }
                innerBuilder.append(valueForType.stream().collect(Collectors.joining(attackTypeSeparator)));
                builder.append(innerBuilder);
                builder.append("/n");
            }

            return builder.toString();
        }
    }
    public static ItemLoreData itemLoreData;
    //</editor-fold>
    //<editor-fold desc="데이터 맵">
    public static FoodData undyingTotem;
    public static DamageMeterData damageMeterData;
    public static WeaponData bearHand;
    public static ProjectileData normalArrow;
    public static ArmorData shield;
    public static Map<Material, WeaponData> weaponDataMap;
    public static Map<Material, ArmorData> armorDataMap;
    public static Map<Material, ProjectileData> projectileDataMap;
    public static Map<Material, FoodData> foodDataMap;
    public static Map<EntityType, DamageApplier> entityDataMap;
    public static Map<Enchantment, ArmorEnchantData> armorEnchantDataMap;
    public static Map<Enchantment, DamageEnchantData> projectileEnchantDataMap;
    public static Map<Enchantment, DamageEnchantData> weaponEnchantDataMap;
    //</editor-fold>
    public static double playerDamageRate;
    public static double etcDamageRate;
    public static int foodDelay;

    //<editor-fold desc="초기화">
    public static void init() {
        config.load(false);
        settings.load(false);

        //<editor-fold desc="데미지 미터기 초기화">
        useDamageMeter = config.get(Boolean.class, "데미지미터기.사용");
        if (useDamageMeter) {
            damageMeterData = new DamageMeterData(
                    config.getToString("데미지미터기.형태"),
                    (int) config.getOrDefault(new TimeUnit(), "데미지미터기.지속시간").getToTick(),

                    Util.remapColor(config.getToString("데미지미터기.색상.일반")),
                    Util.remapColor(config.getToString("데미지미터기.색상.물리")),
                    Util.remapColor(config.getToString("데미지미터기.색상.마법")),
                    Util.remapColor(config.getToString("데미지미터기.색상.고정")),
                    Util.remapColor(config.getToString("데미지미터기.색상.회복")),

                    Util.remapColor(config.getToString("데미지미터기.심볼.화염")),
                    Util.remapColor(config.getToString("데미지미터기.심볼.원거리")),
                    Util.remapColor(config.getToString("데미지미터기.심볼.폭발")),
                    Util.remapColor(config.getToString("데미지미터기.심볼.낙하"))
            );
        }
        //</editor-fold>

        //<editor-fold desc="아이템 로어 초기화">
        useItemLore = config.get(Boolean.class, "아이템로어.사용");
        if (useItemLore) {
            itemLoreData = new ItemLoreData(
                    Util.remapColor(config.getConfig().getStringList("아이템로어.형태.전체")),

                    Util.remapColor(config.getToString("아이템로어.형태.구분선.공격력")),
                    Util.remapColor(config.getToString("아이템로어.형태.구분선.방어관통")),
                    Util.remapColor(config.getToString("아이템로어.형태.구분선.발사체")),
                    Util.remapColor(config.getToString("아이템로어.형태.구분선.방어력")),
                    Util.remapColor(config.getToString("아이템로어.형태.구분선.회복")),
                    Util.remapColor(config.getToString("아이템로어.형태.구분선.속성")),

                    Util.remapColor(config.getToString("아이템로어.형태.속성.공격속도")),
                    Util.remapColor(config.getToString("아이템로어.형태.속성.공격거리")),
                    Util.remapColor(config.getToString("아이템로어.형태.속성.무게")),

                    Util.remapColor(config.getToString("아이템로어.형태.회복.동시섭취개수")),
                    Util.remapColor(config.getToString("아이템로어.형태.회복.허기")),
                    Util.remapColor(config.getToString("아이템로어.형태.회복.회복")),
                    Util.remapColor(config.getToString("아이템로어.형태.회복.초당회복")),
                    Util.remapColor(config.getToString("아이템로어.형태.회복.지속시간")),
                    Util.remapColor(config.getToString("아이템로어.형태.회복.쿨타임")),

                    Util.remapColor(config.getToString("아이템로어.형태.데미지속성.전체")),
                    Util.remapColor(config.getToString("아이템로어.형태.데미지속성.태그.화염")),
                    Util.remapColor(config.getToString("아이템로어.형태.데미지속성.태그.발사체")),
                    Util.remapColor(config.getToString("아이템로어.형태.데미지속성.태그.폭발")),
                    Util.remapColor(config.getToString("아이템로어.형태.데미지속성.태그.낙하")),
                    Util.remapColor(config.getToString("아이템로어.형태.데미지속성.태그.구분")),
                    Util.remapColor(config.getToString("아이템로어.형태.데미지속성.태그.접두사")),
                    Util.remapColor(config.getToString("아이템로어.형태.데미지속성.태그.접미사")),

                    Util.remapColor(config.getToString("아이템로어.형태.데미지속성.데미지.일반")),
                    Util.remapColor(config.getToString("아이템로어.형태.데미지속성.데미지.물리")),
                    Util.remapColor(config.getToString("아이템로어.형태.데미지속성.데미지.마법")),
                    Util.remapColor(config.getToString("아이템로어.형태.데미지속성.데미지.고정")),
                    Util.remapColor(config.getToString("아이템로어.형태.데미지속성.데미지.구분"))
            );
        }
        //</editor-fold>

        //<editor-fold desc="무게 데이터 초기화">
        weightData = new WeightData(
                settings.get(Integer.class, "무게.기본이속"),
                settings.get(Integer.class, "무게.최대무게"),
                settings.getToNumber("무게.무게당이속감소"),
                settings.getToNumber("무게.무게당낙뎀증가율")
        );
        //</editor-fold>

        //데이터 맵 초기화
        weaponDataMap = new HashMap<>();
        armorDataMap = new HashMap<>();
        projectileDataMap = new HashMap<>();
        foodDataMap = new HashMap<>();
        entityDataMap = new HashMap<>();
        armorEnchantDataMap = new HashMap<>();
        projectileEnchantDataMap = new HashMap<>();
        weaponEnchantDataMap = new HashMap<>();

        //기본 데이터
        bearHand = settings.getOrDefault(new WeaponData(), "무기.맨손");
        normalArrow = settings.getOrDefault(new ProjectileData(), "발사체.기본");

        //데이터 맵 Initialize

        for (String key : settings.getKeys("무기")) {
            if (key.equalsIgnoreCase("맨손")) {
                continue;
            }

            Material mat = Material.valueOf(key);
            WeaponData data = settings.getOrDefault(new WeaponData(), "무기." + key);
            weaponDataMap.put(mat, data);
        }

        for (String key : settings.getKeys("갑옷")) {
            Material mat = Material.valueOf(key);
            ArmorData data = settings.getOrDefault(new ArmorData(), "갑옷." + key);
            armorDataMap.put(mat, data);
        }

        for (String key : settings.getKeys("발사체")) {
            if(key.equals("기본")){
                continue;
            }

            Material mat = Material.valueOf(key);
            ProjectileData data = settings.getOrDefault(new ProjectileData(), "발사체." + key);
            projectileDataMap.put(mat, data);
        }

        for (String key : settings.getKeys("음식")) {
            if(key.equals("설정")){
                foodDelay = settings.get(Integer.class, "음식.설정.회복틱");
                continue;
            }

            Material mat = Material.valueOf(key);
            FoodData data = settings.getOrDefault(new FoodData(), "음식." + key);
            foodDataMap.put(mat, data);

            if(useItemLore){
                ItemStack item = CustomItemManager.hasRegisteredItemReplacement(mat) ? CustomItemManager.getRegisteredItemReplacement(mat) : new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                meta.setLore(ConfigManager.itemLoreData.getItemLore(item));
                item.setItemMeta(meta);

                CustomItemManager.registerItemReplacement(mat, item);
            }
        }

        for (String key : settings.getKeys("몬스터")) {
            EntityType type = EntityType.valueOf(key);
            DamageApplier data = settings.getOrDefault(new DamageApplier(), "몬스터." + key);
            entityDataMap.put(type, data);
        }

        for (String key : settings.getKeys("갑옷인첸트")) {
            Enchantment type = Enchantment.getByKey(new NamespacedKey("minecraft", key));
            ArmorEnchantData data = settings.getOrDefault(new ArmorEnchantData(), "갑옷인첸트." + key);
            armorEnchantDataMap.put(type, data);
        }

        for (String key : settings.getKeys("무기인첸트")) {
            Enchantment type = Enchantment.getByKey(new NamespacedKey("minecraft", key));
            DamageEnchantData data = settings.getOrDefault(new DamageEnchantData(), "무기인첸트." + key);
            weaponEnchantDataMap.put(type, data);
        }

        for (String key : settings.getKeys("발사체인첸트")) {
            Enchantment type = Enchantment.getByKey(new NamespacedKey("minecraft", key));
            DamageEnchantData data = settings.getOrDefault(new DamageEnchantData(), "발사체인첸트." + key);
            projectileEnchantDataMap.put(type, data);
        }

        //기타 데이터들

        shield = settings.getOrDefault(new ArmorData(), "기타.방패");
        armorDataMap.put(Material.SHIELD, shield);
        ItemStack item = CustomItemManager.hasRegisteredItemReplacement(Material.SHIELD) ?
                CustomItemManager.getRegisteredItemReplacement(Material.SHIELD) : new ItemStack(Material.SHIELD);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setLore(ConfigManager.itemLoreData.getItemLore(item));
        item.setItemMeta(meta);
        CustomItemManager.registerItemReplacement(Material.SHIELD, item);
        WeaponData data = new WeaponData(bearHand.getAttackDistance(), shield.getWeight(), bearHand.getAttackDistance(), new DamageApplier(bearHand.getDamage()));
        weaponDataMap.put(Material.SHIELD, data);

        undyingTotem = settings.getOrDefault(new FoodData(), "기타.TOTEM_OF_UNDYING");
        foodDataMap.put(Material.TOTEM_OF_UNDYING, undyingTotem);
        item = CustomItemManager.hasRegisteredItemReplacement(Material.TOTEM_OF_UNDYING) ?
                CustomItemManager.getRegisteredItemReplacement(Material.TOTEM_OF_UNDYING) : new ItemStack(Material.TOTEM_OF_UNDYING);
        meta = item.getItemMeta();
        meta.setLore(ConfigManager.itemLoreData.getItemLore(item));
        item.setItemMeta(meta);
        CustomItemManager.registerItemReplacement(Material.TOTEM_OF_UNDYING, item);

        playerDamageRate = settings.getToNumber("기타.데미지보정.플레이어");
        etcDamageRate = settings.getToNumber("기타.데미지보정.기타");
    }
    //</editor-fold>

    //<editor-fold desc="무기 데이터">
    public static boolean hasWeaponData(ItemStack weapon){
        return weaponDataMap.containsKey(weapon.getType());
    }

    public static WeaponData getWeaponData(LivingEntity entity){
        ItemStack weapon = entity.getEquipment().getItemInMainHand();
        if(!ItemUtil.isExist(weapon)){
            return bearHand;
        }
        return getWeaponData(weapon);
    }

    public static WeaponData getWeaponData(ItemStack weapon){
        if(NBTTagStore.containKey(weapon, Keys.Weapon_Data.toString())){
            return NBTTagStore.get(weapon, Keys.Weapon_Data.toString(), WeaponData.class);
        }
        return getNewWeaponData(weapon);
    }

    //무기 데이터 가져오기 (재계산)
    public static WeaponData getNewWeaponData(ItemStack weapon){
        WeaponData output = weaponDataMap.get(weapon.getType());
        if(output == null){
            return bearHand;
        }
        Map<Enchantment, Integer> enchants = weapon.getItemMeta().getEnchants();
        if(enchants != null && enchants.size() > 0){
            for(Enchantment enchant : enchants.keySet()){
                if(hasWeaponEnchantData(enchant)){
                    DamageEnchantData data = getWeaponEnchantData(enchant);
                    output = output.merge(data.getEnchant(enchants.get(enchant)));
                }
            }
        }
        return output;
    }

    public static boolean hasWeaponEnchantData(Enchantment enchantment){
        return weaponEnchantDataMap.containsKey(enchantment);
    }

    public static DamageEnchantData getWeaponEnchantData(Enchantment enchantment){
        if(hasWeaponEnchantData(enchantment)){
            return weaponEnchantDataMap.get(enchantment);
        }
        return new DamageEnchantData();
    }
    //</editor-fold>

    //<editor-fold desc="갑옷 데이터">
    public static boolean hasArmorData(ItemStack armor){
        return armorDataMap.containsKey(armor.getType());
    }

    public static ArmorData getArmorData(ItemStack armor){
        if(NBTTagStore.containKey(armor, Keys.Armor_Data.toString())){
            return NBTTagStore.get(armor, Keys.Armor_Data.toString(), ArmorData.class);
        }
        return getNewArmorData(armor);
    }

    //갑옷 데이터 가져오기 (재계산)
    public static ArmorData getNewArmorData(ItemStack armor){
        ArmorData output = armorDataMap.get(armor.getType());
        if(output == null){
            return new ArmorData();
        }
        Map<Enchantment, Integer> enchants = armor.getItemMeta().getEnchants();
        if(enchants != null && enchants.size() > 0){
            for(Enchantment enchant : enchants.keySet()){
                if(hasArmorEnchantData(enchant)){
                    ArmorEnchantData data = getArmorEnchantData(enchant);
                    output = output.merge(data.getEnchant(enchants.get(enchant)));
                }
            }
        }
        return output;
    }

    public static boolean hasArmorEnchantData(Enchantment enchantment){
        return armorEnchantDataMap.containsKey(enchantment);
    }

    public static ArmorEnchantData getArmorEnchantData(Enchantment enchantment){
        if(hasArmorEnchantData(enchantment)){
            return armorEnchantDataMap.get(enchantment);
        }
        return new ArmorEnchantData();
    }
    //</editor-fold>

    //<editor-fold desc="발사체 데이터">
    public static boolean hasProjectileData(LivingEntity entity){
        ItemStack weapon = entity.getEquipment().getItemInMainHand();
        if(!ItemUtil.isExist(weapon) || !hasProjectileData(weapon)){
            weapon = entity.getEquipment().getItemInOffHand();
            return hasProjectileData(weapon);
        }
        return hasProjectileData(weapon);
    }

    public static boolean hasProjectileData(ItemStack armor){
        return projectileDataMap.containsKey(armor.getType());
    }

    public static ProjectileData getProjectileData(LivingEntity entity){
        ItemStack weapon = entity.getEquipment().getItemInMainHand();
        if(!ItemUtil.isExist(weapon) || !hasProjectileData(weapon)){
            weapon = entity.getEquipment().getItemInOffHand();
            return getProjectileData(weapon);
        }
        return getProjectileData(weapon);
    }

    public static ProjectileData getProjectileData(ItemStack weapon){
        if(NBTTagStore.containKey(weapon, Keys.Projectile_Data.toString())){
            return NBTTagStore.get(weapon, Keys.Projectile_Data.toString(), ProjectileData.class);
        }
        return getNewProjectileData(weapon);
    }

    //발사체 데이터 가져오기 (재계산)
    public static ProjectileData getNewProjectileData(ItemStack weapon){
        ProjectileData output = projectileDataMap.get(weapon.getType());
        if(output == null){
            return normalArrow;
        }
        Map<Enchantment, Integer> enchants = weapon.getItemMeta().getEnchants();
        if(enchants != null && enchants.size() > 0){
            for(Enchantment enchant : enchants.keySet()){
                if(hasProjectileEnchantData(enchant)){
                    DamageEnchantData data = getProjectileEnchantData(enchant);
                    output = output.merge(data.getEnchant(enchants.get(enchant)));
                }
            }
        }
        return output;
    }

    public static boolean hasProjectileEnchantData(Enchantment enchantment){
        return projectileEnchantDataMap.containsKey(enchantment);
    }

    public static DamageEnchantData getProjectileEnchantData(Enchantment enchantment){
        if(hasProjectileEnchantData(enchantment)){
            return projectileEnchantDataMap.get(enchantment);
        }
        return new DamageEnchantData();
    }
    //</editor-fold>

    public static boolean hasFoodData(ItemStack item){
        return foodDataMap.containsKey(item.getType());
    }

    public static FoodData getFoodData(ItemStack item){
        FoodData output = foodDataMap.get(item.getType());
        if(output == null){
            return new FoodData();
        }
        return output;
    }

    public static boolean hasEntityData(EntityType type){
        return entityDataMap.containsKey(type);
    }

    public static DamageApplier getEntityData(EntityType type){
        DamageApplier output = entityDataMap.get(type);
        if(output == null){
            return new DamageApplier();
        }
        return output;
    }
}

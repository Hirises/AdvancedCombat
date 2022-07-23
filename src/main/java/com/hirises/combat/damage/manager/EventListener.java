package com.hirises.combat.damage.manager;

import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.config.Keys;
import com.hirises.combat.damage.calculate.DamageApplier;
import com.hirises.combat.damage.calculate.DamageTag;
import com.hirises.combat.damage.calculate.PartialDamageApplier;
import com.hirises.combat.damage.data.*;
import com.hirises.combat.item.ItemChangeEvent;
import com.hirises.combat.item.ItemRegisteredEvent;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.task.CancelableTask;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

//전투 관련 이벤트 처리
public class EventListener implements Listener {

    //<editor-fold desc="플레이어 기본 설정 적용">
    @EventHandler
    public void onUndying(EntityResurrectEvent event){
        //불사의 토템 무시 (내부적으로 따로 처리함)
        event.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        //공속 적용
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(ConfigManager.bearHand.getAttackSpeed());
        //기본 설정 처리
        assertPlayerSettings(player);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();

        //HP 회복
        NBTTagStore.set(player, Keys.Current_Health.toString(), player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * CombatManager.DAMAGE_MODIFIER);
        CombatManager.applyHealth(player);

        //기본 설정 처리
        Bukkit.getScheduler().runTaskLater(AdvancedCombat.getInst(), () -> {
            assertPlayerSettings(player);
        }, 1);
    }

    //플레이어 기본 설정 확인
    private void assertPlayerSettings(Player player) {
        //이속 (무게) 적용
        double speedRate = ConfigManager.weightData.getSpeedRate(CombatManager.getWeight(player));
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedRate / 1000.0);

        //허기 상한치 적용 (언제든 음식을 먹을 수 있도록)
        if(player.getFoodLevel() > 19){
            player.setFoodLevel(19);
        }

        //쿨타임 다시 적용
        for(String key : NBTTagStore.getKeys(player, Keys.MaterialCoolDown_NoDot.toString())){
            Material mat = Material.valueOf(key);
            int restTick = (int) (NBTTagStore.get(player, Keys.MaterialCoolDown + key, Long.class) - Util.getCurrentTick());
            if(restTick > 0){
                player.setCooldown(mat, restTick);
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="무게 검사">
    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        EntityEquipment equipment = player.getEquipment();
        double speedRate = ConfigManager.weightData.getSpeedRate(
                CombatManager
                        .getWeight(player.getInventory().getItem(event.getNewSlot()), player.getInventory().getItemInOffHand(),
                                equipment.getHelmet(), equipment.getChestplate(),
                                equipment.getLeggings(), equipment.getBoots())
        );
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedRate / 1000.0);
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent event){     //아이템 스왑
        Player player = event.getPlayer();
        applyWeight(player);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event){      //아이템 버리기
        Player player = event.getPlayer();
        applyWeight(player);
    }

    @EventHandler
    public void onItemPickUp(EntityPickupItemEvent event){      //아이템 줍기
        if(event.getEntity() instanceof Player){
            Player player = (Player) event.getEntity();
            applyWeight(player);
        }
    }

    @EventHandler
    public void armorCheck(InventoryCloseEvent event){      //인벤에서 갑옷 장착
        Player player = (Player) event.getPlayer();
        double speedRate = ConfigManager.weightData.getSpeedRate(CombatManager.getWeight(player));
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedRate / 1000.0);
    }

    @EventHandler
    public void armorCheck(PlayerInteractEvent event){      //우클릭 갑옷 장착
        Player player = event.getPlayer();
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(ItemUtil.isExist(event.getItem()) && ConfigManager.hasArmorData(event.getItem())){
                applyWeight(player);
            }
        }
    }

    //무게 적용
    private void applyWeight(Player player) {
        Bukkit.getScheduler().runTaskLater(AdvancedCombat.getInst(), () -> {
            double speedRate = ConfigManager.weightData.getSpeedRate(CombatManager.getWeight(player));
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speedRate / 1000.0);
        }, 1);
    }
    //</editor-fold>

    //<editor-fold desc="아이템 등록 & 변경">
    @EventHandler
    public void onItemRegister(ItemRegisteredEvent event){
        //새로운 아이템 등록
        ItemStack target = event.getItemStack();
        if(!ItemUtil.isExist(target)){
            return;
        }
        ItemStack output = target.clone();

        boolean flag = false;
        if(ConfigManager.hasArmorData(target)){ //갑옷 검사
            NBTTagStore.set(output, Keys.Armor_Data.toString(), ConfigManager.getNewArmorData(target));
            flag = true;
        }
        if(ConfigManager.hasProjectileData(target)){    //발사체 검사
            NBTTagStore.set(output, Keys.Projectile_Data.toString(), ConfigManager.getNewProjectileData(target));
            flag = true;
        }
        if(ConfigManager.hasWeaponData(target)){    //무기 검사
            NBTTagStore.set(output, Keys.Weapon_Data.toString(), ConfigManager.getNewWeaponData(target));
            flag = true;

            //공속 처리
            ItemMeta meta = output.getItemMeta();
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                    new AttributeModifier(
                            UUID.randomUUID(),
                            Keys.Attribute_Modifier.toString(),
                            ConfigManager.getNewWeaponData(target).getAttackSpeed() - ConfigManager.bearHand.getAttackSpeed(),
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlot.HAND
                    )
            );
            //공격력 처리 (나약 포션 맞아도 때려지도록)
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,
                    new AttributeModifier(
                            UUID.randomUUID(),
                            Keys.Attribute_Modifier.toString(),
                            100,
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlot.HAND
                    )
            );
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            output.setItemMeta(meta);
        }

        if(ConfigManager.useItemLore && flag){
            //아이템 로어 설정
            ItemMeta meta = output.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setLore(ConfigManager.itemLoreData.getItemLore(output));
            output.setItemMeta(meta);
        }
        event.setItemStack(output);
    }

    @EventHandler
    public void onItemChange(ItemChangeEvent event){
        //아이템 변경 (이름 수정, 인첸트, 수리)
        ItemStack target = event.getItemStack();
        if(!ItemUtil.isExist(target)){
            return;
        }

        boolean flag = false;
        if(ConfigManager.hasArmorData(target)){     //갑옷 검사
            NBTTagStore.set(target, Keys.Armor_Data.toString(), ConfigManager.getNewArmorData(target));
            flag = true;
        }
        if(ConfigManager.hasProjectileData(target)){    //발사체 검사
            NBTTagStore.set(target, Keys.Projectile_Data.toString(), ConfigManager.getNewProjectileData(target));
            flag = true;
        }
        if(ConfigManager.hasWeaponData(target)){    //무기 검사
            NBTTagStore.set(target, Keys.Weapon_Data.toString(), ConfigManager.getNewWeaponData(target));
            flag = true;
        }

        if(ConfigManager.useItemLore && flag){
            //아이템 로어 적용
            ItemMeta meta = target.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setLore(ConfigManager.itemLoreData.getItemLore(target));
            target.setItemMeta(meta);
        }
    }
    //</editor-fold>

    //<editor-fold desc="음식 & 회복 처리">
    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent event){
        Player player = (Player) event.getEntity();

        //허기 상한치 설정 (언제든지 음식을 먹을 수 있도록)
        if(event.getFoodLevel() > 19){
            event.setCancelled(true);
            player.setFoodLevel(19);
        }
    }

    @EventHandler
    public void onEntityHeal(EntityRegainHealthEvent event){
        if(event.getEntity() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity) event.getEntity();
            switch (event.getRegainReason()){
                case EATING:{
                    //음식 회복 삭제 (내부적으로 따로 처리함)
                    event.setAmount(0);
                    return;
                }
                default: {
                    break;
                }
            }

            CombatManager.heal(entity, event.getAmount() * CombatManager.DAMAGE_MODIFIER);
            event.setAmount(0);
        }
    }

    @EventHandler
    public void onEatFood(PlayerItemConsumeEvent event){
        ItemStack food = event.getItem();
        Player player = event.getPlayer();

        if(ItemUtil.isExist(food) && ConfigManager.hasFoodData(food)){  //음식 데이터가 존재하면
            event.setCancelled(true);
            FoodData data = ConfigManager.getFoodData(food);

            //대상 음식 아이템 가져오기
            ItemStack item;
            if(player.getInventory().getItemInMainHand().getType().equals(food.getType())){
                item = player.getInventory().getItemInMainHand();
            }else{
                item = player.getInventory().getItemInOffHand();
            }

            if(item.getAmount() > data.getMaxConsumeAmount()){  //최대 섭취량보다 많으면
                //최대 섭취량만큼만 적용
                data.eat(player, data.getMaxConsumeAmount());

                if(player.getGameMode() != GameMode.CREATIVE){
                    //아이템 제거
                    ItemUtil.operateAmount(item, -1 * data.getMaxConsumeAmount());
                }
            }else{  //최대 섭취량보다 적으면
                //남은거 전부 먹기
                data.eat(player, item.getAmount());

                if(player.getGameMode() != GameMode.CREATIVE) {
                    //아이템 제거
                    if (player.getInventory().getItemInMainHand().equals(food)) {
                        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    } else {
                        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                    }
                }
            }
        }else if(food.getType().equals(Material.POTION)){   //포션이라면
            //취소 (마크 기본 동작)
            return;
        }else{  //등록되지 않은 음식이라면
            //취소 (섭취 불가)
            event.setCancelled(true);
            return;
        }
    }
    //</editor-fold>

    //<editor-fold desc="발사체 처리">
    @EventHandler
    public void onShootBow(EntityShootBowEvent event){  //화살 발사시
        Projectile projectile = (Projectile) event.getProjectile();
        if(!(event.getEntity() instanceof Player)){
            return;
        }
        Player player = (Player) event.getEntity();

        //차징 정도에 따른 화살 데미지 설정
        ProjectileData data = ConfigManager.getProjectileData(player);
        NBTTagStore.set(projectile, Keys.Projectile_Damage.toString(), data.getDamage().multiply(event.getForce()));

        //<editor-fold desc="화살 유도 기능">

        //유도 대상 찾기
        Vector dir = player.getLocation().getDirection();
        List<Entity> targets = Util.getNearByEntity(player.getLocation(), 75, 75, 75, LivingEntity.class, true);    //유도 대상 후보군
        if(targets.size() <= 0){  //유도 대상 없으면 취소
            return;
        }
        Entity target = null;
        double distance = 0.5;
        double _distance = 0.5;
        for(Entity e : targets){
            if(e.equals(player) || e.equals(projectile) || e instanceof ArmorStand){
                continue;
            }
            //플래이어가 바라보는 방향과 가장 가까이 있는 대상 선정 (백터 내적)
            _distance = dir.normalize().dot(e.getLocation().toVector().subtract(player.getLocation().toVector()).normalize());
            if(e instanceof Player){
                if((_distance + 0.2) > distance){   //플레이어는 보정치 있음
                    target = e;
                    distance = _distance;
                }
            }else if(_distance > distance){
                target = e;
                distance = _distance;
            }
        }
        if(target == null || target.isDead() || !target.isValid()){  //유도 대상 없으면 취소
            return;
        }

        //실제 유도 처리
        double maxAngle = (Math.PI / (180f - (event.getForce() * 50)));     //틱당 최대 유도 각도 (라디안)
        Entity finalTarget = target;
        new CancelableTask(AdvancedCombat.getInst(), 0, 1){
            @Override
            public void run() {
                if(!projectile.isValid() || projectile.isOnGround()){   //화살이 사라지면 취소
                    cancel();
                    return;
                }
                if(finalTarget == null || finalTarget.isDead() || !finalTarget.isValid()){  //유도 대상 없어지면 취소
                    cancel();
                    return;
                }
                
                Vector arrowDir = projectile.getVelocity(); //화살이 날아가는 방향
                double power = arrowDir.length();
                Location loc = projectile.getLocation();
                Vector targetDir = finalTarget.getLocation().toVector().subtract(loc.toVector());   //유도 대상까지의 방향
                float angle = arrowDir.angle(targetDir);    //두 방향 사이의 각도
                if(angle > maxAngle){   //최대 유도 각도 검사
                    angle = (float) maxAngle;
                }
                Vector newDir = arrowDir.rotateAroundAxis(targetDir.crossProduct(arrowDir), -angle);    //유도 대상 방향으로 회전
                projectile.setVelocity(newDir.normalize().multiply(power * 0.99));  //방향 적용
            }
        };
        //</editor-fold>
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event){
        Projectile projectile = event.getEntity();
        if(NBTTagStore.containKey(projectile, Keys.Projectile_Damage.toString())){
            return;
        }
        if(projectile.getShooter() instanceof LivingEntity){
            //발사체 데미지 적용
            LivingEntity entity = (LivingEntity) projectile.getShooter();
            if(ConfigManager.hasProjectileData(entity)){
                ProjectileData data = ConfigManager.getProjectileData(entity);
                NBTTagStore.set(projectile, Keys.Projectile_Damage.toString(), data.getDamage());
            }else if(ConfigManager.hasEntityData(entity.getType())){
                DamageApplier data = ConfigManager.getEntityData(entity.getType());
                NBTTagStore.set(projectile, Keys.Projectile_Damage.toString(), data);
            }
        }else{
            NBTTagStore.set(projectile, Keys.Projectile_Damage.toString(), ConfigManager.normalArrow);
        }
    }
    //</editor-fold>

    //<editor-fold desc="포션 효과 처리">
    @EventHandler
    public void onPotionEffect(PotionSplashEvent event){
        //투척형 포션 처리
        ThrownPotion potion = event.getPotion();
        if(!(potion.getShooter() instanceof Player)){
            return;
        }
        Player shooter = (Player) potion.getShooter();
        event.setCancelled(true);
        applyPotionEffect(potion.getEffects(), shooter, event.getAffectedEntities(), false);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event){
        Projectile entity = event.getEntity();
        if(!(entity instanceof ThrownPotion)){
            return;
        }
        ThrownPotion potion = (ThrownPotion) entity;
        if(!potion.getItem().getType().equals(Material.LINGERING_POTION)){
            return;
        }

        //잔류형 포션 대체 (기존에 생성된 AreaEffectCloud를 지우고 새로 생성)
        entity.getLocation().getWorld().spawn(entity.getLocation(), AreaEffectCloud.class, value -> {
            value.setDuration(400);
            value.setRadius(3);
            value.setReapplicationDelay(20);
            value.setWaitTime(10);
            value.clearCustomEffects();
            for(PotionEffect effect : potion.getEffects()){
                value.addCustomEffect(effect, true);
                value.setColor(effect.getType().getColor());
            }
            if(entity.getShooter() instanceof Player){
                NBTTagStore.set(value, Keys.Potion_Effect_Applied.toString(), ((Player)potion.getShooter()).getUniqueId().toString());
            }
            NBTTagStore.set(value, Keys.Potion_Live.toString(), true);
        });
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event){
        //기존 잔류형 포션의 AreaEffectCloud 지우기
        if(event.getEntity() instanceof AreaEffectCloud && !NBTTagStore.containKey(event.getEntity(), Keys.Potion_Live.toString())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEffectApplied(AreaEffectCloudApplyEvent event){
        //잔류형 포션 효과 처리
        AreaEffectCloud potion = event.getEntity();
        if(!NBTTagStore.containKey(potion, Keys.Potion_Effect_Applied.toString())){
            return;
        }
        OfflinePlayer shooter = Bukkit.getOfflinePlayer(UUID.fromString(NBTTagStore.get(potion, Keys.Potion_Effect_Applied.toString(), String.class)));
        event.setCancelled(true);
        if(!shooter.isOnline()){
            applyPotionEffect(potion.getCustomEffects(), null, event.getAffectedEntities(), true);
        }else{
            applyPotionEffect(potion.getCustomEffects(), shooter.getPlayer(), event.getAffectedEntities(), true);
        }
    }

    //포션 효과 처리
    private void applyPotionEffect(Collection<PotionEffect> effects, Player shooter, Collection<LivingEntity> affected, boolean reduced) {
        for(PotionEffect effect : effects){
            var negative = false;   //부정적 효과 여부
            var positive = false;   //긍정적 효과 여부
            //<editor-fold desc="포션 효과 판별">
            switch (effect.getType().getKey().getKey()){
                case "speed":
                case "haste":
                case "strength":
                case "instant_health":
                case "jump_boost":
                case "regeneration":
                case "resistance":
                case "fire_resistance":
                case "water_breathing":
                case "night_vision":
                case "invisibility":
                case "health_boost":
                case "absorption":
                case "saturation":
                case "luck":
                case "slow_falling":
                case "conduit_power":
                case "dolphins_grace":
                    //긍정적 효과
                    positive = true;
                    break;
                case "slowness":
                case "mining_fatigue":
                case "instant_damage":
                case "nausea":
                case "blindness":
                case "hunger":
                case "weakness":
                case "poison":
                case "wither":
                case "glowing":
                case "unluck":
                case "darkness":
                    //부정적 효과
                    negative = true;
                    break;
                case "levitation":
                case "bad_omen":
                case "hero_of_the_village":
                default:
                    //애매한 것들
                    break;
            }
            //</editor-fold>

            if(negative && shooter != null){
                //사용자는 부정적 효과를 받지 않음
                affected.remove(shooter);
            }
            if(positive){
                //사용자만 긍정적 효과를 받음
                if(shooter != null && affected.contains(shooter)){
                    affected.clear();
                    affected.add(shooter);
                }else{
                    affected.clear();
                }
            }

            //실제 표션효과 적용
            if(effect.getType().equals(PotionEffectType.HARM)){
                //특이케이스1 - 즉시피해
                if(reduced){
                    //잔류형 포션의 경우 효과가 감소되어 적용 (10%)
                    for(LivingEntity entity : affected){
                        DamageApplier applier = new DamageApplier((effect.getAmplifier() + 1) * 3, new DamageTag(DamageTag.AttackType.Magic));
                        applier.apply(entity);
                    }
                }else{
                    for(LivingEntity entity : affected){
                        DamageApplier applier = new DamageApplier((effect.getAmplifier() + 1) * 30, new DamageTag(DamageTag.AttackType.Magic));
                        applier.apply(entity);
                    }
                }
            }else if(effect.getType().equals(PotionEffectType.HEAL)){
                //특이케이스2 - 즉시회복
                if(reduced){
                    //잔류형 포션의 경우 효과가 감소되어 적용 (10%)
                    for(LivingEntity entity : affected){
                        CombatManager.heal(entity, (effect.getAmplifier() + 1) * 3);
                    }
                }else{
                    for(LivingEntity entity : affected){
                        CombatManager.heal(entity, (effect.getAmplifier() + 1) * 30);
                    }
                }
            }else{
                //일반적상황
                for(LivingEntity entity : affected){
                    entity.addPotionEffect(effect);
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="데미지 처리">
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){    //환경 데미지
        if(event.getEntity() instanceof LivingEntity){
            LivingEntity entity = (LivingEntity) event.getEntity();
            DamageApplier applier = null;
            switch (event.getCause()){
                case ENTITY_ATTACK: //플레이어 공격
                case ENTITY_SWEEP_ATTACK: //휘칼
                case PROJECTILE: //화살
                    //데미지 따로 계산
                    event.setDamage(0);
                    return;
                case THORNS: //가시
                case FALLING_BLOCK: //모루 같은 것들
                case CONTACT:   //접촉 (ex 선인장)
                    //물리 데미지
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Physics));
                    break;
                case ENTITY_EXPLOSION:  //엔티티 폭발 (ex 크리퍼)
                case BLOCK_EXPLOSION:   //블럭 폭발 (ex TNT)
                    //물리, 폭발 데미지
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Physics, DamageTag.DamageAttribute.Explosion));
                    break;
                case FIRE:          //근원 불 블럭에 직접 닿아서 입는 데미지
                case FIRE_TICK:     //불이 붙어서 받는 데미지
                case HOT_FLOOR:     //마그마 블럭
                case LAVA:          //용암에 직접 들어가서 입는 데미지
                    //일반, 화염 데미지
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Normal, DamageTag.DamageAttribute.Fire));
                    break;
                case FREEZE:    //동상 (가루눈)
                    //일반 데미지
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Normal));
                    break;
                case SONIC_BOOM:    //워든 원거리 공격
                    //마법, 원거리 데미지
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Magic, DamageTag.DamageAttribute.Projectile));
                    break;
                case DRAGON_BREATH:     //드래곤의 숨결
                case MAGIC:             //마법 데미지 (ex 포션)
                    //마법 데미지
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Magic));
                    break;
                case FALL:  //낙뎀
                    //고정, 낙하 데미지
                    applier = new DamageApplier(event.getDamage() * ConfigManager.weightData.getFallDamageRate(CombatManager.getWeight(entity)),
                            new DamageTag(DamageTag.AttackType.Const, DamageTag.DamageAttribute.Fall));
                    break;
                case POISON:    //독
                case WITHER:    //위더
                    //고정데미지 (최종 데미지 50% 감소 적용)
                    applier = new DamageApplier(event.getDamage() * 0.5, new DamageTag(DamageTag.AttackType.Const));
                    break;
                case CUSTOM:            //플러그인 커스텀 데미지
                    //메소드 호출 스텍 오버플로우 방지
                    return;
                default:    //기타
                    //고정 데미지
                    applier = new DamageApplier(event.getDamage(), new DamageTag(DamageTag.AttackType.Const));
                    break;
            }

            //데미지 적용
            applier.apply(entity, CombatManager.DAMAGE_MODIFIER);
            event.setDamage(0);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event){    //전투로 인한 뎀지
        if(event.getEntity() instanceof LivingEntity){
            switch (event.getCause()){
                case ENTITY_ATTACK: //플레이어 공격
                case ENTITY_SWEEP_ATTACK: //휘칼
                case PROJECTILE: //발사체
                    //여기서 처리
                    break;
                default:
                    //그 외에는 여기서 처리 안 함
                    return;
            }

            LivingEntity entity = (LivingEntity) event.getEntity(); //피격자

            //데미지 배수
            double damageRate = 1;
            if(entity.getType().equals(EntityType.PLAYER)){
                damageRate = ConfigManager.playerDamageRate;
            }else{
                damageRate = ConfigManager.etcDamageRate;
            }

            //최종 데미지 산출
            PartialDamageApplier finalApplier = new PartialDamageApplier();
            if(event.getDamager() instanceof Projectile){   //발사체
                Projectile projectile = (Projectile) event.getDamager();
                //데미지 불러오기
                DamageApplier data = NBTTagStore.get(projectile, Keys.Projectile_Damage.toString(), DamageApplier.class);
                if(data == null){
                    return;
                }
                //적용
                finalApplier.merge(data);
            }else if(event.getDamager() instanceof LivingEntity){   //직접 공격
                LivingEntity damager = (LivingEntity) event.getDamager();
                WeaponData weapon = ConfigManager.getWeaponData(damager);

                if(event.getDamager() instanceof Player){   //플레이어의 경우
                    Player player = (Player) event.getDamager();

                    //공격 쿨타임 (=공격 속도) 검사
                    if(player.getAttackCooldown() <= 0.9){
                        event.setCancelled(true);
                        return;
                    }
                    //공격 거리 검사
                    if(entity.getLocation().distanceSquared(player.getLocation()) > weapon.getAttackDistance() * weapon.getAttackDistance()){
                        event.setCancelled(true);
                        return;
                    }
                }else{  //몬스터의 경우
                    EntityType entityType = damager.getType();
                    if(ConfigManager.hasEntityData(entityType)){
                        //몬스터별 데미지 적용
                        DamageApplier monsterDamage = ConfigManager.getEntityData(entityType);
                        finalApplier.merge(monsterDamage);
                    }
                }

                //무기 데미지 추가
                DamageApplier applier = weapon.getDamage();
                finalApplier.merge(applier.multiply(CombatManager.getDamageIncrease(damager)));
            }

            //데미지 적용
            finalApplier.apply(entity, damageRate);
        }
    }
    //</editor-fold>
}

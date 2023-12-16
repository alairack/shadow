package alairack.shadow;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.*;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;



public class ShadowEntity {
    public static void spawnShadow(World world, Player player){
        int random_x = ThreadLocalRandom.current().nextInt(player.getLocation().getBlockX()-80, player.getLocation().getBlockX() + 80);
        int z = player.getLocation().getBlockZ();
        WitherSkeleton shadowEntity = (WitherSkeleton) world.spawnEntity(new Location(world, random_x, world.getHighestBlockYAt(random_x, z)+1, z), EntityType.WITHER_SKELETON);
        Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(16);
        shadowEntity.setHealth(16);
        Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).setBaseValue(8);
        Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.4);
        Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE)).setBaseValue(120);
        shadowEntity.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 9999999, 2));
        raid(shadowEntity, player);
        Utils.sendGlobalMessage(world, "'不详的眼神在肆意打量着你'");
    }
    public static void commandSpawn(World world, Player player){
        WitherSkeleton shadowEntity = (WitherSkeleton) world.spawnEntity(player.getLocation(), EntityType.WITHER_SKELETON);
        Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(666);
        shadowEntity.setHealth(666);
        Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).setBaseValue(8);
        Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.4);
        Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE)).setBaseValue(120);
        shadowEntity.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 9999999, 2));
        raid(shadowEntity, player);
    }

    public static void swap(LivingEntity shadowEntity, LivingEntity target){
        Location shadowLocation = shadowEntity.getLocation();
        Location targetLocation = target.getLocation();
        shadowEntity.teleport(targetLocation);
        target.teleport(shadowLocation);
        target.getLocation().setDirection(new Vector(RandomUtils.nextInt(0, 180), RandomUtils.nextInt(0, 180), RandomUtils.nextInt(0, 180)));
    }
    public static void superSwap(Mob shadowEntity){
        ArrayList<Player> nearPlayers = Utils.getNearPlayers(shadowEntity);
        if (nearPlayers.size() > 2){
            Player newSubstitute = nearPlayers.get(RandomUtils.nextInt(0, nearPlayers.size()));
            if (newSubstitute.getEntityId() != Objects.requireNonNull(shadowEntity.getTarget()).getEntityId()){
                if (RandomUtils.nextInt(0,2) == 1){
                    ShadowEntity.pretendMob(newSubstitute, DisguiseType.VINDICATOR);
                    shadowEntity.getWorld().playEffect(shadowEntity.getLocation(), Effect.ENDER_SIGNAL, 0);
                }
                else {
                    pretendPlayer(shadowEntity, newSubstitute);
                    PlayerDisguise playerDisguise = new PlayerDisguise((Player) shadowEntity.getTarget());
                    playerDisguise.setEntity(newSubstitute);
                    playerDisguise.startDisguise();
                    shadowEntity.getWorld().playEffect(shadowEntity.getLocation(), Effect.ENDER_SIGNAL, 0);
                    swap(shadowEntity, newSubstitute);
                }
            }
        }
    }
    public static void pretendPlayer(LivingEntity shadowEntity, Player player){
        PlayerDisguise playerDisguise = new PlayerDisguise(player);
        playerDisguise.setEntity(shadowEntity);
        playerDisguise.startDisguise();
        Objects.requireNonNull(shadowEntity.getEquipment()).setArmorContents(Objects.requireNonNull(player.getEquipment()).getArmorContents());
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (mainHandItem.getType() == Material.BOW){
            Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.6);
            shadowEntity.getEquipment().setItemInMainHand(new ItemStack(Material.TRIDENT));
        }
        else {
            Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.4);
            shadowEntity.getEquipment().setItemInMainHand(mainHandItem);
        }
        shadowEntity.getEquipment().setItemInOffHand(player.getInventory().getItemInOffHand());
    }
    public static void pretend(LivingEntity shadowEntity, DisguiseType disguiseType){
        MiscDisguise miscDisguise = new MiscDisguise(disguiseType);
        miscDisguise.setEntity(shadowEntity);
        miscDisguise.startDisguise();
    }
    public static void pretendMob(LivingEntity shadowEntity, DisguiseType disguiseType){
        MobDisguise mobDisguise = new MobDisguise(disguiseType);
        mobDisguise.setEntity(shadowEntity);
        mobDisguise.startDisguise();
    }

    public static void raid(WitherSkeleton shadowEntity, Player player){
        if (RandomUtils.nextInt(0, 2) == 0)
        {
            pretendPlayer(shadowEntity, player.getWorld().getPlayers().get(RandomUtils.nextInt(0, player.getWorld().getPlayers().size())));
            player.getWorld().playSound(player.getLocation(), Sound.MUSIC_DISC_11, 1, 1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 4, 2));
        }
        else
        {
            pretend(shadowEntity, DisguiseType.EXPERIENCE_ORB);
            Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.2);
        }
        shadowEntity.setTarget(player);
    }
    public static void escape(Mob shadowEntity){
        for (Player player: shadowEntity.getWorld().getPlayers()){
            DisguiseAPI.undisguiseToAll(player);
            LivingEntity villager = (LivingEntity) shadowEntity.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
            pretendPlayer(villager, player);
            player.playSound(player, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
            new BukkitRunnable(){
                private int i = 0;
                @Override
                public void run(){
                    if (i > 12){
                        this.cancel();
                    }
                    shadowEntity.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, shadowEntity.getLocation(), 1000, 3, 3, 3, 1);
                    i = i+1;
                }
            }.runTaskTimer(Shadow.getInstance(), 1L, 1L);
        }
        DisguiseAPI.undisguiseToAll(shadowEntity);
        shadowEntity.remove();
        new BukkitRunnable(){
            private int i = 0;
            @Override
            public void run(){
                if (i > 40){
                    this.cancel();
                }
                shadowEntity.getWorld().spawnParticle(Particle.CLOUD, shadowEntity.getLocation(), 3000, 6, 6, 6, 1);
                i = i+1;
            }
        }.runTaskTimer(Shadow.getInstance(), 1L, 1L);



    }
    public static void getHurt(Mob shadowEntity, double remainHealth){
        if (remainHealth < 10){
            Utils.sendGlobalMessage(shadowEntity.getWorld(),  ((Player) Objects.requireNonNull(shadowEntity.getTarget())).getPlayerListName() + "击杀了尾随者!!!他的ID是" + shadowEntity.getTarget().getUniqueId());
            ShadowEntity.escape(shadowEntity);
        }
        else {

            Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.4);
            if (shadowEntity.getTarget() != null && shadowEntity.getTarget() instanceof Player){
                int randomInt = RandomUtils.nextInt(0, 8);
                if ( randomInt == 1){
                    ShadowEntity.superSwap(shadowEntity);
                }
                else if (randomInt == 2){
                    ShadowEntity.hide(shadowEntity);
                }
                else {
                    ShadowEntity.swap(shadowEntity, Objects.requireNonNull(shadowEntity.getTarget()));
                    if (shadowEntity.getTarget() instanceof Player){
                        ShadowEntity.pretendPlayer(shadowEntity, (Player) shadowEntity.getTarget());
                    }
                }
            }
        }
    }
    public static void attackPlayer(Entity targetEntity, Mob shadowEntity,double finalDamage){
        if (targetEntity.getType() == EntityType.PLAYER){
            Player player = (Player) targetEntity;
            if (finalDamage > player.getHealth()){
                if (targetEntity.getWorld().getPlayers().size() > 0){
                    Player nextTarget = player.getWorld().getPlayers().get(RandomUtils.nextInt(0, player.getWorld().getPlayers().size()));
                    ShadowEntity.raid((WitherSkeleton) shadowEntity, nextTarget);
                }
                if(shadowEntity.getHealth() + 10 <= Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue()){
                    shadowEntity.setHealth(shadowEntity.getHealth() + 10);
                }
            }
            Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.4);
            ShadowEntity.pretendPlayer(shadowEntity, player);
        }

    }
    public static void hide(Mob shadowEntity){
        shadowEntity.setAI(false);
        shadowEntity.setInvisible(true);
        FlagWatcher flagWatcher = DisguiseAPI.getDisguise(shadowEntity).getWatcher();
        flagWatcher.setInvisible(true);
        shadowEntity.getWorld().playSound(shadowEntity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        new BukkitRunnable(){
            private int i = 0;
            @Override
            public void run(){
                if (i > 12){
                    this.cancel();
                }
                shadowEntity.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, shadowEntity.getLocation(), 1000, 3, 3, 3, 1);
                i = i+1;
            }
        }.runTaskTimer(Shadow.getInstance(), 1L, 1L);
        ShadowEntity.pretend(shadowEntity, DisguiseType.ENDER_SIGNAL);
        new BukkitRunnable(){
            @Override
            public void run(){
                shadowEntity.setAI(true);
                shadowEntity.setInvisible(false);
                flagWatcher.setInvisible(false);
            }
        }.runTaskLater(Shadow.getInstance(), 10L);

    }

}

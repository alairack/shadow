package alairack.shadow;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTEntity;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTCompoundList;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.*;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;



public class ShadowEntity {
    public static final String bossName = "Shadow";
    public static BossBar bossBar = null;
    public static BossBar bossBar2 = null;
    public static int fullHealth = 300;
    public static boolean isAdvance = false;   //是否变身

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
    public static void commandSpawn(World world, Player player, int type, Location location){
        WitherSkeleton shadowEntity = (WitherSkeleton) world.spawnEntity(location, EntityType.WITHER_SKELETON);
        Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(ShadowEntity.fullHealth);
        shadowEntity.setHealth(ShadowEntity.fullHealth);


        Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).setBaseValue(8);
        Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.4);
        Objects.requireNonNull(shadowEntity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE)).setBaseValue(120);
        shadowEntity.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 9999999, 2));

        BossBar bossBar = Bukkit.getServer().createBossBar(ShadowEntity.bossName, BarColor.RED, BarStyle.SEGMENTED_20, BarFlag.PLAY_BOSS_MUSIC);
        bossBar.setVisible(true);
        bossBar.setProgress(1.0);
        bossBar.addFlag(BarFlag.CREATE_FOG);
        bossBar.addFlag(BarFlag.DARKEN_SKY);
        for (Player onlinePlayer: world.getPlayers()){
            bossBar.addPlayer(onlinePlayer);
        }
        if (type==1) {
            ShadowEntity.bossBar = bossBar;
            NBTEntity nbtEntity = new NBTEntity(shadowEntity);
            NBTCompound com = nbtEntity.getPersistentDataContainer().getOrCreateCompound("customtags");
            com.setInteger("shadowNumber", 1);
        } else if (type==2) {
            ShadowEntity.bossBar2 = bossBar;
            NBTEntity nbtEntity = new NBTEntity(shadowEntity);
            NBTCompound com = nbtEntity.getPersistentDataContainer().getOrCreateCompound("customtags");
            com.setInteger("shadowNumber", 2);
        }
        raid(shadowEntity, player);
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncDelayedTask(Shadow.getInstance(), new Runnable() {
            @Override
            public void run() {
                List<Player> players= shadowEntity.getWorld().getPlayers();
                if (players.size() > 1){
                    ShadowEntity.swap(shadowEntity, players.get(1));
                }
            }
        }, 2400);
    }

    public static int getShadowNumber(Mob shadow){
        NBTEntity nbtEntity = new NBTEntity(shadow); // Vanilla tags only!
        if (nbtEntity.getPersistentDataContainer().getCompound("customtags").hasTag("shadowNumber")){
            return nbtEntity.getPersistentDataContainer().getCompound("customtags").getInteger("shadowNumber");
        }
        Bukkit.getLogger().warning("Shadow Number is error!");
        return 1;
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
        shadowEntity.setCustomName(player.getName());
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
        Utils.sendGlobalMessage(shadowEntity.getWorld(), "来自Shadow的一封信: 没想到一年不见，你们长进挺大啊，不管怎么样，我会回来的。");
        DisguiseAPI.undisguiseToAll(shadowEntity);
        shadowEntity.remove();

        if (ShadowEntity.getShadowNumber(shadowEntity) == 1){
            bossBar.removeAll();
        }
        if (ShadowEntity.getShadowNumber(shadowEntity) == 2){
            bossBar2.removeAll();
        }
        Bukkit.getServer().getScheduler().cancelTasks(Shadow.getInstance());
    }

    public static void reset(){
        ShadowEntity.bossBar = null;
        ShadowEntity.bossBar2 = null;
        ShadowEntity.isAdvance = false;
    }
    public static void getHurt(Mob shadowEntity, double remainHealth){
        if (remainHealth < 10){
            //Utils.sendGlobalMessage(shadowEntity.getWorld(),  ((Player) Objects.requireNonNull(shadowEntity.getTarget())).getPlayerListName() + "击杀了Shadow!!!他的ID是" + shadowEntity.getTarget().getUniqueId());
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

        if (ShadowEntity.getShadowNumber(shadowEntity) == 1 && ShadowEntity.bossBar !=null){
            double health = shadowEntity.getHealth()/ShadowEntity.fullHealth;
            for (Player onlinePlayer: shadowEntity.getWorld().getPlayers()){
                bossBar.addPlayer(onlinePlayer);
            }
            if (health > 0 && health<1){
                ShadowEntity.bossBar.setProgress(health);
            }
            //Bukkit.getLogger().info(String.format("shadowEntity %s", shadowEntity.getHealth()/ShadowEntity.fullHealth));
        } else if (ShadowEntity.getShadowNumber(shadowEntity) == 2 &&ShadowEntity.bossBar2 !=null) {
            double health = shadowEntity.getHealth()/ShadowEntity.fullHealth;
            if (health > 0 && health<1){
                ShadowEntity.bossBar2.setProgress(health);
                for (Player onlinePlayer: shadowEntity.getWorld().getPlayers()){
                    bossBar2.addPlayer(onlinePlayer);
                }
            }

            //Bukkit.getLogger().info(String.format("shadowEntity %s", shadowEntity.getHealth()/ShadowEntity.fullHealth));
        }
        if (!ShadowEntity.isAdvance &&ShadowEntity.getShadowNumber(shadowEntity) == 1&& shadowEntity.getHealth()/ ShadowEntity.fullHealth <= 0.3){
            ShadowEntity.intoAdvanceStatus(shadowEntity);
    }

    }

    public static void intoAdvanceStatus(Mob shadowEntity){
        ShadowEntity.isAdvance = true;
        //变身！
        for (Player player: shadowEntity.getWorld().getPlayers()){
            player.playSound(player, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
            new BukkitRunnable(){
                private int i = 0;
                @Override
                public void run(){
                    if (i > 20){
                        this.cancel();
                    }
                    shadowEntity.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, shadowEntity.getLocation(), 1000, 3, 3, 3, 1);
                    i = i+1;
                }
            }.runTaskTimer(Shadow.getInstance(), 1L, 3L);
        }
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
        }.runTaskTimer(Shadow.getInstance(), 1L, 3L);
        Bukkit.getLogger().info("spawn 2 shadow");
        ShadowEntity.commandSpawn(shadowEntity.getWorld(), shadowEntity.getWorld().getPlayers().get(0), 2, shadowEntity.getWorld().getPlayers().get(0).getLocation());

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
        }.runTaskLater(Shadow.getInstance(), 20L);

    }

}

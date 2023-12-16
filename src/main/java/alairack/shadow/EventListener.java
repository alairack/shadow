package alairack.shadow;

import me.libraryaddict.disguise.DisguiseAPI;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.logging.Logger;


public class EventListener implements Listener {

  @EventHandler
  public void onEntityHint(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Arrow) {
      if (((Arrow) event.getDamager()).getShooter() instanceof WitherSkull) {
        Entity damager = (Entity) ((Arrow) event.getDamager()).getShooter();
        if (DisguiseAPI.isDisguised(damager)) {
          ShadowEntity.attackPlayer(event.getEntity(), (Mob) damager, event.getFinalDamage());
        }
      }
    }
    if (event.getDamager().getType() == EntityType.WITHER_SKELETON && DisguiseAPI.isDisguised(event.getDamager())) {
      // 尾随者攻击玩家
      ShadowEntity.attackPlayer(event.getEntity(), (Mob) event.getDamager(), event.getFinalDamage());
    }
    else if (event.getEntityType() == EntityType.WITHER_SKELETON) {
      if (DisguiseAPI.isDisguised(event.getEntity())) {
        Mob shadowEntity = (Mob) event.getEntity();
        if (shadowEntity.getTarget() == null) {
          if (event.getDamager() instanceof Arrow) {
            if (((Arrow) event.getDamager()).getShooter() instanceof Player) {
              shadowEntity.setTarget((Player) ((Arrow) event.getDamager()).getShooter());
            }
          }
        }
        if (event.getDamager() instanceof Player) {
            shadowEntity.setTarget((Player) event.getDamager());
          }
          ShadowEntity.getHurt(shadowEntity, shadowEntity.getHealth() - event.getFinalDamage());
        }
      }
    }


  @EventHandler
  public void onEntityDied(EntityDeathEvent event) {
    int randomInt = RandomUtils.nextInt(1, 2000);
    if (randomInt == 80 && event.getEntity().getWorld().getTime() > 13000) {
      for (Player player : Bukkit.getOnlinePlayers()) {
        double distance = Utils.getNearestPlayerDistance(player);
        if (distance > 50 && distance < 150) {
          ShadowEntity.spawnShadow(player.getWorld(), player);
          break;
        }
      }
    }
  }
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event){
    event.getPlayer().sendMessage("来自陌生人的警告：\n'我们都被Shadow瓦解了。你们必须保持团结和冷静，否则将会加速团体的死亡。'");
    event.getPlayer().sendMessage("来自shadow的一封信: \n 人类所谓的友谊，充斥着贪婪和虚伪！你们将目睹'友谊'的崩塌!顺便提醒你，注意你的背后。");
  }


}

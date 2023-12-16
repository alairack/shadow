package alairack.shadow;

import jline.internal.Nullable;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;

public class Utils {
    public static @Nullable double getNearestPlayerDistance(Player player) {
        World world = player.getWorld();
        Location location = player.getLocation();
        ArrayList<Player> playersInWorld = new ArrayList<>(world.getPlayers());
        if(playersInWorld.size()==1) return -1;
        playersInWorld.remove(player);
        playersInWorld.sort(Comparator.comparingDouble(o -> o.getLocation().distance(location)));
        return playersInWorld.get(0).getLocation().distance(location);
    }

    public static ArrayList<Player> getNearPlayers(LivingEntity entity){
        ArrayList<Player> nearby = new ArrayList<>();
        double range = 50;
        for (Entity e : entity.getNearbyEntities(range, 10, range)){
            if (e instanceof Player){
                nearby.add((Player) e);
            }
        }
        return nearby;
    }

    public static void sendGlobalMessage(World world, String message){
        for (Player player : world.getPlayers()){
            player.sendMessage(message);
        }

    }
}

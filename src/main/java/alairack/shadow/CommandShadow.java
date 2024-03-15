package alairack.shadow;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandShadow implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if (sender instanceof Player player && player.getWorld().getTime() > 13000){
            ShadowEntity.commandSpawn(player.getWorld(), player, 1, player.getLocation());
        }
        return true;
    }
}

package alairack.shadow;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Shadow extends JavaPlugin {
    private static Shadow instance;
    @Override
    public void onEnable ( ) {
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        Objects.requireNonNull(this.getCommand("shadow")).setExecutor(new CommandShadow());
        getLogger ( ).info ( "尾随者已开启" ) ;
        instance =this;
    }
    @Override
    public void onDisable ( ) {
        getLogger ( ).info ( "尾随者已关闭" ) ;
        instance = null;
    }

    public static Shadow getInstance(){
        return instance;
    }
}

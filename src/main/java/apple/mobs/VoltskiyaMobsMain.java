package apple.mobs;

import apple.mobs.betterSpiderSpawn.BetterSpiderSpwnMain;
import apple.mobs.thyla.ThylaMain;
import org.bukkit.plugin.java.JavaPlugin;

public class VoltskiyaMobsMain extends JavaPlugin {
    public static final String PLUGIN_NAME = "[VoltskiyaMobs]";

    @Override
    public void onEnable() {
        ThylaMain.enable(this);
        BetterSpiderSpwnMain.enable(this);

        System.out.println(PLUGIN_NAME + " enabled");
    }
}

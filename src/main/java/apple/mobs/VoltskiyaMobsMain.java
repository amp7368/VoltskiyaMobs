package apple.mobs;

import apple.mobs.betterSpiderSpawn.BetterSpiderSpawn;
import apple.mobs.thyla.TreeLatchAI;
import apple.mobs.thyla.TreeLatchSpawnOrHitListener;
import org.bukkit.plugin.java.JavaPlugin;

public class VoltskiyaMobsMain extends JavaPlugin {
    public static final String PLUGIN_NAME = "[VoltskiyaMobs]";

    @Override
    public void onEnable() {
        TreeLatchAI.initialize(this);
        new TreeLatchSpawnOrHitListener(this);
        new BetterSpiderSpawn(this);
        System.out.println(PLUGIN_NAME + " enabled");
    }
}

package apple.mobs;

import apple.mobs.betterSpiderSpawn.BetterSpiderSpawn;
import apple.mobs.treeLatch.TreeLatchAI;
import apple.mobs.treeLatch.TreeLatchSpawnListener;
import org.bukkit.plugin.java.JavaPlugin;

public class VoltskiyaMobsMain extends JavaPlugin {
    public static final String PLUGIN_NAME = "[VoltskiyaMobs]";

    @Override
    public void onEnable() {
        TreeLatchAI.initialize(this);
        new TreeLatchSpawnListener(this);
        new BetterSpiderSpawn(this);
        System.out.println(PLUGIN_NAME + " enabled");
    }
}

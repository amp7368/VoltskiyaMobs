package apple.mobs.betterSpiderSpawn;

import apple.mobs.VoltskiyaMobsMain;
import apple.mobs.treeLatch.TreeLatchAI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class BetterSpiderSpawn implements Listener {

    private static final String BETTER_SPIDER_SPAWN = "spawn.spider";

    public BetterSpiderSpawn(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }

    @EventHandler(priority = EventPriority.LOW) // let other people cancel it if needed
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        Set<String> tags = entity.getScoreboardTags();
        if (tags.contains(BETTER_SPIDER_SPAWN)) {
        }
    }
}

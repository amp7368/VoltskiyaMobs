package apple.mobs.thyla;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class TreeLatchSpawnOrHitListener implements Listener {
    private static final String latchTag = "ai.thyla";

    public TreeLatchSpawnOrHitListener(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW) // let other people cancel it if needed
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        Set<String> tags = entity.getScoreboardTags();
        if (tags.contains(latchTag) && entity instanceof Mob) {
            TreeLatchAI.makeThyla((Mob)entity);
        }
    }
    @EventHandler
    public void onEntityHit(EntityDamageEvent event){
        Entity entity = event.getEntity();
        Set<String> tags = entity.getScoreboardTags();
        if (tags.contains(latchTag) && entity instanceof Mob) {
            TreeLatchAI.countHit(entity.getUniqueId(),System.currentTimeMillis());
        }
    }
}

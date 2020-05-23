package apple.mobs.betterSpiderSpawn;

import apple.mobs.utils.BoundingBoxUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class BetterSpiderSpawn implements Listener {

    private static final String BETTER_SPIDER_SPAWN = "spawn.spider";

    public BetterSpiderSpawn(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW) // let other people cancel it if needed
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        Set<String> tags = entity.getScoreboardTags();
        if (tags.contains(BETTER_SPIDER_SPAWN)) {
            World world = entity.getWorld();
            BoundingBox hitbox = entity.getBoundingBox();
            @NotNull List<Vector> corners = BoundingBoxUtils.getCorners(hitbox);
            Vector firstColliding = null;
            Vector secondColliding = null;
            Vector thirdColliding = null;
            for (Vector corner : corners) {
                if (!world.getBlockAt(corner.getBlockX(), corner.getBlockY(), corner.getBlockZ()).isPassable()) {
                    if (firstColliding == null)
                        firstColliding = corner;
                    else if (secondColliding == null) {
                        secondColliding = corner;
                    } else {
                        thirdColliding = corner;
                        break;
                    }
                }
            }
            Vector center = hitbox.getCenter();
            if (firstColliding == null) {
                return;
            } else if (secondColliding == null) {
                // move away from first colliding
                int xAddition;
                int yAddition;
                int zAddition;
                if (firstColliding.getX() < center.getX())
                    xAddition = 1;
                else
                    xAddition = -1;
                if (firstColliding.getY() < center.getY())
                    yAddition = 1;
                else
                    yAddition = -1;
                if (firstColliding.getZ() < center.getZ())
                    zAddition = 1;
                else
                    zAddition = -1;
                entity.teleport(new Location(world, center.getX() + xAddition, center.getY() + yAddition, center.getZ() + zAddition));
            } else if (thirdColliding == null) {
                // these could share one or two faces
                if (firstColliding.getX() == secondColliding.getX() && firstColliding.getZ() == secondColliding.getZ()) {
                    // they share two faces
                    // make the x change
                    int xAddtion;
                    if (firstColliding.getX() < center.getX()) xAddtion = 1;
                    else xAddtion = -1;
                    // make the z change
                    int zAddtion;
                    if (firstColliding.getZ() < center.getZ()) zAddtion = 1;
                    else zAddtion = -1;
                    entity.teleport(new Location(world, center.getX() + xAddtion, center.getY(), center.getZ() + zAddtion));
                } else {
                    // they share one face
                    if (firstColliding.getX() == secondColliding.getX()) {
                        // make the x change
                        int xAddtion;
                        if (firstColliding.getX() < center.getX()) xAddtion = 1;
                        else xAddtion = -1;
                        entity.teleport(new Location(world, center.getX() + xAddtion, center.getY(), center.getZ()));

                    } else {
                        // make the z change
                        int zAddtion;
                        if (firstColliding.getZ() < center.getZ()) zAddtion = 1;
                        else zAddtion = -1;
                        entity.teleport(new Location(world, center.getX(), center.getY(), center.getZ() + zAddtion));
                    }
                }
            } else {
                // move away from both first colliding and second and third colliding

                // they share one face
                if (firstColliding.getX() == secondColliding.getX() && firstColliding.getX() == thirdColliding.getX()) {
                    // make the x change
                    int xAddtion;
                    if (firstColliding.getX() < center.getX()) xAddtion = 1;
                    else xAddtion = -1;
                    entity.teleport(new Location(world, center.getX() + xAddtion, center.getY(), center.getZ()));

                } else if(firstColliding.getZ() == secondColliding.getZ() && firstColliding.getZ() == thirdColliding.getZ()){
                    // make the z change
                    int zAddtion;
                    if (firstColliding.getZ() < center.getZ()) zAddtion = 1;
                    else zAddtion = -1;
                    entity.teleport(new Location(world, center.getX(), center.getY(), center.getZ() + zAddtion));
                }
            }
        }
    }
}

package apple.mobs.betterSpiderSpawn;

import apple.mobs.utils.BoundingBoxUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
        if (entity.getType() == EntityType.SPIDER) {
            fixSpawn(entity, 2);
        }
    }

    private void fixSpawn(Entity entity, int triesLeft) {
        if (triesLeft == 0) {
            return;
        }
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
            double xAddition;
            double yAddition;
            double zAddition;
            if (firstColliding.getX() < center.getX())
                xAddition = 0.5;
            else
                xAddition = -0.5;
            if (firstColliding.getY() < center.getY())
                yAddition = 0.5;
            else
                yAddition = -0.5;
            if (firstColliding.getZ() < center.getZ())
                zAddition = 0.5;
            else
                zAddition = -0.5;
            entity.teleport(new Location(world, center.getX() + xAddition, center.getY() + yAddition, center.getZ() + zAddition));
        } else if (thirdColliding == null) {
            // these could share one or two faces
            if (firstColliding.getX() == secondColliding.getX() && firstColliding.getZ() == secondColliding.getZ()) {
                // they share two faces
                // make the x change
                double xAddtion;
                if (firstColliding.getX() < center.getX()) xAddtion = 0.5;
                else xAddtion = -0.5;
                // make the z change
                double zAddtion;
                if (firstColliding.getZ() < center.getZ()) zAddtion = 0.5;
                else zAddtion = -0.5;
                entity.teleport(new Location(world, center.getX() + xAddtion, center.getY(), center.getZ() + zAddtion));
            } else {
                // they share one face
                if (firstColliding.getX() == secondColliding.getX()) {
                    // make the x change
                    double xAddtion;
                    if (firstColliding.getX() < center.getX()) xAddtion = 0.5;
                    else xAddtion = -0.5;
                    entity.teleport(new Location(world, center.getX() + xAddtion, center.getY(), center.getZ()));

                } else {
                    // make the z change
                    double zAddtion;
                    if (firstColliding.getZ() < center.getZ()) zAddtion = 0.5;
                    else zAddtion = -0.5;
                    entity.teleport(new Location(world, center.getX(), center.getY(), center.getZ() + zAddtion));
                }
            }
        } else {
            // move away from both first colliding and second and third colliding

            // they share one face
            if (firstColliding.getX() == secondColliding.getX() && firstColliding.getX() == thirdColliding.getX()) {
                // make the x change
                double xAddtion;
                if (firstColliding.getX() < center.getX()) xAddtion = 0.5;
                else xAddtion = -0.5;
                entity.teleport(new Location(world, center.getX() + xAddtion, center.getY(), center.getZ()));

            } else if (firstColliding.getZ() == secondColliding.getZ() && firstColliding.getZ() == thirdColliding.getZ()) {
                // make the z change
                double zAddtion;
                if (firstColliding.getZ() < center.getZ()) zAddtion = 0.5;
                else zAddtion = -0.5;
                entity.teleport(new Location(world, center.getX(), center.getY(), center.getZ() + zAddtion));
            }
        }
        fixSpawn(entity, triesLeft - 1);
    }
}

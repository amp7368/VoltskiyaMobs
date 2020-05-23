package apple.mobs.treeLatch;

import org.apache.commons.lang.enums.ValuedEnum;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class TreeLatchAI {
    private static final long CHECK_SIGHT_COOLDOWN = 20;
    private static final String POUNCE_SPEED_PATH = "pounceSpeed";
    private static final String INVULNERABLE_TIME_PATH = "invulnerableTime";
    private static double pounceSpeed;
    private static long invulnerableTime;
    private static final String THYLA_CONFIG = "config.yml";
    private static final Object THYLA_FOLDER = "thyla";
    private static final String THYLA_PATH = "thyla";
    private static JavaPlugin plugin;

    public static void initialize(JavaPlugin pl) {
        plugin = pl;
        YamlConfiguration configOrig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + THYLA_FOLDER + File.separator + THYLA_CONFIG));
        ConfigurationSection config = configOrig.getConfigurationSection(THYLA_PATH);
        if (config == null) {
            System.err.println("There was an error with the config file for thylas");
            pounceSpeed = 0;
            invulnerableTime = 0;
            return;
        }
        pounceSpeed = config.getDouble(POUNCE_SPEED_PATH);
        invulnerableTime = config.getLong(INVULNERABLE_TIME_PATH);
    }

    public static void makeThyla(Mob entity) {
        Location entityLocation = entity.getLocation();

        //todo make it spawn on a solid surface

        entity.setAI(false);
        entity.setAware(true);
        entity.setGravity(false);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> doCheckSight(entity), CHECK_SIGHT_COOLDOWN);
        System.out.println("thyla made");
    }

    private static void doCheckSight(Mob thyla) {
        Location entityLocation = thyla.getLocation();
        World entityWorld = entityLocation.getWorld();
        if (entityWorld == null) {
            return; // this is no longer a working thyla
        }
        @NotNull Collection<Entity> nearbyEntities = entityWorld.getNearbyEntities(entityLocation, 20, 50, 20);
        List<Player> nearbyPlayers = new ArrayList<>();
        for (Entity nearbyEntity : nearbyEntities) {
            if (nearbyEntity instanceof Player) {
                Player player = (Player) nearbyEntity;
                if (player.getGameMode() == GameMode.SURVIVAL)
                    nearbyPlayers.add(player);
            }
        }
        nearbyPlayers.sort((o1, o2) -> {
            Location loc1 = o1.getLocation();
            Location loc2 = o2.getLocation();
            return (int)
                    ((Math.pow(Math.abs(entityLocation.getBlockX() - loc1.getBlockX()), 2) +
                            Math.pow(Math.abs(entityLocation.getBlockY() - loc1.getBlockY()), 2) +
                            Math.pow(Math.abs(entityLocation.getBlockZ() - loc1.getBlockZ()), 2))
                            -
                            (Math.pow(Math.abs(entityLocation.getBlockX() - loc2.getBlockX()), 2) +
                                    Math.pow(Math.abs(entityLocation.getBlockY() - loc2.getBlockY()), 2) +
                                    Math.pow(Math.abs(entityLocation.getBlockZ() - loc2.getBlockZ()), 2)));
        });
        if (!nearbyPlayers.isEmpty()) {
            // the mob is close enough to the player to maybe see it
            for (Player player : nearbyPlayers) {
                if (thylaCanSee(thyla, player)) {
                    // make the thyla jump at this player
                    doThylaPounce(thyla, player);
                    return;
                }
            }
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> doCheckSight(thyla), CHECK_SIGHT_COOLDOWN);

    }

    private static void doThylaPounce(Mob thyla, Player player) {
        Location thylaLoc = thyla.getEyeLocation();
        Location playerLoc = player.getEyeLocation();
        thyla.setInvulnerable(true);
        double x = playerLoc.getX() - thylaLoc.getX();
        double y = playerLoc.getY() - thylaLoc.getY();
        double z = playerLoc.getZ() - thylaLoc.getZ();
        double magnitude = Math.sqrt(x * x + y * y + z * z);
        x /= magnitude;
        y /= magnitude;
        z /= magnitude;
        Vector newVelocity = new Vector(x, y, z);
        newVelocity = newVelocity.multiply(pounceSpeed);
        thyla.setVelocity(newVelocity);
        thyla.setAI(true);
        thyla.setAware(true);
        thyla.setGravity(true);
        thyla.setTarget(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> normalizeThyla(thyla), invulnerableTime);
        System.out.println("pounce!");
    }

    private static void normalizeThyla(Mob thyla) {
        thyla.setInvulnerable(false);
    }

    private static boolean thylaCanSee(Mob thyla, Player player) {
//        BoundingBox hitBox = thyla.getBoundingBox();

        return thyla.hasLineOfSight(player);
    }
}

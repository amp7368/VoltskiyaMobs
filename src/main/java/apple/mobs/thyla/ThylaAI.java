package apple.mobs.thyla;

import apple.mobs.utils.BoundingBoxUtils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class ThylaAI {
    private static final long CHECK_SIGHT_COOLDOWN = 20;
    private static final String POUNCE_SPEED_PATH = "pounceSpeed";
    private static final String SIGHT_Y_PATH = "sightY";
    private static final String SIGHT_PATH = "sightLateral";
    public static final int THYLA_GROWL_LENGTH = 6;
    public static final int THYLA_POUNCE_WAIT = 4;
    private static final double STUN_DISTANCE = 2;
    private static double pounceSpeed;
    private static int sightY;
    private static int sightLateral;
    private static final String THYLA_CONFIG = "config.yml";
    private static final String THYLA_FOLDER = "thyla";
    private static final String THYLA_PATH = "thyla";
    private static JavaPlugin plugin;
    private static final Object thylaWasHitSync = new Object();
    private static final HashMap<UUID, Long> thylaWashit = new HashMap<>();

    public static void initialize(JavaPlugin pl) {
        plugin = pl;
        YamlConfiguration configOrig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + File.separator + THYLA_FOLDER + File.separator + THYLA_CONFIG));
        ConfigurationSection config = configOrig.getConfigurationSection(THYLA_PATH);
        if (config == null) {
            System.err.println("There was an error with the config file for thylas");
            pounceSpeed = 0;
            sightY = 0;
            sightLateral = 0;
            return;
        }
        pounceSpeed = config.getDouble(POUNCE_SPEED_PATH);
        sightY = config.getInt(SIGHT_Y_PATH);
        sightLateral = config.getInt(SIGHT_PATH);
    }

    public static void makeThyla(Mob entity) {
        entity.setAI(false);
        entity.setAware(true);
        entity.setGravity(false);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> doCheckSight(entity), CHECK_SIGHT_COOLDOWN);
    }

    private static void doCheckSight(Mob thyla) {
        synchronized (thylaWasHitSync) {
            thylaWashit.remove(thyla.getUniqueId());
        }
        if (thyla.isDead()) {
            return;
        }
        Location entityLocation = thyla.getLocation();
        World entityWorld = entityLocation.getWorld();
        if (entityWorld == null) {
            return; // this is no longer a working thyla
        }
        @NotNull Collection<Entity> nearbyEntities = entityWorld.getNearbyEntities(entityLocation, sightLateral, sightY, sightLateral);
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
                    for (Player playerToScare : nearbyPlayers) {
                        playerToScare.playSound(playerToScare.getLocation(), Sound.ENTITY_WOLF_GROWL, SoundCategory.HOSTILE, 10, 1);
                    }
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> doThylaPounceCalculation(thyla, player), THYLA_GROWL_LENGTH);
                    return;
                }
            }
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> doCheckSight(thyla), CHECK_SIGHT_COOLDOWN);

    }

    private static void doThylaPounceCalculation(Mob thyla, Player player) {
        Location thylaLoc = thyla.getEyeLocation();
        Location playerLoc = player.getEyeLocation();
        double x = playerLoc.getX() - thylaLoc.getX();
        double y = playerLoc.getY() - thylaLoc.getY();
        double z = playerLoc.getZ() - thylaLoc.getZ();
        double magnitude = Math.sqrt(x * x + y * y + z * z);
        x /= magnitude;
        y /= magnitude;
        z /= magnitude;
        Vector newVelocity = new Vector(x, y, z).multiply(pounceSpeed);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> doThylaPounce(thyla, player, newVelocity, (long) (magnitude / pounceSpeed)), THYLA_POUNCE_WAIT);
    }

    private static void doThylaPounce(Mob thyla, Player player, Vector newVelocity, long timeToSlow) {
        thyla.setAI(true);
        thyla.setAware(true);
        trimThylasWasHit();
        synchronized (thylaWasHitSync) {
            Long timeSinceHit = thylaWashit.getOrDefault(thyla.getUniqueId(), (long) -1);
            if (!timeSinceHit.equals((long) -1)) {
                thylaWashit.remove(thyla.getUniqueId());
                slowThyla(thyla, player);
                normalizeThyla(thyla);
                return;
            }
        }
        thyla.setVelocity(newVelocity);
        thyla.setGravity(false);
        thyla.setInvulnerable(true);

        for (int i = 1; i < timeToSlow; i += 2)
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> thyla.setVelocity(newVelocity), i);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> isThylaHit(thyla, player, 10), Math.max(0, timeToSlow - 3));
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> slowThyla(thyla, player), timeToSlow + 2);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> normalizeThyla(thyla), timeToSlow + 7);
    }

    private static void trimThylasWasHit() {
        synchronized (thylaWasHitSync) {
            long currentTime = System.currentTimeMillis();
            List<UUID> toRemove = new LinkedList<>();
            for (UUID uid : thylaWashit.keySet()) {
                Long timeSinceLastHit = thylaWashit.get(uid);
                if (currentTime - timeSinceLastHit > 10000) {
                    toRemove.add(uid);
                }
            }
            for (UUID uid : toRemove)
                thylaWashit.remove(uid);
        }
    }

    private static void isThylaHit(Mob thyla, Player player, int timesToCheck) {
        if (isLocationsClose(thyla.getLocation(), player.getEyeLocation(), STUN_DISTANCE) || isLocationsClose(thyla.getLocation(), player.getLocation(), STUN_DISTANCE)) {
            thylaStunPlayer(player);
        } else if (timesToCheck != 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> isThylaHit(thyla, player, timesToCheck - 1), 1);
        }
    }

    private static boolean isLocationsClose(Location loc1, Location loc2, double distance) {
        double x = loc1.getX() - loc2.getX();
        double y = loc1.getY() - loc2.getY();
        double z = loc1.getZ() - loc2.getZ();
        double magnitude = Math.sqrt(x * x + y * y + z * z);
        return magnitude < distance;
    }

    private static void thylaStunPlayer(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 10));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 200, 10));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 3));
    }

    private static void slowThyla(Mob thyla, Player player) {
        thyla.setTarget(player);
        thyla.setGravity(true);
        double divisor = pounceSpeed * 2;
        thyla.setVelocity(thyla.getVelocity().divide(new Vector(divisor, divisor, divisor)));
        World world = thyla.getWorld();
        Location soundLoc = thyla.getLocation();
        world.playSound(soundLoc, Sound.ENTITY_WOLF_AMBIENT, SoundCategory.HOSTILE, 10, (float) 0.5);
        world.playSound(soundLoc, Sound.ENTITY_PLAYER_BIG_FALL, SoundCategory.HOSTILE, 10, (float) 0.1);
    }

    private static void normalizeThyla(@NotNull Mob thyla) {
        thyla.setInvulnerable(false);
    }


    private static boolean thylaCanSee(@NotNull Mob thyla, @NotNull Player player) {
        BoundingBox hitBox = thyla.getBoundingBox();
        List<Vector> corners = BoundingBoxUtils.getCorners(hitBox);
        Location eye = player.getEyeLocation();
        World world = eye.getWorld();
        if (world == null) return false;
        Vector toLoc = new Vector(eye.getX(), eye.getY(), eye.getZ());
        for (Vector corner : corners) {
            if (rayTrace(world, toLoc, corner))
                return false;
        }
        return true;
    }

    private static boolean rayTrace(@NotNull World world, @NotNull Vector spot1, @NotNull Vector spot2) {
        // look at spot1 from spot2
        double x = spot1.getX() - spot2.getX();
        double y = spot1.getY() - spot2.getY();
        double z = spot1.getZ() - spot2.getZ();
        double magnitude = Math.sqrt(x * x + y * y + z * z);
        x /= magnitude;
        y /= magnitude;
        z /= magnitude;
        int checks = 5;
        x /= checks;
        y /= checks;
        z /= checks;
        Vector direction = new Vector(x, y, z);
        int totalChecks = checks * 3;
        for (int i = 0; i < totalChecks; i++) {
            spot2 = spot2.add(direction);
            if (world.getBlockAt(spot2.getBlockX(), spot2.getBlockY(), spot2.getBlockZ()).getBlockData().getMaterial().isOccluding()) {
                return true;
            }
        }
        return false;
    }

    public static void countHit(UUID uniqueId, long currentTimeMillis) {
        synchronized (thylaWasHitSync) {
            thylaWashit.put(uniqueId, currentTimeMillis);
        }
    }
}

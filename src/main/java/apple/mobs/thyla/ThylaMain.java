package apple.mobs.thyla;

import org.bukkit.plugin.java.JavaPlugin;

public class ThylaMain {
    public static void enable(JavaPlugin plugin) {
        ThylaAI.initialize(plugin);
        new ThylaSpawnOrHitListener(plugin);
    }
}

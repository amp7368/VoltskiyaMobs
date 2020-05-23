package apple.mobs.utils;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class BoundingBoxUtils { @NotNull
public static List<Vector> getCorners(BoundingBox hitBox) {
    List<Vector> corners = new LinkedList<>();
    double minX = hitBox.getMinX();
    double minY = hitBox.getMinY();
    double minZ = hitBox.getMinZ();
    double maxX = hitBox.getMaxX();
    double maxY = hitBox.getMaxY();
    double maxZ = hitBox.getMaxZ();
    corners.add(new Vector(minX, minY, minZ));
    corners.add(new Vector(minX, minY, maxZ));
    corners.add(new Vector(minX, maxY, minZ));
    corners.add(new Vector(minX, maxY, maxZ));
    corners.add(new Vector(maxX, minY, minZ));
    corners.add(new Vector(maxX, minY, maxZ));
    corners.add(new Vector(maxX, maxY, minZ));
    corners.add(new Vector(maxX, maxY, maxZ));
    return corners;
}

}

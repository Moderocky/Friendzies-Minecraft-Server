package mx.kenzie.survival.pipe;

import io.papermc.paper.math.BlockPosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Marker;
import org.bukkit.util.BoundingBox;

import java.util.EnumSet;
import java.util.Set;

public class PipeManager {
    public static final double MAX_PIPE_DISTANCE = 64;

    private final Set<Material> PIPE_PASSABLE = EnumSet.of(
            Material.COPPER_GRATE, Material.EXPOSED_COPPER_GRATE, Material.WEATHERED_COPPER_GRATE, Material.OXIDIZED_COPPER_GRATE,
            Material.WAXED_COPPER_GRATE, Material.WAXED_EXPOSED_COPPER_GRATE, Material.WAXED_WEATHERED_COPPER_GRATE, Material.WAXED_OXIDIZED_COPPER_GRATE
    );

    public void setup() {
    }

    public void loadPipe(Marker marker) {

    }

    public boolean isEmptyNode(World world, io.papermc.paper.math.Position position) {
        Block blockAt = world.getBlockAt(position.blockX(), position.blockY(), position.blockZ());
        return blockAt.isEmpty() || blockAt.isPassable() || PIPE_PASSABLE.contains(blockAt.getType());
    }

    public record Position(int blockX, int blockY,
                           int blockZ) implements io.papermc.paper.math.Position, BlockPosition {

        @Override
        public Position offset(int x, int y, int z) {
            return new Position(blockX + x, blockY + y, blockZ + z);
        }

        public Location getLocation(World world) {
            return new Location(world, blockX, blockY, blockZ);
        }

        public Block getBlock(World world) {
            return world.getBlockAt(blockX, blockY, blockZ);
        }
    }

    public class Graph {

        final World world;
        final BoundingBox box;

        public Graph(World world, BoundingBox box) {
            this.world = world;
            this.box = box;
        }

        class Node {
            Position pos;
            float distance = Float.MAX_VALUE;
        }

    }
}

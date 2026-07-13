package mx.kenzie.survival.geyser;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class GeyserInternal {

    static int getUnobstructedBlockCount(Block source, BlockFace facing, int length) {
        Block current = source;
        for (int i = 0; i < length; ++i) {
            current = current.getRelative(facing);
            if (!isGeyserPassableBlock(current)) return i;
        }
        return length;
    }


    static boolean isGeyserPassableBlock(Block block) {
        Material type = block.getType();
        return block.isPassable()
                || type.isAir() || !block.isCollidable()
                || Tag.TRAPDOORS.isTagged(type)
                || type == Material.IRON_BARS
                || type == Material.COPPER_GRATE || type == Material.WAXED_COPPER_GRATE
                || type == Material.EXPOSED_COPPER_GRATE || type == Material.WAXED_EXPOSED_COPPER_GRATE
                || type == Material.WEATHERED_COPPER_GRATE || type == Material.WAXED_WEATHERED_COPPER_GRATE
                || type == Material.OXIDIZED_COPPER_GRATE || type == Material.WAXED_OXIDIZED_COPPER_GRATE;
    }


}

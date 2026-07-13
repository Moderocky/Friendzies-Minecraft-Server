package mx.kenzie.survival.builder.action;

import mx.kenzie.survival.builder.BuildingInventory;
import mx.kenzie.survival.builder.task.BuildTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class StripAction extends SetAction {

    private static final String WAXED_PREFIX = "WAXED_";
    private static final String OXIDIZED_PREFIX = "OXIDIZED_";
    private static final String WEATHERED_PREFIX = "WEATHERED_";
    private static final String EXPOSED_PREFIX = "EXPOSED_";
    private static final String STRIPPED_PREFIX = "STRIPPED_";
    private static final Map<Material, Material> known = new HashMap<>();
    protected final Set<BlockData> data;
    public StripAction(Set<BlockData> data) {
        this.data = data;
    }

    public static boolean isStrippable(Block block) {
        Material type = block.getType();
        return getStripped(type) != null;
    }

    public static Material getStripped(Material material) {
        if (known.containsKey(material))
            return known.get(material);
        Material result = getStripped0(material);
        known.put(material, result);
        return result;
    }

    private static Material getStripped0(Material material) {
        String name = material.name();
        try {
            if (name.startsWith(WAXED_PREFIX))
                return Material.valueOf(name.substring(WAXED_PREFIX.length()));
            if (name.startsWith(OXIDIZED_PREFIX))
                return Material.valueOf(WEATHERED_PREFIX + name.substring(OXIDIZED_PREFIX.length()));
            if (name.startsWith(WEATHERED_PREFIX))
                return Material.valueOf(EXPOSED_PREFIX + name.substring(WEATHERED_PREFIX.length()));
            if (name.startsWith(EXPOSED_PREFIX))
                return Material.valueOf(name.substring(EXPOSED_PREFIX.length()));
            return Material.valueOf(STRIPPED_PREFIX + name);
        } catch (IllegalArgumentException _) {
        }
        return null;
    }

    private static boolean exists(String name) {
        try {
            Material.valueOf(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public BuildTask makeTask(Player player, BuildingInventory resources, BoundingBox box) {
        return new StripTask(player, box, resources);
    }

    class StripTask extends BreakTask {


        public StripTask(Player player, BoundingBox box, BuildingInventory inventory) {
            super(player, box, inventory);
        }

        @Override
        public Component name() {
            return Component.text("Replacing");
        }

        @Override
        protected boolean okay(Block block) {
            return !block.isEmpty() && data.contains(block.getBlockData());
        }

        @Override
        protected boolean doBreak(Block block, ItemStack tool) {
            Material material = getStripped(block.getType());
            if (material == null) return true;
            block.setBlockData(material.createBlockData(block.getBlockData()::copyTo), true);
            return true;
        }

        @Override
        protected int isCorrect(Block thing, ItemStack tool) {
            return Tag.ITEMS_AXES.isTagged(tool.getType()) ? 1 : -1;
        }
    }

}

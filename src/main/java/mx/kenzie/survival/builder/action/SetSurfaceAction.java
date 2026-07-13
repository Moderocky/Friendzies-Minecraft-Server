package mx.kenzie.survival.builder.action;

import mx.kenzie.survival.builder.BuildingInventory;
import mx.kenzie.survival.builder.task.BuildTask;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class SetSurfaceAction extends SetAction {

    @Override
    public BuildTask makeTask(Player player, BuildingInventory resources, BoundingBox box) {
        return new ReplaceSurfaceTask(player, box, resources);
    }

    static class ReplaceSurfaceTask extends SetTask {

        public ReplaceSurfaceTask(Player player, BoundingBox box, BuildingInventory inventory) {
            super(player, box, inventory);
        }

        @Override
        public Component name() {
            return Component.text("Replacing Surface");
        }

        @Override
        protected boolean okay(Block block) {
            return block.isSolid() && !block.getRelative(BlockFace.UP).isSolid();
        }

    }
}

package mx.kenzie.survival.builder.action;

import mx.kenzie.survival.builder.BuildingInventory;
import mx.kenzie.survival.builder.task.BuildTask;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.Set;

public class BreakSelectiveAction implements BoxAction {

    protected final Set<BlockData> data;

    public BreakSelectiveAction(Set<BlockData> data) {
        this.data = data;
    }

    @Override
    public BuildTask makeTask(Player player, BuildingInventory resources, BoundingBox box) {
        return new SelectiveBreakTask(player, box, resources);
    }

    class SelectiveBreakTask extends BreakAction.BreakTask {

        public SelectiveBreakTask(Player player, BoundingBox box, BuildingInventory inventory) {
            super(player, box, inventory);
        }

        @Override
        public Component name() {
            return Component.text("Breaking Selectively");
        }

        @Override
        protected boolean okay(Block block) {
            return !block.isEmpty() && data.contains(block.getBlockData());
        }

    }
}

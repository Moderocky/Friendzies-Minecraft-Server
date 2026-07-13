package mx.kenzie.survival.builder.action;

import mx.kenzie.survival.builder.BuildingInventory;
import mx.kenzie.survival.builder.task.BuildTask;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.Set;

public class ReplaceAction extends SetAction {

    protected final Set<BlockData> data;

    public ReplaceAction(Set<BlockData> data) {
        this.data = data;
    }

    @Override
    public BuildTask makeTask(Player player, BuildingInventory resources, BoundingBox box) {
        return new ReplaceTask(player, box, resources);
    }

    class ReplaceTask extends SetTask {


        public ReplaceTask(Player player, BoundingBox box, BuildingInventory inventory) {
            super(player, box, inventory);
        }

        @Override
        public Component name() {
            return Component.text("Replacing");
        }

        @Override
        protected BlockData prepareData(Step step, Block existing) {
            BlockData data = step.data().clone();
            existing.getBlockData().copyTo(data);
            return data;
        }

        @Override
        protected boolean okay(Block block) {
            return !block.isEmpty() && data.contains(block.getBlockData());
        }

    }
}

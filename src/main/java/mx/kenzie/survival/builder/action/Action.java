package mx.kenzie.survival.builder.action;

import mx.kenzie.survival.builder.BuildingInventory;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

@FunctionalInterface
public interface Action {
    Action FILL = new FillAction(),
            BREAK = new BreakAction(),
            SET = new SetAction(),
            TRANSFER = new TransferAction(),
            SORT = new SortAction(),
            SINGLE_SORT = new SingleSortAction(),
            SET_SURFACE = new SetSurfaceAction(),
            SAVE = new SaveAction(),
            LOAD = new LoadAction(),
            MARK_STORAGE = new StorageAction()
//            , EDIT_FILTER = new EditFilterAction()
                    ;

    void run(Player player, BuildingInventory resources, int[][] position);

    default BoundingBox box(int[][] positions) {
        return new BoundingBox(
                positions[0][0], positions[0][1], positions[0][2],
                positions[1][0], positions[1][1], positions[1][2]
        );
    }

}

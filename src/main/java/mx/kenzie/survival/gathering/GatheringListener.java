package mx.kenzie.survival.gathering;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GatheringListener implements Listener {

    @EventHandler
    public void entityAdd(EntityAddToWorldEvent event) {
        if (event.getEntityType() != EntityType.ALLAY) {
        }

        // todo goals
    }

}

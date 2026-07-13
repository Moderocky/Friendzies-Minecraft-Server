package mx.kenzie.survival.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EndermanListener implements Listener {

    @EventHandler
    public void event(EntityChangeBlockEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntityType() != EntityType.ENDERMAN) return;
        event.setCancelled(true);
    }

}

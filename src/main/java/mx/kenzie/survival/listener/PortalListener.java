package mx.kenzie.survival.listener;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalExitEvent;

import java.util.Map;
import java.util.WeakHashMap;

public class PortalListener implements Listener {

    public static Map<Entity, Location> locations = new WeakHashMap<>();


    @EventHandler
    public void event(EntityPortalExitEvent event) {
        if (event.isCancelled()) return;
        locations.put(event.getEntity(), event.getTo());
    }

}

package mx.kenzie.survival.pipe;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import org.bukkit.entity.Marker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PipeListener implements Listener {

    final PipeManager manager;

    public PipeListener(PipeManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void loadPipe(EntityAddToWorldEvent event) {
        if (!(event.getEntity() instanceof Marker marker)) return;
        manager.loadPipe(marker);
    }

}

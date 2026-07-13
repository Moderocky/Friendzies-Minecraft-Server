package mx.kenzie.survival.listener;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static net.kyori.adventure.text.Component.translatable;

public class PingListener implements Listener {

    @EventHandler
    public void event(PaperServerListPingEvent event) {
        event.motd(translatable("merchant.deprecated").hoverEvent(translatable("multiplayerWarning.check")));
        event.setMaxPlayers(-6);
    }

}

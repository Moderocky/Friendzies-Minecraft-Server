package mx.kenzie.survival.utility.pack;

import io.papermc.paper.adventure.PaperAdventure;
import mx.kenzie.survival.Survival;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;
import java.util.UUID;

public class Resources implements Listener {
    private static final String ADDRESS = "server.kenzie.mx";

    public static volatile String hash;

    protected final UUID packId = UUID.fromString("2d15c1c4-e808-4e91-80ad-b86b8c0e5326");

    protected final Component prompt = Component.text("Fix for shulker boxes breaking minecraft :(", NamedTextColor.LIGHT_PURPLE)
            .hoverEvent(Component.text("It's a surprise tool that will help us later"));

    public void setup() {
        ResourcePackMaker.generateResourcePack();
        Bukkit.getPluginManager().registerEvents(this, Survival.plugin);
    }

    private String url(String urlBase) {
        return "http://" + urlBase + ":" + Survival.webServer.port + "/pack";
    }

    public void dispatch(Player player, String urlBase) {
        ServerPlayer handle = ((CraftPlayer) player).getHandle();
        Optional<net.minecraft.network.chat.Component> vanilla = Optional.of(PaperAdventure.asVanilla(prompt));
        handle.connection.send(new ClientboundResourcePackPushPacket(packId, this.url(urlBase), hash, false, vanilla));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String hostName = player.getAddress().getHostName();
        if (hostName.equals("127.0.0.1") || hostName.equals("localhost"))
            this.dispatch(player, "127.0.0.1");
        else this.dispatch(player, ADDRESS);
    }


}

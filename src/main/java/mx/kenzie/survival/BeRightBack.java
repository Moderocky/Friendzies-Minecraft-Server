package mx.kenzie.survival;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public class BeRightBack implements Listener {
    private static final NamespacedKey BRB = NamespacedKey.minecraft("brb");

    public static void register() {
        Bukkit.getPluginManager().registerEvents(new BeRightBack(), Survival.plugin);
    }

    private static Component time(long millis) {
        long seconds = millis / 1000, minutes = 0, hours = 0, days = 0;
        while (seconds >= 60) {
            seconds -= 60;
            ++minutes;
        }
        while (minutes >= 60) {
            minutes -= 60;
            ++hours;
        }
        while (hours >= 24) {
            hours -= 24;
            ++days;
        }
        List<Component> components = new ArrayList<>();
        if (days > 0) components.add(text(days + " days", NamedTextColor.WHITE));
        if (hours > 0) components.add(text(hours + " hours", NamedTextColor.WHITE));
        if (minutes > 0) components.add(text(minutes + " minutes", NamedTextColor.WHITE));
        if (seconds > 0) components.add(text(seconds + " seconds", NamedTextColor.WHITE));
        return Component.join(JoinConfiguration.separators(text(", "), text(", and ")), components);

    }

    public static void startTimer(Player player) {

        player.getPersistentDataContainer().set(BRB, PersistentDataType.LONG, System.currentTimeMillis());
        Survival.plugin.getLogger().info("Started a 'brb' timer for " + player.getName());
    }

    public static void clearTimer(Player player) {

        PersistentDataContainer dataContainer = player.getPersistentDataContainer();
        if (dataContainer.has(BRB, PersistentDataType.LONG))
            Survival.plugin.getLogger().info("Cleared a 'brb' timer for " + player.getName());
        dataContainer.remove(BRB);


    }

    public static long timeSinceBrb(OfflinePlayer player) {
        PersistentDataContainerView container = player.getPersistentDataContainer();
        if (!container.has(BRB, PersistentDataType.LONG)) return -1;
        return container.getOrDefault(BRB, PersistentDataType.LONG, -1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        long l1 = timeSinceBrb(player);
        if (l1 > -1) {
            final long l = System.currentTimeMillis() - l1;
            if (l > -1) {
                Bukkit.broadcast(textOfChildren(
                        player.displayName(),
                        text(" took ", NamedTextColor.YELLOW),
                        time(l),
                        text(" to \"", NamedTextColor.YELLOW),
                        text("be right back", NamedTextColor.WHITE).decorate(TextDecoration.ITALIC),
                        text("\" !!!", NamedTextColor.YELLOW)
                ));
            }
        }
        clearTimer(player);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String serialize = PlainTextComponentSerializer.plainText().serialize(event.message());
        if (serialize.equalsIgnoreCase("brb") || serialize.equalsIgnoreCase("be right back")) {
            startTimer(player);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (timeSinceBrb(player) + (60 * 1000) < System.currentTimeMillis())
            clearTimer(player);
    }


}

package mx.kenzie.survival.command;

import mx.kenzie.centurion.*;
import mx.kenzie.survival.listener.WaypointListener;
import mx.kenzie.survival.utility.DefaultMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static net.kyori.adventure.text.Component.text;

public class CompassCommand extends MinecraftCommand {
    private static final TypedArgument<NamespacedKey> ICON = MinecraftCommand.KEY
            .labelled("icon")
            .described("The icon key.")
            .possible(WaypointListener.possibleIcons()), ICON_OPTIONAL = ICON.asOptional();

    private final Map<Player, List<WaypointListener.Waypoint>> waypoints = new DefaultMap<Player, List<WaypointListener.Waypoint>>(WeakHashMap::new, _ -> new ArrayList<>());

    public CompassCommand() {
        super("Mark temporary waypoints.");
    }

    @Override
    public MinecraftBehaviour create() {
        return this.command("compass")
                .arg("icon", ICON, this::changeIcon)
                .arg("waypoint", OFFSET.labelled("position").described("The world-relative position to mark."), ICON_OPTIONAL, this::addWaypoint)
                .arg("clear", this::clear);
    }

    private Result changeIcon(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player from)) return CommandResult.LAPSE;
        NamespacedKey key = arguments.get(ICON);
        WaypointListener.setWaypointIcon(from, key);
        final ColorProfile profile = this.getProfile();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Changed your waypoint icon.", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    private Result clear(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player from)) return CommandResult.LAPSE;
        for (WaypointListener.Waypoint waypoint : waypoints.get(from)) {
            waypoint.hideFrom(from);
        }
        waypoints.remove(from);
        final ColorProfile profile = this.getProfile();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Cleared your waypoints.", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    protected CommandResult addWaypoint(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player from)) return CommandResult.LAPSE;
        final Location to = arguments.<RelativeVector>get(0).relativeTo(from);
        NamespacedKey key = arguments.get(ICON_OPTIONAL);
        List<WaypointListener.Waypoint> list = waypoints.get(from);
        WaypointListener.Waypoint waypoint;
        if (key == null) waypoint = new WaypointListener.Waypoint(WaypointListener.BOWTIE, to);
        else waypoint = new WaypointListener.Waypoint(key, to);
        list.add(waypoint);
        waypoints.put(from, list);
        waypoint.revealTo(from);
        final ColorProfile profile = this.getProfile();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Added new waypoint at ", profile.dark()),
                WhereCommand.print(to, profile)
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

}

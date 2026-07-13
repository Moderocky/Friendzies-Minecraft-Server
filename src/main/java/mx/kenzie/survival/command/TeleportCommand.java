package mx.kenzie.survival.command;

import mx.kenzie.centurion.*;
import mx.kenzie.centurion.selector.Selector;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.Component.text;

public class TeleportCommand extends MinecraftCommand {

    private static final TypedArgument<Selector<Entity>> FROM = SELECTOR.described("The entity to teleport.");

    public TeleportCommand() {
        super("Teleport something to somewhere.");
    }

    @Override
    public MinecraftBehaviour create() {
        return this.command("teleport")
                .permission("survival.command.teleport")
                .arg(FROM, "to", SELECTOR.described("The entity to teleport to."), this::playerToPlayer)
                .arg(FROM, "to", OFFSET.labelled("position").described("The world-relative position to teleport to."), this::playerToOffset)
                .arg(FROM, "to", LOCAL_OFFSET.labelled("orientation").described("The orientation-relative offset to teleport to."), this::playerToOffset)
                .arg(FROM, "to", LOCATION.described("A location in a world."), this::playerToLocation)
                .arg("to", SELECTOR.described("The entity to teleport to."), this::toPlayer)
                .arg("to", OFFSET.labelled("position").described("The world-relative position to teleport to."), this::toOffset)
                .arg("to", LOCAL_OFFSET.labelled("orientation").described("The orientation-relative offset to teleport to."), this::toOffset)
                .arg("to", LOCATION.described("A location in a world."), this::toLocation);
    }

    protected CommandResult toPlayer(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player from)) return CommandResult.LAPSE;
        final Entity to = arguments.<Selector<Entity>>get(0).getOne(sender);
        if (to == null) return CommandResult.LAPSE;
        from.teleport(to);
        final ColorProfile profile = this.getProfile();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Teleported to ", profile.dark()),
                to.name().color(profile.highlight()),
                text(".", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    protected CommandResult toLocation(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player from)) return CommandResult.LAPSE;
        final Location to = arguments.get(0);
        from.teleport(to);
        final ColorProfile profile = this.getProfile();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Teleported to ", profile.dark()),
                WhereCommand.print(to, profile)
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    protected CommandResult toOffset(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player from)) return CommandResult.LAPSE;
        final Location to = arguments.<RelativeVector>get(0).relativeTo(from);
        from.teleport(to);
        final ColorProfile profile = this.getProfile();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Teleported to ", profile.dark()),
                WhereCommand.print(to, profile)
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    protected CommandResult playerToLocation(CommandSender sender, Arguments arguments) {
        final Player from = arguments.get(0);
        final Location to = arguments.get(1);
        from.teleport(to);
        final ColorProfile profile = this.getProfile();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Teleported ", profile.dark()),
                from.displayName().color(profile.highlight()),
                text(" to ", profile.dark()),
                WhereCommand.print(to, profile)
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    protected CommandResult playerToOffset(CommandSender sender, Arguments arguments) {
        final Player from = arguments.get(0);
        final Location to = arguments.<RelativeVector>get(1).relativeTo(from);
        from.teleport(to);
        final ColorProfile profile = this.getProfile();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Teleported ", profile.dark()),
                from.displayName().color(profile.highlight()),
                text(" to ", profile.dark()),
                WhereCommand.print(to, profile)
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    protected CommandResult playerToPlayer(CommandSender sender, Arguments arguments) {
        final Player from = arguments.get(0);
        final Entity to = arguments.<Selector<Entity>>get(1).getOne(sender);
        if (to == null) return CommandResult.LAPSE;
        from.teleport(to);
        final ColorProfile profile = this.getProfile();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Teleported ", profile.dark()),
                from.displayName().color(profile.highlight()),
                text(" to ", profile.dark()),
                to.name().color(profile.highlight()),
                text(".", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

}

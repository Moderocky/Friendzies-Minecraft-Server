package mx.kenzie.survival.command;

import mx.kenzie.centurion.Arguments;
import mx.kenzie.centurion.ColorProfile;
import mx.kenzie.centurion.CommandResult;
import mx.kenzie.centurion.MinecraftCommand;
import mx.kenzie.centurion.selector.Selector;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.action.FindAction;
import mx.kenzie.survival.listener.PortalListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import static net.kyori.adventure.text.Component.text;

public class WhereCommand extends MinecraftCommand {
    public WhereCommand() {
        super("Find something.");
    }

    public static Component print(Location location, ColorProfile profile) {
        return print(location.toVector(), profile);
    }

    public static Component print(Vector vector, ColorProfile profile) {
        return Component.textOfChildren(Component.text('(', profile.pop()),
                Component.text(vector.getBlockX(), profile.highlight()),
                Component.text(", ", profile.pop()),
                Component.text(vector.getBlockY(), profile.highlight()),
                Component.text(", ", profile.pop()),
                Component.text(vector.getBlockZ(), profile.highlight()),
                Component.text(')', profile.pop()));
    }

    @Override
    public MinecraftBehaviour create() {
        return this.command("where")
                .arg("did", PLAYER.described("The player to find."), "die", this::findDeath)
                .arg("is", SELECTOR.described("The entity to find."), this::findEntity)
                .arg("is", LOCATION.described("The location to find."), this::findLocation)
                .arg("is", "bed", this::findBed)
                .arg("is", "spawn", this::findSpawn)
                .arg("is", "portal", this::findPortal)
                .arg("is", "my", MATERIAL.described("The material to search for."), this::findMaterial);
    }

    protected CommandResult findMaterial(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player player)) return CommandResult.LAPSE;
        final Material material = arguments.get(0);
        final BoundingBox box = player.getBoundingBox().expand(25, 15, 25);
        final ColorProfile profile = this.getProfile();
        final FindAction.FindTask task = new FindAction(material).new FindTask(box, player) {
            @Override
            public void alert(Block block) {
                super.alert(block);
                final BlockVector vector = block.getLocation().toVector().toBlockVector();
                final World world = block.getWorld();
                //<editor-fold desc="Message" defaultstate="collapsed">
                sender.sendMessage(Component.textOfChildren(
                        text("This location is at ", profile.dark()),
                        print(vector, profile)
                ));
                //</editor-fold>
                if (player.getWorld() != world) return;
                final Vector start = player.getLocation().toVector();
                final int distance = (int) (start.distance(vector) + 1);
                //<editor-fold desc="Message" defaultstate="collapsed">
                sender.sendMessage(Component.textOfChildren(
                        text(distance, profile.highlight()),
                        Component.text('m', profile.pop()),
                        text(" away from you.", profile.dark())
                ));
                //</editor-fold>
            }
        };
        Survival.builder.runTask(task);
        return CommandResult.PASSED;
    }

    protected CommandResult findPortal(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player player)) return CommandResult.LAPSE;
        final Location portal = PortalListener.locations.get(player);
        final ColorProfile profile = this.getProfile();
        if (portal == null) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                    text("You haven't been through a portal today.", profile.dark())
            ));
            //</editor-fold>
            return CommandResult.PASSED;
        }
        if (portal.getWorld() != player.getWorld()) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                    text("You haven't been through a portal in this world.", profile.dark())
            ));
            //</editor-fold>
            return CommandResult.PASSED;
        }
        final BlockVector vector = portal.toVector().toBlockVector();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Your portal is at ", profile.dark()),
                print(vector, profile)
        ));
        //</editor-fold>
        final Vector start = player.getLocation().toVector();
        final int distance = (int) (start.distance(vector) + 1);
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text(distance, profile.highlight()),
                Component.text('m', profile.pop()),
                text(" away from you.", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    protected CommandResult findLocation(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player player)) return CommandResult.LAPSE;
        final Location location = arguments.get(0);
        final ColorProfile profile = this.getProfile();
        final BlockVector vector = location.toVector().toBlockVector();
        final World world = location.getWorld();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("This location is at ", profile.dark()),
                print(vector, profile)
        ));
        //</editor-fold>
        if (player.getWorld() != world) return CommandResult.PASSED;
        final Vector start = player.getLocation().toVector();
        final int distance = (int) (start.distance(vector) + 1);
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text(distance, profile.highlight()),
                Component.text('m', profile.pop()),
                text(" away from you.", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    protected CommandResult findSpawn(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player player)) return CommandResult.LAPSE;
        final Location spawn = player.getWorld().getSpawnLocation();
        final ColorProfile profile = this.getProfile();
        final BlockVector vector = spawn.toVector().toBlockVector();
        final World world = spawn.getWorld();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Spawn is at ", profile.dark()),
                print(vector, profile)
        ));
        //</editor-fold>
        if (player.getWorld() != world) return CommandResult.PASSED;
        final Vector start = player.getLocation().toVector();
        final int distance = (int) (start.distance(vector) + 1);
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text(distance, profile.highlight()),
                Component.text('m', profile.pop()),
                text(" away from you.", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    protected CommandResult findEntity(CommandSender sender, Arguments arguments) {
        final Selector<Entity> selector = arguments.get(0);
        for (Entity player : selector.getAll(sender)) {
            final BlockVector vector = player.getLocation().toVector().toBlockVector();
            final World world = player.getWorld();
            final ColorProfile profile = this.getProfile();
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                    player.name().color(profile.highlight()),
                    text(" is in ", profile.dark()),
                    text(world.getName(), profile.highlight()),
                    text(" at ", profile.dark()),
                    print(vector, profile)
            ));
            //</editor-fold>
            if (!(sender instanceof LivingEntity entity) || entity.getWorld() != world) return CommandResult.PASSED;
            final Vector start = entity.getLocation().toVector();
            final int distance = (int) (start.distance(vector) + 1);
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                    text(distance, profile.highlight()),
                    Component.text('m', profile.pop()),
                    text(" away from you.", profile.dark())
            ));
            //</editor-fold>
        }
        return CommandResult.PASSED;
    }

    protected CommandResult findBed(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player player)) return CommandResult.LAPSE;
        final Location bed = player.getPotentialBedLocation();
        final ColorProfile profile = this.getProfile();
        if (bed == null) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                    text("You have no bed.", profile.dark())
            ));
            //</editor-fold>
            return CommandResult.PASSED;
        }
        final BlockVector vector = bed.toVector().toBlockVector();
        final World world = bed.getWorld();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text("Your bed is in ", profile.dark()),
                text(world.getName(), profile.highlight()),
                text(" at ", profile.dark()),
                print(vector, profile)
        ));
        //</editor-fold>
        if (player.getWorld() != world) return CommandResult.PASSED;
        final Vector start = player.getLocation().toVector();
        final int distance = (int) (start.distance(vector) + 1);
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text(distance, profile.highlight()),
                Component.text('m', profile.pop()),
                text(" away from you.", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    protected CommandResult findDeath(CommandSender sender, Arguments arguments) {
        final Player player = arguments.get(0);
        final Location die = player.getLastDeathLocation();
        final ColorProfile profile = this.getProfile();
        if (die == null) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                    player.displayName().color(profile.highlight()),
                    text(" has not died.", profile.dark())
            ));
            //</editor-fold>
            return CommandResult.PASSED;
        }
        final BlockVector vector = die.toVector().toBlockVector();
        final World world = die.getWorld();
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                player.displayName().color(profile.highlight()),
                text(" died in ", profile.dark()),
                text(world.getName(), profile.highlight()),
                text(" at ", profile.dark()),
                print(vector, profile)
        ));
        //</editor-fold>
        if (!(sender instanceof LivingEntity entity) || entity.getWorld() != world) return CommandResult.PASSED;
        if (entity.getWorld() != world) return CommandResult.PASSED;
        final Vector start = entity.getLocation().toVector();
        final int distance = (int) (start.distance(vector) + 1);
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
                text(distance, profile.highlight()),
                Component.text('m', profile.pop()),
                text(" away from you.", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

}

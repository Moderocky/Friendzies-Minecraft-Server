package mx.kenzie.survival.builder.command;

import mx.kenzie.centurion.ColorProfile;
import mx.kenzie.centurion.MinecraftCommand;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.action.SaveAction;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static mx.kenzie.centurion.Arguments.INTEGER;
import static mx.kenzie.centurion.CommandResult.LAPSE;
import static mx.kenzie.centurion.CommandResult.PASSED;

public class WandCommand extends MinecraftCommand {
    protected static final StructureArgument STRUCTURE = new StructureArgument();

    public WandCommand() {
        super("Spawn a wand.");
    }

    @Override
    public MinecraftBehaviour create() {
        final ColorProfile profile = this.getProfile();
        return command("wand")
                .permission("survival.command.wand")
                .arg("get", (sender, arguments) -> {
                    if (!(sender instanceof Player player)) return LAPSE;
                    player.getInventory().addItem(Survival.builder.wand.clone());
                    player.sendMessage(Component.text("Spawned a wand.", profile.light()));
                    return PASSED;
                })
                .arg("selection", "expand", BLOCK_FACE.described("The direction to expand in.").labelled("direction"), INTEGER.asOptional().labelled("amount").withLapse(1), (sender, arguments) -> {
                    if (!(sender instanceof Player player)) return LAPSE;
                    final BlockFace direction = arguments.get(0);
                    final int amount = arguments.get(1);
                    final ItemStack wand = this.getWand(player);
                    if (wand == null) return LAPSE;
                    final BoundingBox box = this.getSelection(wand);
                    box.expand(direction, amount);
                    this.setSelection(wand, box);
                    final BlockVector min, max, vector = new BlockVector(box.getWidthX() + 1, box.getHeight() + 1, box.getWidthZ() + 1);
                    min = box.getMin().toBlockVector();
                    max = box.getMax().toBlockVector();
                    final Component x = Component.text("×", profile.pop());
                    //<editor-fold desc="Message" defaultstate="collapsed">
                    player.sendMessage(Component.textOfChildren(
                            Component.text("Expanded selection: ", profile.dark()),
                            Component.textOfChildren(
                                    Component.text(vector.getBlockX()), x, Component.text(vector.getBlockY()), x, Component.text(vector.getBlockZ())
                            ).color(profile.highlight()),
                            Component.newline(), Component.text("  "),
                            Component.text('#', profile.pop()),
                            Component.text("1", profile.highlight()),
                            Component.text(" = ", profile.dark()),
                            Component.text('(', profile.pop()),
                            Component.text(min.getBlockX(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(min.getBlockY(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(min.getBlockZ(), profile.highlight()),
                            Component.text(')', profile.pop()),
                            Component.newline(), Component.text("  "),
                            Component.text('#', profile.pop()),
                            Component.text("2", profile.highlight()),
                            Component.text(" = ", profile.dark()),
                            Component.text('(', profile.pop()),
                            Component.text(max.getBlockX(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(max.getBlockY(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(max.getBlockZ(), profile.highlight()),
                            Component.text(')', profile.pop())
                    ));
                    //</editor-fold>
                    return PASSED;
                })
                .arg("selection", "expand", INTEGER.asOptional().labelled("amount").withLapse(1), (sender, arguments) -> {
                    if (!(sender instanceof Player player)) return LAPSE;
                    final int amount = arguments.get(1);
                    final ItemStack wand = this.getWand(player);
                    if (wand == null) return LAPSE;
                    final BoundingBox box = this.getSelection(wand);
                    box.expand(amount);
                    this.setSelection(wand, box);
                    final BlockVector min, max, vector = new BlockVector(box.getWidthX() + 1, box.getHeight() + 1, box.getWidthZ() + 1);
                    min = box.getMin().toBlockVector();
                    max = box.getMax().toBlockVector();
                    final Component x = Component.text("×", profile.pop());
                    //<editor-fold desc="Message" defaultstate="collapsed">
                    player.sendMessage(Component.textOfChildren(
                            Component.text("Expanded selection: ", profile.dark()),
                            Component.textOfChildren(
                                    Component.text(vector.getBlockX()), x, Component.text(vector.getBlockY()), x, Component.text(vector.getBlockZ())
                            ).color(profile.highlight()),
                            Component.newline(), Component.text("  "),
                            Component.text('#', profile.pop()),
                            Component.text("1", profile.highlight()),
                            Component.text(" = ", profile.dark()),
                            Component.text('(', profile.pop()),
                            Component.text(min.getBlockX(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(min.getBlockY(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(min.getBlockZ(), profile.highlight()),
                            Component.text(')', profile.pop()),
                            Component.newline(), Component.text("  "),
                            Component.text('#', profile.pop()),
                            Component.text("2", profile.highlight()),
                            Component.text(" = ", profile.dark()),
                            Component.text('(', profile.pop()),
                            Component.text(max.getBlockX(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(max.getBlockY(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(max.getBlockZ(), profile.highlight()),
                            Component.text(')', profile.pop())
                    ));
                    //</editor-fold>
                    return PASSED;
                })
                .arg("selection", "shift", BLOCK_FACE.described("The direction to move in.").labelled("direction"), INTEGER.labelled("amount").asOptional().withLapse(1), (sender, arguments) -> {
                    if (!(sender instanceof Player player)) return LAPSE;
                    final BlockFace direction = arguments.get(0);
                    final int amount = arguments.get(1);
                    final ItemStack wand = this.getWand(player);
                    if (wand == null) return LAPSE;
                    final BoundingBox box = this.getSelection(wand);
                    box.shift(direction.getDirection().multiply(amount));
                    this.setSelection(wand, box);
                    final BlockVector min, max, vector = new BlockVector(box.getWidthX() + 1, box.getHeight() + 1, box.getWidthZ() + 1);
                    min = box.getMin().toBlockVector();
                    max = box.getMax().toBlockVector();
                    final Component x = Component.text("×", profile.pop());
                    //<editor-fold desc="Message" defaultstate="collapsed">
                    player.sendMessage(Component.textOfChildren(
                            Component.text("Shifted selection: ", profile.dark()),
                            Component.textOfChildren(
                                    Component.text(vector.getBlockX()), x, Component.text(vector.getBlockY()), x, Component.text(vector.getBlockZ())
                            ).color(profile.highlight()),
                            Component.newline(), Component.text("  "),
                            Component.text('#', profile.pop()),
                            Component.text("1", profile.highlight()),
                            Component.text(" = ", profile.dark()),
                            Component.text('(', profile.pop()),
                            Component.text(min.getBlockX(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(min.getBlockY(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(min.getBlockZ(), profile.highlight()),
                            Component.text(')', profile.pop()),
                            Component.newline(), Component.text("  "),
                            Component.text('#', profile.pop()),
                            Component.text("2", profile.highlight()),
                            Component.text(" = ", profile.dark()),
                            Component.text('(', profile.pop()),
                            Component.text(max.getBlockX(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(max.getBlockY(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(max.getBlockZ(), profile.highlight()),
                            Component.text(')', profile.pop())
                    ));
                    //</editor-fold>
                    return PASSED;
                })
                .arg("selection", "size", (sender, arguments) -> {
                    if (!(sender instanceof Player player)) return LAPSE;
                    final ItemStack wand = this.getWand(player);
                    if (wand == null) return LAPSE;
                    final BoundingBox box = this.getSelection(wand);
                    final BlockVector vector = new BlockVector(box.getWidthX() + 1, box.getHeight() + 1, box.getWidthZ() + 1);
                    final Component x = Component.text("×", profile.pop());
                    //<editor-fold desc="Message" defaultstate="collapsed">
                    player.sendMessage(Component.textOfChildren(
                            Component.text("Selection size: ", profile.dark()),
                            Component.textOfChildren(
                                    Component.text(vector.getBlockX()), x, Component.text(vector.getBlockY()), x, Component.text(vector.getBlockZ())
                            ).color(profile.highlight())));
                    //</editor-fold>
                    return PASSED;
                })
                .arg("selection", "volume", (sender, arguments) -> {
                    if (!(sender instanceof Player player)) return LAPSE;
                    final ItemStack wand = this.getWand(player);
                    if (wand == null) return LAPSE;
                    final BoundingBox box = this.getSelection(wand);
                    final BlockVector vector = new BlockVector(box.getWidthX() + 1, box.getHeight() + 1, box.getWidthZ() + 1);
                    //<editor-fold desc="Message" defaultstate="collapsed">
                    player.sendMessage(Component.textOfChildren(
                            Component.text("Selection volume: ", profile.dark()),
                            Component.text(vector.getBlockX() * vector.getBlockY() * vector.getBlockZ() + "m", profile.highlight()),
                            Component.text("²", profile.pop())));
                    //</editor-fold>
                    return PASSED;
                })
                .arg("structure", "load", STRUCTURE, (sender, arguments) -> {
                    if (!(sender instanceof Player player)) return LAPSE;
                    final Structure structure = arguments.get(0);
                    final NamespacedKey key;
                    check:
                    {
                        for (Map.Entry<NamespacedKey, Structure> entry : Bukkit.getStructureManager().getStructures().entrySet()) {
                            if (entry.getValue() != structure) continue;
                            key = entry.getKey();
                            break check;
                        }
                        return LAPSE;
                    }
                    player.getInventory().addItem(SaveAction.createBook(structure, sender, key));
                    //<editor-fold desc="Message" defaultstate="collapsed">
                    player.sendMessage(Component.text("Loaded structure to book.", profile.dark()));
                    //</editor-fold>
                    return PASSED;
                })
                .arg("structure", "paste", STRUCTURE, (sender, arguments) -> {
                    if (!(sender instanceof Player player)) return LAPSE;
                    final ItemStack wand = this.getWand(player);
                    if (wand == null) return LAPSE;
                    final BoundingBox box = this.getSelection(wand);
                    final BlockVector min = box.getMin().toBlockVector();
                    final Structure structure = arguments.get(0);
                    final Location location = box.getMin().toLocation(player.getWorld());
                    structure.place(location, false, StructureRotation.NONE, Mirror.NONE, -1, 1, ThreadLocalRandom.current());
                    //<editor-fold desc="Message" defaultstate="collapsed">
                    player.sendMessage(Component.textOfChildren(
                            Component.text("Pasted structure at ", profile.dark()),
                            Component.text('(', profile.pop()),
                            Component.text(min.getBlockX(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(min.getBlockY(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(min.getBlockZ(), profile.highlight()),
                            Component.text(')', profile.pop())
                    ));
                    //</editor-fold>
                    return PASSED;
                })
                .arg("pos", INTEGER.possible("1", "2"), (sender, arguments) -> {
                    if (!(sender instanceof Player player)) return LAPSE;
                    final int pos = arguments.get(0);
                    final BlockVector vector = player.getLocation().toVector().toBlockVector();
                    if (!this.setPos(player.getInventory().getItemInMainHand(), pos, vector)
                            && !this.setPos(player.getInventory().getItemInOffHand(), pos, vector))
                        return LAPSE;
                    //<editor-fold desc="Message" defaultstate="collapsed">
                    player.sendMessage(Component.textOfChildren(
                            Component.text("Set position ", profile.dark()),
                            Component.text('#', profile.pop()),
                            Component.text(pos, profile.highlight()),
                            Component.text(" to ", profile.dark()),
                            Component.text('(', profile.pop()),
                            Component.text(vector.getBlockX(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(vector.getBlockY(), profile.highlight()),
                            Component.text(", ", profile.pop()),
                            Component.text(vector.getBlockZ(), profile.highlight()),
                            Component.text(')', profile.pop())
                    ));
                    //</editor-fold>
                    return PASSED;
                });
    }

    protected boolean setPos(ItemStack stack, int pos, BlockVector vector) {
        if (stack == null) return false;
        if (!Survival.builder.isWand(stack)) return false;
        return switch (pos) {
            case 1 -> Survival.builder.wandAction(stack, vector, Action.LEFT_CLICK_BLOCK);
            case 2 -> Survival.builder.wandAction(stack, vector, Action.RIGHT_CLICK_BLOCK);
            default -> false;
        };
    }

    protected ItemStack getWand(Player player) {
        final ItemStack main = player.getInventory().getItemInMainHand();
        if (Survival.builder.isWand(main)) return main;
        final ItemStack off = player.getInventory().getItemInOffHand();
        if (Survival.builder.isWand(off)) return off;
        return null;
    }

    protected BoundingBox getSelection(ItemStack stack) {
        if (stack == null) return null;
        if (!Survival.builder.isWand(stack)) return null;
        final int[][] positions = Survival.builder.wandPositions(stack);
        return new BoundingBox(
                positions[0][0], positions[0][1], positions[0][2],
                positions[1][0], positions[1][1], positions[1][2]
        );
    }

    protected void setSelection(ItemStack stack, BoundingBox box) {
        final BlockVector min, max;
        min = box.getMin().toBlockVector();
        max = box.getMax().toBlockVector();
        Survival.builder.wandAction(stack, min, Action.LEFT_CLICK_BLOCK);
        Survival.builder.wandAction(stack, max, Action.RIGHT_CLICK_BLOCK);
    }

}


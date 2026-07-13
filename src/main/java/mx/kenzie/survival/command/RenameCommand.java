package mx.kenzie.survival.command;

import mx.kenzie.centurion.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import static mx.kenzie.centurion.Arguments.GREEDY_STRING;
import static net.kyori.adventure.text.Component.text;

public class RenameCommand extends MinecraftCommand {
    private static final TypedArgument<EquipmentSlot> EQUIPMENT_SLOT = new EnumArgument<>(EquipmentSlot.class).described("The item to rename.");
    private static final TypedArgument<String> NAME = GREEDY_STRING.described("The new name.").asOptional();
    private static final TypedArgument<Player> TARGET = PLAYER.described("The player to target.");

    public RenameCommand() {
        super("Rename your tool.");
    }

    @Override
    public MinecraftBehaviour create() {
        return this.command("rename")
                .permission("survival.command.rename")
                .arg("my", EQUIPMENT_SLOT, NAME, this::renameMine)
                .arg(TARGET, EQUIPMENT_SLOT, NAME, this::renameTheirs);
    }

    private CommandResult renameMine(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player player)) return CommandResult.LAPSE;
        EquipmentSlot slot = arguments.get(EQUIPMENT_SLOT);
        String name = arguments.get(NAME);
        return this.doRename(player, slot, name);
    }

    private CommandResult renameTheirs(CommandSender sender, Arguments arguments) {
        Player player = arguments.get(TARGET);
        EquipmentSlot slot = arguments.get(EQUIPMENT_SLOT);
        String name = arguments.get(NAME);
        return this.doRename(player, slot, name);
    }

    private CommandResult doRename(Player player, EquipmentSlot slot, @Nullable String name) {
        ItemStack item = player.getEquipment().getItem(slot);
        if (name == null)
            item.editMeta(meta -> meta.displayName(null));
        else
            item.editMeta(meta -> meta.displayName(text(name)));
        return CommandResult.PASSED;
    }

}

package mx.kenzie.survival.enchanting;

import mx.kenzie.centurion.MinecraftCommand;
import mx.kenzie.centurion.TypedArgument;
import mx.kenzie.survival.Survival;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import static mx.kenzie.centurion.Arguments.INTEGER;
import static mx.kenzie.centurion.CommandResult.LAPSE;
import static mx.kenzie.centurion.CommandResult.PASSED;

public class GrimoireCommand extends MinecraftCommand {
    public GrimoireCommand() {
        super("Modify your grimoire.");
        this.permission = null; // = "survival.command.grimoire";
    }

    @Override
    public MinecraftBehaviour create() {
        final EnchantmentArgument ENCHANTMENT = new EnchantmentArgument();
        TypedArgument<Enchantment> argEnchant = ENCHANTMENT.described("The enchantment to apply.");
        TypedArgument<Integer> argLevel = INTEGER.withLapse(0).asOptional().described("The enchantment level.").labelled("level");
        return command("grimoire")
                .arg("select", argEnchant, argLevel, (sender, arguments) -> {
                    if (!(sender instanceof Player player)) return LAPSE;
                    EntityEquipment equipment = player.getEquipment();
                    Enchantment enchantment = arguments.get(argEnchant);
                    int level = arguments.get(argLevel);
                    EnchantingManager manager = Survival.enchantingManager;
                    ItemStack item = equipment.getItemInMainHand();
                    manager.selectEnchantment(item, enchantment, level);
                    ItemStack alt = equipment.getItemInOffHand();
                    manager.selectEnchantment(alt, enchantment, level);
                    if (manager.isGrimoire(item))
                        player.openBook(manager.prepareBookFor(item));
                    else if (manager.isGrimoire(alt))
                        player.openBook(manager.prepareBookFor(alt));
                    return PASSED;
                });
    }


}


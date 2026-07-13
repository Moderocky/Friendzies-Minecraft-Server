package mx.kenzie.survival.tools.recipe;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.enchanting.EnchantingManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class GrimoireUnchantingRecipe extends UnchantingRecipe {

    private final EnchantingManager manager = Survival.enchantingManager;

    @Override
    public boolean matches(ItemStack s1, ItemStack s2) {
        if (!manager.isGrimoire(s1)) return false;
        return s2 != null && !s2.getEnchantments().isEmpty();
    }

    @NotNull
    @Override
    public ItemStack[] getResult(ItemStack s1, ItemStack s2) {
        final ItemStack result = s1.clone();
        final Map<Enchantment, Integer> enchantments = this.getEnchantments(s2);
        this.manager.mergeStorage(result, enchantments);
        ItemMeta meta = s2.getItemMeta();
        meta.removeEnchantments();
        s2.setItemMeta(meta);
        this.manager.updateGrimoire(result);
        return new ItemStack[]{ItemStack.empty(), s2, result};
    }

}

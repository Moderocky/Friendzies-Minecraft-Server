package mx.kenzie.survival.tools.recipe;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.enchanting.EnchantingManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class GrimoireEnchantingRecipe implements AnvilRecipe {

    private final EnchantingManager manager = Survival.enchantingManager;

    @Override
    public boolean matches(ItemStack s1, ItemStack s2) {
        if (s1 == null || s2 == null) return false;
        if (s1.getAmount() > 1) return false;
        if (!manager.isGrimoire(s2)) return false;
        Map<Enchantment, @NotNull Integer> map = manager.getSelectedEnchantments(s2);
        Map<Enchantment, @NotNull Integer> safe = manager.filterSafeEnchantments(s1, map);
        return !safe.isEmpty();
    }

    @NotNull
    @Override
    public ItemStack[] getResult(ItemStack s1, ItemStack s2) {
        if (s1 == null || s2 == null || !manager.isGrimoire(s2)) return new ItemStack[]{s1, s2, ItemStack.empty()};
        final ItemStack result = s1.clone();
        final ItemStack newBook = s2.clone();
        Map<Enchantment, @NotNull Integer> map = manager.getSelectedEnchantments(s2);
        Map<Enchantment, @NotNull Integer> safe = manager.filterSafeEnchantments(s1, map);
        result.addUnsafeEnchantments(safe);
        this.manager.subtractFromStorage(newBook, safe);
        this.manager.adjustSelectedSafely(newBook);
        this.manager.updateGrimoire(newBook);
        return new ItemStack[]{ItemStack.empty(), newBook, result};
    }

    @Override
    public int getRepairCost(ItemStack s1, ItemStack s2) {
        Map<Enchantment, @NotNull Integer> map = manager.getSelectedEnchantments(s2);
        Map<Enchantment, @NotNull Integer> safe = manager.filterSafeEnchantments(s1, map);
        return manager.predictEnchantmentCost(safe);
    }

}

package mx.kenzie.survival.tools.recipe;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public interface AnvilRecipe {

    /**
     * Do the ingredients in the anvil match your recipe?
     * All nullness checks should be done here.
     * If it passes your criterion it is assumed that the recipe can go ahead.
     *
     * @param s1 Slot 1
     * @param s2 Slot 2
     * @return true if these are valid ingredients.
     */
    boolean matches(ItemStack s1, ItemStack s2);

    /**
     * This will only ever be called if #matches() passes.
     * You should perform all your checks within the matches() method.
     *
     * @param s1 The first slot.
     * @param s2 The second slot.
     * @return What the slots will be after the recipe is completed.
     * This *must* be a 3-value array.
     * [0] is slot 1, [1] is slot 2, [2] is the result slot.
     * Here you can set what will happen in this specific case.
     */
    @NotNull
    ItemStack[] getResult(ItemStack s1, ItemStack s2);

    /// Run after completion with the initial ingredients.
    default void postCompletion(Player crafter, ItemStack s1, ItemStack s2, ItemStack... results) {

    }

    /**
     * The cost of the recipe depending on the ingredients.
     */
    int getRepairCost(ItemStack s1, ItemStack s2);

    /**
     * Whether this should be enabled, useful if you want to create a config.
     */
    default boolean enabled() {
        return true;
    }

}

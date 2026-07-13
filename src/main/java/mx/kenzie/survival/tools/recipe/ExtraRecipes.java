package mx.kenzie.survival.tools.recipe;

import mx.kenzie.survival.Survival;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CraftingBookCategory;

public class ExtraRecipes {
    public static void register() {
        Bukkit.addRecipe(shapeless("copper_torch", new ItemStack(Material.COPPER_TORCH), Material.TORCH, Material.COPPER_NUGGET));
        Bukkit.addRecipe(shapeless("lantern", new ItemStack(Material.LANTERN), Material.IRON_INGOT, Material.TORCH));
        Bukkit.addRecipe(shapeless("soul_lantern", new ItemStack(Material.SOUL_LANTERN), Material.IRON_INGOT, Material.SOUL_TORCH));
        Bukkit.addRecipe(shapeless("copper_lantern", new ItemStack(Material.COPPER_LANTERN), new RecipeChoice.MaterialChoice(Material.COPPER_INGOT), new RecipeChoice.MaterialChoice(Material.TORCH, Material.COPPER_TORCH)));
        Bukkit.addRecipe(new BlastingRecipe(Survival.key("copper_block"), new ItemStack(Material.COPPER_BLOCK), new RecipeChoice.MaterialChoice(Material.RAW_COPPER_BLOCK), 0.7F * 9F, 100 * 5));
        Bukkit.addRecipe(new BlastingRecipe(Survival.key("iron_block"), new ItemStack(Material.IRON_BLOCK), new RecipeChoice.MaterialChoice(Material.RAW_IRON_BLOCK), 0.7F * 9F, 100 * 5));
        Bukkit.addRecipe(new BlastingRecipe(Survival.key("gold_block"), new ItemStack(Material.GOLD_BLOCK), new RecipeChoice.MaterialChoice(Material.RAW_GOLD_BLOCK), 9F, 100 * 5));
    }


    private static Recipe shapeless(String id, ItemStack result, Material... materials) {
        final ShapelessRecipe recipe = new ShapelessRecipe(Survival.key(id), result);
        for (Material material : materials) {
            recipe.addIngredient(material);
        }
        recipe.setGroup("survival");
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }


    private static Recipe shapeless(String id, ItemStack result, RecipeChoice... materials) {
        final ShapelessRecipe recipe = new ShapelessRecipe(Survival.key(id), result);
        for (RecipeChoice material : materials) {
            recipe.addIngredient(material);
        }
        recipe.setGroup("survival");
        recipe.setCategory(CraftingBookCategory.MISC);
        return recipe;
    }
}

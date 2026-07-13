package mx.kenzie.survival.tools.recipe;

import mx.kenzie.survival.Survival;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.StonecuttingRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class WoodcuttingRecipes {
    public static final String LOG = "LOG";
    public static final String WOOD = "WOOD";
    public static final String STEM = "STEM";
    public static final String HYPHAE = "HYPHAE";
    public static final String BLOCK = "BLOCK";
    private static final String[] WOOD_TYPES = {"OAK", "SPRUCE", "BIRCH", "JUNGLE", "ACACIA", "DARK_OAK", "CHERRY", "MANGROVE", "PALE_OAK", "CRIMSON", "WARPED", "BAMBOO"};
    private static final Iterable<Namer> ALL_LOGS = List.of(typeX(LOG), typeX(WOOD), typeX(STEM), typeX(HYPHAE), typeX(BLOCK), xTypeX("STRIPPED", LOG), xTypeX("STRIPPED", WOOD), xTypeX("STRIPPED", STEM), xTypeX("STRIPPED", HYPHAE), xTypeX("STRIPPED", BLOCK));
    private static int counter = 0;

    public static void register() {
        registerRecipe(typeX(LOG), typeX(WOOD), 1);
        registerRecipe(typeX(LOG), xTypeX("STRIPPED", LOG), 1);
        registerRecipe(typeX(LOG), xTypeX("STRIPPED", WOOD), 1);
        registerRecipe(typeX(WOOD), xTypeX("STRIPPED", WOOD), 1);
        registerRecipe(typeX(STEM), typeX(HYPHAE), 1);
        registerRecipe(typeX(STEM), xTypeX("STRIPPED", STEM), 1);
        registerRecipe(typeX(STEM), xTypeX("STRIPPED", HYPHAE), 1);
        registerRecipe(typeX(HYPHAE), xTypeX("STRIPPED", HYPHAE), 1);
        Bukkit.addRecipe(stonecutting(Material.BAMBOO_BLOCK, Material.STRIPPED_BAMBOO_BLOCK));

        registerRecipe(ALL_LOGS, typeX("PLANKS"), 4);

        registerRecipe(ALL_LOGS, typeX("STAIRS"), 4);
        registerRecipe(typeX("PLANKS"), typeX("STAIRS"), 1);

        registerRecipe(ALL_LOGS, typeX("SLAB"), 8);
        registerRecipe(typeX("PLANKS"), typeX("SLAB"), 2);
        registerRecipe(typeX("STAIRS"), typeX("SLAB"), 1);

        registerRecipe(ALL_LOGS, typeX("TRAPDOOR"), 4);
        registerRecipe(typeX("PLANKS"), typeX("TRAPDOOR"), 1);
        registerRecipe(typeX("DOOR"), typeX("TRAPDOOR"), 2);

        registerRecipe(ALL_LOGS, typeX("PRESSURE_PLATE"), 8);
        registerRecipe(typeX("PLANKS"), typeX("PRESSURE_PLATE"), 2);
        registerRecipe(typeX("SLAB"), typeX("PRESSURE_PLATE"), 1);

        registerRecipe(ALL_LOGS, typeX("SHELF"), 2);


        registerRecipe(ALL_LOGS, typeX("FENCE"), 4);
        registerRecipe(ALL_LOGS, typeX("FENCE_GATE"), 4);
        registerRecipe(typeX("PLANKS"), typeX("FENCE"), 1);
        registerRecipe(typeX("PLANKS"), typeX("FENCE_GATE"), 1);

        registerRecipe(ALL_LOGS, typeX("BUTTON"), 16);
        registerRecipe(typeX("PLANKS"), typeX("BUTTON"), 4);
        registerRecipe(typeX("STAIRS"), typeX("BUTTON"), 3);
        registerRecipe(typeX("SLAB"), typeX("BUTTON"), 2);

        registerRecipe(ALL_LOGS, typeX("DOOR"), 1);

        registerRecipe(ALL_LOGS, typeX("SIGN"), 4);
        registerRecipe(typeX("PLANKS"), typeX("SIGN"), 1);

        registerRecipe(ALL_LOGS, typeX("HANGING_SIGN"), 1);

        registerRecipe(ALL_LOGS, typeX("MOSAIC"), 4);
        registerRecipe(ALL_LOGS, typeX("MOSAIC_STAIRS"), 4);
        registerRecipe(ALL_LOGS, typeX("BAMBOO_MOSAIC_SLAB"), 8);

        registerRecipe(ALL_LOGS, typeX("BOAT"), 1);

        registerRecipe(ALL_LOGS, _ -> "LADDER", 4);
        registerRecipe(typeX("PLANKS"), _ -> "LADDER", 1);

        registerRecipe(ALL_LOGS, _ -> "STICK", 8);
        registerRecipe(typeX("PLANKS"), _ -> "STICK", 2);
        registerRecipe(typeX("STAIRS"), _ -> "STICK", 2);
        registerRecipe(typeX("SLAB"), _ -> "STICK", 1);
        registerRecipe(typeX("FENCE"), _ -> "STICK", 2);
        registerRecipe(typeX("FENCE_GATE"), _ -> "STICK", 2);
        registerRecipe(typeX("DOOR"), _ -> "STICK", 4);
        registerRecipe(typeX("TRAPDOOR"), _ -> "STICK", 2);
        registerRecipe(typeX("SIGN"), _ -> "STICK", 1);
        registerRecipe(typeX("HANGING_SIGN"), _ -> "STICK", 1);
        registerRecipe(typeX("BOAT"), _ -> "STICK", 4);
        registerRecipe(typeX("CHEST_BOAT"), _ -> "STICK", 6);
        registerRecipe(typeX("BOAT"), _ -> "PLANKS", 2);
        registerRecipe(typeX("CHEST_BOAT"), _ -> "PLANKS", 4);

        registerRecipe(Material.CHEST, Material.STICK, 4);
        registerRecipe(Material.BARREL, Material.STICK, 4);
        registerRecipe(Material.COMPOSTER, Material.STICK, 4);
        registerRecipe(Material.FLETCHING_TABLE, Material.STICK, 4);
        registerRecipe(Material.SMITHING_TABLE, Material.STICK, 4);
        registerRecipe(Material.BEEHIVE, Material.STICK, 4);
        registerRecipe(Material.LADDER, Material.STICK, 2);

        registerRecipe(ALL_LOGS, _ -> "CRAFTING_TABLE", 1);
        registerRecipe(ALL_LOGS, _ -> "CHEST", 1);
        registerRecipe(ALL_LOGS, _ -> "BARREL", 1);
        registerRecipe(ALL_LOGS, _ -> "COMPOSTER", 1);
        registerRecipe(ALL_LOGS, _ -> "CAMPFIRE", 1);

        registerRecipe(Material.BROWN_MUSHROOM_BLOCK, Material.BROWN_MUSHROOM, 4);
        registerRecipe(Material.RED_MUSHROOM_BLOCK, Material.RED_MUSHROOM, 4);
    }

    protected static void registerRecipe(Iterable<Namer> from, Namer to, int count) {
        for (Namer namer : from) {
            registerRecipe(namer, to, count);
        }
    }

    protected static void registerRecipe(Namer from, Namer to, int count) {
        for (String type : WOOD_TYPES) {
            try {
                Material a = Material.valueOf(from.apply(type).toUpperCase(Locale.ROOT));
                Material b = Material.valueOf(to.apply(type).toUpperCase(Locale.ROOT));
                Bukkit.addRecipe(stonecutting(a, b, count));
            } catch (IllegalArgumentException _) {

            }
        }
    }

    protected static void registerRecipe(Material from, Material to, int count) {
        Bukkit.addRecipe(stonecutting(from, to, count));
    }

    private static Namer typeX(String part) {
        return type -> type + "_" + part;
    }

    private static Namer xType(String part) {
        return type -> part + "_" + type;
    }

    private static Namer xTypeX(String before, String after) {
        return type -> before + "_" + type + "_" + after;
    }

    private static StonecuttingRecipe stonecutting(String id, @NotNull Material source, ItemStack result) {
        return new StonecuttingRecipe(Survival.key(id), result, source);
    }

    private static StonecuttingRecipe stonecutting(@NotNull Material source, ItemStack result) {
        return stonecutting(result.getType().name().toLowerCase(Locale.ROOT) + "_" + ++counter, source, result);
    }

    private static StonecuttingRecipe stonecutting(@NotNull Material source, Material result, int count) {
        return stonecutting(source, new ItemStack(result, count));
    }

    private static StonecuttingRecipe stonecutting(Material source, Material result) {
        return stonecutting(source, result, 1);
    }

    @FunctionalInterface
    protected interface Namer extends Function<String, String> {
        String name(String type);

        @Override
        default String apply(String s) {
            return name(s);
        }
    }

}

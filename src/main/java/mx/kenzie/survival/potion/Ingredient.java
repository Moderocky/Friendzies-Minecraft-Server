package mx.kenzie.survival.potion;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.persistence.PersistentDataContainerView;
import mx.kenzie.ancillary.Model;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.utility.pack.Item;
import mx.kenzie.survival.utility.pack.ResourcePackMaker;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.ZipOutputStream;

import static net.kyori.adventure.text.Component.*;

public record Ingredient(Material base, Component name, NamespacedKey key, Consumer<ItemMeta> creator,
                         Material... sources) {
    public static final String KEY_PREFIX = "powdered_";
    public static final NamespacedKey INGREDIENT_KEY = Survival.key("potion_ingredient");
    private static final Set<Ingredient> INGREDIENTS = new LinkedHashSet<>();
    private static final Set<Material> OTHER_INGREDIENTS = new HashSet<>();
    public static final Ingredient POWDERED_ECHO_SHARD = new Ingredient(Material.ECHO_SHARD, Material.ECHO_SHARD);
    public static final Ingredient POWDERED_SULFUR = new Ingredient(Material.YELLOW_DYE, Material.SULFUR, Material.SULFUR_SPIKE);
    public static final Ingredient POWDERED_SNOW = new Ingredient(Material.WHITE_DYE, Material.SNOW, Material.SNOWBALL);
    public static final Ingredient POWDERED_GHAST_TEAR = new Ingredient(Material.WHITE_DYE, Material.GHAST_TEAR);
    public static final Ingredient POWDERED_AMETHYST_SHARD = new Ingredient(Material.PURPLE_DYE, Material.AMETHYST_SHARD);
    public static final Ingredient GLOWING_GOO = new Ingredient(Material.GLOW_INK_SAC, text("Glowing Goo"), "glowing_goo", _ -> {
    });
    public static final Ingredient RUBBISH = new Ingredient(Material.CHARCOAL, Component.text("Rubbish"), "powdered_rubbish", _ -> {
    });

    public Ingredient(Material base, Component name, NamespacedKey key, Consumer<ItemMeta> creator, Material... sources) {
        this.base = base;
        this.name = name;
        this.key = key;
        this.sources = sources;
        this.creator = creator;
        if (key != null)
            INGREDIENTS.add(this);
    }

    public Ingredient(Material base, Component name, String value, Consumer<ItemMeta> creator, Material... sources) {
        this(base, name, Survival.key(value), creator, sources);
    }

    public Ingredient(Material base, Component name, Material... sources) {
        this(base, name, Survival.key(KEY_PREFIX + sources[0].getKey().getKey()), _ -> {
        }, sources);
    }

    public Ingredient(Material base, Material... sources) {
        this(base, textOfChildren(text("Powdered "), translatable(sources[0])), sources);
    }

    public static Iterable<Ingredient> ingredients() {
        return Collections.unmodifiableCollection(INGREDIENTS);
    }

    public static Ingredient[] values() {
        return INGREDIENTS.toArray(new Ingredient[0]);
    }

    public static void markAsIngredient(Material material) {
        OTHER_INGREDIENTS.add(material);
    }

    public static void makeFiles(ResourcePackMaker maker, ZipOutputStream stream) throws IOException {
        for (Ingredient ingredient : ingredients()) {
            String nextPath = ingredient.key.namespace() + ":item/" + ingredient.key.value();
            Item item = new Item(nextPath);
            maker.create(item, "assets/server/items/" + ingredient.key.value() + ".json", stream);
            Model model = new Model();
            model.parent = "minecraft:item/generated";
            model.textures = new HashMap<>();
            model.textures.put("layer0", nextPath);
            maker.create(model, "assets/server/models/item/" + ingredient.key.value() + ".json", stream);
        }
    }

    public static boolean isIngredient(ItemStack item) {
        if (item == null) return false;
        if (OTHER_INGREDIENTS.contains(item.getType())) return true;
        PersistentDataContainerView container = item.getPersistentDataContainer();
        return Boolean.TRUE.equals(container.get(INGREDIENT_KEY, PersistentDataType.BOOLEAN));
    }

    public void registerRecipe() {
        if (sources.length == 0) return;
        final ShapelessRecipe recipe = new ShapelessRecipe(key, this.asItem());
        recipe.addIngredient(new RecipeChoice.MaterialChoice(sources));
        recipe.setGroup("survival");
        recipe.setCategory(CraftingBookCategory.MISC);
        Bukkit.addRecipe(recipe);
    }

    public ItemStack asItem() {
        ItemStack item = new ItemStack(base);
        item.editMeta(creator);
        item.editPersistentDataContainer(container -> container.set(INGREDIENT_KEY, PersistentDataType.BOOLEAN, true));
        item.setData(DataComponentTypes.ITEM_MODEL, this.key);
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 64);
        item.setData(DataComponentTypes.ITEM_NAME, name);
        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Ingredient that)) return false;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }
}

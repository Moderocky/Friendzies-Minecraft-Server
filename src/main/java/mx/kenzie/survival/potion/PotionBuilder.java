package mx.kenzie.survival.potion;

import io.papermc.paper.event.inventory.PaperInventoryMoveItemEvent;
import io.papermc.paper.potion.PotionMix;
import mx.kenzie.survival.Survival;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class PotionBuilder implements Listener {

    protected final NamespacedKey key;
    protected final PotionManager manager;
    protected PotionType type;
    protected RecipeChoice ingredient;
    protected RecipeChoice base;
    protected ItemStack result = PotionManager.PLACEHOLDER_POTION;
    protected boolean dependentResult = false, baseChecked = false;
    protected List<Modifier> modifiers = new ArrayList<>();
    protected Predicate<? super ItemStack> baseCheck;
    protected Function<ItemStack, ItemStack> ingredientResult;

    public PotionBuilder(PotionManager manager, NamespacedKey key) {
        this.manager = manager;
        this.key = key;
    }

    public PotionBuilder(PotionManager manager, String id) {
        this(manager, new NamespacedKey("survival", id));
    }

    private static boolean moveToOutput(BrewerInventory contents, ItemStack item) {
        Inventory output = PotionListener.getOutput(contents);
        if (output != null) {
            InventoryMoveItemEvent move = new PaperInventoryMoveItemEvent(contents, item, output, true);
            if (move.callEvent()) {
                HashMap<Integer, ItemStack> map = output.addItem(item);
                return map.isEmpty();
            }
        }
        return false;
    }

    private static boolean changeInPlace(ItemStack ingredient, BrewerInventory contents, ItemStack item) {
        if (ingredient.getAmount() == 1 && PotionListener.getIngredientInput(contents) == null) {
            Bukkit.getScheduler().runTaskLater(Survival.plugin, () -> {
                if (contents.getIngredient() == null)
                    contents.setIngredient(item);
                else PotionListener.dropLeftovers(contents, item);
            }, 0L);
            contents.setIngredient(item);
            return true;
        }
        return false;
    }

    public PotionBuilder setType(PotionType type) {
        this.type = type;
        return this;
    }

    public PotionBuilder setBase(RecipeChoice base) {
        this.base = base;
        return this;
    }

    public PotionBuilder setBase(Material base) {
        this.base = new RecipeChoice.MaterialChoice(base);
        if (base != Material.POTION && base != Material.SPLASH_POTION && base != Material.LINGERING_POTION)
            this.baseChecked = true;
        return this;
    }

    public PotionBuilder setBase(ItemStack base) {
        this.baseChecked = true;
        return this.setBase(new RecipeChoice.ExactChoice(base));
    }

    public PotionBuilder setBase(PotionType base) {
        return this.setBasePotion(potion -> potion.hasBasePotionType() && potion.getBasePotionType() == base);
    }

    public PotionBuilder setBase(Predicate<? super ItemStack> base) {
        this.baseChecked = true;
        this.baseCheck = base;
        return this.setBase(PotionMix.createPredicateChoice(base));
    }

    public PotionBuilder setBasePotion(Predicate<? super PotionMeta> base) {
        return this.setBase(item -> item.getItemMeta() instanceof PotionMeta potion && base.test(potion));
    }

    public PotionBuilder setIngredientResult(Function<ItemStack, ItemStack> result) {
        this.ingredientResult = result;
        return this;
    }

    public PotionBuilder setIngredient(RecipeChoice ingredient) {
        this.ingredient = ingredient;
        return this;
    }

    public PotionBuilder setIngredient(Predicate<ItemStack> ingredient) {
        this.ingredient = PotionMix.createPredicateChoice(ingredient);
        return this;
    }

    public PotionBuilder setIngredientPotion(Predicate<PotionMeta> ingredient) {
        this.ingredient = PotionMix.createPredicateChoice(item -> item.getItemMeta() instanceof PotionMeta meta && ingredient.test(meta));
        return this;
    }

    public PotionBuilder setIngredient(ItemStack ingredient) {
        this.ingredient = new RecipeChoice.ExactChoice(ingredient);
        return this;
    }

    public PotionBuilder setIngredient(Material ingredient) {
        this.ingredient = new RecipeChoice.MaterialChoice(ingredient);
        return this;
    }

    public PotionBuilder setIngredient(Material... ingredients) {
        this.ingredient = new RecipeChoice.MaterialChoice(ingredients);
        return this;
    }

    public PotionBuilder setResult(ItemStack result) {
        this.result = result;
        return this;
    }

    public <Meta extends ItemMeta> PotionBuilder setResult(ItemStack result, Class<Meta> metaType, Consumer<Meta> metaConsumer) {
        ItemMeta meta = result.getItemMeta();
        if (metaType.isInstance(meta)) metaConsumer.accept(metaType.cast(meta));
        result.setItemMeta(meta);
        this.result = result;
        return this;
    }

    public PotionBuilder setResult(ItemStack result, Consumer<PotionMeta> metaConsumer) {
        ItemMeta meta = result.getItemMeta();
        if (meta instanceof PotionMeta potion) metaConsumer.accept(potion);
        result.setItemMeta(meta);
        this.result = result;
        return this;
    }

    public PotionBuilder setResult(Consumer<PotionMeta> metaConsumer) {
        return this.setResult(new ItemStack(Material.POTION), metaConsumer);
    }

    public PotionBuilder setResult(Material material, Consumer<PotionMeta> metaConsumer) {
        return this.setResult(new ItemStack(material), metaConsumer);
    }

    public PotionBuilder modifyResultByBase(BiFunction<ItemStack, ItemStack, ItemStack> resultModifier) {
        return this.modifyResult(new BaseModifier(resultModifier));
    }

    public PotionBuilder modifyResultByBasePotion(BiFunction<PotionMeta, PotionMeta, PotionMeta> resultModifier) {
        return this.modifyResultByBase((base, result) -> {
            if (base.getItemMeta() instanceof PotionMeta baseMeta && result.getItemMeta() instanceof PotionMeta resultMeta)
                result.setItemMeta(resultModifier.apply(baseMeta, resultMeta));
            return result;
        });
    }

    public PotionBuilder modifyResultByIngredient(BiFunction<ItemStack, ItemStack, ItemStack> resultModifier) {
        return this.modifyResult(new IngredientModifier(resultModifier));
    }

    protected PotionBuilder modifyResult(Modifier modifier) {
        this.dependentResult = true;
        this.modifiers.add(modifier);
        return this;
    }

    public void build() {
        this.manager.registerPotion(this);
    }

    public boolean isDependentResult() {
        return dependentResult;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void brewModification(BrewEvent event) {
        BrewerInventory contents = event.getContents();
        ItemStack inputIngredient = contents.getIngredient();
        if (inputIngredient == null) return;
        if (!this.ingredient.test(inputIngredient)) return;
        List<ItemStack> outputs = event.getResults();
        boolean anyMatched = false;
        for (int i = 0; i < Math.min(3, outputs.size()); i++) {
            ItemStack basePotion = contents.getItem(i);
            if (basePotion == null) continue;
            if (!base.test(basePotion)) continue;
            anyMatched = true;
            if (baseChecked && (baseCheck == null || baseCheck.test(basePotion))) {
                outputs.set(i, result.clone());
            }
            ItemStack resultPotion = outputs.get(i);
            for (Modifier modifier : modifiers) {
                resultPotion = modifier.apply(inputIngredient, basePotion, resultPotion);
            }
            outputs.set(i, resultPotion);
        }
        if (ingredientResult != null && anyMatched) {
            ItemStack item = this.ingredientResult.apply(inputIngredient);
            final boolean catalyst = (item == inputIngredient);
            if (catalyst) {
                item = item.clone(); // minecraft subtracts it!
                // the item isn't consumed, prioritise keeping it where it is
                if (changeInPlace(inputIngredient, contents, item)) return;
                if (moveToOutput(contents, item)) return;
            } else {
                if (moveToOutput(contents, item)) return;
                if (changeInPlace(inputIngredient, contents, item)) return;
            }
            PotionListener.dropLeftovers(contents, item);
        }
    }

    public void close() {
        BrewEvent.getHandlerList().unregister(this);
    }

    public boolean shouldRegisterListener() {
        return !modifiers.isEmpty() || baseChecked || ingredientResult != null;
    }

    protected interface Modifier {
        ItemStack apply(ItemStack ingredient, ItemStack base, ItemStack result);
    }

    record BaseModifier(BiFunction<ItemStack, ItemStack, ItemStack> modifier) implements Modifier {
        @Override
        public ItemStack apply(ItemStack ingredient, ItemStack base, ItemStack result) {
            return modifier.apply(base, result);
        }
    }

    record IngredientModifier(BiFunction<ItemStack, ItemStack, ItemStack> modifier) implements Modifier {
        @Override
        public ItemStack apply(ItemStack ingredient, ItemStack base, ItemStack result) {
            return modifier.apply(ingredient, result);
        }
    }

}

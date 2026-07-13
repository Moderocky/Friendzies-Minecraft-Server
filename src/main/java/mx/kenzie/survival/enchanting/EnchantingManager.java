package mx.kenzie.survival.enchanting;

import com.google.common.base.Suppliers;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.listener.AnvilListener;
import mx.kenzie.survival.tools.recipe.*;
import mx.kenzie.survival.utility.DefaultMap;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static net.kyori.adventure.text.Component.*;

public class EnchantingManager {
    static final Component TIMES = text('×', TextColor.color(0, 102, 83)), SET = text('■', TextColor.color(0, 102, 83)), UNSET = text("□", TextColor.color(0, 102, 83)), LOCKED = text("□", TextColor.color(140, 161, 157)), EMPTY = text("○", TextColor.color(0, 102, 83));
    static final NamespacedKey GRIMOIRE_KEY = NamespacedKey.minecraft("grimoire"), STORED_ENCHANTMENTS = NamespacedKey.minecraft("stored_enchantments"), SELECTED_ENCHANTMENTS = NamespacedKey.minecraft("selected_enchantments");
    private static final TextColor OFF_WHITE = TextColor.color(205, 205, 220);
    public static int BOXES_TO_SHOW = 5;
    public final Supplier<Integer> totalEnchantmentPower = Suppliers.memoize(this::calculateTotalEnchantmentPower);
    protected final Map<Enchantment, String> enchantmentNames = new DefaultMap<>(enchantment -> enchantment.key().value());
    final List<TextComponent> originalLore = List.of(textOfChildren(text("Configure Enchantments ", NamedTextColor.GRAY), keybind("key.use", NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false));
    protected final ItemStack grimoire = this.createGrimoire();

    private static String commandForSet(Enchantment enchantment, int level) {
        return "/grimoire select " + enchantment.getKey().asString() + " " + level;
    }

    private static Component interactions(Component button, Enchantment enchantment, int level) {
        final int cost = (enchantment.getAnvilCost() * level) / 2;
        return button.hoverEvent(textOfChildren(text("Select Level " + level), newline(), text("("), text("+" + cost, NamedTextColor.AQUA), text(" Experience Levels)")).color(OFF_WHITE)).clickEvent(ClickEvent.runCommand(commandForSet(enchantment, level)));
    }

    private static Component interactionsLocked(Component button, int level, int total) {
        return button.hoverEvent(textOfChildren(text("Not enough enchantment power stored."), newline(), text("Merge more enchanted books in an anvil."), newline(), newline(), text("(Another "), text(factor(level) - total, NamedTextColor.AQUA), text(" Level I books)")).color(OFF_WHITE));
    }

    private static Component boxes(Enchantment enchantment, int level, int maxLevel, int total) {
        TextComponent.Builder builder = text();
        int i = 0;
        int semiMax = unfactor(total);
        builder.append(interactions(EMPTY, enchantment, 0)).append(text("  "));
        for (; i < Math.min(level, BOXES_TO_SHOW); ++i) {
            builder.append(interactions(SET, enchantment, i + 1));
        }

        for (; i < Math.min(Math.min(semiMax, maxLevel), BOXES_TO_SHOW); ++i) {
            builder.append(interactions(UNSET, enchantment, i + 1));
        }

        for (; i < Math.min(maxLevel, BOXES_TO_SHOW); ++i) {
            builder.append(interactionsLocked(LOCKED, i + 1, total));
        }
        return builder.build();
    }

    protected static int factor(int level) {
        if (level == 0) return 0;
        return (int) Math.pow(2, level - 1);
    }

    protected static int unfactor(int total) {
        // Since the numbers are small, this is quicker than a logarithm
        if (total < 1) return 0;
        if (total < 2) return 1;
        if (total < 4) return 2;
        if (total < 8) return 3;
        if (total < 16) return 4;
        if (total < 32) return 5;
        if (total < 64) return 6;
        int levels = 1;
        while ((total = total / 2) > 0) ++levels;
        return levels;
    }

    public static Enchantment getByKey(@Nullable NamespacedKey key) {
        return EnchantingRegistry.getByKey(key);
    }

    private static int getNumericStoredPower(Map<Enchantment, @NotNull Integer> stored) {
        int power = 0;
        for (Map.Entry<Enchantment, Integer> entry : stored.entrySet()) {
            final Enchantment enchantment = entry.getKey();
            final int total = entry.getValue();
            if (total < 1) continue;
            power += Math.min(unfactor(total), enchantment.getMaxLevel());
        }
        return power;
    }

    public void setup() {
        Bukkit.getPluginManager().registerEvents(new AnvilListener(), Survival.plugin);
        Bukkit.getPluginManager().registerEvents(new Actions(), Survival.plugin);
        Survival.anvilRecipes.add(new UnchantingRecipe());
        Survival.anvilRecipes.add(new GrimoireEnchantingRecipe());
        Survival.anvilRecipes.add(new GrimoireMergeRecipe());
        Survival.anvilRecipes.add(new GrimoireUnchantingRecipe());
        new GrimoireCommand().register(Survival.plugin);

        final ShapelessRecipe recipe = new ShapelessRecipe(GRIMOIRE_KEY, grimoire);
        recipe.addIngredient(Material.ENCHANTED_BOOK);
        recipe.addIngredient(Material.NETHER_STAR);
        recipe.setGroup("survival");
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        Bukkit.addRecipe(recipe);
        Survival.anvilRecipes.add(new PotionEnchantingRecipe());

        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        BOXES_TO_SHOW = Math.min(15, registry.stream().mapToInt(Enchantment::getMaxLevel).max().orElseGet(() -> BOXES_TO_SHOW));
    }

    protected ItemStack createGrimoire() {
        final ItemStack stack = new ItemStack(Material.KNOWLEDGE_BOOK);
        final KnowledgeBookMeta meta = ((KnowledgeBookMeta) stack.getItemMeta());
        meta.customName(text("Grimoire", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setEnchantmentGlintOverride(true);
        meta.setEnchantable(30);
        meta.lore(originalLore);
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        PersistentDataAdapterContext context = dataContainer.getAdapterContext();
        PersistentDataContainer inner = context.newPersistentDataContainer();
        inner.set(STORED_ENCHANTMENTS, PersistentDataType.TAG_CONTAINER, context.newPersistentDataContainer());
        inner.set(SELECTED_ENCHANTMENTS, PersistentDataType.TAG_CONTAINER, context.newPersistentDataContainer());
        dataContainer.set(GRIMOIRE_KEY, PersistentDataType.TAG_CONTAINER, inner);
        stack.setItemMeta(meta);
        this.updateGrimoire(stack);
        return stack;
    }

    public boolean isGrimoire(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getPersistentDataContainer().has(GRIMOIRE_KEY, PersistentDataType.TAG_CONTAINER);
    }

    public Book prepareBookFor(ItemStack grimoire) {
        Map<Enchantment, Integer> selected = this.getSelectedEnchantments(grimoire);
        Map<Enchantment, @NotNull Integer> stored = this.getStoredEnchantments(grimoire);
        List<TextComponent.Builder> pages = new ArrayList<>();
        TextComponent.Builder builder = text();
        if (stored.isEmpty()) {
            builder.append(text("This grimoire is empty.")).append(newline()).append(text("Combine it with enchanted books in an anvil to store enchantments," + " then select those you wish to apply to an item.")).append(newline()).append(newline()).append(text("Grimoires can be used to enchant items in an anvil."));
            return Book.book(text("Grimoire"), Component.empty(), new Component[]{builder.build()});
        }
        int lineCount = 2;
        final int cost = this.predictEnchantmentCost(selected);
        builder.append(Component.text("(Costs ")).append(text(cost, TextColor.color(0, 102, 83))).append(Component.text(" Levels)")).append(newline()).append(newline());
        final var entries = new ArrayList<>(stored.entrySet());
        entries.sort(Comparator.<Map.Entry<Enchantment, @NotNull Integer>>comparingInt(entry -> entry.getKey().getWeight()).reversed().thenComparingInt(Map.Entry::getValue));
        for (Map.Entry<Enchantment, Integer> entry : entries) {
            final Enchantment enchantment = entry.getKey();
            final int total = entry.getValue();
            if (total < 1) continue;
            final int level = selected.get(enchantment);
            final int maxLevel = enchantment.getMaxLevel();
            if (lineCount >= 12) {
                lineCount = 0;
                pages.add(builder);
                builder = text();
            }
            Component component = text().append(enchantment.description()).append(text(" ")).append(TIMES).append(text(" ")).append(text(total)).build();
            builder.append(component).append(newline()).append(boxes(enchantment, level, maxLevel, total)).append(newline());
            lineCount += 2;
        }
        pages.add(builder);
        TextComponent[] written = pages.stream().map(TextComponent.Builder::build).toArray(TextComponent[]::new);
        return Book.book(text("Grimoire"), Component.empty(), written);
    }

    private int calculateTotalEnchantmentPower() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream().mapToInt(Enchantment::getMaxLevel).sum();
    }

    public void updateGrimoire(ItemStack item) {
        Map<Enchantment, Integer> selected = this.getSelectedEnchantments(item);
        Map<Enchantment, @NotNull Integer> stored = this.getStoredEnchantments(item);
        ItemMeta meta = item.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        List<Component> lore = new ArrayList<>();
        if (!stored.isEmpty()) lore.add(text(""));
        lore.addAll(originalLore);
        int power = getNumericStoredPower(stored);
        meta.lore(lore);
        item.setItemMeta(meta);
        item.removeEnchantments();
        item.addUnsafeEnchantments(selected);
        // storage totals
        int totalPower = totalEnchantmentPower.get();
        item.setData(DataComponentTypes.MAX_DAMAGE, totalPower);
        item.setData(DataComponentTypes.DAMAGE, Math.clamp(totalPower - power, 1, totalPower));
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS).build());
    }

    public int predictEnchantmentCost(Map<Enchantment, Integer> enchantments) {
        if (enchantments == null || enchantments.isEmpty()) return 0;
        int cost = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            final Enchantment enchantment = entry.getKey();
            final int level = entry.getValue();
            cost += level * enchantment.getAnvilCost();
        }
        return cost / 2;
    }

    private DefaultMap<Enchantment, Integer> toEnchantmentMap(PersistentDataContainerView container) {
        DefaultMap<Enchantment, Integer> map = new DefaultMap<>(LinkedHashMap::new, 0);
        if (container == null) return map;
        for (NamespacedKey key : container.getKeys()) {
            Integer value = container.get(key, PersistentDataType.INTEGER);
            map.put(getByKey(key), value);
        }
        return map;
    }

    private PersistentDataContainer storeEnchantmentMap(Map<Enchantment, Integer> enchantments, PersistentDataAdapterContext context) {
        PersistentDataContainer container = context.newPersistentDataContainer();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            container.set(entry.getKey().getKey(), PersistentDataType.INTEGER, entry.getValue());
        }
        return container;
    }

    @Deprecated(forRemoval = true)
    public boolean take(ItemStack item, Enchantment enchantment, int level) {
        int storageRequired = factor(level);

        Map<Enchantment, Integer> storedEnchantments = this.getStoredEnchantments(item);
        int currentStored = storedEnchantments.get(enchantment);
        if (currentStored < storageRequired) return false;
        int value = currentStored - storageRequired;
        if (value == 0) storedEnchantments.remove(enchantment);
        else storedEnchantments.put(enchantment, value);
        this.setStoredEnchantments(item, storedEnchantments);

        return true;
    }

    public void selectEnchantment(ItemStack item, Enchantment enchantment, int level) {
        if (!isGrimoire(item)) return;
        level = Math.min(enchantment.getMaxLevel(), level);
        int storageRequired = factor(level);

        Map<Enchantment, Integer> storedEnchantments = this.getStoredEnchantments(item);
        int currentStored = storedEnchantments.get(enchantment);
        if (currentStored == 0) return;
        int adjusted = Math.min(storageRequired, currentStored);
        int log = unfactor(adjusted);

        Map<Enchantment, Integer> map = this.getSelectedEnchantments(item);
        if (log < 1) map.remove(enchantment);
        else map.put(enchantment, log);

        this.setEnchantments(item, SELECTED_ENCHANTMENTS, map);
        this.updateGrimoire(item);
    }

    public void setSelectedEnchantments(ItemStack item, Map<Enchantment, Integer> enchantments) {
        this.setEnchantments(item, SELECTED_ENCHANTMENTS, enchantments);
    }

    public void setStoredEnchantments(ItemStack item, Map<Enchantment, Integer> enchantments) {
        this.setEnchantments(item, STORED_ENCHANTMENTS, enchantments);
    }

    public Map<Enchantment, @NotNull Integer> getStoredEnchantments(ItemStack item) {
        return this.getEnchantments(item, STORED_ENCHANTMENTS);
    }

    public Map<Enchantment, @NotNull Integer> getSelectedEnchantments(ItemStack item) {
        return this.getEnchantments(item, SELECTED_ENCHANTMENTS);
    }

    public Map<Enchantment, @NotNull Integer> filterSafeEnchantments(ItemStack item, Map<Enchantment, @NotNull Integer> enchantments) {
        if (enchantments == null) return new DefaultMap<>(0);
        if (enchantments.isEmpty()) return enchantments;
        Map<Enchantment, Integer> filtered = new DefaultMap<>(0);
        List<Enchantment> conflicts = new ArrayList<>(item.getEnchantments().keySet());
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            final Enchantment enchantment = entry.getKey();
            if (conflicts.contains(enchantment)) continue;
            if (!enchantment.canEnchantItem(item)) continue;
            if (this.conflictsWith(enchantment, conflicts)) continue;
            final int level = Math.clamp(entry.getValue(), 1, enchantment.getMaxLevel());
            filtered.put(enchantment, level);
            conflicts.add(enchantment);
        }
        return filtered;
    }

    public void mergeStorage(ItemStack item, Map<Enchantment, Integer> other) {
        if (other.isEmpty()) return;
        Map<Enchantment, @NotNull Integer> stored = this.getStoredEnchantments(item);
        for (Map.Entry<Enchantment, Integer> entry : other.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = factor(entry.getValue());
            if (!stored.containsKey(enchantment)) stored.put(enchantment, level);
            else stored.put(enchantment, stored.get(enchantment) + level);
        }
        this.setStoredEnchantments(item, stored);
    }

    public void subtractFromStorage(ItemStack item, Map<Enchantment, @NotNull Integer> enchantments) {
        Map<Enchantment, @NotNull Integer> map = this.getStoredEnchantments(item);
        for (Map.Entry<Enchantment, @NotNull Integer> entry : enchantments.entrySet()) {
            final Enchantment enchantment = entry.getKey();
            final int level = entry.getValue(), total = factor(level);
            if (level < 1) continue;
            int current = map.get(enchantment);
            int replace = Math.max(0, current - total);
            if (replace < 1) map.remove(enchantment);
            else map.put(enchantment, replace);
        }
        this.setStoredEnchantments(item, map);
    }

    protected boolean conflictsWith(@NotNull Enchantment enchantment, Collection<Enchantment> conflicts) {
        return conflicts.stream().anyMatch(enchantment::conflictsWith);
    }

    private void setEnchantments(ItemStack item, NamespacedKey containerKey, Map<Enchantment, Integer> enchantments) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer outer = meta.getPersistentDataContainer();
        if (!outer.has(GRIMOIRE_KEY)) return;
        PersistentDataContainer inner = outer.get(GRIMOIRE_KEY, PersistentDataType.TAG_CONTAINER);
        if (inner == null) return;
        PersistentDataAdapterContext context = inner.getAdapterContext();
        PersistentDataContainer stored = this.storeEnchantmentMap(enchantments, context);
        inner.set(containerKey, PersistentDataType.TAG_CONTAINER, stored);
        outer.set(GRIMOIRE_KEY, PersistentDataType.TAG_CONTAINER, inner);
        item.setItemMeta(meta);
    }

    private DefaultMap<Enchantment, Integer> getEnchantments(ItemStack item, NamespacedKey containerKey) {
        if (item == null || !item.hasItemMeta()) return new DefaultMap<>(0);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storage) return new DefaultMap<>(storage.getStoredEnchants(), 0);
        PersistentDataContainer outer = meta.getPersistentDataContainer();
        if (!outer.has(GRIMOIRE_KEY)) return new DefaultMap<>(0);

        PersistentDataContainer inner = outer.get(GRIMOIRE_KEY, PersistentDataType.TAG_CONTAINER);
        if (inner == null) return new DefaultMap<>(0);

        PersistentDataContainer container = inner.get(containerKey, PersistentDataType.TAG_CONTAINER);
        return this.toEnchantmentMap(container);
    }

    public void adjustSelectedSafely(ItemStack item) {
        Map<Enchantment, @NotNull Integer> selected = this.getSelectedEnchantments(item);
        if (selected.isEmpty()) return;
        Map<Enchantment, @NotNull Integer> stored = this.getStoredEnchantments(item);
        selected.replaceAll((enchantment, integer) -> unfactor(Math.min(factor(integer), stored.get(enchantment))));
        this.setSelectedEnchantments(item, selected);
    }

    public final class Actions implements Listener {

        @EventHandler(priority = EventPriority.LOW)
        public void openBook(PlayerInteractEvent event) {
            switch (event.getAction()) {
                case LEFT_CLICK_AIR:
                case LEFT_CLICK_BLOCK:
                case PHYSICAL:
                    return;
            }
            ItemStack item = event.getItem();
            if (!EnchantingManager.this.isGrimoire(item)) return;
            event.setCancelled(true);
            Book book = EnchantingManager.this.prepareBookFor(item);
            event.getPlayer().openBook(book);
        }
    }
}

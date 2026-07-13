package mx.kenzie.survival.enchanting;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import mx.kenzie.ancillary.MCMeta;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.enchanting.effects.Attributes;
import mx.kenzie.survival.enchanting.effects.Effect;
import mx.kenzie.survival.enchanting.events.HoeEvent;
import mx.kenzie.survival.listener.TreeChopListener;
import mx.kenzie.survival.utility.editor.ItemEditor;
import mx.kenzie.survival.utility.editor.TagEditor;
import mx.kenzie.survival.utility.pack.DataPackMaker;
import net.kyori.adventure.text.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import org.bukkit.*;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

public class EnchantingRegistry {

    public static Enchantment TESTING;
    public static Enchantment BOUNCING_CURSE, SHRINKING_CURSE;
    public static Enchantment BUFF;
    public static Enchantment LUCKY;
    public static Enchantment HOPPER;
    public static Enchantment FEATHERWEIGHT, PROPULSION;
    public static Enchantment REACH;
    public static Enchantment HARVESTER, GREEN_THUMB;
    public static Enchantment LIGHT_STEP;
    public static Enchantment MOLTEN, GOUGE;
    private static URI packUri;
    private final Set<EnchantmentDefinition> definitions = new LinkedHashSet<>();
    private final Queue<Runnable> postSetupTasks = new LinkedList<>();
    private final Supplier<net.minecraft.core.RegistryAccess> registryAccessor = Suppliers.memoize(() -> ((CraftServer) Bukkit.getServer()).getHandle().getServer().registryAccess());

    public static Enchantment getByKey(@Nullable NamespacedKey key) {
        if (key == null) {
            return null;
        }
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(key);
    }

    public static URI dataLocation() throws IOException, URISyntaxException {
        return packUri;
    }

    public void registerDefinitions() {
//        this.register(e -> TESTING = e, this.new EnchantmentBuilder("testing").name("Test Enchantment").support(Tags.BOOTS).validSlots(EquipmentSlot.FEET).build());
        this.register(e -> BOUNCING_CURSE = e, this.new EnchantmentBuilder("bouncing_curse").name("Curse of Bouncing").attributes(
                        new Attributes.Attribute("bounciness", "bouncing_curse", 0.3F)
                ).weight(1).maxLevel(3).cost(5, 28, 8).anvilCost(4).preclude(() -> Survival.key("shrinking_curse")).primaryItems(Tags.BOOTS).support(Tags.BOOTS).validSlots(EquipmentSlot.FEET)
                .tags(EnchantmentTags.ON_RANDOM_LOOT, EnchantmentTags.CURSE, EnchantmentTags.IN_ENCHANTING_TABLE, EnchantmentTags.NON_TREASURE, EnchantmentTags.TRADEABLE).build());
        this.register(e -> SHRINKING_CURSE = e, this.new EnchantmentBuilder("shrinking_curse").name("Curse of Shrinking").attributes(
                        new Attributes.Attribute("scale", "shrinking_curse", -0.04F)
                ).weight(1).maxLevel(3).cost(5, 28, 8).anvilCost(4).preclude(() -> Survival.key("bouncing_curse"), () -> Survival.key("buff")).primaryItems(Tags.BOOTS).support(Tags.BOOTS).validSlots(EquipmentSlot.FEET)
                .tags(EnchantmentTags.ON_RANDOM_LOOT, EnchantmentTags.CURSE, EnchantmentTags.IN_ENCHANTING_TABLE).build());
        this.register(e -> BUFF = e, this.new EnchantmentBuilder("buff").name("Buff").attributes(
                new Attributes.Attribute("scale", "buff", 0.05F),
                new Attributes.Attribute("attack_knockback", "buff", 0.05F),
                new Attributes.Attribute("max_health", "buff", 1)
        ).tags(EnchantmentTags.NON_TREASURE, EnchantmentTags.IN_ENCHANTING_TABLE, EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT, EnchantmentTags.ON_RANDOM_LOOT).weight(6).maxLevel(3).cost(10, 30, 10).anvilCost(6).preclude(() -> Survival.key("shrinking_curse")).primaryItems(Tags.ANY_CHEST).support(Tags.ANY_CHEST).validSlots(EquipmentSlot.CHEST).build());
        this.register(e -> LIGHT_STEP = e, this.new EnchantmentBuilder("light_step").name("Light Step").attributes(
                new Attributes.Attribute("movement_speed", "light_step", 0.03F)
        ).tags(EnchantmentTags.TREASURE, EnchantmentTags.TRADEABLE, EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT, EnchantmentTags.ON_RANDOM_LOOT, EnchantmentTags.ON_TRADED_EQUIPMENT).weight(8).maxLevel(1).cost(25, 30, 10).anvilCost(10).primaryItems(Tags.BOOTS).support(Tags.BOOTS).validSlots(EquipmentSlot.FEET).build());
        this.register(e -> HOPPER = e, this.new EnchantmentBuilder("hopper").name("Hopper").attributes(
                new Attributes.Attribute("jump_strength", "hopper", LevelProvider.perLevel(0.05F, 0.075F)),
                new Attributes.Attribute("safe_fall_distance", "hopper", 1.5F)
        ).tags(EnchantmentTags.IN_ENCHANTING_TABLE, EnchantmentTags.NON_TREASURE).weight(7).maxLevel(2).cost(15, 30, 7).anvilCost(5).primaryItems(Tags.LEGGINGS).support(Tags.LEGGINGS).support(Tags.BOOTS).validSlots(EquipmentSlot.FEET, EquipmentSlot.LEGS).build());
        this.register(e -> FEATHERWEIGHT = e, this.new EnchantmentBuilder("featherweight").name("Featherweight").attributes(
                        new Attributes.Attribute("gravity", "featherweight", LevelProvider.perLevel(-0.13F), Attributes.Operation.ADD_MULTIPLIED_TOTAL),
                        new Attributes.Attribute("air_drag_modifier", "featherweight", LevelProvider.perLevel(-0.05F), Attributes.Operation.ADD_MULTIPLIED_TOTAL)
                ).tags(EnchantmentTags.IN_ENCHANTING_TABLE, EnchantmentTags.TREASURE, EnchantmentTags.TRADEABLE, EnchantmentTags.DOUBLE_TRADE_PRICE).weight(7).maxLevel(1).cost(23, 30, 7).anvilCost(6)
                .primaryItems(Tags.OTHER_CHEST).support(Tags.OTHER_CHEST).support(Tags.BOOTS).validSlots(EquipmentSlot.CHEST, EquipmentSlot.FEET).build());
        this.register(e -> LUCKY = e, this.new EnchantmentBuilder("lucky").name("Lucky").attributes(
                new Attributes.Attribute("luck", "lucky", 1)
        ).tags(EnchantmentTags.ON_RANDOM_LOOT, EnchantmentTags.TREASURE, EnchantmentTags.TRADEABLE, EnchantmentTags.DOUBLE_TRADE_PRICE).weight(8).maxLevel(1).cost(29, 30, 10).anvilCost(12).primaryItems(Tags.ANY_HEAD).support(Tags.ANY_HEAD).validSlots(EquipmentSlot.HEAD).build());
        this.register(e -> REACH = e, this.new EnchantmentBuilder("reach").name("Reach").attributes(
                new Attributes.Attribute("block_interaction_range", "reach", 1),
                new Attributes.Attribute("entity_interaction_range", "reach", 1)
        ).tags(EnchantmentTags.IN_ENCHANTING_TABLE, EnchantmentTags.NON_TREASURE, EnchantmentTags.TRADEABLE, EnchantmentTags.DOUBLE_TRADE_PRICE).weight(6).maxLevel(4).cost(14, 40, 8).anvilCost(5).support(Tags.ANY_HEAD).support(Tags.ANY_MELEE_WEAPONS).support(Tags.ANY_TOOLS).primaryItems(Tags.ANY_TOOLS).validSlots(EquipmentSlot.HEAD, EquipmentSlot.HAND).build());
        this.register(e -> HARVESTER = e, this.new EnchantmentBuilder("harvester").name("Harvester").attributes(
                        new Attributes.Attribute("block_break_speed", "harvester", LevelProvider.perLevel(-0.1F, -0.04F), Attributes.Operation.ADD_MULTIPLIED_TOTAL)
                ).preclude(() -> Survival.key("gouge"))
                .tags(EnchantmentTags.IN_ENCHANTING_TABLE, EnchantmentTags.TREASURE, EnchantmentTags.TRADEABLE, EnchantmentTags.DOUBLE_TRADE_PRICE, EnchantmentTags.ON_TRADED_EQUIPMENT)
                .weight(5).maxLevel(3).cost(12, 30, 8).anvilCost(6)
                .primaryItems(Tags.AXES).support(Tags.AXES).support(Tags.HOES).validSlots(EquipmentSlot.HAND).build());
        this.register(e -> PROPULSION = e, this.new EnchantmentBuilder("propulsion").name("Propulsion")
                .tags(EnchantmentTags.IN_ENCHANTING_TABLE, EnchantmentTags.TREASURE, EnchantmentTags.TRADEABLE, EnchantmentTags.DOUBLE_TRADE_PRICE)
                .weight(8).maxLevel(1).cost(29, 30, 10).anvilCost(12)
                .primaryItems(Material.ELYTRA).support(Material.ELYTRA)
                .validSlots(EquipmentSlot.CHEST).build());
        this.register(e -> MOLTEN = e, this.new EnchantmentBuilder("molten").name("Molten")
                .preclude(Enchantments.SILK_TOUCH, Enchantments.FIRE_ASPECT, Enchantments.FLAME)
                .tags(EnchantmentTags.ON_RANDOM_LOOT, EnchantmentTags.SMELTS_LOOT, EnchantmentTags.NON_TREASURE, EnchantmentTags.TRADEABLE, EnchantmentTags.ON_TRADED_EQUIPMENT)
                .weight(4).maxLevel(1).cost(13, 30, 10).anvilCost(4)
                .primaryItems(Tags.PICKAXES).support(Tags.ANY_TOOLS).support(Tags.SWORDS).validSlots(EquipmentSlot.HAND).build());
        this.register(e -> GOUGE = e, this.new EnchantmentBuilder("gouge").name("Gouge")
                .preclude(() -> Survival.key("harvester"))
                .tags(EnchantmentTags.ON_RANDOM_LOOT, EnchantmentTags.IN_ENCHANTING_TABLE, EnchantmentTags.NON_TREASURE, EnchantmentTags.TRADEABLE)
                .weight(4).maxLevel(3).cost(17, 60, 22).anvilCost(6)
                .primaryItems(Tags.PICKAXES).support(Tags.PICKAXES).support(Tags.SHOVELS).support(Tags.AXES).validSlots(EquipmentSlot.HAND).build());
        this.register(e -> GREEN_THUMB = e, this.new EnchantmentBuilder("green_thumb").name("Green Thumb")
                .tags(EnchantmentTags.IN_ENCHANTING_TABLE, EnchantmentTags.NON_TREASURE, EnchantmentTags.TRADEABLE, EnchantmentTags.ON_RANDOM_LOOT, EnchantmentTags.PREVENTS_BEE_SPAWNS_WHEN_MINING, EnchantmentTags.ON_TRADED_EQUIPMENT)
                .weight(3).maxLevel(1).cost(14, 35, 10).anvilCost(3)
                .primaryItems(Tags.HOES).support(Tags.HOES).validSlots(EquipmentSlot.HAND).build()
        );
    }

    public void registerHooks() {
        this.setupTillableHook(use -> {
            CraftBlock block = CraftBlock.at(use.getLevel(), use.getClickedPos());
            new HoeEvent(block, (Player) use.getPlayer().getBukkitEntity(), use.getItemInHand().getBukkitStack()).callEvent();
        });
    }

    public void preSetup() {
        this.definitions.clear();
        this.registerDefinitions();

        try {
            this.buildDatapack();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void setup() {
        this.fixDisplaysFor(Tags.BOOTS);
        this.fixDisplaysFor(Tags.LEGGINGS);
        this.fixDisplaysFor(Tags.ANY_CHEST);
        this.fixDisplaysFor(Tags.ANY_HEAD);

        postSetupTasks.forEach(Runnable::run);
        postSetupTasks.clear();

        this.fixTags();
        //<editor-fold desc="Edit Item Components" defaultstate="collapsed">
        try (ItemEditor editor = new ItemEditor(ItemType.END_ROD)) {
            editor.edit(DataComponents.EQUIPPABLE, Equippable.builder(net.minecraft.world.entity.EquipmentSlot.HEAD).setAllowedEntities(EntityTypes.PLAYER).build());
        }
        try (ItemEditor editor = new ItemEditor(ItemType.ELYTRA)) {
            editor.edit(DataComponents.ENCHANTABLE, new Enchantable(2));
        }
        try (ItemEditor editor = new ItemEditor(ItemType.POTION)) {
            editor.edit(DataComponents.MAX_STACK_SIZE, 16);
        }
        try (ItemEditor editor = new ItemEditor(ItemType.SPLASH_POTION)) {
            editor.edit(DataComponents.MAX_STACK_SIZE, 16);
        }
        try (ItemEditor editor = new ItemEditor(ItemType.LINGERING_POTION)) {
            editor.edit(DataComponents.MAX_STACK_SIZE, 16);
        }
        try (ItemEditor editor = new ItemEditor(ItemType.ENCHANTED_BOOK)) {
            editor.edit(DataComponents.MAX_STACK_SIZE, 16);
        }
        try (ItemEditor editor = new ItemEditor(ItemType.RABBIT_STEW)) {
            editor.edit(DataComponents.MAX_STACK_SIZE, 16);
        }
        try (ItemEditor editor = new ItemEditor(ItemType.MUSHROOM_STEW)) {
            editor.edit(DataComponents.MAX_STACK_SIZE, 16);
        }
        try (ItemEditor editor = new ItemEditor(ItemType.SUSPICIOUS_STEW)) {
            editor.edit(DataComponents.MAX_STACK_SIZE, 16);
        }
        try (ItemEditor editor = new ItemEditor(ItemType.SADDLE)) {
            editor.edit(DataComponents.MAX_STACK_SIZE, 16);
        }
        final ItemType[] discs = {ItemType.MUSIC_DISC_5, ItemType.MUSIC_DISC_11, ItemType.MUSIC_DISC_13, ItemType.MUSIC_DISC_BLOCKS, ItemType.MUSIC_DISC_BOUNCE, ItemType.MUSIC_DISC_CAT, ItemType.MUSIC_DISC_CHIRP, ItemType.MUSIC_DISC_CREATOR, ItemType.MUSIC_DISC_CREATOR_MUSIC_BOX, ItemType.MUSIC_DISC_FAR, ItemType.MUSIC_DISC_LAVA_CHICKEN, ItemType.MUSIC_DISC_MALL, ItemType.MUSIC_DISC_MELLOHI, ItemType.MUSIC_DISC_OTHERSIDE, ItemType.MUSIC_DISC_PIGSTEP, ItemType.MUSIC_DISC_PRECIPICE, ItemType.MUSIC_DISC_RELIC, ItemType.MUSIC_DISC_STAL, ItemType.MUSIC_DISC_STRAD, ItemType.MUSIC_DISC_TEARS, ItemType.MUSIC_DISC_WAIT, ItemType.MUSIC_DISC_WARD};
        for (ItemType disc : discs) {
            try (ItemEditor editor = new ItemEditor(disc)) {
                editor.edit(DataComponents.MAX_STACK_SIZE, 16);
            }
        }
        final ItemType[] armour = {ItemType.NETHERITE_HORSE_ARMOR, ItemType.DIAMOND_HORSE_ARMOR, ItemType.GOLDEN_HORSE_ARMOR, ItemType.IRON_HORSE_ARMOR, ItemType.COPPER_HORSE_ARMOR, ItemType.LEATHER_HORSE_ARMOR};
        for (ItemType type : armour) {
            try (ItemEditor editor = new ItemEditor(type)) {
                editor.edit(DataComponents.MAX_STACK_SIZE, 4);
            }
        }
        //</editor-fold>

        this.editTag(EnchantmentTags.BOW_EXCLUSIVE, TagEditor::clear);

        this.registerHooks();

        Bukkit.getPluginManager().registerEvents(new EnchantmentListener(), Survival.plugin);
        Bukkit.getPluginManager().registerEvents(new TreeChopListener(), Survival.plugin);
    }

    private void setupTillableHook(Consumer<UseOnContext> hook) {
        try {
            Field field = HoeItem.class.getDeclaredField("TILLABLES");
            field.setAccessible(true);
            Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> map = (Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>>) field.get(null);
            final var copy = new HashMap<>(map);
            map.clear();
            copy.forEach((key, value) -> {
                Predicate<UseOnContext> first = value.getFirst();
                Consumer<UseOnContext> second = value.getSecond();
                Consumer<UseOnContext> total = use -> {
                    second.accept(use);
                    hook.accept(use);
                };
                map.put(key, new Pair<>(first, total));
            });
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected void fixTags() {

        var access = registryAccessor.get();
//        World next = Bukkit.getServer().getWorlds().getFirst();
//        ServerLevel level = ((CraftWorld) next).getHandle();
//        net.minecraft.core.RegistryAccess access = level.registryAccess();

        final var enchantments = access.lookupOrThrow(Registries.ENCHANTMENT);

        for (EnchantmentDefinition definition : definitions()) {
            Identifier identifier = Identifier.fromNamespaceAndPath(definition.getKey().namespace(), definition.getKey().value());
            for (var tag : definition.tags()) {
                try (var editor = new TagEditor<>(enchantments.get(tag))) {
                    editor.add(enchantments.get(identifier).get());
                }
            }
        }

    }

    protected void editTag(TagKey<net.minecraft.world.item.enchantment.Enchantment> tag, Consumer<TagEditor<net.minecraft.world.item.enchantment.Enchantment>> consumer) {

        var access = registryAccessor.get();

        final var enchantments = access.lookupOrThrow(Registries.ENCHANTMENT);
        try (var editor = new TagEditor<>(enchantments.get(tag))) {
            consumer.accept(editor);
        }
    }

    protected Iterable<EnchantmentDefinition> definitions() {
        return definitions;
    }

    public void fixDisplaysFor(Material... materials) {
//        for (Material material : materials) {
//            try (ItemEditor editor = new ItemEditor(material)) {
//                editor.edit(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.ATTRIBUTE_MODIFIERS, true).withHidden(DataComponents.EQUIPPABLE, true));
//            }
//        }
    }

    private void register(Consumer<Enchantment> setField, EnchantmentDefinition definition) {
        this.definitions.add(definition);
        this.postSetupTasks.add(() -> setField.accept(getByKey(definition.getKey())));
    }

    private void buildDatapack() throws IOException, URISyntaxException {
        packUri = new DataPackMaker(new File("enchanting-datapack.zip")).create(maker -> {
            maker.add("pack.mcmeta", new MCMeta(107, 200, textOfChildren(text("Enchantments"))));
            for (EnchantmentDefinition definition : this.definitions()) {
                maker.add("data/server/enchantment/" + definition.id() + ".json", definition.toData());
            }
        });
    }

    public int getLevel(Enchantment enchantment, ItemStack item) {
        if (item == null || enchantment == null) return -1;
        return item.getEnchantments().getOrDefault(enchantment, -1);
    }

    public record Modifier(double value, AttributeModifier.Operation operation) {
        public Modifier(double value) {
            this(value, AttributeModifier.Operation.ADD_NUMBER);
        }
    }

    public class EnchantmentBuilder {

        protected final String id;
        protected Component name;
        protected Set<String> precludes = new LinkedHashSet<>();
        protected Set<String> supported = new LinkedHashSet<>();
        protected Set<String> primary = new LinkedHashSet<>();
        protected Set<TagKey<net.minecraft.world.item.enchantment.Enchantment>> tags = new LinkedHashSet<>();
        protected int weight = 1, maxLevel = 1;
        protected int minBaseCost;
        protected int maxBaseCost;
        protected int minPerLevel;
        protected int maxPerLevel;
        protected int anvilCost = 1;
        protected Set<EquipmentSlot> slots = new LinkedHashSet<>();
        protected Collection<Effect<?>> effects = new ArrayList<>();


        public EnchantmentBuilder(String id) {
            this.id = id;
        }

        public EnchantmentBuilder name(Component name) {
            this.name = name;
            return this;
        }

        public EnchantmentBuilder name(String name) {
            return name(textOfChildren(text(name)));
        }

        public EnchantmentBuilder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public EnchantmentBuilder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        public EnchantmentBuilder preclude(Keyed... enchantments) {
            this.precludes.addAll(Arrays.stream(enchantments).map(i -> i.getKey().toString()).toList());
            return this;
        }

        @SafeVarargs
        public final EnchantmentBuilder preclude(ResourceKey<net.minecraft.world.item.enchantment.Enchantment>... enchantments) {
            this.precludes.addAll(Arrays.stream(enchantments).map(i -> i.identifier().toString()).toList());
            return this;
        }

        public EnchantmentBuilder support(Material... items) {
            this.supported.addAll(Arrays.stream(items).map(i -> i.getKey().toString()).toList());
            return this;
        }

        public EnchantmentBuilder support(String... tags) {
            this.supported.addAll(Arrays.stream(tags).toList());
            return this;
        }

        @SafeVarargs
        public final EnchantmentBuilder support(Tag<Material>... items) {
            this.supported.addAll(Arrays.stream(items).map(i -> '#' + i.getKey().toString()).toList());
            return this;
        }

        public EnchantmentBuilder primaryItems(String... tags) {
            this.primary.addAll(Arrays.stream(tags).toList());
            return this;
        }

        public EnchantmentBuilder primaryItems(Material... items) {
            this.primary.addAll(Arrays.stream(items).map(i -> i.getKey().toString()).toList());
            return this;
        }

        @SafeVarargs
        public final EnchantmentBuilder primaryItems(Tag<Material>... items) {
            this.primary.addAll(Arrays.stream(items).map(i -> '#' + i.getKey().toString()).toList());
            return this;
        }

        public EnchantmentBuilder cost(int min, int max, int minPerLevel, int maxPerLevel) {
            this.minBaseCost = min;
            this.maxBaseCost = max;
            this.minPerLevel = minPerLevel;
            this.maxPerLevel = maxPerLevel;
            return this;
        }

        public EnchantmentBuilder cost(int min, int max, int perLevel) {
            return this.cost(min, max, perLevel, perLevel);
        }

        public EnchantmentBuilder anvilCost(int anvilCost) {
            this.anvilCost = anvilCost;
            return this;
        }

        public EnchantmentBuilder validSlots(EquipmentSlot... slots) {
            this.slots.addAll(List.of(slots));
            return this;
        }

        public EnchantmentBuilder effects(Effect<?>... effects) {
            this.effects.addAll(Arrays.asList(effects));
            return this;
        }

        public EnchantmentBuilder attributes(Attributes.Attribute... attributes) {
            this.effects.add(new Attributes(attributes));
            return this;
        }

        @SafeVarargs
        public final EnchantmentBuilder tags(TagKey<net.minecraft.world.item.enchantment.Enchantment>... tags) {
            this.tags.addAll(Arrays.asList(tags));
            return this;
        }

        public EnchantmentDefinition build() {
            return new EnchantmentDefinition(id, name, weight, maxLevel, precludes.toArray(new String[0]), supported.toArray(new String[0]), primary.toArray(new String[0]), minBaseCost, maxBaseCost, minPerLevel, maxPerLevel, anvilCost, slots.toArray(new EquipmentSlot[0]), effects.toArray(new Effect[0]), tags.toArray(new TagKey[0]));
        }
    }
}

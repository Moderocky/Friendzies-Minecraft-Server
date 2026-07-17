package mx.kenzie.survival.potion;

import com.google.common.collect.BiMap;
import com.mojang.datafixers.util.Pair;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.papermc.paper.configuration.GlobalConfiguration;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.event.inventory.PaperInventoryMoveItemEvent;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.potion.PotionMix;
import io.papermc.paper.util.sanitizer.ItemObfuscationBinding;
import io.papermc.paper.util.sanitizer.ItemObfuscationSession;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.attributes.AttributeManager;
import mx.kenzie.survival.potion.effects.*;
import mx.kenzie.survival.tools.recipe.PotionEnchantingRecipe;
import mx.kenzie.survival.utility.DefaultMap;
import mx.kenzie.survival.utility.editor.DataComponentPatchEditor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import org.bukkit.*;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.potion.CraftPotionEffectType;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.*;
import java.util.logging.Level;

import static net.kyori.adventure.text.Component.*;
import static net.minecraft.core.component.DataComponents.*;
import static org.bukkit.potion.PotionEffectType.*;

public class PotionManager {

    public static final Predicate<? super ItemStack> VALID_POTION = item -> item.getItemMeta() instanceof PotionMeta meta && !meta.getAllEffects().isEmpty();
    public static final int LONG = ticks(3, 0), MEDIUM = ticks(1, 30), SHORT = ticks(0, 45);
    public static final int EXTENSION_TIME = 30 * 20;
    public static final int MAX_AMPLIFICATION = 5;
    public static final float SPLASH_FACTOR = 0.5F;
    public static final float LINGERING_FACTOR = 0.25F;
    public static final int MINIMUM_DURATION = 20 * 5, MAXIMUM_DURATION = (20 * 60) * 60;
    public static PotionEffectType INSTANT_DEATH, FREEZING, BIG, SMALL, RETURNING, BOUNCE, STEEL, FARSIGHT, TOUGHNESS, FEATHER_FALLING, HOMEWARD, EXPERIENCE, CORRUPTION, ZOMBIFICATION, PURIFICATION, UNDYING, JITTERING, STABILISER, SAFEKEEPING, DIMENSIONALITY, INERTIA, FLAMMABILITY, FLOUNDERING, NEARSIGHT, RUST, FRAGILITY, WEAK_KNEES,
            CLEANSING, CAMOUFLAGE, CRAWLING, FIZZING,
            MUMMIFYING, INVULNERABILITY, SPELUNKING, DELAYED_INSTANT_HEALTH, DELAYED_INSTANT_DAMAGE, DAYLIGHT_SAVING, MOONLIGHT;
    public static MobEffect DIMENSIONALITY_eff;
    protected static ItemStack PLACEHOLDER_POTION;
    protected static WatchGlass watchGlass;
    static int guideCounter;
    private static MobEffect INSTANT_DEATH_eff;
    private static MobEffect FREEZING_eff;
    private static MobEffect BIG_eff;
    private static MobEffect SMALL_eff;
    private static MobEffect RETURNING_eff;
    private static MobEffect BOUNCE_eff;
    private static MobEffect STEEL_eff;
    private static MobEffect FARSIGHT_eff;
    private static MobEffect TOUGHNESS_eff;
    private static MobEffect FEATHER_FALLING_eff;
    private static MobEffect HOMEWARD_eff;
    private static MobEffect EXPERIENCE_eff;
    private static MobEffect CORRUPTION_eff;
    private static MobEffect ZOMBIFICATION_eff;
    private static MobEffect PURIFICATION_eff;
    private static MobEffect UNDYING_eff;
    private static MobEffect JITTERING_eff;
    private static MobEffect STABILISER_eff;
    private static MobEffect SAFEKEEPING_eff;
    private static MobEffect INERTIA_eff;
    private static MobEffect FLAMMABILITY_eff;
    private static MobEffect FLOUNDERING_eff;
    private static MobEffect NEARSIGHT_eff;
    private static MobEffect RUST_eff;
    private static MobEffect FRAGILITY_eff;
    private static MobEffect WEAK_KNEES_eff;
    private static MobEffect CLEANSING_eff;
    private static MobEffect CAMOUFLAGE_eff;
    private static MobEffect CRAWLING_eff;
    private static MobEffect FIZZING_eff;
    private static MobEffect MUMMIFYING_eff, INVULNERABILITY_eff;
    private static MobEffect SPELUNKING_eff, DELAYED_INSTANT_HEALTH_eff, DELAYED_INSTANT_DAMAGE_eff;
    private static MobEffect DAYLIGHT_SAVING_eff, MOONLIGHT_eff;
    private static Method register;
    private static BiMap<Integer, PotionEffectType> ID_MAP;
    private static RecipeChoice INVERSION_INGREDIENT;
    private static NamespacedKey EFFECT_CONTAINER;
    protected final Predicate<? super PotionMeta> isSafeForReduction = base -> {
        List<PotionEffect> effects = base.getAllEffects();
        if (effects.isEmpty()) return false;
        for (PotionEffect effect : effects) {
            if (effect.getDuration() > MINIMUM_DURATION) return true;
        }
        return false;
    };
    private final Map<PotionEffectType, String> names = new DefaultMap<>(type -> type.getKey().value());
    private final DefaultMap<PotionEffectType, String> bottleNameOverrides = new DefaultMap<>(names);
    private final Map<PotionEffectType, Integer> defaultDurations = new HashMap<>();
    private final Map<PotionEffectType, PotionEffectType> opposites = new HashMap<>();
    private final Predicate<PotionMeta> hasEffects = base -> !base.getAllEffects().isEmpty();
    private final Predicate<PotionMeta> canBeDelayed = base -> {
        List<PotionEffect> effects = base.getAllEffects();
        if (effects.isEmpty()) return false;
        for (PotionEffect effect : effects) {
            if (effect.getType().isInstant()) return true;
        }
        return false;
    };
    private final Predicate<PotionMeta> canBeStabilised = base -> {
        List<PotionEffect> effects = base.getAllEffects();
        if (effects.isEmpty()) return false;
        for (PotionEffect effect : effects) {
            if (effect.getType() == STABILISER) return false;
        }
        return true;
    };
    private final Predicate<PotionMeta> canBeExtended = base -> {
        List<PotionEffect> effects = base.getAllEffects();
        if (effects.isEmpty()) return false;
        return effects.stream().anyMatch(effect -> !effect.getType().isInstant() && effect.getDuration() < MAXIMUM_DURATION);
    };
    private final Map<PotionEffectType, Integer> maxAmplifications = new HashMap<>();
    protected final Predicate<? super PotionMeta> isSafeForAmplification = base -> {
        List<PotionEffect> effects = base.getAllEffects();
        if (effects.isEmpty()) return false;
        for (PotionEffect effect : effects) {
            if (effect.getAmplifier() < this.getMaxAmplification(effect.getType())) return true;
        }
        return false;
    };
    private final Map<MobEffect, MobEffect> delayedVersions = new HashMap<>();
    private final List<PotionEffectType> ours = new ArrayList<>();
    protected Set<PotionBuilder> potions = new HashSet<>();
    protected Map<PotionEffectType, ItemStack> principalIngredients;
    private PotionBrewer potionBrewer;

    public PotionManager() {
    }

    static ItemStack inversionIngredient() {
        if (INVERSION_INGREDIENT != null) try {
            //noinspection deprecation
            return INVERSION_INGREDIENT.getItemStack();
        } catch (Exception _) {
        }
        return new ItemStack(Material.WARPED_FUNGUS);
    }

    private static int ticks(int minutes, int seconds) {
        return (minutes * 60 * 20) + (seconds * 20);
    }

    private static Holder<MobEffect> register0(String name, MobEffect effect) {
        try {
            return (Holder<MobEffect>) register.invoke(null, name, effect);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to register potion", e);
        }
        return null;
    }

    static String getTimer(int duration) {
        int minutes = duration / (60 * 20);
        int seconds = (duration / 20) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public static Iterable<PotionEffectType> allPotionTypes() {
        return Registry.MOB_EFFECT;
    }

    public static Collection<PotionEffect> getPotionEnchantmentEffects(ItemStack item) {
        if (item == null) return Collections.emptyList();
        PersistentDataContainerView container = item.getPersistentDataContainer();
        if (!container.has(EFFECT_CONTAINER, PersistentDataType.LIST.dataContainers())) return Collections.emptyList();
        List<PersistentDataContainer> list = container.getOrDefault(EFFECT_CONTAINER, PersistentDataType.LIST.dataContainers(), new ArrayList<>());
        List<PotionEffect> instances = new ArrayList<>();
        for (PersistentDataContainer persistentDataContainer : list) {
            PotionEffect effect = fromContainer(persistentDataContainer);
            if (effect == null) continue;
            instances.add(effect);
        }
        return instances;
    }

    public static void setPotionEnchantmentEffects(ItemStack item, Collection<PotionEffect> instances, ItemStack potion) {
        item.editPersistentDataContainer(to -> {
            List<PersistentDataContainer> list = new ArrayList<>();
            PersistentDataAdapterContext context = to.getAdapterContext();
            for (PotionEffect effect : instances) {
                list.add(toContainer(effect, context));
            }
            List<PersistentDataContainer> saved = PersistentDataType.LIST.dataContainers().toPrimitive(list, context);
            to.set(EFFECT_CONTAINER, PersistentDataType.LIST.dataContainers(), saved);
        });
    }

    private static PersistentDataContainer toContainer(PotionEffect effect, PersistentDataAdapterContext context) {
        PersistentDataContainer container = context.newPersistentDataContainer();
        container.set(NamespacedKey.minecraft("effect"), PersistentDataType.STRING, effect.getType().getKey().asString());
        container.set(NamespacedKey.minecraft("duration"), PersistentDataType.INTEGER, effect.getDuration());
        container.set(NamespacedKey.minecraft("amplifier"), PersistentDataType.INTEGER, effect.getAmplifier());
        return container;
    }

    @SuppressWarnings("DataFlowIssue")
    private static PotionEffect fromContainer(PersistentDataContainer container) {
        try {
            String string = container.get(NamespacedKey.minecraft("effect"), PersistentDataType.STRING);
            int duration = container.get(NamespacedKey.minecraft("duration"), PersistentDataType.INTEGER);
            int amplification = container.get(NamespacedKey.minecraft("amplifier"), PersistentDataType.INTEGER);
            NamespacedKey key = NamespacedKey.fromString(string);
            PotionEffectType type = Registry.MOB_EFFECT.get(key);
            return new PotionEffect(type, duration, amplification);
        } catch (NullPointerException _) {
            return null;
        }
    }

    private void registerPotionTypes() {
    }

    private void registerPotionEffects() {
        INSTANT_DEATH_eff = this.register("instant_death", (new InstantDeathMobEffect(MobEffectCategory.HARMFUL, Color.fromRGB(0, 0, 0).asRGB())));
        FREEZING_eff = this.register("freezing", (new FreezingMobEffect(MobEffectCategory.HARMFUL, Color.fromRGB(176, 248, 255).asRGB())).addAttributeModifier(Attributes.FRICTION_MODIFIER, Identifier.withDefaultNamespace("effect.freezing"), -0.25F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        BIG_eff = this.register("big", new SimpleMobEffect(MobEffectCategory.BENEFICIAL, Color.fromRGB(242, 35, 12).asRGB()).addAttributeModifier(Attributes.SCALE, Identifier.withDefaultNamespace("effect.big"), 0.5F, AttributeModifier.Operation.ADD_VALUE).addAttributeModifier(Attributes.STEP_HEIGHT, Identifier.withDefaultNamespace("effect.big"), 0.3F, AttributeModifier.Operation.ADD_VALUE).addAttributeModifier(Attributes.SAFE_FALL_DISTANCE, Identifier.withDefaultNamespace("effect.big"), 2F, AttributeModifier.Operation.ADD_VALUE).addAttributeModifier(Attributes.JUMP_STRENGTH, Identifier.withDefaultNamespace("effect.big"), 0.5F, AttributeModifier.Operation.ADD_VALUE).addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, Identifier.withDefaultNamespace("effect.big"), 0.3F, AttributeModifier.Operation.ADD_VALUE).addAttributeModifier(Attributes.BLOCK_INTERACTION_RANGE, Identifier.withDefaultNamespace("effect.big"), 1.0F, AttributeModifier.Operation.ADD_VALUE));
        SMALL_eff = this.register("small", new SimpleMobEffect(MobEffectCategory.BENEFICIAL, Color.fromRGB(209, 194, 192).asRGB()).addAttributeModifier(Attributes.SCALE, Identifier.withDefaultNamespace("effect.small"), -0.5F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE).addAttributeModifier(Attributes.STEP_HEIGHT, Identifier.withDefaultNamespace("effect.small"), -0.2F, AttributeModifier.Operation.ADD_VALUE).addAttributeModifier(Attributes.SAFE_FALL_DISTANCE, Identifier.withDefaultNamespace("effect.small"), 3F, AttributeModifier.Operation.ADD_VALUE).addAttributeModifier(Attributes.GRAVITY, Identifier.withDefaultNamespace("effect.small"), -0.1F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE).addAttributeModifier(Attributes.AIR_DRAG_MODIFIER, Identifier.withDefaultNamespace("effect.small"), -0.1F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE).addAttributeModifier(Attributes.JUMP_STRENGTH, Identifier.withDefaultNamespace("effect.small"), 0.2F, AttributeModifier.Operation.ADD_VALUE).addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, Identifier.withDefaultNamespace("effect.small"), -0.1F, AttributeModifier.Operation.ADD_VALUE).addAttributeModifier(Attributes.BLOCK_INTERACTION_RANGE, Identifier.withDefaultNamespace("effect.small"), -0.5F, AttributeModifier.Operation.ADD_VALUE));
        RETURNING_eff = this.register("returning", new ReturnMobEffect(MobEffectCategory.BENEFICIAL, Color.fromRGB(11, 99, 95).asRGB()));
        BOUNCE_eff = this.register("bounce", new BounceMobEffect(MobEffectCategory.BENEFICIAL, Color.fromRGB(147, 245, 173).asRGB()).addAttributeModifier(Attributes.AIR_DRAG_MODIFIER, Identifier.withDefaultNamespace("effect.bounce"), -0.5F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE).addAttributeModifier(Attributes.FRICTION_MODIFIER, Identifier.withDefaultNamespace("effect.bounce"), -0.5F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE).addAttributeModifier(Attributes.BOUNCINESS, Identifier.withDefaultNamespace("effect.bounce"), 0.5F, AttributeModifier.Operation.ADD_VALUE).addAttributeModifier(Attributes.SAFE_FALL_DISTANCE, Identifier.withDefaultNamespace("effect.bounce"), 8F, AttributeModifier.Operation.ADD_VALUE));
        STEEL_eff = this.register("steel", new SimpleMobEffect(MobEffectCategory.BENEFICIAL, Color.fromRGB(182, 187, 191).asRGB()).addAttributeModifier(Attributes.ARMOR, Identifier.withDefaultNamespace("effect.steel"), 1.0F, AttributeModifier.Operation.ADD_VALUE));
        RUST_eff = this.register("rust", new SimpleMobEffect(MobEffectCategory.HARMFUL, Color.fromRGB(201, 72, 16).asRGB()).addAttributeModifier(Attributes.ARMOR, Identifier.withDefaultNamespace("effect.rust"), -1.0F, AttributeModifier.Operation.ADD_VALUE));
        FARSIGHT_eff = this.register("farsight", new SimpleMobEffect(MobEffectCategory.BENEFICIAL, Color.fromRGB(5, 255, 234).asRGB()).addAttributeModifier(Attributes.BLOCK_INTERACTION_RANGE, Identifier.withDefaultNamespace("effect.farsight"), 0.5F, AttributeModifier.Operation.ADD_VALUE).addAttributeModifier(Attributes.CAMERA_DISTANCE, Identifier.withDefaultNamespace("effect.farsight"), 1F, AttributeModifier.Operation.ADD_VALUE));
        NEARSIGHT_eff = this.register("nearsight", new SimpleMobEffect(MobEffectCategory.HARMFUL, Color.fromRGB(201, 123, 177).asRGB()).addAttributeModifier(Attributes.BLOCK_INTERACTION_RANGE, Identifier.withDefaultNamespace("effect.nearsight"), -0.5F, AttributeModifier.Operation.ADD_VALUE).addAttributeModifier(Attributes.CAMERA_DISTANCE, Identifier.withDefaultNamespace("effect.nearsight"), -0.8F, AttributeModifier.Operation.ADD_VALUE));
        TOUGHNESS_eff = this.register("toughness", new SimpleMobEffect(MobEffectCategory.BENEFICIAL, Color.fromRGB(115, 156, 133).asRGB()).addAttributeModifier(Attributes.ARMOR_TOUGHNESS, Identifier.withDefaultNamespace("effect.toughness"), 1F, AttributeModifier.Operation.ADD_VALUE));
        FRAGILITY_eff = this.register("fragility", new SimpleMobEffect(MobEffectCategory.HARMFUL, Color.fromRGB(179, 175, 107).asRGB()).addAttributeModifier(Attributes.ARMOR_TOUGHNESS, Identifier.withDefaultNamespace("effect.fragility"), -1F, AttributeModifier.Operation.ADD_VALUE));
        FEATHER_FALLING_eff = this.register("feather_falling", new SimpleMobEffect(MobEffectCategory.BENEFICIAL, 255, 234, 214).addAttributeModifier(Attributes.SAFE_FALL_DISTANCE, Identifier.withDefaultNamespace("effect.feather_falling"), 8F, AttributeModifier.Operation.ADD_VALUE));
        WEAK_KNEES_eff = this.register("weak_knees", new SimpleMobEffect(MobEffectCategory.HARMFUL, 201, 159, 129).addAttributeModifier(Attributes.SAFE_FALL_DISTANCE, Identifier.withDefaultNamespace("effect.weak_knees"), -1F, AttributeModifier.Operation.ADD_VALUE));
        HOMEWARD_eff = this.register("homeward", new HomewardMobEffect(MobEffectCategory.BENEFICIAL, Color.fromRGB(151, 19, 240).asRGB()));
        EXPERIENCE_eff = this.register("experience", new ExperienceMobEffect(MobEffectCategory.BENEFICIAL, Color.fromRGB(197, 255, 23).asRGB()));
        CORRUPTION_eff = this.register("corrupting", new CorruptingMobEffect(MobEffectCategory.HARMFUL, Color.fromRGB(107, 40, 28).asRGB()));
        ZOMBIFICATION_eff = this.register("zombification", new ZombificationMobEffect(MobEffectCategory.HARMFUL, Color.fromRGB(26, 105, 37).asRGB()));
        PURIFICATION_eff = this.register("purification", new PurifyingMobEffect(MobEffectCategory.BENEFICIAL, Color.fromRGB(255, 250, 115).asRGB()));
        UNDYING_eff = this.register("undying", new UndyingMobEffect(MobEffectCategory.BENEFICIAL, 250, 214, 70));
        JITTERING_eff = this.register("jittering", new JitteringMobEffect(MobEffectCategory.BENEFICIAL, 237, 21, 234));
        STABILISER_eff = this.register("stabiliser", new StabiliserMobEffect(MobEffectCategory.NEUTRAL, 64, 77, 255));
        SAFEKEEPING_eff = this.register("safekeeping", new SafekeepingMobEffect(MobEffectCategory.BENEFICIAL, 139, 144, 214));
        DIMENSIONALITY_eff = this.register("dimensionality", new DimensionalityMobEffect(MobEffectCategory.BENEFICIAL, 225, 26, 232));
        INERTIA_eff = this.register("inertia", new SimpleMobEffect(MobEffectCategory.HARMFUL, 105, 98, 93).addAttributeModifier(Attributes.JUMP_STRENGTH, Identifier.withDefaultNamespace("effect.inertia"), -0.2F, AttributeModifier.Operation.ADD_VALUE));
        FLAMMABILITY_eff = this.register("flammability", new SimpleMobEffect(MobEffectCategory.HARMFUL, 140, 79, 56).addAttributeModifier(Attributes.BURNING_TIME, Identifier.withDefaultNamespace("effect.flammability"), 4.0F, AttributeModifier.Operation.ADD_VALUE));
        FLOUNDERING_eff = this.register("floundering", new SimpleMobEffect(MobEffectCategory.HARMFUL, 85, 83, 143).addAttributeModifier(Attributes.WATER_MOVEMENT_EFFICIENCY, Identifier.withDefaultNamespace("effect.floundering"), -0.3F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE).addAttributeModifier(Attributes.OXYGEN_BONUS, Identifier.withDefaultNamespace("effect.floundering"), -0.25F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        CLEANSING_eff = this.register("cleansing", new CleansingMobEffect(MobEffectCategory.BENEFICIAL, Color.WHITE.asRGB()));
        CAMOUFLAGE_eff = this.register("camouflage", new SimpleMobEffect(MobEffectCategory.NEUTRAL, 128, 76, 13).addAttributeModifier(Attributes.WAYPOINT_TRANSMIT_RANGE, Identifier.withDefaultNamespace("effect.camouflage"), -1F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL).addAttributeModifier(Attributes.NAME_TAG_DISTANCE, Identifier.withDefaultNamespace("effect.camouflage"), -1F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        CRAWLING_eff = this.register("crawling", new CrawlingMobEffect(MobEffectCategory.HARMFUL, 61, 54, 43));
        FIZZING_eff = this.register("fizzing", new FizzingMobEffect(MobEffectCategory.HARMFUL, 224, 227, 143));
        MUMMIFYING_eff = this.register("mummifying", new MummifyingMobEffect(MobEffectCategory.HARMFUL, 240, 229, 182));
        SPELUNKING_eff = this.register("spelunking", new SpelunkingMobEffect(MobEffectCategory.BENEFICIAL, 50, 139, 168));
        INVULNERABILITY_eff = this.register("invulnerability", new SimpleMobEffect(MobEffectCategory.BENEFICIAL, 44, 31, 32));

        DAYLIGHT_SAVING_eff = this.register("daylight_saving", new TimeMobEffect(MobEffectCategory.NEUTRAL, ClockTimeMarkers.DAY, 118, 219, 245));
        MOONLIGHT_eff = this.register("moonlight", new TimeMobEffect(MobEffectCategory.NEUTRAL, ClockTimeMarkers.NIGHT, 233, 244, 247));

        DELAYED_INSTANT_HEALTH_eff = this.register("delayed_instant_health", new ExpiringMobEffect(MobEffectCategory.BENEFICIAL, 16262179, MobEffects.INSTANT_HEALTH));
        DELAYED_INSTANT_DAMAGE_eff = this.register("delayed_instant_damage", new ExpiringMobEffect(MobEffectCategory.HARMFUL, 11101546, MobEffects.INSTANT_DAMAGE));

    }

    private void registerBukkitTypes() {
        INSTANT_DEATH = fix(INSTANT_DEATH_eff, "Instant Death", 0, 0);
        FREEZING = fix(FREEZING_eff, "Freezing", MEDIUM, 1);
        BIG = fix(BIG_eff, "Big", LONG);
        SMALL = fix(SMALL_eff, "Small", LONG, 3);
        RETURNING = fix(RETURNING_eff, "Returning", 0, 0);
        BOUNCE = fix(BOUNCE_eff, "Bouncing", MEDIUM, 3);
        STEEL = fix(STEEL_eff, "Steel", LONG);
        FARSIGHT = fix(FARSIGHT_eff, "Farsight", LONG);
        TOUGHNESS = fix(TOUGHNESS_eff, "Toughness", MEDIUM, 2);
        FEATHER_FALLING = fix(FEATHER_FALLING_eff, "Feather Falling", LONG);
        HOMEWARD = fix(HOMEWARD_eff, "Homeward", 0, 0);
        EXPERIENCE = fix(EXPERIENCE_eff, "Experience", 0, 19);
        CORRUPTION = fix(CORRUPTION_eff, "Corruption", 0, 0);
        ZOMBIFICATION = fix(ZOMBIFICATION_eff, "Zombification", 0, 0);
        PURIFICATION = fix(PURIFICATION_eff, "Purification", 0, 0);
        UNDYING = fix(UNDYING_eff, "Undying", SHORT, 0);
        JITTERING = fix(JITTERING_eff, "Jittering", SHORT, 2);
        STABILISER = fix(STABILISER_eff, "Stable", 0, 0);
        SAFEKEEPING = fix(SAFEKEEPING_eff, "Safekeeping", LONG, 3);
        DIMENSIONALITY = fix(DIMENSIONALITY_eff, "Teleportation", 0, 0);
        INERTIA = fix(INERTIA_eff, "Inertia", MEDIUM, 3);
        FLAMMABILITY = fix(FLAMMABILITY_eff, "Flammability", MEDIUM, 3);
        FLOUNDERING = fix(FLOUNDERING_eff, "Floundering", SHORT, 4);
        NEARSIGHT = fix(NEARSIGHT_eff, "Near Sight", LONG);
        RUST = fix(RUST_eff, "Rust", SHORT);
        FRAGILITY = fix(FRAGILITY_eff, "Fragility", MEDIUM, 4);
        WEAK_KNEES = fix(WEAK_KNEES_eff, "Weak Knees", SHORT, 3);
        CLEANSING = fix(CLEANSING_eff, "Cleansing", 0, 0);
        CAMOUFLAGE = fix(CAMOUFLAGE_eff, "Camouflage", LONG, 0);
        CRAWLING = fix(CRAWLING_eff, "Crawling", SHORT, 0);
        FIZZING = fix(FIZZING_eff, "Fizzing", SHORT, 0);
        MUMMIFYING = fix(MUMMIFYING_eff, "Mummification", SHORT, 0);
        SPELUNKING = fix(SPELUNKING_eff, "Spelunking", LONG, 0);
        INVULNERABILITY = fix(INVULNERABILITY_eff, "Invulnerability", SHORT, 1);
        DAYLIGHT_SAVING = fix(DAYLIGHT_SAVING_eff, "Daylight Saving", 0, 0);
        MOONLIGHT = fix(MOONLIGHT_eff, "Moonlight", 0, 0);
        bottleNameOverrides.put(STABILISER, "Inert Potion");
        bottleNameOverrides.put(CLEANSING, "Milk Bottle");

        this.fixBukkit(REGENERATION, SHORT, 3);
        this.fixBukkit(POISON, SHORT, MAX_AMPLIFICATION);
        this.fixBukkit(SLOW_FALLING, MEDIUM, 3);
        this.fixBukkit(WEAKNESS, MEDIUM, 3);
        this.fixBukkit(BLINDNESS, MEDIUM, 0);
        this.fixBukkit(DARKNESS, SHORT, 0);
        this.fixBukkit(NAUSEA, SHORT, 0);
        this.fixBukkit(NIGHT_VISION, LONG, 0);
        this.fixBukkit(WATER_BREATHING, LONG, 0);
        this.fixBukkit(INVISIBILITY, LONG, 0);
        this.fixBukkit(FIRE_RESISTANCE, LONG, 0);
        this.fixBukkit(GLOWING, LONG, 0);
        this.fixBukkit(RESISTANCE, SHORT, 2);
        this.fixBukkit(LUCK, SHORT, 1);
        this.fixBukkit(UNLUCK, SHORT, 1);
        this.fixBukkit(HERO_OF_THE_VILLAGE, LONG, MAX_AMPLIFICATION);
        this.fixBukkit(DOLPHINS_GRACE, SHORT, 1);
        this.fixBukkit(CONDUIT_POWER, SHORT, 3);
        this.fixBukkit(WITHER, SHORT, 3);
        this.fixBukkit(HUNGER, SHORT, 2);
        this.fixBukkit(SATURATION, SHORT, MAX_AMPLIFICATION);
        this.fixBukkit(MINING_FATIGUE, SHORT, MAX_AMPLIFICATION);
        this.fixBukkit(HASTE, MEDIUM, 1);
        this.fixBukkit(INSTANT_HEALTH, 0, 9);
        this.fixBukkit(INSTANT_DAMAGE, 0, 9);
        this.fixBukkit(OOZING, LONG, 0);
        this.fixBukkit(WEAVING, LONG, 0);
        this.fixBukkit(INFESTED, LONG, 0);
        this.fixBukkit(WIND_CHARGED, LONG, 0);
        this.fixBukkit(STRENGTH, LONG, 2);
        this.fixBukkit(LEVITATION, SHORT, 1);
        this.fixBukkit(POISON, LONG, 3);
        this.fixBukkit(ABSORPTION, MEDIUM, MAX_AMPLIFICATION);
        this.fixBukkit(HEALTH_BOOST, LONG, 3);
        this.fixBukkit(SLOWNESS, LONG, 2);
        this.fixBukkit(SPEED, LONG, 2);
        DELAYED_INSTANT_HEALTH = fix(DELAYED_INSTANT_HEALTH_eff, "Instant Health", 0, 9);
        DELAYED_INSTANT_DAMAGE = fix(DELAYED_INSTANT_DAMAGE_eff, "Instant Damage", 0, 9);

    }

    private void registerGuide() {
        for (PotionGuide value : PotionGuide.values()) {
            value.register();
        }
        PotionGuide.thenDoPotions();
    }

    private void registerPotionRecipes() {
        //<editor-fold desc="Empty Bottle Effects" defaultstate="collapsed">
        this.buildPotion("water_fill").setIngredient(Material.WATER_BUCKET).setIngredientResult(i -> i).setBase(Material.GLASS_BOTTLE).setResult(potion -> potion.setBasePotionType(PotionType.WATER)).build();
        this.buildPotion("milk_fill").setIngredient(Material.MILK_BUCKET).setIngredientResult(_ -> new ItemStack(Material.BUCKET)).setBase(Material.GLASS_BOTTLE).setResult(this.createBasePotion(CLEANSING)).build();
        //</editor-fold>

        //<editor-fold desc="Water Bottle Effects" defaultstate="collapsed">
        this.buildPotion("ominous").setIngredient(Material.POISONOUS_POTATO).setBase(PotionType.WATER).setResult(new ItemStack(Material.OMINOUS_BOTTLE), OminousBottleMeta.class, bottle -> {
            bottle.setMaxStackSize(16);
            bottle.setAmplifier(0);
        }).build();
        //</editor-fold>

        //<editor-fold desc="Honey Bottle Effects" defaultstate="collapsed">
        this.buildPotion("saturation_potion").setIngredient(Material.GOLDEN_CARROT).setBase(Material.HONEY_BOTTLE).setResult(this.createBasePotion(SATURATION)).build();
        this.buildPotion("hunger_potion").setIngredient(Material.ROTTEN_FLESH).setBase(Material.HONEY_BOTTLE).setResult(this.createBasePotion(HUNGER)).build();
        this.buildPotion("haste_potion").setIngredient(Material.GOLDEN_DANDELION).setBase(Material.HONEY_BOTTLE).setResult(this.createBasePotion(HASTE)).build();
        //</editor-fold>

        //<editor-fold desc="Ominous Effects">
        this.buildPotion("hero_of_the_village").setIngredient(Material.EMERALD).setBase(Material.OMINOUS_BOTTLE).setResult(this.createBasePotion(HERO_OF_THE_VILLAGE)).modifyResultByBase((base, result) -> {
            if (!(base.getItemMeta() instanceof OminousBottleMeta bottle)) return result;
            if (!(result.getItemMeta() instanceof PotionMeta potion)) return result;
            int amplifier = bottle.hasAmplifier() ? bottle.getAmplifier() : 0;
            potion.addCustomEffect(new PotionEffect(HERO_OF_THE_VILLAGE, LONG, amplifier, false), true);
            result.setItemMeta(potion);
            return result;
        }).build();
        this.buildPotion("ominous_amplification_extension").setIngredient(Material.GLOWSTONE_DUST).setBase(Material.OMINOUS_BOTTLE).setBase(item -> item.getType() == Material.OMINOUS_BOTTLE && item.getItemMeta() instanceof OminousBottleMeta bottle && (!bottle.hasAmplifier() || bottle.getAmplifier() < 4)).setResult(new ItemStack(Material.OMINOUS_BOTTLE)).modifyResultByBase((base, result) -> {
            if (base.getItemMeta() instanceof OminousBottleMeta from && result.getItemMeta() instanceof OminousBottleMeta to) {
                if (!from.hasAmplifier()) to.setAmplifier(0);
                else to.setAmplifier(from.getAmplifier() + 1);
                result.setItemMeta(to);
            }
            return result;
        }).build();
        //noinspection UnstableApiUsage
        this.buildPotion("instant_death").setIngredient(RecipeChoice.itemType(ItemType.SKELETON_SKULL, ItemType.WITHER_SKELETON_SKULL)).setBase(Material.OMINOUS_BOTTLE).setResult(this.createBasePotion(INSTANT_DEATH)).build();
        this.buildPotion("corrupting").setIngredient(Material.RESIN_CLUMP).setBase(Material.OMINOUS_BOTTLE).setResult(this.createBasePotion(CORRUPTION)).build();
        this.buildPotion("zombification").setIngredient(Material.ROTTEN_FLESH).setBase(Material.OMINOUS_BOTTLE).setResult(this.createBasePotion(ZOMBIFICATION)).build();
        this.buildPotion("purification").setIngredient(Material.GOLD_INGOT).setBase(Material.OMINOUS_BOTTLE).setResult(this.createBasePotion(PURIFICATION)).build();
        this.buildPotion("withering").setIngredient(Material.WITHER_ROSE).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(WITHER)).build();
        //</editor-fold>

        //<editor-fold desc="Mundane Effects" defaultstate="collapsed">
        this.buildPotion("dilution_mundane").setIngredient(item -> item.getType() == Material.POTION && item.getItemMeta() instanceof PotionMeta potion && potion.hasCustomEffects()).setIngredientResult(_ -> new ItemStack(Material.GLASS_BOTTLE)).setBasePotion(potion -> potion.getBasePotionType() == PotionType.MUNDANE && potion.getAllEffects().isEmpty()).modifyResultByIngredient(this::getEmptyCopy).modifyResult(this::dilute).build();
        //</editor-fold>

        //<editor-fold desc="Thick Effects">
        this.buildPotion("homeward").setIngredient(Material.CRYING_OBSIDIAN).setBase(PotionType.THICK).setResult(this.createBasePotion(HOMEWARD)).build();
        this.buildPotion("weaving").setIngredient(new RecipeChoice.MaterialChoice(Tag.WOOL)).setBase(PotionType.THICK).setResult(this.createBasePotion(WEAVING)).build();
        this.buildPotion("crawling").setIngredient(Material.COBWEB).setBase(PotionType.THICK).setResult(this.createBasePotion(CRAWLING)).build();
        this.buildPotion("fizzing").setIngredient(Material.SULFUR).setBase(PotionType.THICK).setResult(this.createBasePotion(FIZZING)).build();
        this.buildPotion("oozing").setIngredient(Material.SLIME_BLOCK).setBase(PotionType.THICK).setResult(this.createBasePotion(OOZING)).build();
        this.buildPotion("infested").setIngredient(Material.STONE).setBase(PotionType.THICK).setResult(this.createBasePotion(INFESTED)).build();
        this.buildPotion("mummifying").setIngredient(Material.SAND).setBase(PotionType.THICK).setResult(this.createBasePotion(MUMMIFYING)).build();
        this.buildPotion("experience").setIngredient(Material.SCULK).setBase(PotionType.THICK).setResult(this.createBasePotion(EXPERIENCE)).build();
        this.buildPotion("turtle_master").setIngredient(Material.TURTLE_HELMET).setBase(PotionType.THICK).setResult(potion -> potion.setBasePotionType(PotionType.TURTLE_MASTER)).build();
        this.buildPotion("experience_amplification").setIngredient(Material.SCULK).setBase(VALID_POTION).setBasePotion(this.hasEffect(EXPERIENCE, this.getMaxAmplification(EXPERIENCE) - 1)).setResult(new ItemStack(Material.POTION)).modifyResultByBase(this::getEmptyCopy).modifyResultByBasePotion(this.modify(EXPERIENCE, this::extendAmplificationUnsafe)).build();
        //</editor-fold>

        //<editor-fold desc="Awkward Effects" defaultstate="collapsed">
        this.buildPotion("weakness").setIngredient(Material.FERMENTED_SPIDER_EYE).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(WEAKNESS)).build();
        this.buildPotion("strength").setIngredient(Material.BLAZE_POWDER).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(STRENGTH)).build();
        this.buildPotion("speed").setIngredient(Material.SUGAR).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(SPEED)).build();
        this.buildPotion("jump_boost").setIngredient(Material.COCOA_BEANS).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(JUMP_BOOST)).build();
        this.buildPotion("instant_health").setIngredient(Material.GLISTERING_MELON_SLICE).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(INSTANT_HEALTH)).build();
        this.buildPotion("instant_damage").setIngredient(Material.RED_MUSHROOM).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(INSTANT_DAMAGE)).build();
        this.buildPotion("poison").setIngredient(Material.SPIDER_EYE).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(POISON)).build();
        this.buildPotion("regeneration").setIngredient(Material.GHAST_TEAR).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(REGENERATION)).build();
        this.buildPotion("regeneration_powder").setIngredient(Ingredient.POWDERED_GHAST_TEAR.asItem()).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(REGENERATION)).build();
        this.buildPotion("fire_resistance").setIngredient(Material.MAGMA_CREAM).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(FIRE_RESISTANCE)).build();
        this.buildPotion("water_breathing").setIngredient(Material.PUFFERFISH).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(WATER_BREATHING)).build();
        this.buildPotion("night_vision").setIngredient(Material.GOLDEN_CARROT).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(NIGHT_VISION)).build();
        this.buildPotion("slow_falling").setIngredient(Material.PHANTOM_MEMBRANE).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(SLOW_FALLING)).build();
        this.buildPotion("absorption").setIngredient(Material.GOLDEN_APPLE).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(ABSORPTION)).build();
        this.buildPotion("wind_charged").setIngredient(Material.BREEZE_ROD).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(WIND_CHARGED)).build();
        this.buildPotion("health_boost").setIngredient(Material.GHAST_TEAR).setBasePotion(this.hasEffect(ABSORPTION)).setResult(this.createBasePotion(HEALTH_BOOST)).modifyResultByBasePotion(this.modify(ABSORPTION, HEALTH_BOOST)).build();
        this.buildPotion("darkness").setIngredient(Material.INK_SAC).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(DARKNESS)).build();
        this.buildPotion("nausea").setIngredient(Material.TROPICAL_FISH).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(NAUSEA)).build();
        this.buildPotion("luck_potion").setIngredient(Material.RABBIT_FOOT).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(LUCK)).build();
        this.buildPotion("unluck_potion").setIngredient(Material.FIRE_CHARGE).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(LUCK)).build();
        this.buildPotion("glowing_potion").setIngredient(Material.GLOW_INK_SAC).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(GLOWING)).build();
        this.buildPotion("resistance_potion").setIngredient(Material.TURTLE_SCUTE).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(RESISTANCE)).build();
        this.buildPotion("levitation_potion").setIngredient(Material.POPPED_CHORUS_FRUIT).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(LEVITATION)).build();
        this.buildPotion("freezing").setIngredient(Material.POWDER_SNOW_BUCKET).setBase(PotionType.AWKWARD).setIngredientResult(_ -> new ItemStack(Material.BUCKET)).setResult(this.createBasePotion(FREEZING)).build();
        this.buildPotion("freezing_powder").setIngredient(Ingredient.POWDERED_SNOW.asItem()).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(FREEZING)).build();
        this.buildPotion("big").setIngredient(Material.COOKED_CHICKEN).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(BIG)).build();
        this.buildPotion("returning").setIngredient(Material.ECHO_SHARD).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(RETURNING)).build();
        this.buildPotion("steel").setIngredient(Material.IRON_INGOT).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(STEEL)).build();
        this.buildPotion("toughness").setIngredient(Material.ARMADILLO_SCUTE).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(TOUGHNESS)).build();
        this.buildPotion("farsight").setIngredient(Material.AMETHYST_SHARD).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(FARSIGHT)).build();
        this.buildPotion("farsight_powder").setIngredient(Ingredient.POWDERED_AMETHYST_SHARD.asItem()).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(FARSIGHT)).build();
        this.buildPotion("bounce").setIngredient(Material.SLIME_BALL).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(BOUNCE)).build();
        this.buildPotion("feather_falling").setIngredient(Material.FEATHER).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(FEATHER_FALLING)).build();
        this.buildPotion("undying").setIngredient(Material.TOTEM_OF_UNDYING).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(UNDYING)).build();
        this.buildPotion("jittering").setIngredient(Material.CHORUS_FRUIT).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(JITTERING)).build();
        this.buildPotion("safekeeping").setIngredient(Material.CRIMSON_FUNGUS).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(SAFEKEEPING)).build();
        this.buildPotion("dimensionality").setIngredient(Material.ENDER_EYE).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(DIMENSIONALITY)).build();
        this.buildPotion("flammability").setIngredient(Material.COAL).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(FLAMMABILITY)).build();
        this.buildPotion("fragility").setIngredient(Material.BONE).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(FRAGILITY)).build();
        this.buildPotion("floundering").setIngredient(Material.KELP).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(FLOUNDERING)).build();
        this.buildPotion("nautilus").setIngredient(Material.NAUTILUS_SHELL).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(BREATH_OF_THE_NAUTILUS)).build();
        this.buildPotion("conduit_power").setIngredient(Material.HEART_OF_THE_SEA).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(CONDUIT_POWER)).build();
        this.buildPotion("camouflage").setIngredient(Material.LEAF_LITTER).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(CAMOUFLAGE)).build();
        this.buildPotion("fizzing_powder").setIngredient(Ingredient.POWDERED_SULFUR.asItem()).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(FIZZING)).build();
        this.buildPotion("invulnerability").setIngredient(Material.NETHERITE_SCRAP).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(INVULNERABILITY)).build();
        this.buildPotion("spelunking").setIngredient(Material.QUARTZ).setBasePotion(this.hasEffect(DIMENSIONALITY)).setResult(this.createBasePotion(SPELUNKING)).modifyResultByBasePotion(this.modify(DIMENSIONALITY, SPELUNKING)).build();
        this.buildPotion("daylight_saving").setIngredient(Material.SUNFLOWER, Material.CLOSED_EYEBLOSSOM).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(DAYLIGHT_SAVING)).build();
        this.buildPotion("moonlight").setIngredient(Material.OPEN_EYEBLOSSOM).setBase(PotionType.AWKWARD).setResult(this.createBasePotion(MOONLIGHT)).build();
        //</editor-fold>

        //<editor-fold desc="Meta Modification" defaultstate="collapsed">
        this.buildPotion("delaying").setIngredient(Material.QUARTZ).setBase(Material.POTION).setBasePotion(canBeDelayed).modifyResultByBase(this::getEmptyCopy).modifyResultByBasePotion(this::delay).build();
        this.buildPotion("inversion").setIngredient(INVERSION_INGREDIENT).setBasePotion(this::hasOpposite).modifyResultByBase(this::getEmptyCopy).modifyResultByBasePotion(this::invert).build();
        this.buildPotion("dilution").setIngredient(Material.POTION).setIngredientResult(_ -> new ItemStack(Material.GLASS_BOTTLE)).setBasePotion(this.hasEffect(STABILISER)).setResult(this.createBasePotion(STABILISER)).modifyResult(this::dilute).build();
        this.buildPotion("stabilisation").setIngredient(Material.LAPIS_LAZULI).setBase(VALID_POTION).setBasePotion(canBeStabilised).setResult(this.createBasePotion(STABILISER)).modifyResultByBase((base, _) -> base.clone()).modifyResultByBasePotion((_, potion) -> {
            potion.addCustomEffect(new PotionEffect(STABILISER, 0, 0, false), false);
            return potion;
        }).build();
        this.buildPotion("permanence").setIngredient(Material.NETHER_STAR).setBase(VALID_POTION).setBasePotion(canBeExtended).setResult(new ItemStack(Material.POTION)).modifyResultByBase(this::getEmptyCopy).modifyResultByBasePotion(this.modify(type -> !type.isInstant(), effect -> effect.withDuration(PotionEffect.INFINITE_DURATION))).build();
        this.buildPotion("length_extension").setIngredient(Material.REDSTONE).setBase(VALID_POTION).setBasePotion(canBeExtended).setResult(new ItemStack(Material.POTION)).modifyResultByBase(this::getEmptyCopy).modifyResultByBase(this.extendDuration(1)).build();
        this.buildPotion("length_extension_9").setIngredient(Material.REDSTONE_BLOCK).setBase(VALID_POTION).setBasePotion(canBeExtended).setResult(new ItemStack(Material.POTION)).modifyResultByBase(this::getEmptyCopy).modifyResultByBase(this.extendDuration(9)).build();
        this.buildPotion("length_reduction").setIngredient(Material.CHARCOAL).setBase(VALID_POTION).setBasePotion(isSafeForReduction).setResult(new ItemStack(Material.POTION)).modifyResultByBase(this::getEmptyCopy).modifyResultByBasePotion(this.modify(_ -> true, this::reduceDuration)).build();
        this.buildPotion("amplification_extension").setIngredient(Material.GLOWSTONE_DUST).setBase(VALID_POTION).setBasePotion(isSafeForAmplification).setResult(new ItemStack(Material.POTION)).modifyResultByBase(this::getEmptyCopy).modifyResultByBasePotion(this.modify(_ -> true, this::extendAmplification)).build();
        this.buildPotion("amplification_extension_4").setIngredient(Material.GLOWSTONE).setBase(VALID_POTION).setBasePotion(isSafeForAmplification).setResult(new ItemStack(Material.POTION)).modifyResultByBase(this::getEmptyCopy).modifyResultByBasePotion(this.modify(_ -> true, this.extendAmplification(4))).build();
        //</editor-fold>

        //<editor-fold desc="Potion Type Conversion" defaultstate="collapsed">
        this.buildPotion("splash_conversion").setIngredient(Material.GUNPOWDER).setBase(Material.POTION).setBasePotion(hasEffects).setResult(new ItemStack(Material.SPLASH_POTION)).modifyResult(this::copyCompanionData).modifyResultByBasePotion(this::copyColour).modifyResultByBasePotion(this.modify(_ -> true, effect -> this.multiplyDuration(effect, SPLASH_FACTOR))).build();
        this.buildPotion("lingering_conversion").setIngredient(Material.DRAGON_BREATH).setBase(Material.POTION).setBasePotion(hasEffects).setResult(new ItemStack(Material.LINGERING_POTION)).modifyResult(this::copyCompanionData).modifyResultByBasePotion(this::copyColour).modifyResultByBasePotion(this.modify(_ -> true, effect -> this.multiplyDuration(effect, LINGERING_FACTOR))).build();
        //</editor-fold>

        //<editor-fold desc="Distillation" defaultstate="collapsed">
        this.buildPotion("ingredient_separation").setIngredient(Material.POTION).setBase(WatchGlass::isWatchGlass).setIngredientResult(_ -> new ItemStack(Material.GLASS_BOTTLE)).setResult(WatchGlass.ITEM.clone()).modifyResult(WatchGlass::distill).build();

        //</editor-fold>
    }

    protected void registerPotionOpposites() {
        this.registerPotionOpposite(STRENGTH, WEAKNESS);
        this.registerPotionOpposite(SPEED, SLOWNESS);
        this.registerPotionOpposite(REGENERATION, POISON);
        this.registerPotionOpposite(LUCK, UNLUCK);
        this.registerPotionOpposite(INSTANT_HEALTH, INSTANT_DAMAGE);
        this.registerPotionOpposite(SATURATION, HUNGER);
        this.registerPotionOpposite(SLOW_FALLING, LEVITATION);
        this.registerPotionOpposite(JUMP_BOOST, INERTIA);
        this.registerPotionOpposite(FIRE_RESISTANCE, FLAMMABILITY);
        this.registerPotionOpposite(NIGHT_VISION, BLINDNESS);
        this.registerPotionOpposite(INVISIBILITY, GLOWING);
//        this.registerPotionOpposite(HERO_OF_THE_VILLAGE, BAD_OMEN);
        this.registerPotionOpposite(TOUGHNESS, FRAGILITY);
        this.registerPotionOpposite(WATER_BREATHING, FLOUNDERING);
        this.registerPotionOpposite(STEEL, RUST);
        this.registerPotionOpposite(PURIFICATION, CORRUPTION);
        this.registerPotionOpposite(FARSIGHT, NEARSIGHT);
        this.registerPotionOpposite(BIG, SMALL);
        this.registerPotionOpposite(DAYLIGHT_SAVING, MOONLIGHT);

        this.registerOneWayOpposite(ZOMBIFICATION, PURIFICATION);
        this.registerOneWayOpposite(UNDYING, WITHER);
        this.registerOneWayOpposite(FREEZING, FLAMMABILITY);
        this.registerOneWayOpposite(CAMOUFLAGE, GLOWING);

    }

    protected boolean hasOpposite(PotionMeta meta) {
        if (meta == null) return false;
        List<PotionEffect> effects = meta.getAllEffects();
        if (effects.isEmpty()) return false;
        for (PotionEffect effect : effects) {
            if (opposites.containsKey(effect.getType())) return true;
        }
        return false;
    }

    protected PotionMeta invert(PotionMeta base, PotionMeta result) {
        result.clearCustomEffects();
        for (PotionEffect effect : base.getAllEffects()) {
            result.addCustomEffect(effect.withType(this.invert(effect.getType())), true);
        }
        return result;
    }

    protected PotionEffectType invert(PotionEffectType type) {
        return opposites.getOrDefault(type, type);
    }

    private void registerOneWayOpposite(PotionEffectType type, PotionEffectType other) {
        opposites.put(type, other);
    }

    private void registerPotionOpposite(PotionEffectType type, PotionEffectType other) {
        opposites.put(type, other);
        opposites.put(other, type);
    }

    protected <Data> void copyCompanionData(ItemMeta ingredient, ItemMeta base, ItemMeta result, Function<ItemMeta, @Nullable Data> retrieve, BiConsumer<ItemMeta, Data> mutate) {
        Data existing = retrieve.apply(result);
        if (existing != null) return;
        Data a = retrieve.apply(ingredient), b = retrieve.apply(base);
        mutate.accept(result, a != null ? a : b);
    }

    protected <Data> void copyCompanionData(ItemMeta from, ItemMeta to, Function<ItemMeta, @Nullable Data> retrieve, BiConsumer<ItemMeta, Data> mutate) {
        Data existing = retrieve.apply(to);
        if (existing != null) return;
        mutate.accept(to, retrieve.apply(from));
    }

    public ItemStack copyCompanionData(ItemStack from, ItemStack to) {
        ItemMeta a = from.getItemMeta(), b = to.getItemMeta();
        copyCompanionData(a, b, DimensionalityMobEffect::getLocation, DimensionalityMobEffect::setLocation);
        copyCompanionData(a, b, SpelunkingMobEffect::getLocation, SpelunkingMobEffect::setLocation);
        to.setItemMeta(b);
        return to;
    }

    protected ItemStack copyCompanionData(ItemStack ingredient, ItemStack base, ItemStack result) {
        ItemMeta a = ingredient.getItemMeta(), b = base.getItemMeta(), c = result.getItemMeta();
        copyCompanionData(a, b, c, DimensionalityMobEffect::getLocation, DimensionalityMobEffect::setLocation);
        copyCompanionData(a, b, c, SpelunkingMobEffect::getLocation, SpelunkingMobEffect::setLocation);
        result.setItemMeta(c);
        return result;
    }

    protected ItemStack dilute(ItemStack ingredient, ItemStack base, ItemStack result) {
        if (ingredient == null) return result;
        if (base == null) return result;
        result = base.clone();
        if (!(ingredient.getItemMeta() instanceof PotionMeta potion)) return result;
        if (!(result.getItemMeta() instanceof PotionMeta meta)) return result;
        for (PotionEffect effect : potion.getAllEffects()) {
            int duration = effect.getDuration();
            if (duration == PotionEffect.INFINITE_DURATION) duration = LONG * 12;
            PotionEffect copy = effect.withAmplifier(Math.max(0, effect.getAmplifier() / 3)).withDuration(Math.max(MINIMUM_DURATION, duration / 3));
            meta.addCustomEffect(copy, false);
        }
        meta.removeCustomEffect(STABILISER);
        result.setItemMeta(meta);
        result = this.copyCompanionData(ingredient, base, result);
        this.fixPotionDuringCreation(result);
        return result;
    }

    protected ItemStack createBasePotion(PotionEffectType type) {
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta potion = (PotionMeta) item.getItemMeta();
        potion.addCustomEffect(new PotionEffect(type, defaultDuration(type), 0, false), true);
        potion.setColor(type.getColor());
        item.setItemMeta(potion);
        if (!potion.getAllEffects().isEmpty()) {
            Component value = this.bottleName(item, potion);
            item.setData(DataComponentTypes.ITEM_NAME, value);
            item.setData(DataComponentTypes.CUSTOM_NAME, value);
        }
        return item;
    }

    protected PotionMeta delay(PotionMeta base, PotionMeta result) {
        result.clearCustomEffects();
        for (PotionEffect effect : base.getAllEffects()) {
            PotionEffectType type = this.delayedVersion(effect.getType());
            if (type != null)
                result.addCustomEffect(effect.withType(type).withDuration(this.defaultDuration(type)), true);
            else result.addCustomEffect(effect, true);
        }
        return result;
    }

    public @Nullable PotionEffectType delayedVersion(PotionEffectType type) {
        NamespacedKey key = type.getKey();
        return Registry.MOB_EFFECT.get(new NamespacedKey(key.namespace(), "delayed_" + key.value()));
    }

    protected ItemStack createSafePotion(PotionEffectType type) {
        ItemStack stack = new ItemStack(Material.POTION);
        PotionMeta potion = ((PotionMeta) stack.getItemMeta());
        potion.addCustomEffect(new PotionEffect(type, defaultDuration(type), 0, false), false);
        potion.setColor(type.getColor());
        stack.setItemMeta(potion);
        this.fixPotionDuringCreation(stack);
        if (!potion.getAllEffects().isEmpty()) {
            Component value = this.bottleName(stack, potion);
            stack.setData(DataComponentTypes.ITEM_NAME, value);
            stack.setData(DataComponentTypes.CUSTOM_NAME, value);
        }
        // remove so we don't crash client
        if (ours.contains(type)) {
            potion = ((PotionMeta) stack.getItemMeta());
            for (PotionEffect effect : potion.getAllEffects()) {
                potion.removeCustomEffect(effect.getType());
            }
            stack.setItemMeta(potion);
        }
        return stack;
    }

    protected int defaultDuration(PotionEffectType type) {
        return defaultDurations.getOrDefault(type, LONG);
    }

    protected int adjustedDuration(PotionEffectType type, Material kind) {
        int duration = defaultDuration(type) / 3;
        if (kind == Material.SPLASH_POTION) duration = (int) (SPLASH_FACTOR * duration);
        if (kind == Material.LINGERING_POTION) duration = (int) (LINGERING_FACTOR * duration);
        return duration;
    }

    private Component getItemName(ItemStack stack, PotionMeta meta) {
        Component component = meta.customName();
        if (component != null) return component;
        return stack.getData(DataComponentTypes.ITEM_NAME);
    }

    protected Component bottleName(ItemStack stack, PotionMeta meta) {
        List<PotionEffect> customEffects = meta.getAllEffects();
        if (customEffects.isEmpty()) return this.getItemName(stack, meta);
        if (customEffects.size() == 1 && customEffects.getFirst().getType() == STABILISER)
            return text("Inert Potion").decoration(TextDecoration.ITALIC, false);
        return Component.textOfChildren(translatable(stack.getType()), text(" of "), potionNames(customEffects)).decoration(TextDecoration.ITALIC, false);
    }

    protected Component potionNames(Collection<PotionEffect> effects) {
        if (effects.size() > 4) return text("Multiple Effects");
        return Component.join(JoinConfiguration.separators(text(", "), text(" & ")), effects.stream().filter(effect -> effect.getType() != STABILISER).map(eff -> potionTypeName(eff.getType())).toList());
    }

    protected Component potionTypeName(PotionEffectType type) {
        if (isDelayed(type))
            return textOfChildren(text("Delayed "), potionTypeName0(type));
        return potionTypeName0(type);
    }

    public boolean isDelayed(PotionEffectType type) {
        return type.key().value().startsWith("delayed_");
    }

    private Component potionTypeName0(PotionEffectType type) {
        if (names.containsKey(type)) return text(names.get(type));
        return translatable(type, type.toString().toLowerCase());
    }

    protected Component potionName(PotionEffectType type) {
        if (bottleNameOverrides.containsKey(type))
            return text(bottleNameOverrides.get(type)).decoration(TextDecoration.ITALIC, false);
        if (names.containsKey(type)) {
            String content = names.get(type);
            if (content.endsWith(" Potion")) return text(content).decoration(TextDecoration.ITALIC, false);
            return Component.textOfChildren(text("Potion of "), text(content)).decoration(TextDecoration.ITALIC, false);
        }
        return Component.textOfChildren(text("Potion of "), translatable(type)).decoration(TextDecoration.ITALIC, false);
    }

    private MobEffect register(String id, MobEffect effect) {
        Holder<MobEffect> _ = register0(id, effect);

        if (effect.isInstantaneous())
            this.registerDelayed(id, effect);
        return effect;
    }

    private MobEffect registerDelayed(String id, MobEffect effect) {
        ExpiringMobEffect expiring = new ExpiringMobEffect(effect);
        Holder<MobEffect> _ = register0("delayed_" + id, expiring);
        delayedVersions.put(effect, expiring);
        return effect;
    }

    public PotionEffectType fix(MobEffect effect, String name) {
        return this.fix(effect, name, effect.isInstantaneous() ? 0 : MEDIUM);
    }

    public PotionEffectType fix(MobEffect effect, String name, int defaultDuration) {
        return this.fix(effect, name, defaultDuration, MAX_AMPLIFICATION);
    }

    public void fixBukkit(PotionEffectType type, int defaultDuration, int maxAmplification) {
        defaultDurations.put(type, defaultDuration);
        maxAmplifications.put(type, maxAmplification);
        PotionEffectType delayed = delayedVersion(type);
        if (delayed != null) {
            defaultDurations.put(delayed, SHORT);
            maxAmplifications.put(delayed, maxAmplification);
        }
    }

    public PotionEffectType fix(MobEffect effect, String name, int defaultDuration, int maxAmplification) {
        PotionEffectType type = CraftPotionEffectType.minecraftToBukkit(effect);
        names.put(type, name);
        ID_MAP.put(BuiltInRegistries.MOB_EFFECT.getId(effect), type);
        defaultDurations.put(type, defaultDuration);
        maxAmplifications.put(type, maxAmplification);
        ours.add(type);
        if (effect instanceof Listener listener)
            Bukkit.getPluginManager().registerEvents(listener, Survival.plugin);
        if (delayedVersions.containsKey(effect)) {
            MobEffect mobEffect = delayedVersions.remove(effect);
            this.fix(mobEffect, name, defaultDuration, maxAmplification);
        }
        return type;
    }

    public void setPrincipalIngredient(PotionEffectType type, ItemStack ingredient) {
        this.principalIngredients.put(type, ingredient);
    }

    public void setPrincipalIngredient(PotionEffectType type, Ingredient ingredient) {
        this.setPrincipalIngredient(type, ingredient.asItem());
    }

    public void setPrincipalIngredient(PotionEffectType type, Material material) {
        this.setPrincipalIngredient(type, new ItemStack(material));
    }

    public void preSetup() {
        try {
            register = MobEffects.class.getDeclaredMethod("register", String.class, MobEffect.class);
            register.setAccessible(true);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to access MobEffects register", e);
        }

        this.fixCodec();

        this.registerPotionEffects();

    }

    private void fixCodec() {
//        var STREAM_CODEC = StreamCodec.composite(Potion.STREAM_CODEC.apply(ByteBufCodecs::optional), PotionContents::potion, ByteBufCodecs.INT.apply(ByteBufCodecs::optional), PotionContents::customColor, MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list()), this::customEffects, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional), PotionContents::customName, PotionContents::new);
//        Utility.replaceField(PotionContents.class, "STREAM_CODEC", STREAM_CODEC);
    }

    protected List<MobEffectInstance> customEffects(PotionContents contents) {
        ArrayList<MobEffectInstance> list = new ArrayList<>(contents.customEffects());
        list.removeIf(this::isCustom);
        return list;
    }

    public void setup() {

        //<editor-fold desc="Do all the rubbish" defaultstate="collapsed">
        final var binding = GlobalConfiguration.get().anticheat.obfuscation.items.binding;
        final Field baseField;
        try {
            baseField = binding.getClass().getDeclaredField("base");
            baseField.setAccessible(true);
            final ItemObfuscationBinding.BoundObfuscationConfiguration bound = (ItemObfuscationBinding.BoundObfuscationConfiguration) baseField.get(binding);
            bound.patchStrategy().clear();
            final Class<?> sanitize = Class.forName("io.papermc.paper.util.sanitizer.ItemObfuscationBinding$BoundObfuscationConfiguration$MutationType$Sanitize");
            final var sanitizer = sanitize.getConstructor(UnaryOperator.class).newInstance((UnaryOperator<PotionContents>) (contents) -> {
                final var newList = new ArrayList<>(contents.customEffects());
                newList.removeIf(e -> isCustom(e.getEffect()));
                return new PotionContents(contents.potion(), contents.customColor(), newList, contents.customName());
            });
            ((Map) bound.patchStrategy()).put(POTION_CONTENTS, sanitizer);
            ChannelInitializeListenerHolder.addListener(Key.key("server", "obfuscator"), channel -> {
                channel.pipeline().addLast(new ChannelOutboundHandlerAdapter() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        msg = sanitise(msg);
                        if (msg != null) super.write(ctx, msg, promise);
                    }
                });
                channel.pipeline().addFirst(new ChannelOutboundHandlerAdapter() {
//                    @Override
//                    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
//                        ItemObfuscationSession start = ItemObfuscationSession.start(ItemObfuscationSession.ObfuscationLevel.ALL);
//                        ItemObfuscationSession.ObfuscationContext context = start.context();
//                        start.switchContext(context.itemStack(net.minecraft.world.item.ItemStack.EMPTY));
//                        super.connect(ctx, remoteAddress, localAddress, promise);
//                    }

                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        switch (msg) {
                            case ClientboundSetEntityDataPacket _:
                                try (ItemObfuscationSession start = ItemObfuscationSession.start(ItemObfuscationSession.ObfuscationLevel.ALL)) {
                                    ItemObfuscationSession.ObfuscationContext context = start.context();
                                    start.switchContext(context.itemStack(net.minecraft.world.item.ItemStack.EMPTY));
                                    super.write(ctx, msg, promise);
                                }
                                break;
                            default:
                                super.write(ctx, msg, promise);
//                                ItemObfuscationSession.start(ItemObfuscationSession.ObfuscationLevel.ALL);
                                break;
                        }
                    }
                });
            });
        } catch (final Exception tits) {
            tits.printStackTrace();
        }
        try {
            Field idMap = PotionEffectType.class.getDeclaredField("ID_MAP");
            idMap.setAccessible(true);
            ID_MAP = (BiMap<Integer, PotionEffectType>) idMap.get(null);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to access MobEffects register", e);
        }
        //</editor-fold>
        PLACEHOLDER_POTION = new ItemStack(Material.POTION);
        INVERSION_INGREDIENT = new RecipeChoice.MaterialChoice(Material.WARPED_FUNGUS, Material.POTENT_SULFUR);


        EFFECT_CONTAINER = new NamespacedKey(Survival.plugin, "potion_effects");
        this.registerBukkitTypes();
        this.registerPotionOpposites();


        this.removeMinecraftRecipes();
        this.potionBrewer = Bukkit.getPotionBrewer();
        this.principalIngredients = new DefaultMap<>(Ingredient.RUBBISH.asItem());

        Bukkit.getPluginManager().registerEvents(new PotionListener(this), Survival.plugin);
        this.registerPotionRecipes();
        this.registerGuide();
        this.setDistillationIngredients();
        watchGlass = new WatchGlass();
        watchGlass.registerRecipe();
        Bukkit.getPluginManager().registerEvents(watchGlass, Survival.plugin);
        Survival.anvilRecipes.add(new PotionEnchantingRecipe());
    }

    private void setDistillationIngredients() {
        this.setPrincipalIngredient(REGENERATION, Ingredient.POWDERED_GHAST_TEAR);
        this.setPrincipalIngredient(FREEZING, Ingredient.POWDERED_SNOW);
        this.setPrincipalIngredient(FIZZING, Ingredient.POWDERED_SULFUR);
        this.setPrincipalIngredient(RETURNING, Ingredient.POWDERED_ECHO_SHARD);
        this.setPrincipalIngredient(FARSIGHT, Ingredient.POWDERED_AMETHYST_SHARD);
        this.setPrincipalIngredient(SPEED, Material.SUGAR);
        this.setPrincipalIngredient(STRENGTH, Material.BLAZE_POWDER);

        this.setPrincipalIngredient(GLOWING, Ingredient.GLOWING_GOO);
        this.setPrincipalIngredient(BOUNCE, Material.SLIME_BALL);
        this.setPrincipalIngredient(FIRE_RESISTANCE, Material.MAGMA_CREAM);

        this.setPrincipalIngredient(STEEL, Material.IRON_NUGGET);
        this.setPrincipalIngredient(PURIFICATION, Material.GOLD_NUGGET);
        this.setPrincipalIngredient(INVULNERABILITY, Material.NETHERITE_SCRAP);
        this.setPrincipalIngredient(RUST, Material.COPPER_NUGGET);
        this.setPrincipalIngredient(CORRUPTION, Material.RESIN_CLUMP);
        this.setPrincipalIngredient(CRAWLING, Material.STRING);
    }

    private void removeMinecraftRecipes() {
        DedicatedServer server = ((CraftServer) Bukkit.getServer()).getServer();
        PotionBrewing _ = server.potionBrewing;
        final PotionBrewing.Builder builder = new PotionBrewing.Builder(server.overworld().enabledFeatures());
        this.addUsefulMinecraftComponents(builder);
        server.potionBrewing = builder.build();

    }

    private void addUsefulMinecraftComponents(PotionBrewing.Builder builder) {
        builder.addContainer(Items.POTION);
        builder.addContainer(Items.SPLASH_POTION);
        builder.addContainer(Items.LINGERING_POTION);
//        builder.addContainerRecipe(Items.POTION, Items.GUNPOWDER, Items.SPLASH_POTION);
//        builder.addContainerRecipe(Items.SPLASH_POTION, Items.DRAGON_BREATH, Items.LINGERING_POTION);
        builder.addMix(Potions.WATER, Items.GLOWSTONE_DUST, Potions.THICK);
        builder.addMix(Potions.WATER, Items.LAPIS_LAZULI, Potions.MUNDANE);
        builder.addMix(Potions.WATER, Items.NETHER_WART, Potions.AWKWARD);
    }

    protected PotionMeta copyColour(PotionMeta base, PotionMeta target) {
        Color color = base.getColor();
        if (color == null) color = base.computeEffectiveColor();
        target.setColor(color);
        return target;
    }

    protected ItemStack getEmptyCopy(ItemStack base, ItemStack result) {
        ItemStack copy = new ItemStack(base.getType(), base.getAmount());
        return Survival.potionManager.copyCompanionData(base, copy);
    }

    protected Predicate<PotionMeta> hasEffect(PotionEffectType type) {
        return meta -> {
            for (PotionEffect effect : meta.getAllEffects()) {
                if (type == effect.getType() || effect.getType().equals(type)) return true;
            }
            return false;
        };
    }

    protected Predicate<PotionMeta> hasEffect(PotionEffectType type, int maxAmplification) {
        return meta -> {
            for (PotionEffect effect : meta.getAllEffects()) {
                if (type == effect.getType() || effect.getType().equals(type))
                    if (effect.getAmplifier() <= maxAmplification) return true;
            }
            return false;
        };
    }

    private int safeDuration(int duration) {
        if (duration == PotionEffect.INFINITE_DURATION) return duration;
        return Math.clamp(duration, MINIMUM_DURATION, MAXIMUM_DURATION);
    }

    protected BiFunction<ItemStack, ItemStack, ItemStack> extendDuration(float factor) {
        return (base, item) -> {
            if (base == null || item == null) return item;
            Material type = item.getType();
            if (base.getItemMeta() instanceof PotionMeta a && item.getItemMeta() instanceof PotionMeta b) {
                final var modifier = this.modify(_ -> true, effect -> this.extendDuration(effect, type, factor));
                PotionMeta c = modifier.apply(a, b);
                item.setItemMeta(c);
            }
            return item;
        };
    }

    protected PotionEffect multiplyDuration(PotionEffect effect, float factor) {
        if (effect.getType().isInstant()) return effect;
        if (effect.getDuration() == PotionEffect.INFINITE_DURATION) return effect;
        int b = (int) (effect.getDuration() * factor);
        return effect.withDuration(this.safeDuration(b));
    }

    protected PotionEffect extendDuration(PotionEffect effect, Material material, float factor) {
        PotionEffectType type = effect.getType();
        if (type.isInstant() || effect.getDuration() == PotionEffect.INFINITE_DURATION) return effect;
        int a = effect.getDuration() + (int) (adjustedDuration(effect.getType(), material) * factor);
        return effect.withDuration(this.safeDuration(a));
    }

    protected PotionEffect reduceDuration(PotionEffect effect) {
        PotionEffectType type = effect.getType();
        if (type.isInstant() || effect.getDuration() == PotionEffect.INFINITE_DURATION) return effect;
        return effect.withDuration(this.safeDuration(effect.getDuration() - EXTENSION_TIME));
    }

    protected PotionModifierAlt extendAmplification(int amount) {
        return effect -> effect.withAmplifier(Math.min(this.getMaxAmplification(effect.getType()), effect.getAmplifier() + amount));
    }

    protected PotionEffect extendAmplification(PotionEffect effect) {
        return effect.withAmplifier(Math.min(this.getMaxAmplification(effect.getType()), effect.getAmplifier() + 1));
    }

    protected PotionEffect extendAmplificationUnsafe(PotionEffect effect) {
        return effect.withAmplifier(effect.getAmplifier() + 1);
    }

    protected int getMaxAmplification(PotionEffectType type) {
        return maxAmplifications.getOrDefault(type, MAX_AMPLIFICATION);
    }

    protected BiFunction<PotionMeta, PotionMeta, PotionMeta> modify(Predicate<PotionEffectType> typeSelector, PotionModifierAlt modifier) {
        return (base, result) -> {
            for (PotionEffect effect : base.getAllEffects()) {
                if (typeSelector.test(effect.getType())) {
                    PotionEffect transformed = modifier.transform(effect);
                    if (transformed == null) continue;
                    result.addCustomEffect(transformed, false);
                }
            }
            return result;
        };
    }

    protected BiFunction<PotionMeta, PotionMeta, PotionMeta> modify(Predicate<PotionEffectType> typeSelector, PotionModifier modifier) {
        return this.modify(typeSelector, (PotionModifierAlt) modifier);
    }

    protected BiFunction<PotionMeta, PotionMeta, PotionMeta> modify(PotionEffectType type, PotionModifierAlt modifier) {
        return this.modify(t -> t == type, modifier);
    }

    protected BiFunction<PotionMeta, PotionMeta, PotionMeta> modify(PotionEffectType type, PotionModifier modifier) {
        return this.modify(type, (PotionModifierAlt) modifier);
    }

    protected BiFunction<PotionMeta, PotionMeta, PotionMeta> modify(PotionEffectType from, PotionEffectType to) {
        return this.modify(t -> t == from, (_, duration, amplifier) -> new PotionEffect(to, duration, amplifier));
    }

    public void close() {
        PotionBrewer potionBrewer = Bukkit.getPotionBrewer();
        for (PotionBuilder potion : potions) {
            potion.close();
            potionBrewer.removePotionMix(potion.key);
        }
    }

    public PotionBuilder buildPotion(String id) {
        return new PotionBuilder(this, id);
    }

    public void registerPotion(PotionBuilder builder) {
        this.potions.add(builder);
        potionBrewer.addPotionMix(new PotionMix(builder.key, builder.result, builder.base, builder.ingredient));
        if (builder.shouldRegisterListener()) {
            Bukkit.getPluginManager().registerEvents(builder, Survival.plugin);
        }
    }

    public boolean isCustom(final MobEffectInstance instance) {
        return instance.getEffect().value() instanceof CustomMobEffect;
    }

    public boolean isCustom(final Holder<MobEffect> instance) {
        return instance.value() == BIG_eff || instance.value() instanceof CustomMobEffect;
    }

    public boolean isCustom(final PotionEffectType instance) {
        return this.isCustom(((CraftPotionEffectType) instance).getHolder());
    }

    public boolean isCustom(final PotionEffect instance) {
        return this.isCustom(instance.getType());
    }

    public void fixListDetails(ItemMeta meta, Iterable<PotionEffect> effects) {
        List<Component> lore = new ArrayList<>();
        for (PotionEffect effect : effects) {
            lore.add(this.potionComponent(effect));
        }
        meta.lore(lore);
    }

    public void fixPotion(ItemStack stack) {
        if (stack == null) return;
        if (stack.hasItemMeta() && stack.getItemMeta() instanceof PotionMeta meta) {
            meta.setMaxStackSize(16);
            stack.setItemMeta(meta);
            if (!meta.getAllEffects().isEmpty()) {
                Component value = this.bottleName(stack, meta);
                stack.setData(DataComponentTypes.ITEM_NAME, value);
                stack.setData(DataComponentTypes.CUSTOM_NAME, value);
            }
            stack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.POTION_CONTENTS, DataComponentTypes.CONSUMABLE).build());
        }
    }

    public void fixPotionDuringCreation(ItemStack stack) {
        if (stack == null) return;
        this.fixPotion(stack);
        this.fixPotionLore(stack);
        if (stack.hasItemMeta() && stack.getItemMeta() instanceof PotionMeta meta) {
            List<PotionEffect> allEffects = meta.getAllEffects();
            meta.setMaxStackSize(16);
            if (allEffects.isEmpty()) return;
            Color color = this.average(meta::computeEffectiveColor, allEffects);
            meta.setColor(color);
            stack.setItemMeta(meta);
            if (!meta.getAllEffects().isEmpty()) {
                Component value = this.bottleName(stack, meta);
                stack.setData(DataComponentTypes.ITEM_NAME, value);
                stack.setData(DataComponentTypes.CUSTOM_NAME, value);
            }
            stack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.POTION_CONTENTS, DataComponentTypes.CONSUMABLE).build());

        }
    }

    public void fixPotionLore(ItemStack stack) {
        if (stack == null) return;
        if (stack.hasItemMeta() && stack.getItemMeta() instanceof PotionMeta meta) {
            List<PotionEffect> allEffects = meta.getAllEffects();
            if (allEffects.isEmpty()) return;
            this.fixListDetails(meta, allEffects);
            stack.setItemMeta(meta);
            stack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.POTION_CONTENTS, DataComponentTypes.CONSUMABLE).build());
        }
    }

    public String numeral(int number) {
        return "I".repeat(number).replace("IIIII", "V").replace("IIII", "IV").replace("VV", "X").replace("VIV", "IX").replace("XXXXX", "L").replace("XXXX", "XL").replace("LL", "C").replace("LXL", "XC");
    }

    private Color average(Supplier<Color> current, Iterable<PotionEffect> effect) {
        int avg = 0, r = 0, g = 0, b = 0;
        for (PotionEffect eff : effect) {
            if (eff.getType() == STABILISER) continue;
            ++avg;
            Color color = eff.getType().getColor();
            r += color.getRed();
            g += color.getGreen();
            b += color.getBlue();
        }
        if (avg == 0) return current.get();
        r /= avg;
        g /= avg;
        b /= avg;
        return Color.fromRGB(r, g, b);
    }

    protected Component potionComponent(PotionEffect effect) {
        PotionEffectType type = effect.getType();
        Component name = this.potionTypeName(type);
        NamedTextColor label = switch (type.getEffectCategory()) {
            case HARMFUL -> NamedTextColor.RED;
            case BENEFICIAL -> NamedTextColor.BLUE;
            default -> NamedTextColor.GRAY;
        };
        return this.duration(this.amplification(name, effect), effect).color(label).decoration(TextDecoration.ITALIC, false);
    }

    private Component amplification(Component name, PotionEffect effect) {
        if (effect.getAmplifier() > 0)
            return translatable("potion.withAmplifier", name, translatable("potion.potency." + effect.getAmplifier(), this.numeral(effect.getAmplifier() + 1)));
        return name;
    }

    private Component duration(Component name, PotionEffect effect) {
        if (!effect.getType().isInstant() && effect.getDuration() > 0)
            return translatable("potion.withDuration", name, text(getTimer(effect.getDuration())));
        else return name;

    }

    protected <C extends Collection<net.minecraft.world.item.ItemStack>> C modifyForSerialisation(C list) {
        return (C) list.stream().map(this::modifyForSerialisation).toList();
    }

    protected net.minecraft.world.item.ItemStack modifyForSerialisation(net.minecraft.world.item.ItemStack item) {
        try (var patcher = new DataComponentPatchEditor(item.getComponents())) {
            patcher.patch(DataComponents.MAX_STACK_SIZE, item.getMaxStackSize());
        }
//        new TypedDataComponent<>(DataComponents.MAX_STACK_SIZE, item.getMaxStackSize()).applyTo((PatchedDataComponentMap) item.getComponents());
//        if (maxStackSizes.containsKey(item.getBukkitStack().getType())) {
//            new TypedDataComponent<>(DataComponents.MAX_STACK_SIZE, item.getMaxStackSize()).applyTo((PatchedDataComponentMap) item.getComponents());
//            item.applyComponents(DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 16).build());
//        }
        item = AttributeManager.modifyForSerialisation(item);
        BundleContents bundleContents = item.getComponents().get(BUNDLE_CONTENTS);
        if (bundleContents != null) {
            final var newList = new ArrayList<>(bundleContents.items());
            newList.replaceAll(it -> {
                net.minecraft.world.item.ItemStack itemStack = modifyForSerialisation(it.create());
//                itemStack.limitSize(1);
                return ItemStackTemplate.fromNonEmptyStack(itemStack);
            });
            item = item.copy();
            item.set(BUNDLE_CONTENTS, new BundleContents(newList));
        }
        ItemContainerContents containerContents = item.getComponents().get(CONTAINER);
        if (containerContents != null) {
            final List<net.minecraft.world.item.ItemStack> newList = new ArrayList<>();
            for (Optional<ItemStackTemplate> optional : containerContents.items) {
                if (optional.isPresent()) {
                    net.minecraft.world.item.ItemStack e = modifyForSerialisation(optional.get().create());
//                    e.limitSize(maxStackSizes.get(e.getBukkitStack().getType()));
//                    e.limitSize(1);
                    newList.add(e);
                } else newList.add(net.minecraft.world.item.ItemStack.EMPTY);
            }
            item = item.copy();
            item.set(CONTAINER, ItemContainerContents.fromItems(newList));
        }
        PotionContents contents = item.getComponents().get(POTION_CONTENTS);
        if (contents != null) {
            final var newList = new ArrayList<>(contents.customEffects());
            newList.removeIf(e -> isCustom(e.getEffect()));
            PotionContents replace = new PotionContents(contents.potion(), contents.customColor(), newList, contents.customName());
            net.minecraft.world.item.ItemStack copy = item.copy();
            copy.remove(POTION_CONTENTS);
            copy.set(POTION_CONTENTS, replace);
            item = copy;
        }
        return item;
    }

    protected ClientboundSetEntityDataPacket modifyEntityDataPacket(ClientboundSetEntityDataPacket packet) {
        return new ClientboundSetEntityDataPacket(packet.id(), (List<SynchedEntityData.DataValue<?>>) (Object) packet.packedItems().stream().map(PotionManager.this::modifyForSerialisation).toList());
    }

    protected SynchedEntityData.DataValue<?> modifyForSerialisation(SynchedEntityData.DataValue<?> data) {
        if (data.serializer() == EntityDataSerializers.ITEM_STACK) {
            net.minecraft.world.item.ItemStack itemStack = modifyForSerialisation((net.minecraft.world.item.ItemStack) data.value());
            return new SynchedEntityData.DataValue(data.id(), data.serializer(), itemStack);
        }
        return switch (data.value()) {
            case net.minecraft.world.item.ItemStack item ->
                    new SynchedEntityData.DataValue(data.id(), data.serializer(), modifyForSerialisation(item));
            case PotionContents item ->
                    new SynchedEntityData.DataValue(data.id(), data.serializer(), new PotionContents(item.potion(), item.customColor(), customEffects(item), item.customName()));
            default -> data;
        };
    }

    protected Object sanitise(Object msg) {
        return switch (msg) {
            case ClientboundPlayerChatPacket packet -> {
                var comp = packet.unsignedContent() == null ? net.minecraft.network.chat.Component.literal(packet.body().content()) : packet.unsignedContent();

                comp = packet.chatType().decorate(comp);
                yield new ClientboundSystemChatPacket(comp, false);
            }
            case ClientboundContainerSetContentPacket(
                    int containerId, int stateId, List<net.minecraft.world.item.ItemStack> items,
                    net.minecraft.world.item.ItemStack carriedItem
            ) ->
                    new ClientboundContainerSetContentPacket(containerId, stateId, modifyForSerialisation(items), modifyForSerialisation(carriedItem));
            case ClientboundSetPlayerInventoryPacket(
                    int slot, net.minecraft.world.item.ItemStack contents
            ) -> new ClientboundSetPlayerInventoryPacket(slot, modifyForSerialisation(contents));
            case ClientboundContainerSetSlotPacket packet ->
                    new ClientboundContainerSetSlotPacket(packet.getContainerId(), packet.getStateId(), packet.getSlot(), modifyForSerialisation(packet.getItem()));
            case ClientboundSetEquipmentPacket packet ->
                    new ClientboundSetEquipmentPacket(packet.getEntity(), packet.getSlots().stream().map(pair -> new Pair<>(pair.getFirst(), modifyForSerialisation(pair.getSecond()))).toList());
            case ClientboundUpdateMobEffectPacket packet ->
                    PotionManager.this.isCustom(packet.getEffect()) ? null : packet;
            case ClientboundRemoveMobEffectPacket packet ->
                    PotionManager.this.isCustom(packet.effect()) ? null : packet;
            case ClientboundSetCursorItemPacket(net.minecraft.world.item.ItemStack contents) ->
                    new ClientboundSetCursorItemPacket(modifyForSerialisation(contents));
            case ClientboundSetEntityDataPacket packet -> modifyEntityDataPacket(packet);
            case ClientboundBundlePacket packet -> {
                List<Packet<? super ClientGamePacketListener>> list = new ArrayList<>();
                for (Packet<? super ClientGamePacketListener> subPacket : packet.subPackets()) {
                    Packet sanitise = (Packet) sanitise(subPacket);
                    if (sanitise == null) continue;
                    list.add(sanitise);
                }
                yield new ClientboundBundlePacket(list);
            }
            default -> msg;
        };
    }

    public ItemStack takeIngredient(ItemStack potion) {
        if (potion == null || !(potion.getItemMeta() instanceof PotionMeta meta)) return null;
        ItemStack item = null;
        for (PotionEffect effect : meta.getCustomEffects()) {
            item = this.getPrincipalIngredient(effect);
            if (item != null) {
                meta.removeCustomEffect(effect.getType());
                break;
            }
        }
        potion.setItemMeta(meta);
        return item;
    }

    public ItemStack getPrincipalIngredient(PotionEffect effect) {
        return this.getPrincipalIngredient(effect.getType());
    }

    public ItemStack getPrincipalIngredient(PotionEffectType type) {
        ItemStack stack = principalIngredients.get(type);
        return stack == null ? null : stack.asOne();
    }

    public void extractResultToContainer(Inventory source, Inventory target, ItemStack result, Consumer<ItemStack> setSlot) {
        if (result == null || result.isEmpty()) return;
        ItemStack start = result.clone();
        ItemStack taken = this.extractResult(start);
        if (taken == null) return;
        InventoryMoveItemEvent move = new PaperInventoryMoveItemEvent(source, taken, target, true);
        if (!move.callEvent()) {
            return;
        }
        HashMap<Integer, ItemStack> map = target.addItem(taken);
        if (map.isEmpty()) setSlot.accept(start);

    }

    protected ItemStack extractResult(ItemStack stack) {
        if (WatchGlass.isWatchGlass(stack))
            return WatchGlass.takeItem(stack);
        ItemStack one = stack.asOne();
        stack.subtract();
        return one;
    }

    @FunctionalInterface
    public interface PotionModifier extends PotionModifierAlt {
        PotionEffect transform(PotionEffectType type, int duration, int amplification);

        @Override
        default PotionEffect transform(PotionEffect effect) {
            return this.transform(effect.getType(), effect.getDuration(), effect.getAmplifier());
        }
    }

    @FunctionalInterface
    public interface PotionModifierAlt extends Function<PotionEffect, PotionEffect> {

        PotionEffect transform(PotionEffect effect);

        @Override
        default PotionEffect apply(PotionEffect potionEffect) {
            return this.transform(potionEffect);
        }
    }

}

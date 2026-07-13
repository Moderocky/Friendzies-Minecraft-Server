package mx.kenzie.survival.potion;

import mx.kenzie.advancements.Advancement;
import mx.kenzie.advancements.Display;
import mx.kenzie.survival.Survival;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static mx.kenzie.survival.potion.PotionManager.inversionIngredient;

public enum PotionGuide {

    ROOT("Brewing Guide", "A guide to brewing potions.\nFollow the (square) ingredient steps.", Material.BREWING_STAND, "minecraft:block/basalt_top"),
    GLASS_BOTTLE("Make a potion bottle from glass.", Material.GLASS_BOTTLE, ROOT, "goal"),
    WATER_BUCKET("Gather some water from a water source.", Material.WATER_BUCKET, GLASS_BOTTLE),
    MILK_BUCKET("Pasteurise some milk in a brewing stand.", Material.MILK_BUCKET, GLASS_BOTTLE),
    WATER_BOTTLE("Water Bottle", "Boil your water in a brewing stand.", createPotion(PotionType.WATER), WATER_BUCKET, "goal"),
    CLEANSING("Milk Bottle", "Pasteurise your milk in a brewing stand.", Survival.potionManager.createSafePotion(PotionManager.CLEANSING), MILK_BUCKET, "goal"),
    HONEY_BOTTLE("Gather honey in a bottle.", Material.HONEY_BOTTLE, GLASS_BOTTLE, "goal"),
    AWKWARD_MAKE("Creates an awkward potion.", Material.NETHER_WART, WATER_BOTTLE),
    AWKWARD("Awkward Potion", "Awkward potions can be brewed into most potion types.", createPotion(PotionType.AWKWARD), AWKWARD_MAKE, "goal"),
    MUNDANE_MAKE("Creates a mundane potion for dilution.", Material.LAPIS_LAZULI, WATER_BOTTLE),
    MUNDANE("Mundane Potion", "Other potions can be diluted into a mundane potion to copy them.", createPotion(PotionType.MUNDANE), MUNDANE_MAKE, "goal"),
    THICK_MAKE("Creates a thick potion.", Material.GLOWSTONE_DUST, WATER_BOTTLE),
    THICK("Thick Potion", "Thick potions can be brewed into irregular kinds.", createPotion(PotionType.MUNDANE), THICK_MAKE, "goal"),
    OMINOUS_BOTTLE("Creates an ominous bottle.", Material.POISONOUS_POTATO, WATER_BOTTLE),
    TEMPLATE("Brew a Potion", "Add any ingredient to brew a potion.", createPotion(PotionType.HEALING), AWKWARD, "goal"),
    GUNPOWDER("Add gunpowder to create a splash potion.", Material.GUNPOWDER, TEMPLATE),
    SPLASH_POTION("Throw your splash potion at a monster.", Material.SPLASH_POTION, GUNPOWDER, "goal"),
    DRAGON_BREATH("Add dragon breath to create a lingering potion.", Material.DRAGON_BREATH, TEMPLATE),
    LINGERING_POTION("Throw your lingering potion at the ground.", Material.LINGERING_POTION, DRAGON_BREATH, "goal"),
    DELAYING("Add quartz to delay the instant effect of your potion.", Material.QUARTZ, TEMPLATE, "challenge"),
    REDSTONE("Add redstone to lengthen your potion.\nAdds 15/30/60 seconds each time.", Material.REDSTONE, TEMPLATE, "challenge"),
    GLOWSTONE("Add glowstone to amplify your potion.\nEach potion has its own maximum power.", Material.GLOWSTONE_DUST, TEMPLATE, "challenge"),
    NETHER_STAR("Add a nether star to make your potion permanent!\nPermanence is lost during dilution.", Material.NETHER_STAR, TEMPLATE, "challenge"),
    STABILISER("Add lapis to stabilise your potion.", Material.LAPIS_LAZULI, TEMPLATE, "challenge"),
    COMBINATION("Combination", "Dilute a second potion into the stabilised mixture.", createPotion(potion -> {
        potion.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 1800, 0), true);
        potion.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 1800, 0), true);
    }), STABILISER, "goal"),
    CHARCOAL("Add charcoal to reduce the duration of your potion.", Material.CHARCOAL, TEMPLATE, "challenge"),
    DILUTION("Dilute a Potion", "Dilute one source potion into three mundane bottles.\nThe concentration of the potion is divided by three.", createPotion(PotionType.INVISIBILITY), MUNDANE, "goal"),
    WATCH_GLASS("Watch Glass", "Craft a watch glass using amethyst for carefully separating potion ingredients.", WatchGlass.ITEM, TEMPLATE, "goal"),
    DISTILLATION("Distill a Potion", "Remove the water and separate your mixture back into its ingredients.\nNot all ingredients are recoverable in their original form.", Ingredient.POWDERED_ECHO_SHARD.asItem(), WATCH_GLASS, "challenge"),

    INVERSION("Invert the Effect", "Reverse the effects of your potion.", inversionIngredient(), TEMPLATE, "challenge"),

    WEAVING("Creates a potion of weaving.", Material.WHITE_WOOL, THICK),
    CRAWLING("Creates a potion of crawling.\nInfests the drinker with spider eggs.", Material.COBWEB, THICK),
    FIZZING("Creates a potion of fizzing.\nInfests the drinker with sulfur cubes.", Material.SULFUR, THICK),
    OOZING("Creates a potion of oozing.\nInfests the drinker with slimes.", Material.SLIME_BLOCK, THICK),
    MUMMIFYING("Creates a potion of mummification.\nRaises a skeleton from the corpse of the drinker.", Material.SAND, THICK),
    INFESTED("Creates a potion of infestation.", Material.STONE, THICK),
    EXPERIENCE("Creates a potion of experience.\nStores experience for later.", Material.SCULK, THICK),
    TURTLE_HELMET("Creates a potion of the turtle master.\nGrants resistance and slowness.", Material.TURTLE_HELMET, THICK),


    SATURATION("Creates a potion of saturation.", Material.GOLDEN_CARROT, HONEY_BOTTLE),
    HUNGER("Creates a potion of hunger.", Material.ROTTEN_FLESH, HONEY_BOTTLE),
    HASTE("Creates a potion of haste.", Material.GOLDEN_DANDELION, HONEY_BOTTLE),
    MINING_FATIGUE("Creates a potion of mining fatigue.", inversionIngredient(), HASTE),

    HERO_OF_THE_VILLAGE("Creates a potion of hero of the village.\nReduces trade prices.", Material.EMERALD, OMINOUS_BOTTLE),
    INSTANT_DEATH("Creates a potion of instant death.", Material.SKELETON_SKULL, OMINOUS_BOTTLE),
    CORRUPTING("Creates a potion of corruption.\nTransforms some monsters into their evil variants.", Material.RESIN_CLUMP, OMINOUS_BOTTLE),
    ZOMBIFICATION("Creates a potion of zombification.\nTransforms a create into its zombie counterpart.", Material.ROTTEN_FLESH, OMINOUS_BOTTLE),
    PURIFICATION("Creates a potion of purification.\nRestores some corrupted creatures.", Material.GOLD_INGOT, OMINOUS_BOTTLE),
    WITHER("Creates a potion of withering.", Material.WITHER_ROSE, OMINOUS_BOTTLE),


    WEAKNESS("Creates a potion of weakness.", Material.FERMENTED_SPIDER_EYE, AWKWARD),
    STRENGTH("Creates a potion of strength.", Material.BLAZE_POWDER, AWKWARD),
    SPEED("Creates a potion of speed.", Material.SUGAR, AWKWARD),
    SLOWNESS("Creates a potion of slowness.", inversionIngredient(), SPEED),
    JUMP_BOOST("Creates a potion of jump boost.", Material.COCOA_BEANS, AWKWARD),
    INERTIA("Creates a potion of jump boost.", inversionIngredient(), JUMP_BOOST),
    INSTANT_HEALTH("Creates a potion of instant health.", Material.GLISTERING_MELON_SLICE, AWKWARD),
    INSTANT_DAMAGE("Creates a potion of instant damage.", Material.RED_MUSHROOM, AWKWARD),
    POISON("Creates a potion of poison.", Material.SPIDER_EYE, AWKWARD),
    REGENERATION("Creates a potion of regeneration.", Material.GHAST_TEAR, AWKWARD),
    FIRE_RESISTANCE("Creates a potion of fire resistance.", Material.MAGMA_CREAM, AWKWARD),
    WATER_BREATHING("Creates a potion of water breathing.", Material.PUFFERFISH, AWKWARD),
    NIGHT_VISION("Creates a potion of night vision.", Material.GOLDEN_CARROT, AWKWARD),
    BLINDNESS("Creates a potion of blindness.", inversionIngredient(), NIGHT_VISION),
    SLOW_FALLING("Creates a potion of slow falling.", Material.PHANTOM_MEMBRANE, AWKWARD),
    ABSORPTION("Creates a potion of absorption.\nGrants a temporary health shield.", Material.GOLDEN_APPLE, AWKWARD),
    WIND_CHARGED("Creates a potion of wind charged.", Material.BREEZE_ROD, AWKWARD),
    HEALTH_BOOST("Creates a potion of health boost.\nGrants additional hearts.", Material.GHAST_TEAR, ABSORPTION, "challenge"),
    DARKNESS("Creates a potion of darkness.", Material.INK_SAC, AWKWARD),
    NAUSEA("Creates a potion of nausea.", Material.TROPICAL_FISH, AWKWARD),
    LUCK("Creates a potion of luck.", Material.RABBIT_FOOT, AWKWARD),
    UNLUCK("Creates a potion of bad luck.", Material.FIRE_CHARGE, AWKWARD),
    GLOWING("Creates a potion of glowing.", Material.GLOW_INK_SAC, AWKWARD),
    INVISIBILITY("Creates a potion of invisibility.", inversionIngredient(), GLOWING),
    RESISTANCE("Creates a potion of resistance.", Material.TURTLE_SCUTE, AWKWARD),
    LEVITATION("Creates a potion of levitation.", Material.POPPED_CHORUS_FRUIT, AWKWARD),
    BIG("Creates a potion of enlargement.\nIncreases your size.", Material.COOKED_CHICKEN, AWKWARD),
    SMALL("Creates a potion of shrinking.\nReduces your size and gravity.", inversionIngredient(), BIG),
    RETURNING("Creates a potion of returning.\nReturns you to where you last died.", Material.ECHO_SHARD, AWKWARD),
    STEEL("Creates a potion of steel.\nIncreases your armour.", Material.IRON_INGOT, AWKWARD),
    RUST("Creates a potion of rusting.\nDecreases your armour.", inversionIngredient(), STEEL),
    TOUGHNESS("Creates a potion of toughness.\nIncreases armour effectivity.", Material.ARMADILLO_SCUTE, AWKWARD),
    FARSIGHT("Creates a potion of farsight.\nAllows you to reach further.", Material.AMETHYST_SHARD, AWKWARD),
    NEARSIGHT("Creates a potion of near sight.", inversionIngredient(), FARSIGHT),
    BOUNCE("Creates a potion of bouncing.", Material.SLIME_BALL, AWKWARD),
    FEATHER_FALLING("Creates a potion of feather falling.\nIncreases the safe fall height.", Material.FEATHER, AWKWARD),
    WEAK_KNEES("Creates a potion of weak knees.\nDecreases the safe fall height.", inversionIngredient(), FEATHER_FALLING),
    HOMEWARD("Creates a potion of homeward.\nTeleports you to your bed.", Material.CRYING_OBSIDIAN, THICK),
    UNDYING("Creates a potion of undying.\nYou cannot be killed by normal means.", Material.TOTEM_OF_UNDYING, AWKWARD),
    JITTERING("Creates a potion of jittering.\nEvade damage by teleportation.", Material.CHORUS_FRUIT, AWKWARD),
    SAFEKEEPING("Creates a potion of safekeeping.\nKeeps 25% of your equipment per level on death.", Material.CRIMSON_FUNGUS, AWKWARD),
    DIMENSIONALITY("Creates a potion of teleportation.\nTeleports you back to the brewing location.", Material.ENDER_EYE, AWKWARD),
    FLOUNDERING("Creates a potion of floundering.\nReduces affinity for water.", Material.KELP, AWKWARD),
    BREATH_OF_THE_NAUTILUS("Creates a potion of nautilus grace.", Material.NAUTILUS_SHELL, AWKWARD),
    CONDUIT_POWER("Creates a potion of conduit power.", Material.HEART_OF_THE_SEA, AWKWARD),
    FRAGILITY("Creates a potion of fragility.", Material.BONE, AWKWARD),
    CAMOUFLAGE("Creates a potion of camouflage.\nHides you from the waypoint map.", Material.LEAF_LITTER, AWKWARD),
    FREEZING("Creates a potion of freezing.", Material.POWDER_SNOW_BUCKET, AWKWARD),
    FLAMMABILITY("Creates a potion of flammability.", Material.COAL, AWKWARD),
    INVULNERABILITY("Creates a potion of invulnerability.\nReduces all damage taken by one point.", Material.NETHERITE_SCRAP, AWKWARD),
    DAYLIGHT_SAVING("Creates a potion of daylight saving.\nAdvances the time to morning.", Material.SUNFLOWER, AWKWARD),
    MOONLIGHT("Creates a potion of moonlight.\nAdvances the time to dusk.", Material.OPEN_EYEBLOSSOM, AWKWARD),


    DELAYED_INSTANT_DEATH("Delays the effect.", Material.QUARTZ, INSTANT_DEATH),
    DELAYED_INSTANT_DAMAGE("Delays the effect.", Material.QUARTZ, INSTANT_DAMAGE),
    DELAYED_INSTANT_HEALTH("Delays the effect.", Material.QUARTZ, INSTANT_HEALTH),
    DELAYED_CLEANSING("Delays the effect.", Material.QUARTZ, CLEANSING),
    DELAYED_CORRUPTING("Delays the effect.", Material.QUARTZ, CORRUPTING),
    DELAYED_PURIFICATION("Delays the effect.", Material.QUARTZ, PURIFICATION),
    DELAYED_ZOMBIFICATION("Delays the effect.", Material.QUARTZ, ZOMBIFICATION),
    DELAYED_HOMEWARD("Delays the effect.", Material.QUARTZ, HOMEWARD),
    DELAYED_RETURNING("Delays the effect.", Material.QUARTZ, RETURNING),
    DELAYED_EXPERIENCE("Delays the effect.", Material.QUARTZ, EXPERIENCE),
    SPELUNKING("Creates a potion of spelunking.\nTeleports the drinker back to their starting location once the effect expires.", Material.QUARTZ, DIMENSIONALITY),

    ;

    public final Component name, description;
    public final ItemStack icon;
    public final String background, frame;
    public final NamespacedKey parent;
    private final NamespacedKey key;

    PotionGuide(Component name, Component description, ItemStack icon, String frame, String background, NamespacedKey parent) {
        this.parent = parent;
        this.frame = frame;
        this.background = background;
        this.icon = icon;
        this.description = description;
        this.name = name;
        this.key = new NamespacedKey(Survival.plugin, "guide_" + ++PotionManager.guideCounter);
    }

    PotionGuide(String name, String description, Material icon, String background) {
        this(Component.text(name), description, new ItemStack(icon), background);
    }

    PotionGuide(Component name, String description, ItemStack icon, String background) {
        this(name, lines(description), icon, "task", background, null);
    }

    PotionGuide(String name, String description, Material icon, PotionGuide parent) {
        this(name, description, new ItemStack(icon), parent);
    }

    PotionGuide(String name, String description, ItemStack icon, PotionGuide parent) {
        this(Component.text(name), lines(description), icon, "task", null, parent.key());
    }

    PotionGuide(Component name, String description, ItemStack icon, PotionGuide parent) {
        this(name, lines(description), icon, "task", null, parent.key());
    }

    PotionGuide(String description, Material icon, PotionGuide parent) {
        this(Component.translatable(icon), description, new ItemStack(icon), parent);
    }

    PotionGuide(String description, ItemStack icon, PotionGuide parent, String frame) {
        this(Component.translatable(icon), lines(description), icon, frame, null, parent.key());
    }

    PotionGuide(String description, ItemStack icon, PotionGuide parent) {
        this(Component.translatable(icon), description, icon, parent);
    }

    PotionGuide(String description, Material icon, PotionGuide parent, String frame) {
        this(Component.translatable(icon), lines(description), new ItemStack(icon), frame, null, parent.key());
    }

    PotionGuide(String name, String description, Material icon, PotionGuide parent, String frame) {
        this(name, description, new ItemStack(icon), parent, frame);
    }

    PotionGuide(String name, String description, ItemStack icon, PotionGuide parent, String frame) {
        this(Component.text(name), lines(description), icon, frame, null, parent.key());
    }

    private static ItemStack createPotion(Consumer<PotionMeta> potion) {
        return createPotion(Material.POTION, potion);
    }

    private static ItemStack createPotion(Material type, Consumer<PotionMeta> potion) {
        ItemStack item = new ItemStack(type);
        if (item.getItemMeta() instanceof PotionMeta meta) {
            potion.accept(meta);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createPotion(PotionType type) {
        return createPotion(Material.POTION, potion -> potion.setBasePotionType(type));
    }

    private static Component lines(String text) {
        if (!text.contains("\n")) return Component.textOfChildren(Component.text(text));
        String[] split = text.split("\n");
        return Component.join(JoinConfiguration.newlines(), Arrays.stream(split).map(Component::text).toList());
    }

    private static Advancement createAdvancement(Component name, Component description, NamespacedKey key, ItemStack icon, String frame, String background, NamespacedKey parent) {
        final Advancement advancement = new Advancement(key.value());
        advancement.display = new Display();
        advancement.display.icon.setItem(icon);
        advancement.display.setTitle(name);
        advancement.display.setDescription(description);
        if (frame != null) advancement.display.frame = frame;
        if (Survival.plugin == null) return advancement;
        advancement.display.show_toast = false;
        advancement.display.announce_to_chat = false;
        advancement.display.hidden = false;
        if (parent == null) {
            advancement.display.setBackground(background);
        } else advancement.setParent(parent);
        return advancement;

    }

    public static void thenDoPotions() {
        List<PotionEffectType> missing = new ArrayList<>();
        for (PotionEffectType type : PotionManager.allPotionTypes()) {
            String name = type.key().value().toUpperCase();
            try {
                PotionGuide guide = PotionGuide.valueOf(name);
                try {
                    final Advancement advancement = createAdvancementFor(type, guide.key);
                    advancement.register(Survival.plugin);
                } catch (Throwable ex) {
                    throw new RuntimeException("Error registering advancement for " + name, ex);
                }
            } catch (IllegalArgumentException _) {
                missing.add(type);
            }
        }
        if (!missing.isEmpty()) {
            Survival.plugin.getLogger().info("No potion recipe guides for: " + missing.stream().map(PotionEffectType::getKey).map(NamespacedKey::toString).toList());
        }
    }

    private static Advancement createAdvancementFor(PotionEffectType type, NamespacedKey parent) {
        ItemStack icon = Survival.potionManager.createSafePotion(type);
        ItemMeta meta = icon.getItemMeta();
        List<Component> lore = meta.lore();
        Component description;
        if (lore == null) description = Component.textOfChildren(Component.text());
        else description = Component.join(JoinConfiguration.newlines(), lore);
        NamespacedKey key = new NamespacedKey(Survival.plugin, type.key().value() + "_potion");
        return createAdvancement(meta.displayName(), description, key, icon, "goal", null, parent);
    }

    public NamespacedKey key() {
        return this.key;
    }

    public void register() {
        try {
            final Advancement advancement = this.createAdvancement();
            advancement.register(Survival.plugin);
        } catch (Throwable ex) {
            throw new RuntimeException("Error registering advancement for " + key, ex);
        }
    }

    public Advancement createAdvancement() {
        return createAdvancement(this.displayName(), description, key, icon, frame, background, parent);
    }

    public Component displayName() {
        return name.hoverEvent(this.description());
    }

    public Component description() {
        return description;
    }

    public void give(Player player) {
        this.complete(player);
    }

    public void complete(Player player) {
        final org.bukkit.advancement.Advancement advancement = Bukkit.getAdvancement(this.key());
        if (advancement == null) return;
        final AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (progress.isDone()) return;
        for (String criterion : progress.getRemainingCriteria()) progress.awardCriteria(criterion);
    }

}

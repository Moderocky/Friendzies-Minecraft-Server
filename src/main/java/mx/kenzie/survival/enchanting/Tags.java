package mx.kenzie.survival.enchanting;

import org.bukkit.Material;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class Tags {
    public static final Material[] AXES = {Material.WOODEN_AXE, Material.STONE_AXE, Material.COPPER_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE};
    public static final Material[] SWORDS = {Material.WOODEN_SWORD, Material.STONE_SWORD, Material.COPPER_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD};
    public static final Material[] SHOVELS = {Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.COPPER_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL};
    public static final Material[] SPEARS = {Material.WOODEN_SPEAR, Material.STONE_SPEAR, Material.COPPER_SPEAR, Material.IRON_SPEAR, Material.GOLDEN_SPEAR, Material.DIAMOND_SPEAR, Material.NETHERITE_SPEAR};
    public static final Material[] PICKAXES = {Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.COPPER_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE};
    public static final Material[] HOES = {Material.WOODEN_HOE, Material.STONE_HOE, Material.COPPER_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE};
    public static final Material[] BUCKETS = {Material.BUCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET, Material.MILK_BUCKET, Material.AXOLOTL_BUCKET, Material.COD_BUCKET, Material.POWDER_SNOW_BUCKET, Material.SULFUR_CUBE_BUCKET, Material.TADPOLE_BUCKET, Material.SALMON_BUCKET, Material.TROPICAL_FISH_BUCKET, Material.PUFFERFISH_BUCKET};
    public static final Material[] OTHER_MELEE = {Material.TRIDENT, Material.MACE};
    public static final Material[] RANGED = {Material.BOW, Material.CROSSBOW, Material.TRIDENT};
    public static final Material[] BOWS = {Material.BOW, Material.CROSSBOW};

    public static final Material[] ANY_TOOLS = join(AXES, SHOVELS, join(PICKAXES, HOES));
    public static final Material[] ANY_MELEE_WEAPONS = join(AXES, SWORDS, join(SPEARS, OTHER_MELEE));

    public static final Material[] BOOTS = {Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.COPPER_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS,};

    public static final Material[] LEGGINGS = {Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.COPPER_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS,};

    public static final Material[] CHESTPLATE = {Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.COPPER_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE,};

    public static final Material[] OTHER_CHEST = {Material.ELYTRA};

    public static final Material[] ANY_CHEST = join(CHESTPLATE, OTHER_CHEST);

    public static final Material[] HELMET = {Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.COPPER_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.TURTLE_HELMET};

    public static final Material[] OTHER_HEAD = {Material.CARVED_PUMPKIN, Material.SKELETON_SKULL, Material.WITHER_SKELETON_SKULL, Material.CREEPER_HEAD, Material.DRAGON_HEAD, Material.PLAYER_HEAD, Material.PIGLIN_HEAD, Material.ZOMBIE_HEAD,};

    public static final Material[] ANY_HEAD = join(HELMET, OTHER_HEAD);
    public static final Material[] MOB_ARMOR = {Material.LEATHER_HORSE_ARMOR, Material.COPPER_HORSE_ARMOR, Material.IRON_HORSE_ARMOR, Material.GOLDEN_HORSE_ARMOR, Material.DIAMOND_HORSE_ARMOR, Material.NETHERITE_HORSE_ARMOR, Material.COPPER_NAUTILUS_ARMOR, Material.IRON_NAUTILUS_ARMOR, Material.GOLDEN_NAUTILUS_ARMOR, Material.DIAMOND_NAUTILUS_ARMOR, Material.NETHERITE_NAUTILUS_ARMOR, Material.WOLF_ARMOR};
    public static final EnumSet<Material> ALL_ARMOR = EnumSet.copyOf(Arrays.asList(join(BOOTS, LEGGINGS, join(ANY_CHEST, ANY_HEAD, MOB_ARMOR))));

    @SafeVarargs
    public static <Type> Type[] join(Type[] a, Type... b) {
        List<Type> list = new ArrayList<>(Arrays.asList(a));
        list.addAll(Arrays.asList(b));
        return list.toArray((Type[]) Array.newInstance(a.getClass().getComponentType(), list.size()));
    }

    @SafeVarargs
    public static <Type> Type[] join(Type[] a, Type[] b, Type... c) {
        List<Type> list = new ArrayList<>(Arrays.asList(a));
        list.addAll(Arrays.asList(b));
        list.addAll(Arrays.asList(c));
        return list.toArray((Type[]) Array.newInstance(a.getClass().getComponentType(), list.size()));
    }

}

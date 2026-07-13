package mx.kenzie.survival.enchanting;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.enchanting.events.HoeEvent;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.*;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class EnchantmentListener implements Listener {

    final Map<Material, Material> farmPlants = Map.of(Material.WHEAT_SEEDS, Material.WHEAT, Material.BEETROOT_SEEDS, Material.BEETROOTS, Material.CARROT, Material.CARROTS, Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM, Material.MELON_SEEDS, Material.MELON_STEM);
    final Map<Material, Material> morePlants = new HashMap<>();
    final Material[] RANDOM_PLANT = {Material.WHEAT, Material.BEETROOTS, Material.CARROTS, Material.POTATOES};
    final Supplier<Material> randomPlant = () -> RANDOM_PLANT[ThreadLocalRandom.current().nextInt(RANDOM_PLANT.length)];

    {
        morePlants.putAll(farmPlants);
        morePlants.putAll(Map.of(Material.NETHER_WART, Material.NETHER_WART, Material.COCOA_BEANS, Material.COCOA, Material.TORCHFLOWER_SEEDS, Material.TORCHFLOWER_CROP));
    }

    @EventHandler(ignoreCancelled = true)
    public void harvest(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        BlockState state = event.getBlockState();
        if (!(state.getBlockData() instanceof Ageable ageable)) return;
        if (ageable.getAge() < ageable.getMaximumAge()) return;
        if (!morePlants.containsValue(state.getType())) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        int level = Survival.enchantingRegistry.getLevel(EnchantingRegistry.GREEN_THUMB, item);
        if (level < 0) return;
        Material material = null;
        Iterator<Item> iterator = event.getItems().iterator();
        while (iterator.hasNext()) {
            Item eventItem = iterator.next();
            ItemStack stack = eventItem.getItemStack();
            Material type = stack.getType();
            if (morePlants.containsKey(type)) {
                material = morePlants.get(type);
                if (stack.getAmount() > 1)
                    stack.subtract();
                else
                    iterator.remove();
                break;
            }
        }
        if (material == null) return;
        Material finalMaterial = material;
        Bukkit.getScheduler().runTaskLater(Survival.plugin, () -> {
            if (!block.getType().isAir()) return;
            block.setBlockData(finalMaterial.createBlockData());
            block.getWorld().playEffect(block.getLocation(), Effect.BEE_GROWTH, 4);
        }, 5L);
    }

    @EventHandler(ignoreCancelled = true)
    public void hoe(HoeEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Block up = block.getRelative(BlockFace.UP);
        if (!up.getType().isAir()) return;
        int level = Survival.enchantingRegistry.getLevel(EnchantingRegistry.GREEN_THUMB, item);
        if (level < 0) return;
        if (block.getBlockData() instanceof Farmland farmland)
            farmland.setMoisture(farmland.getMaximumMoisture());
        final float chance = 0.4F + (level * 0.2F);
        float random = ThreadLocalRandom.current().nextFloat();
        if (random < chance) {
            Material seeds = this.chooseSeeds(block.getType(), player.getInventory());
            if (seeds == null) return;
            up.setType(seeds, true);
            block.getWorld().playEffect(up.getLocation(), Effect.BEE_GROWTH, 4);
        }
    }

    private Material chooseSeeds(Material farm, Inventory inventory) {
        if (farm == Material.SOUL_SAND) {
            return Material.NETHER_WART;
        } else if (farm == Material.FARMLAND) {
            List<Material> possible = new ArrayList<>(farmPlants.size());
            for (Material material : farmPlants.keySet()) {
                if (inventory.contains(material)) {
                    possible.add(farmPlants.get(material));
                }
            }
            if (possible.isEmpty())
                return randomPlant.get();
            return possible.get(ThreadLocalRandom.current().nextInt(possible.size()));
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true)
    public void molten(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        int level = Survival.enchantingRegistry.getLevel(EnchantingRegistry.MOLTEN, item);
        if (level < 0) return;
        for (Item entity : event.getItems()) {
            ItemStack stack = entity.getItemStack();
            ItemStack smelted = this.smelt(player, stack);
            entity.setItemStack(smelted);
        }
    }

    private Stream<Recipe> cookingRecipes(LivingEntity entity) {
        RecipeAccess recipeAccess = ((CraftEntity) entity).getHandle().level().recipeAccess();
        RecipeManager manager = (RecipeManager) recipeAccess;
        return Stream.concat(manager.recipes.byType(RecipeType.BLASTING).stream(), manager.recipes.byType(RecipeType.SMELTING).stream()).map(RecipeHolder::toBukkitRecipe);
    }

    private ItemStack smelt(LivingEntity entity, ItemStack stack) {
        if (stack == null) return null;
        Optional<Recipe> any = this.cookingRecipes(entity).filter(CookingRecipe.class::isInstance)
                .filter(recipe -> ((CookingRecipe<?>) recipe).getInputChoice().test(stack)).findAny();
        if (any.isPresent())
            return any.get().getResult();
        return stack;
    }

    @EventHandler(ignoreCancelled = true)
    public void glide(EntityToggleGlideEvent event) {
        if (!event.isGliding()) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living)) return;
        EntityEquipment equipment = living.getEquipment();
        if (equipment == null) return;
        for (ItemStack content : equipment.getArmorContents()) {
            int level = Survival.enchantingRegistry.getLevel(EnchantingRegistry.PROPULSION, content);
            if (level < 0) continue;
            Vector direction = living.getEyeLocation().getDirection();
            direction.setY(Math.clamp(direction.getY(), 0.4, 1));
            living.setVelocity(living.getVelocity().add(direction.multiply((level + 1) * 0.38)));
            living.setNoDamageTicks(40);
            World world = living.getWorld();
            world.spawnParticle(Particle.GEYSER_BASE, living.getLocation(), 8, 0.4, 0.3, 0.4, 0.1, new Particle.GeyserBase(1, 1.1F));
            break;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void damage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living)) return;
        if (event.getDamageSource().getDamageType() != DamageType.FLY_INTO_WALL) return;
        EntityEquipment equipment = living.getEquipment();
        if (equipment == null) return;
        for (ItemStack content : equipment.getArmorContents()) {
            int level = Survival.enchantingRegistry.getLevel(EnchantingRegistry.PROPULSION, content);
            if (level < 0) continue;
            if (event.getDamage() < 3) event.setCancelled(true);
            event.setDamage(event.getDamage() * 0.5);
            break;
        }
    }

}

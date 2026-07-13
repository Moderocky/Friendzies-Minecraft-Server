package mx.kenzie.survival.potion;

import io.papermc.paper.persistence.PersistentDataContainerView;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.potion.effects.DimensionalityMobEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Hopper;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static org.bukkit.damage.DamageType.*;

public class PotionListener implements Listener {
    final PotionManager potionManager;
    private final Set<DamageType> IGNORE_DAMAGE_TYPES = Set.of(GENERIC_KILL, OUT_OF_WORLD, OUTSIDE_BORDER, STARVE, CRAMMING);
    private final Set<DamageType> IGNORE_DAMAGE_TYPES_JITTER = Set.of(GENERIC_KILL, OUT_OF_WORLD, OUTSIDE_BORDER, STARVE, CRAMMING, MAGIC, THORNS, INDIRECT_MAGIC, WITHER, ON_FIRE);
    private final EnumSet<Material> DANGEROUS = EnumSet.of(Material.MAGMA_BLOCK, Material.CAMPFIRE, Material.WITHER_ROSE, Material.SWEET_BERRY_BUSH, Material.LAVA, Material.LAVA_CAULDRON, Material.POINTED_DRIPSTONE, Material.SULFUR_SPIKE);

    public PotionListener(PotionManager potionManager) {
        this.potionManager = potionManager;
    }

    static Inventory getOutput(BrewerInventory inventory) {
        BrewingStand holder = inventory.getHolder();
        if (holder == null) return null;
        Block block = holder.getBlock().getRelative(BlockFace.DOWN);
        if (block.getState() instanceof InventoryHolder down)
            return down.getInventory();
        return null;
    }

    static Inventory getIngredientInput(BrewerInventory inventory) {
        BrewingStand holder = inventory.getHolder();
        if (holder == null) return null;
        Block block = holder.getBlock().getRelative(BlockFace.UP);
        if (block.getState() instanceof Hopper hopper)
            return hopper.getInventory();
        return null;
    }

    static void dropLeftovers(BrewerInventory contents, ItemStack item) {
        BrewingStand holder = contents.getHolder();
        if (holder == null) return;
        Location location = holder.getLocation();
        location.add(0.5, 0.25, 0.5);
        location.getWorld().dropItem(location, item);
    }

    private static Location getDimensionalityLocation(ItemStack item) {
        if (!(item.getItemMeta() instanceof PotionMeta potion)) return null;
        if (!potion.hasCustomEffect(PotionManager.DIMENSIONALITY)) return null;
        PersistentDataContainerView container = item.getPersistentDataContainer();
        return DimensionalityMobEffect.getLocation(container);
    }

    private static void dimensionalityTeleport(Iterable<LivingEntity> entities, ItemStack item) {
        Location location = getDimensionalityLocation(item);
        if (location == null) return;
        for (LivingEntity entity : entities) {
            dimensionalityTeleport(entity, location);
        }
    }

    @Deprecated
    private static void dimensionalityTeleport(LivingEntity entity, Location location) {
        DimensionalityMobEffect.dimensionalityTeleport(entity, location);
    }

    @EventHandler(ignoreCancelled = true)
    public void noTakeBottles(InventoryMoveItemEvent event) {
        if (!(event.getSource() instanceof BrewerInventory)) return;
        Inventory destination = event.getDestination();
        if (destination.getType() == InventoryType.HOPPER && event.getInitiator() == destination)
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void invulnerable(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!entity.hasPotionEffect(PotionManager.INVULNERABILITY)) return;
        PotionEffect effect = entity.getPotionEffect(PotionManager.INVULNERABILITY);
        assert effect != null;
        int amplifier = effect.getAmplifier();
        double damage = event.getDamage() - amplifier;
        event.setDamage(damage);
        if (damage < 0.05) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void ejectResults(BrewEvent event) {
        BrewerInventory contents = event.getContents();
        BrewingStand holder = contents.getHolder();
        if (holder == null) return;
        Bukkit.getScheduler().runTaskLater(Survival.plugin, () -> {
            if (holder.getBrewingTime() > 0) return;
            Inventory output = getOutput(contents);
            if (output == null) return;
            for (int i = 0; i < 3; i++) {
                ItemStack content = contents.getItem(i);
                int finalI = i;
                potionManager.extractResultToContainer(contents, output, content, (item) -> contents.setItem(finalI, item));
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void fixItemDisplay(BrewEvent event) {
        for (ItemStack result : event.getResults()) {
            if (result == null) continue;
            potionManager.fixPotion(result);
            potionManager.fixPotionLore(result);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void pickupBottle(EntityPickupItemEvent event) {
        Item item = event.getItem();
        ItemStack stack = item.getItemStack();
        if (stack.isEmpty()) return;
        if (stack.getType() != Material.POTION && stack.getType() != Material.SPLASH_POTION && stack.getType() != Material.LINGERING_POTION)
            return;
        potionManager.fixPotion(stack);
        item.setItemStack(stack);
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        for (PotionGuide value : PotionGuide.values()) {
            if (Objects.equals(value.frame, "task")) value.complete(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void dimensionalityEffect(PlayerItemConsumeEvent event) {
        dimensionalityTeleport(List.of(event.getPlayer()), event.getItem());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void dimensionalityEffect(PotionSplashEvent event) {
        ItemStack item = event.getEntity().getItem();
        dimensionalityTeleport(event.getAffectedEntities(), item);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void dimensionalityEffect(LingeringPotionSplashEvent event) {
        ItemStack item = event.getEntity().getItem();
        Location location = getDimensionalityLocation(item);
        if (location == null) return;
        AreaEffectCloud cloud = event.getAreaEffectCloud();
        DimensionalityMobEffect.setLocation(cloud.getPersistentDataContainer(), location);

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void dimensionalityEffect(AreaEffectCloudApplyEvent event) {
        AreaEffectCloud cloud = event.getEntity();
        Location location = DimensionalityMobEffect.getLocation(cloud.getPersistentDataContainer());
        if (location == null) return;
        for (LivingEntity living : event.getAffectedEntities()) {
            dimensionalityTeleport(living, location);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void safekeepingEffect(PlayerDeathEvent event) {
        if (event.getKeepInventory()) return;
        Player entity = event.getEntity();
        if (!entity.hasPotionEffect(PotionManager.SAFEKEEPING)) return;
        int amplification = this.getPotionAmplification(entity, PotionManager.SAFEKEEPING);
        if (amplification >= 3) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
        } else {
            final float chance = amplification + 1 / 4.0F;
            final Random random = new Random();
            Iterator<ItemStack> iterator = event.getDrops().iterator();
            final List<ItemStack> itemsToKeep = event.getItemsToKeep();
            while (iterator.hasNext()) {
                ItemStack item = iterator.next();
                if (random.nextFloat() > chance) continue;
                iterator.remove();
                itemsToKeep.add(item);
            }
            if (event.getKeepLevel()) return;
            event.setNewTotalExp((int) (entity.getTotalExperience() * chance));
            event.setDroppedExp(0);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void undyingEffect(EntityDeathEvent event) {
        DamageType type = event.getDamageSource().getDamageType();
        if (IGNORE_DAMAGE_TYPES.contains(type)) return;
        LivingEntity entity = event.getEntity();
        if (!entity.hasPotionEffect(PotionManager.UNDYING)) return;
        event.setCancelled(true);
        event.setReviveHealth(0.5);
        event.setShouldPlayDeathSound(false);
        entity.setNoDamageTicks(30);
    }

    @EventHandler(ignoreCancelled = true)
    public void jitteringEffect(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!entity.isValid()) return;
        DamageType type = event.getDamageSource().getDamageType();
        if (IGNORE_DAMAGE_TYPES_JITTER.contains(type)) return;
        if (!entity.hasPotionEffect(PotionManager.JITTERING)) return;
        int amplification = this.getPotionAmplification(entity, PotionManager.JITTERING);
        if (this.teleportJitter(entity, amplification)) {
            event.setCancelled(true);
            entity.setNoDamageTicks(20);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void damageSword(EntityDamageByEntityEvent event) {
        if (event.getDamage() < 0.001) return;
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity victim) || !(damager instanceof LivingEntity attacker)) return;
        if (event.getDamageSource().getDamageType() == THORNS) check:{
            EntityEquipment equipment = attacker.getEquipment();
            if (equipment == null) break check;
            for (ItemStack content : equipment.getArmorContents()) {
                if (content == null || !content.hasItemMeta() || !content.getItemMeta().hasEnchant(Enchantment.THORNS))
                    continue;
                this.applyPotionEffects(victim, content);
            }
        }
        else if (event.getDamageSource().getDamageType() == PLAYER_ATTACK) check:{
            EntityEquipment equipment = attacker.getEquipment();
            if (equipment == null) break check;
            ItemStack item = equipment.getItemInMainHand();
            this.applyPotionEffects(victim, item);
        }
    }

    private void applyPotionEffects(LivingEntity victim, ItemStack stack) {
        if (victim.isDead()) return;
        if (!stack.hasItemMeta()) return;
        Collection<PotionEffect> effects = PotionManager.getPotionEnchantmentEffects(stack);
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        effects.forEach(effect -> meta.addCustomEffect(effect, true));
        potion.setItemMeta(meta);
        Survival.potionManager.copyCompanionData(stack, potion);
        ThrownPotion spawn = victim.getWorld().createEntity(victim.getLocation(), ThrownPotion.class);
        spawn.setItem(potion);
        final boolean called = new PotionSplashEvent(spawn, victim, null, null, Map.of(victim, 1.0)).callEvent();
        if (called) victim.addPotionEffects(effects);
    }

    private boolean teleportJitter(LivingEntity entity, int amplification) {
        if (entity.isDead()) return false;
        Location location = entity.getLocation();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            double xx = (random.nextDouble() - (double) 0.5F) * (double) (8.0F * (amplification + 1));
            double yy = random.nextInt(10) - 2;
            double zz = (random.nextDouble() - (double) 0.5F) * (double) (8.0F * (amplification + 1));
            if (this.teleport(location.clone().add(xx, yy, zz), entity)) return true;
        }
        return false;
    }

    private boolean teleport(Location location, LivingEntity entity) {
        int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();
        final World world = location.getWorld();
        int count = 0;
        while (y > world.getMinHeight() && !world.getBlockAt(x, y, z).isSolid()) {
            --y;
            ++count;
            if (count > 32) return false;
        }
        Block block = world.getBlockAt(x, y, z), up = block.getRelative(BlockFace.UP);
        if (!block.isSolid()) return false;
        if (DANGEROUS.contains(block.getType()) || DANGEROUS.contains(up.getType())) return false;
        Location add = up.getLocation().add(0.5, 0, 0.5).setRotation(entity.getEyeLocation().getRotation());
        if (entity.hasLineOfSight(add)) return false;
        entity.teleport(add);
        world.playSound(add, Sound.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.NEUTRAL, 0.5F, 1.2F);
        return true;
    }

    private int getPotionAmplification(LivingEntity entity, PotionEffectType type) {
        if (type == null) return 0;
        for (PotionEffect active : entity.getActivePotionEffects()) {
            if (type == active.getType() || type.equals(active.getType())) return active.getAmplifier();
        }
        return 0;
    }

}

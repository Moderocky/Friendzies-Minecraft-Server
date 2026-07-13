package mx.kenzie.survival.potion.effects;

import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.persistence.PersistentDataViewHolder;
import mx.kenzie.survival.potion.PotionManager;
import net.minecraft.world.effect.MobEffectCategory;
import org.bukkit.*;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public class DimensionalityMobEffect extends SimpleInstantaneousMobEffect implements CustomMobEffect, Listener {
    public static final NamespacedKey LOCATION_WORLD = new NamespacedKey("survival", "dimensional_target_world");
    public static final NamespacedKey LOCATION = new NamespacedKey("survival", "dimensional_target");

    public DimensionalityMobEffect(MobEffectCategory category, int r, int g, int b) {
        super(category, r, g, b);
    }

    public DimensionalityMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public static void setPendingTeleport(LivingEntity entity, Location location) {
        SpelunkingMobEffect.setLocation(entity.getPersistentDataContainer(), location);
    }

    public static void clearPendingTeleport(LivingEntity entity) {
        SpelunkingMobEffect.setLocation(entity.getPersistentDataContainer(), null);
    }

    public static void dimensionalityTeleport(LivingEntity entity, Location location) {
        if (location == null) return;
        try {
            Location safe = ReturnMobEffect.safeLocation(location);
            if (safe != null) location = safe;
            entity.getWorld().playEffect(entity.getEyeLocation(), Effect.ENDER_SIGNAL, 1, 30);
            entity.teleport(location, PlayerTeleportEvent.TeleportCause.CONSUMABLE_EFFECT);
            Location finalLocation = location;
            finalLocation.getWorld().playEffect(finalLocation.add(0, entity.getEyeHeight(false), 0), Effect.ENDER_SIGNAL, 1, 30);
        } finally {
            clearPendingTeleport(entity);
        }
    }

    public static @Nullable Location getLocation(PersistentDataViewHolder holder) {
        return getLocation(holder.getPersistentDataContainer());
    }

    public static @Nullable Location getLocation(PersistentDataContainerView container) {
        if (!container.has(LOCATION, PersistentDataType.INTEGER_ARRAY)) return null;
        if (!container.has(LOCATION_WORLD, PersistentDataType.STRING)) return null;
        int[] ints = container.get(LOCATION, PersistentDataType.INTEGER_ARRAY);
        assert ints != null && ints.length == 3;
        String name = container.get(LOCATION_WORLD, PersistentDataType.STRING);
        if (name == null) return null;
        World world = Bukkit.getWorld(name);
        if (world == null) return null;
        return new Location(world, ints[0], ints[1], ints[2]);
    }

    public static void setLocation(PersistentDataHolder holder, @Nullable Location location) {
        setLocation(holder.getPersistentDataContainer(), location);
    }

    public static void setLocation(PersistentDataContainer container, @Nullable Location location) {
        if (location == null) {
            container.remove(LOCATION_WORLD);
            container.remove(LOCATION);
        } else {
            World world = location.getWorld();
            String name = world.getName();
            container.set(LOCATION_WORLD, PersistentDataType.STRING, name);
            int[] ints = new int[3];
            ints[0] = location.getBlockX();
            ints[1] = location.getBlockY();
            ints[2] = location.getBlockZ();
            container.set(LOCATION, PersistentDataType.INTEGER_ARRAY, ints);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void drink(EntityPotionEffectEvent event) {
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED) return;
        PotionEffect effect = event.getNewEffect();
        if (effect == null) return;
        PotionEffectType type = effect.getType();
        if (type != PotionManager.DIMENSIONALITY) return;
        LivingEntity entity = event.getEntity();
        Location location = SpelunkingMobEffect.getLocation(entity.getPersistentDataContainer());
        if (location == null) return;
        dimensionalityTeleport(entity, location);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void dimensionalityBrew(BrewEvent event) {
        BrewingStand holder = event.getContents().getHolder();
        if (holder == null) return;
        final Location location = holder.getLocation();
        for (ItemStack result : event.getResults()) {
            if (result == null) continue;
            if (!(result.getItemMeta() instanceof PotionMeta potion)) continue;
            if (!potion.hasCustomEffect(PotionManager.DIMENSIONALITY)) continue;
            PersistentDataContainer container = potion.getPersistentDataContainer();
            Location current = DimensionalityMobEffect.getLocation(container);
            if (current != null) continue;
            DimensionalityMobEffect.setLocation(container, location);
            result.setItemMeta(potion);
        }
    }

}

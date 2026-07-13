package mx.kenzie.survival.potion.effects;

import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.persistence.PersistentDataViewHolder;
import mx.kenzie.survival.potion.PotionManager;
import net.minecraft.world.effect.MobEffectCategory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpelunkingMobEffect extends ExpiringMobEffect implements CustomMobEffect {
    public static final NamespacedKey LOCATION_WORLD = new NamespacedKey("survival", "pending_dimensional_target_world");
    public static final NamespacedKey LOCATION = new NamespacedKey("survival", "pending_dimensional_target");

    public SpelunkingMobEffect(MobEffectCategory category, int r, int g, int b) {
        super(PotionManager.DIMENSIONALITY_eff);
    }

    private static void updateSpelunkingLocation(Iterable<LivingEntity> entities, ItemStack item) {
        if (item == null || !(item.getItemMeta() instanceof PotionMeta potion)) return;
        if (!potion.hasCustomEffect(PotionManager.SPELUNKING)) return;
        for (LivingEntity entity : entities) {
            setLocation(entity.getPersistentDataContainer(), entity.getLocation());
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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void dimensionalityEffect(PlayerItemConsumeEvent event) {
        updateSpelunkingLocation(List.of(event.getPlayer()), event.getItem());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void dimensionalityEffect(PotionSplashEvent event) {
        ItemStack item = event.getEntity().getItem();
        updateSpelunkingLocation(event.getAffectedEntities(), item);
    }

}

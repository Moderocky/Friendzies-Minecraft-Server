package mx.kenzie.survival.potion.effects;

import com.google.common.base.Suppliers;
import mx.kenzie.survival.Survival;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.potion.CraftPotionEffectType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Supplier;

public class ExpiringMobEffect extends SimpleMobEffect implements Listener {
    protected final Supplier<PotionEffectType> delayed;
    protected final Supplier<PotionEffectType> ours = Suppliers.memoize(() -> CraftPotionEffectType.minecraftToBukkit(this));

    public ExpiringMobEffect(MobEffect effect) {
        super(effect.getCategory(), effect.getColor());
        this.delayed = Suppliers.memoize(() ->
                CraftPotionEffectType.minecraftToBukkit(effect));
    }

    public ExpiringMobEffect(MobEffectCategory category, int color, Holder<MobEffect> effect) {
        super(category, color);
        this.delayed = Suppliers.memoize(() -> CraftPotionEffectType.minecraftToBukkit(effect.value()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onExpiry(EntityPotionEffectEvent event) {
        if (event.getCause() != EntityPotionEffectEvent.Cause.EXPIRATION) return;
        PotionEffect oldEffect = event.getOldEffect();
        if (oldEffect == null) return;
        PotionEffectType type = event.getModifiedType();
        if (type != ours.get()) return;
        PotionEffectType newType = delayed.get();
        LivingEntity entity = event.getEntity();
        PotionEffect effect = newType.createEffect(0, oldEffect.getAmplifier());
        Bukkit.getScheduler().runTaskLater(Survival.plugin, () -> entity.addPotionEffect(effect), 1L);
    }

}

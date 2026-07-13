package mx.kenzie.survival.potion.effects;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class HomewardMobEffect extends ReturnMobEffect implements CustomMobEffect {

    public HomewardMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    protected Location startPosition(LivingEntity entity) {
        CraftLivingEntity living = entity.getBukkitLivingEntity();
        if (living instanceof CraftPlayer player)
            return player.getPotentialRespawnLocation();
        else return super.startPosition(entity);
    }

    @Override
    protected Location getSafeLocation(Location centre) {
        Location location = super.getSafeLocation(centre);
        if (location == null) return centre;
        return location;
    }

}

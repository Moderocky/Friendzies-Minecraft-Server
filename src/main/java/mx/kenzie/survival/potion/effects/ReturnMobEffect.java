package mx.kenzie.survival.potion.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.event.player.PlayerTeleportEvent;

public class ReturnMobEffect extends TeleportingMobEffect {

    public ReturnMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public ReturnMobEffect(MobEffectCategory category, int r, int g, int b) {
        super(category, r, g, b);
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        boolean b = super.applyEffectTick(serverLevel, mob, amplification);
        CraftLivingEntity living = mob.getBukkitLivingEntity();
        if (living instanceof CraftPlayer player) {
            Location location = this.startPosition(mob);
            location = this.getSafeLocation(location);
            player.getWorld().playEffect(player.getEyeLocation(), Effect.ENDER_SIGNAL, 1, 30);
            if (location != null) {
                player.teleport(location, PlayerTeleportEvent.TeleportCause.CONSUMABLE_EFFECT);
                player.getWorld().playEffect(location.add(0, player.getEyeHeight(false), 0), Effect.ENDER_SIGNAL, 1, 30);
            }

        }
        return b;
    }

    @Override
    protected Location startPosition(LivingEntity entity) {
        CraftLivingEntity living = entity.getBukkitLivingEntity();
        if (living instanceof CraftPlayer player)
            return player.getLastDeathLocation();
        if (entity.originWorld == null || entity.origin == null)
            return null;
        World world = Bukkit.getWorld(entity.originWorld);
        return CraftLocation.toBukkit(entity.origin, world);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return super.shouldApplyEffectTickThisTick(tickCount, amplification);
    }

    @Override
    public void onEffectAdded(LivingEntity mob, int amplifier) {
        super.onEffectAdded(mob, amplifier);
    }

}

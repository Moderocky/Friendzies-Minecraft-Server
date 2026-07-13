package mx.kenzie.survival.potion.effects;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Random;

public abstract class TeleportingMobEffect extends SimpleInstantaneousMobEffect implements CustomMobEffect {
    public TeleportingMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public TeleportingMobEffect(MobEffectCategory category, int r, int g, int b) {
        super(category, r, g, b);
    }

    private static boolean isOk(Location test) {
        Block block = test.getBlock();
        return block.isPassable() && block.getRelative(BlockFace.UP).isPassable() && !block.getRelative(BlockFace.DOWN).isPassable();
    }

    public static Location safeLocation(Location centre) {
        if (centre == null) return null;
        Random rand = new Random();
        Location test = centre;
        double checkRadius = 3;
        do {
            if (isOk(test)) return test;
            double x = rand.nextDouble(0, checkRadius * 2) - checkRadius;
            double y = rand.nextDouble(0, checkRadius);
            double z = rand.nextDouble(0, checkRadius * 2) - checkRadius;
            test = centre.clone().add(x, y, z);
            checkRadius += 0.15;
        } while (checkRadius < 40);
        return null;
    }

    protected abstract Location startPosition(LivingEntity entity);

    protected Location getSafeLocation(Location centre) {
        return safeLocation(centre);
    }
}

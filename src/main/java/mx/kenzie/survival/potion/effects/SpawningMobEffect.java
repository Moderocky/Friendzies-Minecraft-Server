package mx.kenzie.survival.potion.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public class SpawningMobEffect<Type extends Entity> extends SimpleMobEffect implements CustomMobEffect {
    protected static final NamespacedKey KEY = NamespacedKey.minecraft("spawn_effect");
    protected static final float RADIUS_TO_CHECK = 2;
    protected final ToIntFunction<RandomSource> spawnCount;
    protected final EntityType<Type> entityType;
    protected final SpawnFunction<Type> postSpawn;

    public SpawningMobEffect(MobEffectCategory category, EntityType<Type> entityType, SpawnFunction<@NotNull Type> postSpawn, ToIntFunction<RandomSource> spawnCount, int r, int g, int b) {
        super(category, r, g, b);
        this.entityType = entityType;
        this.spawnCount = spawnCount;
        this.postSpawn = postSpawn;
    }

    protected static int numberOfCreaturesToSpawn(int maxEntityCramming, NearbyCreatures nearbyCreatures, int numberRequested) {
        return maxEntityCramming < 1 ? numberRequested : Mth.clamp(0, maxEntityCramming - nearbyCreatures.count(maxEntityCramming), numberRequested);
    }

    @Override
    public void onMobRemoved(ServerLevel level, LivingEntity mob, int amplifier, Entity.RemovalReason reason) {
        if (mob.getType() == entityType) return;
        if (reason == Entity.RemovalReason.KILLED) {
            int entitiesToSpawn = this.spawnCount.applyAsInt(mob.getRandom());
            int maxEntityCramming = level.getGameRules().get(GameRules.MAX_ENTITY_CRAMMING);
            int numberOfEntitiesToSpawn = numberOfCreaturesToSpawn(maxEntityCramming, NearbyCreatures.closeTo(mob, entityType), entitiesToSpawn);

            for (int i = 0; i < numberOfEntitiesToSpawn; ++i) {
                this.spawnOffspring(mob.level(), mob.getX(), mob.getY() + (double) 0.5F, mob.getZ());
            }
        }
    }

    private void spawnOffspring(Level level, double x, double y, double z) {
        Type entity = entityType.create(level, EntitySpawnReason.TRIGGERED);

        if (entity == null || !postSpawn.preSpawn(entity)) return;
        entity.snapTo(x, y, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(entity, CreatureSpawnEvent.SpawnReason.POTION_EFFECT);
    }

    @FunctionalInterface
    protected interface NearbyCreatures {
        private static NearbyCreatures closeTo(LivingEntity mob, EntityType<?> entityType) {
            return (maxResults) -> {
                List<Entity> slimesNearby = new ArrayList<>();
                mob.level().getEntities(entityType, mob.getBoundingBox().inflate(RADIUS_TO_CHECK), (slime) -> slime != mob, slimesNearby, maxResults);
                return slimesNearby.size();
            };
        }

        int count(int var1);
    }

    @FunctionalInterface
    public interface SpawnFunction<Type extends Entity> {
        boolean preSpawn(@NotNull Type entity);
    }
}

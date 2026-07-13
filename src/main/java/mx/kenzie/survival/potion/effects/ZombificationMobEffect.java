package mx.kenzie.survival.potion.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.villager.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;

public class ZombificationMobEffect extends SimpleInstantaneousMobEffect implements CustomMobEffect {

    public ZombificationMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public ZombificationMobEffect(MobEffectCategory category, int r, int g, int b) {
        super(category, r, g, b);
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        LivingEntity result = switch (mob) {
            case Pig entity ->
                    entity.convertTo(EntityTypes.ZOMBIFIED_PIGLIN, ConversionParams.single(entity, true, true), (ConversionParams.AfterConversion) (zombified) -> zombified.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)), EntityTransformEvent.TransformReason.INFECTION, CreatureSpawnEvent.SpawnReason.INFECTION);

            case Villager entity ->
                    Zombie.convertVillagerToZombieVillager(serverLevel, entity, entity.blockPosition(), true, EntityTransformEvent.TransformReason.INFECTION, CreatureSpawnEvent.SpawnReason.INFECTION);

            case Nautilus entity ->
                    entity.convertTo(EntityTypes.ZOMBIE_NAUTILUS, ConversionParams.single(entity, true, true), (ConversionParams.AfterConversion) (zombified) -> zombified.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)), EntityTransformEvent.TransformReason.INFECTION, CreatureSpawnEvent.SpawnReason.INFECTION);

            case Horse entity ->
                    entity.convertTo(EntityTypes.ZOMBIE_HORSE, ConversionParams.single(entity, true, true), (ConversionParams.AfterConversion) (zombified) -> zombified.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)), EntityTransformEvent.TransformReason.INFECTION, CreatureSpawnEvent.SpawnReason.INFECTION);

            case Hoglin entity ->
                    entity.convertTo(EntityTypes.ZOGLIN, ConversionParams.single(entity, true, true), (ConversionParams.AfterConversion) (zombified) -> zombified.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)), EntityTransformEvent.TransformReason.INFECTION, CreatureSpawnEvent.SpawnReason.INFECTION);

            case Piglin entity ->
                    entity.convertTo(EntityTypes.ZOMBIFIED_PIGLIN, ConversionParams.single(entity, true, true), (ConversionParams.AfterConversion) (zombified) -> zombified.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)), EntityTransformEvent.TransformReason.INFECTION, CreatureSpawnEvent.SpawnReason.INFECTION);

            case Guardian entity ->
                    entity.convertTo(EntityTypes.ELDER_GUARDIAN, ConversionParams.single(entity, true, true), (ConversionParams.AfterConversion) (zombified) -> zombified.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)), EntityTransformEvent.TransformReason.INFECTION, CreatureSpawnEvent.SpawnReason.INFECTION);

            default -> mob;
        };
        return super.applyEffectTick(serverLevel, mob, amplification);
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

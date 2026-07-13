package mx.kenzie.survival.robot.task;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.memory.MemoryKey;

import java.util.Collection;

public interface Task<Creature extends LivingEntity> {

    NamespacedKey key();

    void start(World world, Creature creature, long timestamp);

    void tick(World world, Creature creature, long timestamp);

    void end(World world, Creature creature, long timestamp);

    Collection<MemoryCondition> startConditions();


    default int minimumDuration() {
        return this.maximumDuration();
    }

    int maximumDuration();


    default boolean checkExtraStartConditions(World world, Creature creature) {
        creature.getMemory(MemoryKey.ADMIRING_DISABLED);
        return true;
    }

    enum Status {
        VALUE_PRESENT,
        VALUE_ABSENT,
        REGISTERED

    }

    record MemoryCondition(Keyed key, Status status) {
    }
}

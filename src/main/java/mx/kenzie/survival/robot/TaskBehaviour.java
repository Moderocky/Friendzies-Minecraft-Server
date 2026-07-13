package mx.kenzie.survival.robot;

import mx.kenzie.survival.robot.task.Task;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TaskBehaviour<Minecraft extends LivingEntity, Bukkit extends org.bukkit.entity.LivingEntity> extends Behavior<Minecraft> {

    public TaskBehaviour(Task<Bukkit> task) {
        super(buildMemoryMap(task.startConditions()), task.maximumDuration(), task.maximumDuration());
    }

    static Map<MemoryModuleType<?>, MemoryStatus> buildMemoryMap(Iterable<Task.MemoryCondition> memoryConditions) {
        Map<MemoryModuleType<?>, MemoryStatus> map = new HashMap<>();
        for (Task.MemoryCondition condition : memoryConditions) {
            NamespacedKey key = condition.key().getKey();
            Identifier identifier = Identifier.tryBuild(key.namespace(), key.value());
            if (identifier == null) continue;
            Optional<Holder.Reference<MemoryModuleType<?>>> module = BuiltInRegistries.MEMORY_MODULE_TYPE.get(identifier);
            module.ifPresent(reference -> map.put(reference.value(), RobotManager.toInternal(condition.status())));
        }
        return map;
    }
}

package mx.kenzie.survival.robot;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.robot.program.Program;
import mx.kenzie.survival.robot.task.Task;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftCopperGolem;
import org.bukkit.entity.CopperGolem;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RobotManager {

    static {
        Task.Status[] a = Task.Status.values();
        MemoryStatus[] b = MemoryStatus.values();
        assert a.length == b.length;
        for (int i = 0; i < a.length; i++) {
            assert a[i].name().equals(b[i].name());
        }
    }

    protected final Map<NamespacedKey, Task<CopperGolem>> tasks = new LinkedHashMap<>();

    static MemoryStatus toInternal(Task.Status status) {
        return switch (status) {
            case Task.Status.VALUE_PRESENT -> MemoryStatus.VALUE_PRESENT;
            case Task.Status.VALUE_ABSENT -> MemoryStatus.VALUE_ABSENT;
            case Task.Status.REGISTERED -> MemoryStatus.REGISTERED;
        };
    }

    public void setup() {

        new TestingCommand().register(Survival.plugin);
    }

    public void registerTask(Task<CopperGolem> task) {
        this.registerTask(task.key(), task);
    }

    protected void registerTask(NamespacedKey key, Task<CopperGolem> task) {
        tasks.put(key, task);
    }

    public boolean isCircuit(ItemStack stack) {

        return false;//todo
    }

    public Program getProgram(ItemStack circuit) {
        return null;//todo
    }

    protected boolean isProgrammed(CopperGolem golem) {
        if (golem.isDead()) return false;
        EntityEquipment equipment = golem.getEquipment();
        ItemStack item = equipment.getItem(EquipmentSlot.SADDLE);
        return (this.isCircuit(item));
    }


    public void applyProgram(CopperGolem golem, Program program) {
        final var handle = ((CraftCopperGolem) golem).getHandle();
        handle.removeFreeWill();

        var brain = handle.getBrain();

        for (var activity : this.toActivities(program)) {
            brain.addActivity(activity.activityType(), activity.behaviorPriorityPairs(), activity.conditions(), activity.memoriesToEraseWhenStopped());
        }
    }

    protected List<ActivityData<net.minecraft.world.entity.animal.golem.CopperGolem>> toActivities(Program program) {
        //todo
        return null;
    }

}

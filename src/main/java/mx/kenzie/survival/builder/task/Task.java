package mx.kenzie.survival.builder.task;

import mx.kenzie.survival.Survival;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface Task {
    boolean tick();

    void finish();

    boolean isFinished();

    int getTotalStages();

    int getStage();

    int remaining();

    void cancel();

    boolean isCancelled();

    default void onEnd() {
    }

    @Nullable Player owner();

    int taskId();

    default NamespacedKey identifier() {
        return new NamespacedKey(Survival.plugin, "task_" + this.taskId());
    }

    Component name();

}

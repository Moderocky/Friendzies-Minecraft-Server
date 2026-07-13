package mx.kenzie.survival.builder.task;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.util.function.Consumer;

public interface BuildTask extends Task {

    default boolean tick(Consumer<Step> consumer) {
        consumer.accept(this.peek());
        return this.tick();
    }

    Step peek();

    @Override
    String toString();

    @Override
    default Component name() {
        return Component.text("Build Task");
    }

    record Step(BlockData data, Location location) {
    }

}

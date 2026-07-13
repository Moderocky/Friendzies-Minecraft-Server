package mx.kenzie.survival.builder;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.task.Task;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBarViewer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Iterator;
import java.util.Set;

public class TickListener implements Listener {

    private static final int MAX = 100;
    private static final String UNIT = "ı";

    @EventHandler
    public void event(ServerTickEndEvent event) {
        if (event.getTickDuration() > 45) return;
        final Set<Task> tasks = Survival.builder.tasks;
        final Iterator<Task> iterator = tasks.iterator();
        check:
        while (iterator.hasNext()) {
            final Task task = iterator.next();
            int attempts = 0;
            if (task.isCancelled() || task.isFinished()) {
                task.onEnd();
                iterator.remove();
                BossBar bar = Survival.builder.bars.remove(task);
                if (bar != null) {
                    for (BossBarViewer viewer : bar.viewers()) {
                        if (viewer instanceof Audience audience)
                            bar.removeViewer(audience);
                    }
                }
            } else while (++attempts < 64 && !task.tick() && !task.isFinished() && !task.isCancelled())
                if (event.getTickDuration() > 48) break check;
            this.drawProgressBar(task);
        }
    }

    protected void drawProgressBar(Task task) {
        final Player player = task.owner();
        if (player == null || !player.isOnline()) return;
        final float progress = Math.min(1, Math.max((float) task.getStage() / Math.max(1, task.getTotalStages()), 0));
        BossBar bar = Survival.builder.bars.get(task);
        if (bar != null) {
            bar.progress(progress);
        }
//        final int current = (int) (progress * MAX), remaining = MAX - current;
//        player.sendActionBar(Component.textOfChildren(
//                Component.text(UNIT.repeat(current), NamedTextColor.GREEN),
//                Component.text(UNIT.repeat(remaining), NamedTextColor.GRAY)
//        ));
    }

}

package mx.kenzie.survival.utility;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.command.GetCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.entity.CraftAllay;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ItemFetcher implements Listener {

    protected final Survival plugin;

    public ItemFetcher(Survival plugin) {
        this.plugin = plugin;
    }


    public void fetch(ItemStack item, Target source, Target target) {
        if (item == null) return;
        Location start = source.get();
        Location end = target.get();
        if (start == null || end == null) return;
        if (start.getWorld() != end.getWorld()) this.fetchFromOtherWorld(item, target);
        else this.fetchFromSameWorld(item, source, target);
    }

    protected void fetchFromSameWorld(ItemStack item, Target source, Target target) {
        Location start = source.get();
        Location end = target.get();
        Vector direction = end.toVector().subtract(start.toVector()), normalised, facing;
        normalised = direction.normalize();
        facing = normalised.clone().multiply(-1);
        if (start.distanceSquared(end) >= (48 * 48)) {
            Location closer = end.clone().add(normalised.clone().multiply(20F));
            this.drawAllayFrom(closer, facing, target, item);
        } else {
            this.drawAllayFrom(source.get(), facing, target, item);
        }
    }

    protected void drawAllayFrom(Location start, Vector facing, Target target, ItemStack item) {
        Allay spawn = start.getWorld().spawn(start, Allay.class, allay -> {
            allay.setCanPickupItems(false);
            allay.setCanDuplicate(false);
            allay.setInvulnerable(true);
            allay.setNoPhysics(true);
            allay.setAware(false);
            allay.getEquipment().setItem(EquipmentSlot.HAND, item);
            net.minecraft.world.entity.animal.allay.Allay handle = ((CraftAllay) allay).getHandle();
            allay.setVelocity(facing);
        });
        AtomicInteger counter = new AtomicInteger(64);
        Bukkit.getScheduler().runTaskTimer(plugin, (job) -> {
            Location location = target.get();
            Location here = spawn.getLocation();
            if (location.distanceSquared(here) <= (1)) {
                ItemFetcher.this.endTask(spawn, item, target);
                job.cancel();
                return;
            }
            Vector direction = location.toVector().subtract(here.toVector()).normalize();
            if (location.getWorld() != here.getWorld() || counter.decrementAndGet() <= 0) {
                ItemFetcher.this.endTask(spawn, item, target);
                job.cancel();
            } else {
                this.setRotation(spawn, direction);
                direction.multiply(0.2);
                spawn.setVelocity(direction);
            }
        }, 10, 10);
    }

    private void setRotation(Entity entity, Vector vector) {
        final float pitch, yaw;
        final double _2PI = 2 * Math.PI;
        final double x = vector.getX();
        final double z = vector.getZ();

        if (x == 0 && z == 0) {
            pitch = vector.getY() > 0 ? -90 : 90;
            yaw = 0;
        } else {
            double theta = Math.atan2(-x, z);
            yaw = (float) Math.toDegrees((theta + _2PI) % _2PI);
            double x2 = NumberConversions.square(x);
            double z2 = NumberConversions.square(z);
            double xz = Math.sqrt(x2 + z2);
            pitch = (float) Math.toDegrees(Math.atan(-vector.getY() / xz));
        }
        entity.setRotation(pitch, yaw);
    }

    private void endTask(Allay allay, ItemStack item, Target end) {
        if (allay.isDead()) return;
        allay.remove();
        GetCommand.giveItemsSafely(end.inventory.get(), end.get(), item);
    }

    protected void fetchFromOtherWorld(ItemStack item, Target target) {

    }

    @EventHandler
    public void onTick(ServerTickEndEvent event) {

    }

    public record Target(Supplier<Inventory> inventory,
                         Supplier<Location> target) implements InventoryHolder, Supplier<Location> {

        public static <Type extends Entity & InventoryHolder> Target entity(Type entity) {
            return new Target(entity::getInventory, entity::getLocation);
        }

        public static <Type extends InventoryHolder & BlockState> Target block(Type entity) {
            return new Target(entity::getInventory, entity::getLocation);
        }

        @Override
        public Location get() {
            return target.get();
        }

        public double x() {
            return this.get().getX();
        }

        public double y() {
            return this.get().getY();
        }

        public double z() {
            return this.get().getZ();
        }

        public double yaw() {
            return this.get().getYaw();
        }

        public double pitch() {
            return this.get().getPitch();
        }

        public World world() {
            return this.get().getWorld();
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory.get();
        }
    }


}

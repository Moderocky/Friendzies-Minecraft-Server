package mx.kenzie.survival.geyser;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static mx.kenzie.survival.geyser.GeyserInternal.getUnobstructedBlockCount;

public class DispenserListener implements Listener {
    public static final int LIFETIME = 80;
    public static final int LENGTH = 5;
    protected final Map<Block, Geyser> geysers = new HashMap<>();

    @EventHandler
    public void onDispense(BlockDispenseEvent event) {
        if (event.isCancelled()) return;
        ItemStack item = event.getItem();
        if (item.getType() != Material.LAVA_BUCKET) return;
        Block block = event.getBlock();
        Directional dispenser = (Directional) block.getBlockData();
        BlockFace facing = dispenser.getFacing();
        Block relative = block.getRelative(facing);
        if (relative.getType() != Material.POTENT_SULFUR) return;
        event.setCancelled(true);
        Block source = relative.getRelative(facing);
        if (source.isSolid()) return;
        this.geyser(source, facing);
    }

    public void geyser(Block source, BlockFace facing) {
        Vector direction = facing.getDirection();
        int geyserForceSize = getUnobstructedBlockCount(source, facing, LENGTH);
        BoundingBox box = new BoundingBox(source.getX(), source.getY(), source.getZ(), source.getX() + 1, source.getY() + 1, source.getZ() + 1);
        box = box.expand(direction, geyserForceSize);
        Geyser geyser = new Geyser(source.getWorld(), source.getLocation().add(0.5, 0.5, 0.5), direction, box, Bukkit.getCurrentTick());
        geysers.put(source, geyser);
    }

    @EventHandler
    public void event(ServerTickEndEvent event) {
        if (event.getTickDuration() > 80) return;
        int tickNumber = event.getTickNumber();
        final Iterator<Map.Entry<Block, Geyser>> iterator = geysers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Block, Geyser> entry = iterator.next();
            final Geyser value = entry.getValue();
            if (!value.world.isPositionLoaded(value.source)) {
                iterator.remove();
                continue;
            }
            final int created = value.created;
            if (tickNumber < created || tickNumber > created + LIFETIME) {
                iterator.remove();
                continue;
            }
            value.tick();
        }
    }

    protected record Geyser(World world, Location source, Vector direction, BoundingBox box, int created) {

        void tick() {
            Collection<Entity> entities = world.getNearbyEntities(box);
            Vector force = direction.clone().multiply(0.2);
            this.spawnGeyserParticle(source);
            for (Entity entity : entities) {
                if (entity.isDead()) continue;
                if (entity.hasNoPhysics()) continue;
                if (entity.isInsideVehicle()) continue;
                if (Tag.ENTITY_TYPES_NOT_AFFECTED_BY_GEYSERS.isTagged(entity.getType())) continue;
                if (entity instanceof Player living
                        && (living.getGameMode() == GameMode.SPECTATOR || living.isFlying())) continue;
                entity.setFallDistance(0.0F);
                Vector velocity = entity.getVelocity();
                double dot = direction.dot(velocity);
                if (dot > 0.31) continue;
                entity.setVelocity(velocity.add(force));
            }
            if (Bukkit.getCurrentTick() % 20 == 0)
                source.getWorld().playSound(source, Sound.BLOCK_POTENT_SULFUR_GEYSER_CONTINUOUS_ERUPTION_ACTIVE, 0.5F, 1F);
        }

        void spawnGeyserParticle(Location location) {
            world.spawnParticle(Particle.GEYSER_BASE, location, 0, direction.getX(), direction.getY(), direction.getZ(), 2.0, new Particle.GeyserBase(1, 0.5F));
            world.spawnParticle(Particle.GEYSER_BASE, location, 0, direction.getX(), direction.getY(), direction.getZ(), 2.0, new Particle.GeyserBase(1, 0.5F));
            world.spawnParticle(Particle.GEYSER_BASE, location, 2, 0.4, 0.4, 0.4, 0.1, new Particle.GeyserBase(1, 1.1F));
        }

    }


}

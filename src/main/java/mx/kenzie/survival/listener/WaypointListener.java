package mx.kenzie.survival.listener;

import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.persistence.PersistentDataViewHolder;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.utility.DataHelper;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.minecraft.world.waypoints.WaypointStyleAssets.ROOT_ID;

public class WaypointListener implements Listener {
    public static final NamespacedKey WAYPOINTS = Survival.key("waypoints");
    public static final NamespacedKey KIND = Survival.key("kind");
    public static final ResourceKey<WaypointStyleAsset> BOWTIE = createId("bowtie");
    private static final ResourceKey<WaypointStyleAsset> DEFAULT = createId("default");
    private static final ResourceKey<WaypointStyleAsset> HUMAN = createId("server", "human");
    private static final ResourceKey<WaypointStyleAsset> UNKNOWN = createId("server", "unknown");
    private static final ResourceKey<WaypointStyleAsset> NETHER_PORTAL = createId("server", "nether_portal");
    private static final ResourceKey<WaypointStyleAsset> NETHER_PORTAL_OVERWORLD = createId("server", "nether_portal_overworld");
    private static final ResourceKey<WaypointStyleAsset> END_PORTAL = createId("server", "end_portal");
    private static final ResourceKey<WaypointStyleAsset> OBSIDIAN_PLATFORM = createId("server", "obsidian_platform");

    static ResourceKey<WaypointStyleAsset> parseId(String name) {
        return ResourceKey.create(ROOT_ID, Identifier.parse(name));
    }

    static ResourceKey<WaypointStyleAsset> createId(String name) {
        return ResourceKey.create(ROOT_ID, Identifier.withDefaultNamespace(name));
    }


    static ResourceKey<WaypointStyleAsset> createId(String namespace, String name) {
        return ResourceKey.create(ROOT_ID, Identifier.fromNamespaceAndPath(namespace, name));
    }

    private static void removeIfNear(Player player, Location location, ResourceKey<WaypointStyleAsset> kind) {
        List<Waypoint> waypoints = new ArrayList<>(getWaypoints(player));
        Iterator<Waypoint> iterator = waypoints.iterator();
        boolean changed = false;
        while (iterator.hasNext()) {
            Waypoint waypoint = iterator.next();
            if (waypoint.isNear(location) && waypoint.kind.equals(kind.identifier().toString())) {
                notifyRemove(player, waypoint);
                iterator.remove();
                changed = true;
            }
        }
        if (changed) setWaypoints(player, waypoints);
    }

    private static void updateWaypoints(Player player) {
        World world = player.getWorld();
        List<Waypoint> waypoints = getWaypoints(player);
        for (Waypoint waypoint : waypoints) {
            if (waypoint.location.getWorld() != world) notifyRemove(player, waypoint);
            else notifyAdd(player, waypoint);
        }
    }

    private static void notifyAdd(Player player, Waypoint waypoint) {
        Location location = waypoint.location();
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        UUID uuid = toUUID(location);
        net.minecraft.world.waypoints.Waypoint.Icon icon = new net.minecraft.world.waypoints.Waypoint.Icon();
        icon.style = waypoint.getKey();
        icon.color = Optional.of(Color.fromRGB(255, 255, 255).asRGB());
        connection.send(ClientboundTrackedWaypointPacket.addWaypointPosition(uuid, icon, new Vec3i(location.getBlockX(), location.getBlockY(), location.getBlockZ())));
    }

    private static void notifyRemove(Player player, Waypoint waypoint) {
        notifyRemove(player, waypoint.location);
    }

    private static void notifyRemove(Player player, Location location) {
        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        UUID uuid = toUUID(location);
        connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(uuid));
    }

    public static UUID toUUID(Location location) {
        World world = location.getWorld();
        final int worldNum = world != null ? world.getUID().hashCode() : 0;
        final int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();
        long a = (((long) x) << 32) | (y & 0xffffffffL), b = (((long) z) << 32) | (worldNum & 0xffffffffL);
        return new UUID(a, b);
    }

    public static List<Waypoint> getWaypoints(PersistentDataViewHolder holder) {
        return getWaypoints(holder.getPersistentDataContainer());
    }

    public static List<Waypoint> getWaypoints(PersistentDataContainerView container) {
        List<PersistentDataContainer> list = container.getOrDefault(WAYPOINTS, PersistentDataType.LIST.dataContainers(), List.of());
        return list.stream().map(WaypointListener::loadLocation).toList();
    }

    public static void setWaypoints(PersistentDataHolder holder, Iterable<Waypoint> locations) {
        setWaypoints(holder.getPersistentDataContainer(), locations);
    }

    public static void setWaypoints(PersistentDataContainer container, Iterable<Waypoint> locations) {
        PersistentDataAdapterContext context = container.getAdapterContext();
        List<PersistentDataContainer> containers = new ArrayList<>();
        for (Waypoint waypoint : locations) {
            PersistentDataContainer added = context.newPersistentDataContainer();
            saveLocation(added, waypoint);
            containers.add(added);
        }
        container.set(WAYPOINTS, PersistentDataType.LIST.dataContainers(), containers);
    }

    public static void addWaypoint(PersistentDataHolder holder, Waypoint waypoint) {
        if (waypoint == null) return;
        if (holder instanceof Player player) notifyAdd(player, waypoint);
        addWaypoint(holder.getPersistentDataContainer(), waypoint);
    }

    public static void addWaypoint(PersistentDataContainer container, Waypoint waypoint) {
        if (waypoint == null) return;
        PersistentDataAdapterContext context = container.getAdapterContext();
        List<PersistentDataContainer> list = new ArrayList<>(container.getOrDefault(WAYPOINTS, PersistentDataType.LIST.dataContainers(), List.of()));
        PersistentDataContainer added = context.newPersistentDataContainer();
        saveLocation(added, waypoint);
        list.add(added);
        container.set(WAYPOINTS, PersistentDataType.LIST.dataContainers(), list);
    }

    public static void removeWaypoint(PersistentDataHolder holder, @Nullable Location location) {
        if (location == null) return;
        if (holder instanceof Player player) notifyRemove(player, location);
        removeWaypoint(holder.getPersistentDataContainer(), location);
    }

    public static void removeWaypoint(PersistentDataContainer container, @Nullable Location location) {
        if (location == null) return;
        List<PersistentDataContainer> list = new ArrayList<>(container.getOrDefault(WAYPOINTS, PersistentDataType.LIST.dataContainers(), List.of()));
        list.removeIf(c -> loadLocation(c).location.equals(location));
        container.set(WAYPOINTS, PersistentDataType.LIST.dataContainers(), list);
    }

    private static void saveLocation(PersistentDataContainer container, Waypoint waypoint) {
        DataHelper.saveLocation(container, waypoint.location);
        container.set(KIND, PersistentDataType.STRING, waypoint.kind);
    }

    private static Waypoint loadLocation(PersistentDataContainerView container) {
        Location location = DataHelper.loadLocation(container);
        String orDefault = container.getOrDefault(KIND, PersistentDataType.STRING, UNKNOWN.identifier().toString());
        return new Waypoint(orDefault, location);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom(), to = event.getTo();
        Player player = event.getPlayer();
        switch (event.getCause()) {
            case END_PORTAL:
                removeIfNear(player, from, END_PORTAL);
                removeIfNear(player, to, END_PORTAL);
                break;
            case NETHER_PORTAL:
                removeIfNear(player, from, NETHER_PORTAL);
                removeIfNear(player, to, NETHER_PORTAL);
                removeIfNear(player, from, NETHER_PORTAL_OVERWORLD);
                removeIfNear(player, to, NETHER_PORTAL_OVERWORLD);
                break;
            default:
                return;
        }
        switch (event.getCause()) {
            case END_PORTAL:
                if (from.getWorld().getEnvironment() == World.Environment.THE_END) break;
                addWaypoint(player, new Waypoint(END_PORTAL, from));
                addWaypoint(player, new Waypoint(OBSIDIAN_PLATFORM, to));
                break;
            case NETHER_PORTAL:
                addWaypoint(player, new Waypoint(from.getWorld().getEnvironment() == World.Environment.NETHER ? NETHER_PORTAL : NETHER_PORTAL_OVERWORLD, from));
                addWaypoint(player, new Waypoint(to.getWorld().getEnvironment() == World.Environment.NETHER ? NETHER_PORTAL : NETHER_PORTAL_OVERWORLD, to));
                break;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (block.getType() != Material.LODESTONE) return;
        addWaypoint(event.getPlayer(), new Waypoint(UNKNOWN, block.getLocation()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.LODESTONE) return;
        Location location = block.getLocation();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            removeWaypoint(onlinePlayer, location);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.checkWaypointsValid(player);
        //noinspection PatternValidation
        player.setWaypointStyle(Key.key(HUMAN.registry().toString()));
        updateWaypoints(player);
    }

    private void checkWaypointsValid(PersistentDataHolder holder) {
        List<Waypoint> list = new ArrayList<>(getWaypoints(holder));
        if (list.isEmpty()) return;
        list.removeIf(Waypoint::isInvalid);
        setWaypoints(holder, list);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(Survival.plugin, () -> updateWaypoints(player), 1L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void teleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getFrom().getWorld() == event.getTo().getWorld()) return;
        Bukkit.getScheduler().runTaskLater(Survival.plugin, () -> updateWaypoints(player), 1L);
    }

    public record Waypoint(String kind, Location location) {
        public Waypoint(NamespacedKey kind, Location location) {
            this(kind.asString(), location);
        }

        public Waypoint(ResourceKey<?> kind, Location location) {
            this(kind.identifier().toString(), location);
        }

        public Waypoint(Location location) {
            this(UNKNOWN, location);
        }

        public ResourceKey<WaypointStyleAsset> getKey() {
            return parseId(kind);
        }

        public void revealTo(Player player) {
            notifyAdd(player, this);
        }

        public void hideFrom(Player player) {
            notifyRemove(player, this);
        }

        public boolean isInvalid() {
            Block block = location.getBlock();
            return switch (kind) {
                case "server:unknown" -> block.getType() != Material.LODESTONE;
                case "server:nether_portal" ->
                        block.getType() != Material.OBSIDIAN && block.getType() != Material.NETHER_PORTAL;
                default -> false;
            };
        }

        public boolean isNear(Location location) {
            if (location == null) return false;
            if (this.location.getWorld() != location.getWorld()) return false;
            return location.distanceSquared(this.location) < 32 * 32;
        }
    }
}

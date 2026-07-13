package mx.kenzie.survival.utility;

import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DataHelper {
    private static final String DATA_HELPER = "data_helper";
    private static final NamespacedKey POSITION = new NamespacedKey(DATA_HELPER, "position");
    private static final NamespacedKey WORLD = new NamespacedKey(DATA_HELPER, "world");

    public static @Nullable Location loadLocation(PersistentDataContainerView container) {
        if (!container.has(POSITION, PersistentDataType.INTEGER_ARRAY)) return null;
        int[] ints = container.get(POSITION, PersistentDataType.INTEGER_ARRAY);
        assert ints != null && ints.length == 3;
        if (!container.has(WORLD, PersistentDataType.STRING)) return new Location(null, ints[0], ints[1], ints[2]);
        String uid = container.get(WORLD, PersistentDataType.STRING);
        World world;
        if (uid == null) world = null;
        else world = Bukkit.getWorld(UUID.fromString(uid));
        return new Location(world, ints[0], ints[1], ints[2]);
    }

    public static void saveLocation(PersistentDataContainer container, @NotNull Location location) {
        World world = location.getWorld();
        container.set(WORLD, PersistentDataType.STRING, world.getUID().toString());
        int[] ints = new int[3];
        ints[0] = location.getBlockX();
        ints[1] = location.getBlockY();
        ints[2] = location.getBlockZ();
        container.set(POSITION, PersistentDataType.INTEGER_ARRAY, ints);
    }

    public interface Containerised {

        void loadData(PersistentDataContainer container);

        void saveData(PersistentDataContainer container);


    }


}

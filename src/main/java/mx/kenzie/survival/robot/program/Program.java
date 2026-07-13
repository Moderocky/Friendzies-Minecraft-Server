package mx.kenzie.survival.robot.program;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;

public interface Program {

    PersistentDataContainer store(PersistentDataAdapterContext context);

}

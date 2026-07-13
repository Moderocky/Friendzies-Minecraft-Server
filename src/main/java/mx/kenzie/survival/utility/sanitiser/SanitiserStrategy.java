package mx.kenzie.survival.utility.sanitiser;

import net.minecraft.network.protocol.Packet;

public interface SanitiserStrategy<P extends Packet<?>> {
    void sanitise(P packet);

    Class<P> getPacketClass();
}

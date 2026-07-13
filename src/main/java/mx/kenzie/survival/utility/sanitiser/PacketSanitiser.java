package mx.kenzie.survival.utility.sanitiser;

import io.netty.channel.*;
import io.papermc.paper.network.ChannelInitializeListenerHolder;
import net.kyori.adventure.key.Key;
import net.minecraft.network.protocol.Packet;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@ChannelHandler.Sharable
public class PacketSanitiser extends ChannelOutboundHandlerAdapter implements Closeable {
    private final List<SanitiserStrategy<?>> sanitisers = new ArrayList<>();
    private final List<Channel> hookedChannels = new ArrayList<>();

    private PacketSanitiser() {
    }

    public static PacketSanitiser hook() {
        final PacketSanitiser sanitiser = new PacketSanitiser();
        ChannelInitializeListenerHolder.addListener(sanitiser.getKey(), channel -> {
            channel.pipeline().addLast(sanitiser);
            sanitiser.hookedChannels.add(channel);
        });

        return sanitiser;
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) throws Exception {
        if (msg instanceof final Packet<?> packet) sanitise(packet);
        super.write(ctx, msg, promise);
    }

    @SuppressWarnings("PatternValidation")
    private Key getKey() {
        return Key.key("cenchants", "packet_sanitiser_" + hashCode());
    }

    public void addStrategy(final SanitiserStrategy<?> sanitiser) {
        sanitisers.add(sanitiser);
    }

    private void sanitise(final Packet<?> packet) {
        for (final SanitiserStrategy<?> sanitiser : sanitisers) {
            if (sanitiser.getPacketClass().isInstance(packet)) {
                sanitise((SanitiserStrategy<? super Packet<?>>) sanitiser, packet);
            }
        }
    }

    private <P extends Packet<?>> void sanitise(final SanitiserStrategy<P> strategy, final P packet) {
        strategy.sanitise(packet);
    }

    @Override
    public void close() {
        for (final Channel channel : hookedChannels) {
            try {
                channel.pipeline().remove(this);
            } catch (final NoSuchElementException ignored) {
            }
        }
        ChannelInitializeListenerHolder.removeListener(getKey());
    }
}

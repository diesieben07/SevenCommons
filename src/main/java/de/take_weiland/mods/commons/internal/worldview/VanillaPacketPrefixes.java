package de.take_weiland.mods.commons.internal.worldview;

import com.google.common.collect.BiMap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.internal.client.ClientProxy;
import de.take_weiland.mods.commons.internal.client.worldview.WorldViewImpl;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;

import static net.minecraft.client.Minecraft.getMinecraft;

/**
 * This is absolutely hacky and evil. Please move on.
 *
 * @author diesieben07
 */
@SuppressWarnings("unused")
public class VanillaPacketPrefixes {

    // if packet has no prefix, layout is like vanilla: <packetID><payload>
    // if packet has prefix, layout is: <PREFIX_ID><packetID><prefixData><payload>

    public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/worldview/VanillaPacketPrefixes";

    public static final int PREFIX_ID   = 127; // highest one-byte VarInt
    public static final int NOOP_DIM_ID = Integer.MIN_VALUE;

    public static final String WRITE_PREFIX = "writePrefix";

    public static void writePrefix(PacketBuffer buf, VanillaPacketProxy proxy) {
        if (proxy._sc$targetDimension() != NOOP_DIM_ID) {
            buf.writeVarIntToBuffer(PREFIX_ID);
        }
    }

    public static final String WRITE_DIM_ID = "writeDimId";

    public static void writeDimId(PacketBuffer buf, VanillaPacketProxy proxy) {
        int dimension = proxy._sc$targetDimension();
        if (dimension != NOOP_DIM_ID) {
            buf.writeInt(dimension);
        }
    }

    public static final String IS_PREFIX_ID = "isPrefix";

    public static boolean isPrefix(int id) {
        return id == PREFIX_ID;
    }

    public static final String READ_WITH_PREFIX = "readPacketWithPrefix";

    @SuppressWarnings("unchecked")
    public static Packet readPacketWithPrefix(ChannelHandlerContext context, PacketBuffer buf, int packetId) {
        // ASM patched code already read <PREFIX_ID>
        Packet packet = Packet.generatePacket((BiMap<Integer, Class<?>>) context.channel().attr(NetworkManager.attrKeyReceivable).get(), packetId);
        ((VanillaPacketProxy) packet)._sc$setTargetDimension(buf.readInt());
        return packet;
    }

    public static final String PRE_PACKET_PROCESS = "prePacketProcess";

    @SideOnly(Side.CLIENT)
    private static WorldClient clientWorldBackup;

    @SideOnly(Side.CLIENT)
    public static void prePacketProcess(boolean isClient, Packet packet) throws Throwable {
        if (!isClient) return;

        if (clientWorldBackup != null) {
            System.out.println("=== WARNING!!! LEFTOVER World backup! ===");
        }

        int targetDim = ((VanillaPacketProxy) packet)._sc$targetDimension();
        WorldClient world = getMinecraft().theWorld;
        if (targetDim != NOOP_DIM_ID && targetDim != world.provider.dimensionId) {
            clientWorldBackup = world;
            WorldClient newWorld = WorldViewImpl.getOrCreateWorld(targetDim);
            getMinecraft().theWorld = newWorld;
            ClientProxy.netHandlerClientWorldSet.invokeExact(getMinecraft().getNetHandler(), newWorld);
        }
    }

    public static final String POST_PACKET_PROCESS = "postPacketProcess";

    @SideOnly(Side.CLIENT)
    public static void postPacketProcess(boolean isClient, Packet packet) throws Throwable {
        if (!isClient) return;

        WorldClient oldWorld = clientWorldBackup;
        if (oldWorld != null) {
            getMinecraft().theWorld = oldWorld;
            ClientProxy.netHandlerClientWorldSet.invokeExact(getMinecraft().getNetHandler(), oldWorld);
            clientWorldBackup = null;
        }
    }

    // todo serversafe!


}

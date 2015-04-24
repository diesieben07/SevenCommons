package de.take_weiland.mods.commons.netx;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.Packets;
import de.take_weiland.mods.commons.net.SimplePacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static de.take_weiland.mods.commons.netx.NicePacketSupport.clientboundPacket;
import static de.take_weiland.mods.commons.netx.NicePacketSupport.serverboundPacket;

/**
 * @author diesieben07
 */
public interface Packet extends SimplePacket {

    void writeTo(ByteBuf buf);

    default int expectedSize() {
        return Network.DEFAULT_EXPECTED_SIZE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Receiver {

        Side value();

    }

    @Override
    default Packet sendTo(EntityPlayer player) {

        return this;
    }

    @Override
    default Packet sendToServer() {
        Packets.sendToServer(serverboundPacket(this));
        return this;
    }

    @Override
    default SimplePacket sendTo(Iterable<? extends EntityPlayer> players) {
        Packets.sendTo(clientboundPacket(this), players);
        return this;
    }

    @Override
    default SimplePacket sendToAll() {
        Packets.sendToAll(clientboundPacket(this));
        return this;
    }

    @Override
    default SimplePacket sendToAllIn(World world) {
        Packets.sendToAllIn(clientboundPacket(this), world);
        return this;
    }

    @Override
    default SimplePacket sendToAllNear(World world, double x, double y, double z, double radius) {
        Packets.sendToAllNear(clientboundPacket(this), world, x, y, z, radius);
        return this;
    }

    @Override
    default SimplePacket sendToAllNear(Entity entity, double radius) {
        Packets.sendToAllNear(clientboundPacket(this), entity, radius);
        return this;
    }

    @Override
    default SimplePacket sendToAllNear(TileEntity te, double radius) {
        Packets.sendToAllNear(clientboundPacket(this), te, radius);
        return this;
    }

    @Override
    default SimplePacket sendToAllTracking(Entity entity) {
        Packets.sendToAllTracking(clientboundPacket(this), entity);
        return this;
    }

    @Override
    default SimplePacket sendToAllAssociated(Entity entity) {
        Packets.sendToAllAssociated(clientboundPacket(this), entity);
        return this;
    }

    @Override
    default SimplePacket sendToAllTracking(TileEntity te) {
        Packets.sendToAllTracking(clientboundPacket(this), te);
        return this;
    }

    @Override
    default SimplePacket sendToAllTrackingChunk(World world, int chunkX, int chunkZ) {
        Packets.sendToAllTrackingChunk(clientboundPacket(this), world, chunkX, chunkZ);
        return this;
    }

    @Override
    default SimplePacket sendToAllTracking(Chunk chunk) {
        Packets.sendToAllTracking(clientboundPacket(this), chunk);
        return this;
    }

    @Override
    default SimplePacket sendToViewing(Container c) {
        Packets.sendToViewing(clientboundPacket(this), c);
        return this;
    }
}

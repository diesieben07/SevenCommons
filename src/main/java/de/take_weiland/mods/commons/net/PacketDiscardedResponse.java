package de.take_weiland.mods.commons.net;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * @author diesieben07
 */
final class PacketDiscardedResponse implements SimplePacket {

    private final SimplePacket.WithResponse<?> packet;

    PacketDiscardedResponse(SimplePacket.WithResponse<?> packet) {
        this.packet = packet;
    }

    @Override
    public void sendTo(NetworkManager manager) {
        packet.sendTo(manager);
    }

    @Override
    public void sendToServer() {
        packet.sendToServer();
    }

    @Override
    public void sendTo(EntityPlayerMP player) {
        packet.sendTo(player);
    }

    @Override
    public void sendTo(EntityPlayer player) {
        packet.sendTo(player);
    }

    @Override
    public void sendTo(EntityPlayer... players) {
        packet.sendTo(players);
    }

    @Override
    public void sendTo(Iterable<? extends EntityPlayer> players) {
        packet.sendTo(players);
    }

    @Override
    public void sendTo(Iterable<? extends EntityPlayer> players, Predicate<? super EntityPlayerMP> filter) {
        packet.sendTo(players, filter);
    }

    @Override
    public void sendTo(Iterator<? extends EntityPlayer> players) {
        packet.sendTo(players);
    }

    @Override
    public void sendTo(Iterator<? extends EntityPlayer> players, Predicate<? super EntityPlayerMP> filter) {
        packet.sendTo(players, filter);
    }

    @Override
    public void sendTo(World world, Predicate<? super EntityPlayer> filter) {
        packet.sendTo(world, filter);
    }

    @Override
    public void sendTo(Predicate<? super EntityPlayerMP> filter) {
        packet.sendTo(filter);
    }

    @Override
    public void sendToAll() {
        packet.sendToAll();
    }

    @Override
    public void sendToAllIn(World world) {
        packet.sendToAllIn(world);
    }

    @Override
    public void sendToAllNear(World world, double x, double y, double z, double radius) {
        packet.sendToAllNear(world, x, y, z, radius);
    }

    @Override
    public void sendToAllNear(World world, BlockPos pos, double radius) {
        packet.sendToAllNear(world, pos, radius);
    }

    @Override
    public void sendToAllNear(Entity entity, double radius) {
        packet.sendToAllNear(entity, radius);
    }

    @Override
    public void sendToAllNear(TileEntity te, double radius) {
        packet.sendToAllNear(te, radius);
    }

    @Override
    public void sendToAllTracking(Entity entity) {
        packet.sendToAllTracking(entity);
    }

    @Override
    public void sendToAllAssociated(Entity entity) {
        packet.sendToAllAssociated(entity);
    }

    @Override
    public void sendToAllTracking(TileEntity te) {
        packet.sendToAllTracking(te);
    }

    @Override
    public void sendToAllTracking(Chunk chunk) {
        packet.sendToAllTracking(chunk);
    }

    @Override
    public void sendToAllTrackingChunk(World world, int chunkX, int chunkZ) {
        packet.sendToAllTrackingChunk(world, chunkX, chunkZ);
    }

    @Override
    public void sendToViewing(Container c) {
        packet.sendToViewing(c);
    }
}

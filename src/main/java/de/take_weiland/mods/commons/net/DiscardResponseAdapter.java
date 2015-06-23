package de.take_weiland.mods.commons.net;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * @author diesieben07
 */
final class DiscardResponseAdapter implements SimplePacket {

    private final SimplePacket.WithResponse<?> original;

    DiscardResponseAdapter(SimplePacket.WithResponse<?> original) {
        this.original = original;
    }

    @Override
    public void sendToServer() {
        original.sendToServer();
    }

    @Override
    public void sendTo(EntityPlayerMP player) {
        original.sendTo(player);
    }

    @Override
    public void sendTo(Iterator<? extends EntityPlayer> it, Predicate<? super EntityPlayerMP> filter) {
        original.sendTo(it, filter);
    }

    @Override
    public void sendTo(EntityPlayer player) {
        original.sendTo(player);
    }

    @Override
    public void sendTo(EntityPlayer... players) {
        original.sendTo(players);
    }

    @Override
    public void sendTo(Iterable<? extends EntityPlayer> players) {
        original.sendTo(players);
    }

    @Override
    public void sendTo(Iterable<? extends EntityPlayer> players, Predicate<? super EntityPlayerMP> filter) {
        original.sendTo(players, filter);
    }

    @Override
    public void sendTo(Iterator<? extends EntityPlayer> it) {
        original.sendTo(it);
    }

    @Override
    public void sendTo(World world, Predicate<? super EntityPlayer> filter) {
        original.sendTo(world, filter);
    }

    @Override
    public void sendTo(Predicate<? super EntityPlayerMP> filter) {
        original.sendTo(filter);
    }

    @Override
    public void sendToAll() {
        original.sendToAll();
    }

    @Override
    public void sendToAllIn(World world) {
        original.sendToAllIn(world);
    }

    @Override
    public void sendToAllNear(World world, double x, double y, double z, double radius) {
        original.sendToAllNear(world, x, y, z, radius);
    }

    @Override
    public void sendToAllNear(Entity entity, double radius) {
        original.sendToAllNear(entity, radius);
    }

    @Override
    public void sendToAllNear(TileEntity te, double radius) {
        original.sendToAllNear(te, radius);
    }

    @Override
    public void sendToAllTracking(Entity entity) {
        original.sendToAllTracking(entity);
    }

    @Override
    public void sendToAllAssociated(Entity entity) {
        original.sendToAllAssociated(entity);
    }

    @Override
    public void sendToAllTracking(TileEntity te) {
        original.sendToAllTracking(te);
    }

    @Override
    public void sendToAllTracking(Chunk chunk) {
        original.sendToAllTracking(chunk);
    }

    @Override
    public void sendToAllTrackingChunk(World world, int chunkX, int chunkZ) {
        original.sendToAllTrackingChunk(world, chunkX, chunkZ);
    }

    @Override
    public void sendToViewing(Container c) {
        original.sendToViewing(c);
    }
}

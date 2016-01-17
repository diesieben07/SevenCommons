package de.take_weiland.mods.commons.internal.client;

import de.take_weiland.mods.commons.internal.SevenCommonsProxy;
import de.take_weiland.mods.commons.internal.client.worldview.WorldViewImpl;
import de.take_weiland.mods.commons.internal.worldview.PacketBlockChange;
import de.take_weiland.mods.commons.internal.worldview.PacketChunkData;
import de.take_weiland.mods.commons.internal.worldview.PacketChunkUnload;
import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;

import static net.minecraft.client.Minecraft.getMinecraft;

public final class ClientProxy implements SevenCommonsProxy {

    @Override
    public void sendPacketToServer(Packet p) {
        getMinecraft().getNetHandler().addToSendQueue(p);
    }

    @Override
    public EntityPlayer getClientPlayer() {
        return getMinecraft().thePlayer;
    }

    @Override
    public NetworkManager getClientNetworkManager() {
        return getMinecraft().getNetHandler().getNetworkManager();
    }

    @Override
    public Packet makeC17Packet(String channel, byte[] data) {
        return new C17PacketCustomPayload(channel, data);
    }

    @Override
    public void handleChunkDataPacket(PacketChunkData packet, EntityPlayer player) {
        WorldClient world = WorldViewImpl.getOrCreateWorld(packet.dimension);
        if (packet.initChunk) {
            if (packet.lsbYLevels == 0) {
                world.doPreChunk(packet.x, packet.z, false);
                return;
            }

            world.doPreChunk(packet.x, packet.z, true);
        }

        int blockX = packet.x << 4;
        int blockZ = packet.z << 4;

        world.invalidateBlockReceiveRegion(blockX, 0, blockZ, blockX + 15, 256, blockZ + 15);
        Chunk chunk = world.getChunkFromChunkCoords(packet.x, packet.z);
        chunk.fillChunk(packet.data, packet.lsbYLevels, packet.msbYLevels, packet.initChunk);
        world.markBlockRangeForRenderUpdate(blockX, 0, blockZ, blockX + 15, 256, blockZ + 15);

        if (!packet.initChunk || !(world.provider instanceof WorldProviderSurface)) {
            chunk.resetRelightChecks();
        }
    }

    @Override
    public void handleChunkUnloadPacket(PacketChunkUnload packet, EntityPlayer player) {
        WorldViewImpl.getOrCreateWorld(packet.dimension).doPreChunk(packet.chunkX, packet.chunkZ, false);
    }

    @Override
    public void handleBlockChangePacket(PacketBlockChange packet, EntityPlayer player) {
        WorldViewImpl.getOrCreateWorld(packet.dimension).setBlock(packet.x, packet.y, packet.z, Block.getBlockById(packet.data >>> 4), packet.data & 0xF, 3);
    }
}

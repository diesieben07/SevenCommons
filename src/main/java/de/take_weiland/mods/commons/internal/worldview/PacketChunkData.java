package de.take_weiland.mods.commons.internal.worldview;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/**
 * @author diesieben07
 */
public class PacketChunkData implements Packet {

    public final int dimension;
    public final int x, z;
    public final byte[]  data;
    public final boolean initChunk;
    public final int     lsbYLevels;
    public final int     msbYLevels;

    private PacketChunkData(int dimension, int x, int z, byte[] data, boolean initChunk, int lsbYLevels, int msbYLevels) {
        this.dimension = dimension;
        this.x = x;
        this.z = z;
        this.data = data;
        this.initChunk = initChunk;
        this.lsbYLevels = lsbYLevels;
        this.msbYLevels = msbYLevels;
    }

    public PacketChunkData(MCDataInput in) {
        initChunk = in.readBoolean();
        dimension = in.readInt();
        x = in.readInt();
        z = in.readInt();
        lsbYLevels = in.readUnsignedShort();
        msbYLevels = in.readUnsignedShort();

        data = in.readBytes();
    }

    @Override
    public void writeTo(MCDataOutput out) throws Exception {
        out.writeBoolean(initChunk);
        out.writeInt(dimension);
        out.writeInt(x);
        out.writeInt(z);
        out.writeShort(lsbYLevels);
        out.writeShort(msbYLevels);
        out.writeBytes(data);
    }

    private static byte[] cache = new byte[196864];

    public static PacketChunkData sendWholeChunk(Chunk chunk) {
        return create(chunk, true, 0xFFFF);
    }

    public static PacketChunkData sendChunkLayers(Chunk chunk, int yLayers) {
        return create(chunk, false, yLayers);
    }

    private static PacketChunkData create(Chunk chunk, boolean initial, int yLayers) {
        int pos = 0;
        ExtendedBlockStorage[] blockStorage = chunk.getBlockStorageArray();
        int msbLevelCount = 0;

        int lsbYLevels = 0;
        int msbYLevels = 0;

        byte[] bytes = cache;

        if (initial) {
            chunk.sendUpdates = true;
        }

        // format is:
        // blockIDLsb - metadata - blocklight - skylight - blockIDMsb - biomes

        int yLevel;
        for (yLevel = 0; yLevel < blockStorage.length; ++yLevel) {
            if (blockStorage[yLevel] != null && (!initial || !blockStorage[yLevel].isEmpty()) && (yLayers & 1 << yLevel) != 0) {
                lsbYLevels |= 1 << yLevel;

                if (blockStorage[yLevel].getBlockMSBArray() != null) {
                    msbYLevels |= 1 << yLevel;
                    ++msbLevelCount;
                }
            }
        }

        for (yLevel = 0; yLevel < blockStorage.length; ++yLevel) {
            if (blockStorage[yLevel] != null && (!initial || !blockStorage[yLevel].isEmpty()) && (yLayers & 1 << yLevel) != 0) {
                byte[] lsbArray = blockStorage[yLevel].getBlockLSBArray();
                System.arraycopy(lsbArray, 0, bytes, pos, lsbArray.length);
                pos += lsbArray.length;
            }
        }

        for (yLevel = 0; yLevel < blockStorage.length; ++yLevel) {
            if (blockStorage[yLevel] != null && (!initial || !blockStorage[yLevel].isEmpty()) && (yLayers & 1 << yLevel) != 0) {
                NibbleArray metadataArray = blockStorage[yLevel].getMetadataArray();
                System.arraycopy(metadataArray.data, 0, bytes, pos, metadataArray.data.length);
                pos += metadataArray.data.length;
            }
        }

        for (yLevel = 0; yLevel < blockStorage.length; ++yLevel) {
            if (blockStorage[yLevel] != null && (!initial || !blockStorage[yLevel].isEmpty()) && (yLayers & 1 << yLevel) != 0) {
                NibbleArray blocklightArray = blockStorage[yLevel].getBlocklightArray();
                System.arraycopy(blocklightArray.data, 0, bytes, pos, blocklightArray.data.length);
                pos += blocklightArray.data.length;
            }
        }

        if (!chunk.worldObj.provider.hasNoSky) {
            for (yLevel = 0; yLevel < blockStorage.length; ++yLevel) {
                if (blockStorage[yLevel] != null && (!initial || !blockStorage[yLevel].isEmpty()) && (yLayers & 1 << yLevel) != 0) {
                    NibbleArray skylightArray = blockStorage[yLevel].getSkylightArray();
                    System.arraycopy(skylightArray.data, 0, bytes, pos, skylightArray.data.length);
                    pos += skylightArray.data.length;
                }
            }
        }

        if (msbLevelCount > 0) {
            for (yLevel = 0; yLevel < blockStorage.length; ++yLevel) {
                if (blockStorage[yLevel] != null && (!initial || !blockStorage[yLevel].isEmpty()) && blockStorage[yLevel].getBlockMSBArray() != null && (yLayers & 1 << yLevel) != 0) {
                    NibbleArray msbArray = blockStorage[yLevel].getBlockMSBArray();
                    System.arraycopy(msbArray.data, 0, bytes, pos, msbArray.data.length);
                    pos += msbArray.data.length;
                }
            }
        }

        if (initial) {
            byte[] biomeArray = chunk.getBiomeArray();
            System.arraycopy(biomeArray, 0, bytes, pos, biomeArray.length);
            pos += biomeArray.length;
        }

        byte[] data = new byte[pos];
        System.arraycopy(bytes, 0, data, 0, pos);
        return new PacketChunkData(chunk.worldObj.provider.dimensionId, chunk.xPosition, chunk.zPosition, data, initial, lsbYLevels, msbYLevels);
    }

}

package de.take_weiland.mods.commons.net;

import com.google.common.io.ByteArrayDataInput;
import io.netty.buffer.ByteBuf;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

/**
 * <p>An extension to the DataInput interface which adds methods useful in the Minecraft environment.</p>
 * <p>This interface is intended to read from a memory-based buffer and thus does not throw IOExceptions.</p>
 * <p>This interface is mostly intended for network communication purposes and <i>not</i> for reading data from disk.
 * Additionally this interface specifies <b>Little Endian</b> byte order, violating the contract of the DataInput interface,
 * to offer better performance on most systems.</p>
 * <p>Instances of this interfaces can be obtained using {@link Network#newInput(byte[])}.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public interface MCDataInput extends ByteArrayDataInput {

    /**
     * <p>The total number of bytes in this stream.</p>
     *
     * @return the number of bytes
     */
    int size();

    /**
     * <p>The current position within the stream.</p>
     *
     * @return the position
     */
    int position();

    /**
     * <p>Set the current position within the stream.</p>
     *
     * @param pos the new position
     */
    void position(int pos);

    /**
     * <p>The remaining number of bytes to be read, i.e. {@code size() - position()}.</p>
     *
     * @return the number of bytes remaining
     */
    int remaining();

    /**
     * <p>Get an {@code InputStream} view of this stream. The created stream reads through to this stream.</p>
     *
     * @return an InputStream
     */
    InputStream asInputStream();

    /**
     * <p>Get a {@code ReadableByteChannel} view of this stream. The created channel reads through to this stream.</p>
     *
     * @return a ReadableByteChannel
     */
    ScatteringByteChannel asByteChannel();

    /**
     * <p>Get a {@code ByteBuf} view of this stream. The buffer's readerIndex is linked to this stream's position.
     * Data written to the returned buffer will be appended to the data to be read by this stream.</p>
     *
     * @return a ByteBuf
     */
    ByteBuf asByteBuf();

    /**
     * <p>Copy the given number of bytes from this stream into the given OutputStream.</p>
     *
     * @param out   the OutputStream
     * @param bytes the number of bytes to copy
     */
    void readBytes(OutputStream out, int bytes) throws IOException;

    /**
     * <p>Copy the given number of bytes from this stream into the given DataOutput.</p>
     *
     * @param out   the DataOutput
     * @param bytes the number of bytes to copy
     */
    void readBytes(DataOutput out, int bytes) throws IOException;

    /**
     * <p>Copy the given number of bytes from this stream into the given channel.</p>
     *
     * @param out   the channel
     * @param bytes the number of bytes to copy
     */
    void readBytes(WritableByteChannel out, int bytes) throws IOException;

    /**
     * <p>Copy as many bytes as the given buffer has available from this stream into the given buffer.</p>
     *
     * @param buf the buffer
     */
    void readBytes(ByteBuffer buf);

    /**
     * <p>Copy as many bytes as the given buffer has writable bytes from this stream into the given buffer.</p>
     *
     * @param buf the buffer
     */
    void readBytes(ByteBuf buf);

    /**
     * <p>Copy the given number of bytes from this stream into the given buffer.</p>
     *
     * @param buf   the buffer
     * @param bytes the number of bytes to copy
     */
    void readBytes(ByteBuf buf, int bytes);

    int readMedium();

    int readUnsignedMedium();

    long readUnsignedInt();

    int readVarInt();

    int readShiftedVarInt();

    long readVarLong();

    long readShiftedVarLong();

    /**
     * <p>Read a String from the stream.</p>
     *
     * @return a String or null
     */
    String readString();

    /**
     * <p>Read a UUID from the stream.</p>
     *
     * @return a UUID or null
     */
    UUID readUUID();

    /**
     * <p>Read an Enum from the stream.</p>
     *
     * @param clazz the enum type
     * @return an Enum or null
     */
    <E extends Enum<E>> E readEnum(Class<E> clazz);

    /**
     * <p>Read a BitSet from the stream.</p>
     *
     * @return a BitSet or null
     */
    BitSet readBitSet();

    /**
     * <p>Read an EnumSet from the stream.</p>
     *
     * @param enumClass the enum type
     * @return an EnumSet or null
     */
    <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> enumClass);

    /**
     * <p>Read an Item reference from the stream.</p>
     *
     * @return an Item or null
     * @throws java.lang.IllegalStateException if an invalid ID is read
     */
    Item readItem();

    /**
     * <p>Read a Block reference from the stream.</p>
     *
     * @return a Block or null
     * @throws java.lang.IllegalStateException if an invalid ID is read
     */
    Block readBlock();

    /**
     * <p>Read a registry entry from the stream.</p>
     *
     * @param registry the registry
     * @return a registry entry or null
     */
    <T extends IForgeRegistryEntry<T>> T readRegistryEntry(IForgeRegistry<T> registry);

    /**
     * <p>Read an ItemStack from the stream.</p>
     *
     * @return an ItemStack or null
     */
    ItemStack readItemStack();

    /**
     * <p>Read a FluidStack from the stream.</p>
     *
     * @return a FluidStack or null
     */
    FluidStack readFluidStack();

    /**
     * <p>Read an NBTTagCompound from the stream.</p>
     *
     * @return an NBTTagCompound or null
     */
    NBTTagCompound readNBT();

    /**
     * <p>Read a block state from the stream.</p>
     *
     * @return a block state or null
     */
    IBlockState readBlockState();

    /**
     * <p>Read a block state from the stream. This method actually serializes all properties that are set, so properties
     * which are not stored in metadata are preserved.</p>
     *
     * @return a block state or null
     */
    IBlockState readRichBlockState();

    /**
     * <p>Read a block position from the stream.</p>
     *
     * @return a BlockPos or null
     */
    BlockPos readBlockPos();

    /**
     * <p>Read a chunk position from the buffer.</p>
     *
     * @return a ChunkPos or null
     */
    ChunkPos readChunkPos();

    /**
     * <p>Read a nullable boxed Boolean from the buffer.</p>
     *
     * @return a Boolean or null
     */
    Boolean readNullableBoolean();

    /**
     * <p>Read a nullable boxed Byte from the buffer.</p>
     *
     * @return a Byte or null
     */
    Byte readNullableByte();

    /**
     * <p>Read a nullable boxed Short from the buffer.</p>
     *
     * @return a Short or null
     */
    Short readNullableShort();

    /**
     * <p>Read a nullable boxed Character from the buffer.</p>
     *
     * @return a Character or null
     */
    Character readNullableChar();

    /**
     * <p>Read a nullable boxed Integer from the buffer.</p>
     *
     * @return a Integer or null
     */
    Integer readNullableInt();

    /**
     * <p>Read a nullable boxed Long from the buffer.</p>
     *
     * @return a Long or null
     */
    Long readNullableLong();

    /**
     * <p>Read a nullable boxed Float from the buffer.</p>
     *
     * @return a Float or null
     */
    Float readNullableFloat();

    /**
     * <p>Read a nullable boxed Double from the buffer.</p>
     *
     * @return a Double or null
     */
    Double readNullableDouble();

    @Override
    @Deprecated
    String readLine();
}

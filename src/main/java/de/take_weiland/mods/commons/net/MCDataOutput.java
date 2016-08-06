package de.take_weiland.mods.commons.net;

import com.google.common.io.ByteArrayDataOutput;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

/**
 * <p>An extension to the DataOutput interface which adds methods useful in the Minecraft environment.</p>
 * <p>This interface is intended to write to a memory-based buffer and thus does not throw IOExceptions.</p>
 * <p>This interface is mostly intended for network communication purposes and <i>not</i> for saving data to disk.
 * Additionally this interface specifies <b>Little Endian</b> byte order, violating the contract of the DataOutput interface,
 * to offer better performance on most systems.</p>
 * <p>Instances of this interface can be obtained using {@link Network#newOutput()}.</p>
 *
 * @author diesieben07
 */
@ParametersAreNullableByDefault
public interface MCDataOutput extends ByteArrayDataOutput {

    /**
     * <p>The number of bytes written to this stream to far.</p>
     *
     * @return the number of bytes
     */
    int length();

    /**
     * <p>Get an {@code OutputStream} view of this stream. The created stream writes through to this stream.</p>
     *
     * @return an OutputStream
     */
    OutputStream asOutputStream();

    /**
     * <p>Get a {@code WritableByteChannel} view of this stream. The created channel writes through to this stream.</p>
     *
     * @return a WritableByteChannel
     */
    GatheringByteChannel asByteChannel();

    /**
     * <p>Get a {@code ByteBuf} view of this stream. The writerIndex of the buffer is linked to this stream's position.
     * Reading from the buffer will read the data written to this stream so far.</p>
     *
     * @return a ByteBuf
     */
    ByteBuf asByteBuf();

    /**
     * <p>Copy as many bytes as are available from the given InputStream into this stream.</p>
     *
     * @param in the InputStream
     * @return the number of bytes copied
     */
    int writeBytes(InputStream in) throws IOException;

    /**
     * <p>Copy at most the given number of bytes from the given InputStream into this stream.</p>
     *
     * @param in    the InputStream
     * @param bytes the maximum number of bytes to copy
     * @return the number of bytes copied
     */
    int writeBytes(InputStream in, int bytes) throws IOException;

    /**
     * <p>Copy as many bytes as are available from the given channel into this stream.</p>
     *
     * @param in the channel
     * @return the number of bytes copied
     */
    int writeBytes(ReadableByteChannel in) throws IOException;

    /**
     * <p>Copy at most the given number of bytes from the given channel into this stream.</p>
     * <p>The number of bytes actually transferred depends on the provided channel, as specified by {@link ReadableByteChannel#read(ByteBuffer)}.</p>
     *
     * @param in    the channel
     * @param bytes the maximum number of bytes to copy
     * @return the number of bytes copied
     */
    int writeBytes(ReadableByteChannel in, int bytes) throws IOException;

    /**
     * <p>Copy exactly the given number of bytes from the DataInput into this buffer.</p>
     *
     * @param in    the DataInput
     * @param bytes the number of bytes to copy
     */
    void writeBytes(DataInput in, int bytes) throws IOException;

    /**
     * <p>Copy all bytes from the given buffer into this stream.</p>
     * <p>After this operation the buffer's position will be equal to it's limit.</p>
     *
     * @param buf the buffer
     */
    void writeBytes(ByteBuffer buf);

    /**
     * <p>Copy all readable bytes from the given buffer into this stream.</p>
     * <p>After this operation the buffer's readerIndex will be equal to it's writerIndex.</p>
     *
     * @param buf the buffer
     */
    void writeBytes(ByteBuf buf);

    /**
     * <p>Copy the given number of bytes from the given buffer starting at it's readerIndex into this stream.</p>
     * <p>After this operation the buffer's readerIndex will be increased by {@code len}.</p>
     *
     * @param buf the buffer
     * @param len the number of bytes to copy
     * @throws IndexOutOfBoundsException if the buffer does not have at least {@code len} readable bytes
     */
    void writeBytes(ByteBuf buf, int len);

    /**
     * <p>Write a 24-bit integer to the stream.</p>
     * <p>The minimum value is {@code ‭-‭8388608‬} ({@code 0xFF 80 00 00}), the maximum value is {@code ‭8388607‬}
     * ({@code 0x00 7F FF FF}).</p>
     *
     * @param i the int to write
     */
    void writeMedium(int i);

    /**
     * <p>Write a VarInt to the stream.</p>
     * <p>A VarInt is useful if the full range of integers needs to be supported but low numbers are much more common.
     * In that case it will use less bandwidth than a regular int.</p>
     *
     * @param i the int to write
     */
    void writeVarInt(int i);

    /**
     * <p>Write a VarInt to the stream but add 1 to it before writing.</p>
     * <p>This is useful if the values are usually low but can also include -1.</p>
     *
     * @param i the int to write
     */
    void writeShiftedVarInt(int i);

    /**
     * <p>Write a VarLong to the stream.</p>
     * <p>Like a VarInt but for longs.</p>
     *
     * @param l the long to write
     */
    void writeVarLong(long l);

    /**
     * <p>Write a VarLong to the stream but add 1 to it before writing.</p>
     *
     * @param i the long to write
     */
    void writeShiftedVarLong(long l);

    /**
     * <p>Write the given String to the stream.</p>
     *
     * @param s the String to write, may be null
     */
    void writeString(@Nullable String s);

    /**
     * <p>Write the given UUID to the stream.</p>
     *
     * @param uuid the UUID to write, may be null
     */
    void writeUUID(@Nullable UUID uuid);

    /**
     * <p>Write the given Enum to the stream.</p>
     * <p>This method supports enum types with at most 255 constants. For bigger enum types {@link #writeBigEnum(Enum)} needs to be used.</p>
     *
     * @param e the Enum to write, may be null
     */
    <E extends Enum<E>> void writeEnum(@Nullable E e);

    /**
     * <p>Write the given BitSet to the stream.</p>
     *
     * @param bitSet the BitSet to write, may be null
     */
    void writeBitSet(@Nullable BitSet bitSet);

    /**
     * <p>Write the given EnumSet to the stream.</p>
     *
     * @param enumSet the EnumSet to write, may be null
     */
    <E extends Enum<E>> void writeEnumSet(@Nullable EnumSet<E> enumSet);

    /**
     * <p>Writes an Item reference to the stream.</p>
     *
     * @param item the Item to write, may be null
     */
    void writeItem(@Nullable Item item);

    /**
     * <p>Writes a Block reference to the stream.</p>
     *
     * @param block the Block, may be null
     */
    void writeBlock(@Nullable Block block);

    /**
     * <p>Write a registry entry to the stream.</p>
     *
     * @param entry the entry, may be null
     */
    <T extends IForgeRegistryEntry<T>> void writeRegistryEntry(@Nullable T entry);

    /**
     * <p>Write the given ItemStack to the stream.</p>
     *
     * @param stack the ItemStack to write, may be null
     */
    void writeItemStack(@Nullable ItemStack stack);

    /**
     * <p>Write the given FluidStack to the stream.</p>
     *
     * @param stack the FluidStack to write, may be null
     */
    void writeFluidStack(FluidStack stack);

    /**
     * <p>Write the given NBTTagCompound to the stream.</p>
     *
     * @param nbt the NBTTagCompound to write, may be null
     */
    void writeNBT(NBTTagCompound nbt);

    /**
     * <p>Write a block state to the stream. This method uses {@link Block#getMetaFromState(IBlockState)} to serialize the state.</p>
     *
     * @param state the block state
     */
    void writeBlockState(IBlockState state);

    /**
     * <p>Write a block state to the stream. This method supports states which are not fully serialized into metadata.
     * However this method does not serialize unlisted properties.</p>
     *
     * @param state the block state
     */
    void writeRichBlockState(IBlockState state);

    /**
     * <p>Writes the coordinates to the stream.</p>
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    void writeBlockPos(int x, int y, int z);

    /**
     * <p>Writes the coordinates to the stream.</p>
     *
     * @param pos the block position
     */
    void writeBlockPos(BlockPos pos);

    /**
     * <p>Writes the chunk coordinates to the stream.</p>
     *
     * @param chunkX the x coordinate of the chunk
     * @param chunkZ the z coordinate of the chunk
     */
    void writeChunkPos(int chunkX, int chunkZ);

    /**
     * <p>Writes the coordinates to the stream.</p>
     *
     * @param pos the chunk coordinates
     */
    void writeChunkPos(ChunkPos pos);

    /**
     * <p>Write the given Boolean to the stream. The Boolean is encoded as a single byte. The value {@code false} is
     * encoded as {@code 0}, {@code true} as {@code 1} and {@code null} as {@code -1}.</p>
     *
     * @param b the Boolean
     */
    void writeNullableBoolean(Boolean b);

    /**
     * <p>Write the given Byte to the stream. If the value is {@code null}, a single byte {@code 0} is written.
     * Otherwise a byte {@code 1} is written followed by the given Byte.</p>
     *
     * @param b the the Byte
     */
    void writeNullableByte(Byte b);

    /**
     * <p>Write the given Short to the stream. If the value is {@code null}, a single byte {@code 0} is written.
     * Otherwise a byte {@code 1} is written followed by the given Short.</p>
     *
     * @param s the Short
     */
    void writeNullableShort(Short s);

    /**
     * <p>Write the given Character to the stream. If the value is {@code null}, a single byte {@code 0} is written.
     * Otherwise a byte {@code 1} is written followed by the given Character.</p>
     *
     * @param c the Character
     */
    void writeNullableChar(Character c);

    /**
     * <p>Write the given Integer to the stream. If the value is {@code null}, a single byte {@code 0} is written.
     * Otherwise a byte {@code 1} is written followed by the given Integer.</p>
     *
     * @param i the Integer
     */
    void writeNullableInt(Integer i);

    /**
     * <p>Write the given Long to the stream. If the value is {@code null}, a single byte {@code 0} is written.
     * Otherwise a byte {@code 1} is written followed by the given Long.</p>
     *
     * @param l the Long
     */
    void writeNullableLong(Long l);

    /**
     * <p>Write the given Float to the stream. If the value is {@code null}, a single byte {@code 0} is written.
     * Otherwise a byte {@code 1} is written followed by the given Float.</p>
     *
     * @param f the Float
     */
    void writeNullableFloat(Float f);

    /**
     * <p>Write the given Double to the stream. If the value is {@code null}, a single byte {@code 0} is written.
     * Otherwise a byte {@code 1} is written followed by the given Double.</p>
     *
     * @param d the Double
     */
    void writeNullableDouble(Double d);

    /**
     * <p>Copy the contents of this stream to the given OutputStream as a series of bytes.</p>
     *
     * @param stream the OutputStream to write to
     */
    void copyInto(OutputStream stream) throws IOException;

    /**
     * <p>Copy the contents of this stream to the given DataOutput as a series of bytes.</p>
     *
     * @param out the DataOutput to write to
     */
    void copyInto(DataOutput out) throws IOException;

    /**
     * <p>Copy the contents of this stream to the given ByteBuffer.</p>
     * <p>The buffer must have at least {@link #length()} bytes {@linkplain ByteBuffer#remaining() remaining}.</p>
     *
     * @param buf the buffer to write to
     */
    void copyInto(ByteBuffer buf);

    /**
     * <p>Write the contents of this stream to the given ByteChannel.</p>
     *
     * @param channel the channel to write to
     */
    void copyInto(WritableByteChannel channel) throws IOException;

}

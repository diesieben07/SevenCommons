package de.take_weiland.mods.commons.net;

import com.google.common.io.ByteArrayDataInput;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

/**
 * <p>An extension to the DataInput interface which adds methods useful in the Minecraft environment.</p>
 * <p>This interface is intended to read from a memory-based buffer and thus does not throw IOExceptions.</p>
 * <p>This interface is mostly intended for network communication purposes and <i>not</i> for reading data from disk.
 * Additionally this interface specifies <b>Little Endian</b> byte order, violating the contract of the DataInput interface,
 * to offer better performance on most systems.</p>
 * <p>Instances of this interfaces can be obtained using {@link Network#newDataInput(byte[])}.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public interface MCDataInput extends ByteArrayDataInput {

    /**
     * <p>Set the byte position pointer of this stream.</p>
     * <p>Valid positions reach from 0 through {@link #len()}. A position equal to {@code len()} defines that no further
     * bytes can be read from this stream.</p>
     *
     * @param pos the new position
     * @throws java.lang.IllegalArgumentException  if the argument is negative
     * @throws java.lang.IndexOutOfBoundsException if the argument is not a valid position
     * @see #skipBytes(int)
     */
    void seek(int pos);

    /**
     * <p>Get the current byte position pointer of this stream.</p>
     * <p>Valid positions reach from 0 through {@link #len()}. A position equal to {@code len()} defines that no further
     * bytes can be read from this stream.</p>
     *
     * @return the current position
     */
    int pos();

    /**
     * <p>Get the maximum number of bytes that can be read from this stream. This value is not affected by the current
     * position pointer.</p>
     *
     * @return the total length of this stream
     */
    int len();

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
    ReadableByteChannel asByteChannel();

    /**
     * <p>Read a VarInt from the buffer.</p>
     *
     * @return an int
     * @see MCDataOutput#writeVarInt(int)
     */
    int readVarInt();

    /**
     * <p>Read a String from the buffer.</p>
     * <p>This method first reads a VarInt specifying the length of the String. If the length is -1, null is returned.
     * Otherwise length characters are read from the stream as if by the {@link #readChar()} method.</p>
     *
     * @return a String containing all the characters read in order or null
     */
    String readString();

    /**
     * <p>Read an ItemStack from the buffer.</p>
     * <p>This method first reads a short, specifying the ItemID of the ItemStack. If the ItemID is -1, null is returned.
     * Otherwise a short and a byte are read, specifying the damage value and stack size respectively. After that the
     * NBTTagCompound associated with the ItemStack is read as if by the {@link #readNBT()} method.</p>
     *
     * @return an ItemStack or null
     */
    ItemStack readItemStack();

    /**
     * <p>Read a FluidStack from the buffer.</p>
     * <p>This method first reads a VarInt, specifying the FluidID of the FluidStack. If the FluidID is -1, null is returned.
     * Otherwise a VarInt is read, specifying the amount of the FluidStack and then the NBTTagCompound associated with
     * the FluidStack as if by the {@link #readNBT()} method.</p>
     *
     * @return a FluidStack or null
     */
    FluidStack readFluidStack();

    /**
     * <p>Read an NBTTagCompound from the buffer.</p>
     * <p>This method first reads a byte, specifying the type of the next tag ({@link net.minecraft.nbt.NBTBase#getId()}).
     * If the first ID is -1, null is returned. If the ID is 0, the method returns the NBTTagCompound. Otherwise a String
     * is read as if by the {@link #readString()} method and then tag's data is read, via the
     * {@link net.minecraft.nbt.NBTBase#load(java.io.DataInput, int)} method.</p>
     *
     * @return an NBTTagCompound or null
     */
    NBTTagCompound readNBT();

    /**
     * <p>Read an Item reference from the buffer.</p>
     * <p>This method reads a VarInt from the buffer. If the value is equal to {@code 32001}, null is returned.
     * Otherwise the Item with the given ID returned.</p>
     *
     * @return an Item
     * @throws java.lang.IllegalStateException if an invalid ID is read
     */
    Item readItem();

    /**
     * <p>Read a Block reference from the buffer.</p>
     * <p>This method reads a VarInt from the buffer. If the value is equal to {@code 4096}, null is returned.
     * Otherwise the Block with the given ID is returned.</p>
     *
     * @return a Block
     * @throws java.lang.IllegalStateException if an invalid ID is read
     */
    Block readBlock();

    /**
     * <p>Read a UUID from the buffer.</p>
     * <p>This method first reads a long, containing the UUIDs most significant bits.
     * If this long equals {@code 0xF000}, null is returned. Otherwise a second long value is read, containing the UUIDs
     * least significant bits.</p>
     *
     * @return a UUID or null
     */
    UUID readUUID();

    /**
     * <p>Read an Enum from the buffer.</p>
     * <p>This method reads a VarInt from the buffer. If the VarInt is -1, null is returned. Otherwise the Enum value with
     * the ordinal value specified by the VarInt is returned.</p>
     *
     * @param clazz the Enum class
     * @return an Enum or null
     */
    <E extends Enum<E>> E readEnum(Class<E> clazz);

    /**
     * <p>Read a BitSet from the buffer.</p>
     * <p>This method reads a long array from the buffer, as if by the {@link #readLongs()} method. If the array is null,
     * null is returned. Otherwise a BitSet is returned, created as if by the {@link java.util.BitSet#valueOf(long[])} method.</p>
     *
     * @return a BitSet or null
     */
    BitSet readBitSet();

    /**
     * <p>Read an EnumSet from the buffer.</p>
     * <p>This method reads a long value from the buffer, as if by the {@link #readLong()} method. If that long value is
     * equal to {@code 1L << 63L}, null is returned. Otherwise a new EnumSet is returned. If the bit {@code 1 << i} the
     * Enum constant with ordinal value {@code i} is present in the set.</p>
     * <p>This method only supports Enum types with at most 63 constants. If an Enm type with 64 or more constants is used,
     * this method throws an {@code IllegalArgumentException}.</p>
     *
     * @param enumClass the type of enum in the EnumSet to be read
     * @return an EnumSet or null
     */
    <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> enumClass);

    /**
     * <p>Read a set of coordinates from the buffer.</p>
     * <p>This method reads a long value. If it is equal to {@code 1 << 63}, {@code null} is returned. Otherwise
     * the {@code x}, {@code y} and {@code z} components are read as specified in {@link MCDataOutput#writeCoords(int, int, int)}.</p>
     *
     * @return a new ChunkPosition object
     */
    ChunkPosition readCoords();

    /**
     * <p>Read a set of chunk coordinates from the buffer.</p>
     * <p>This method reads a long value. If it is equal to {@code 1 << 63}, {@code null} is returned. Otherwise
     * the {@code x}, and {@code z} components are read as specified in {@link MCDataOutput#writeChunkCoords(int, int)}.</p>
     *
     * @return a new {@code ChunkCoordIntPair} object
     */
    ChunkCoordIntPair readChunkCoords();

    /**
     * <p>Read a nullable boxed Boolean from the buffer.</p>
     *
     * @return a Boolean or null
     */
    Boolean readBooleanBox();

    /**
     * <p>Read a nullable boxed Byte from the buffer.</p>
     *
     * @return a Byte or null
     */
    Byte readByteBox();

    /**
     * <p>Read a nullable boxed Short from the buffer.</p>
     *
     * @return a Short or null
     */
    Short readShortBox();

    /**
     * <p>Read a nullable boxed Character from the buffer.</p>
     *
     * @return a Character or null
     */
    Character readCharBox();

    /**
     * <p>Read a nullable boxed Integer from the buffer.</p>
     *
     * @return a Integer or null
     */
    Integer readIntBox();

    /**
     * <p>Read a nullable boxed Long from the buffer.</p>
     *
     * @return a Long or null
     */
    Long readLongBox();

    /**
     * <p>Read a nullable boxed Float from the buffer.</p>
     *
     * @return a Float or null
     */
    Float readFloatBox();

    /**
     * <p>Read a nullable boxed Double from the buffer.</p>
     *
     * @return a Double or null
     */
    Double readDoubleBox();

    /**
     * <p>Read an array of booleans from the stream.</p>
     * <p>This method first reads a VarInt {@code length} from the buffer. If the VarInt is -1, null is returned. Otherwise
     * <code>ceil(length &#47; 8)}</code> bytes are read. Every byte specifies 8 elements in the array, from least significant to
     * most significant bit. A set bit represents true, an unset bit represents false.</p>
     *
     * @return a boolean array or null
     */
    boolean[] readBooleans();

    /**
     * <p>Read an array of booleans from the stream.</p>
     * <p>This method acts similar to {@link #readBooleans()}, but uses the given buffer if possible.</p>
     *
     * @param buf an existing array to use
     * @return a boolean array or null
     */
    boolean[] readBooleans(@Nullable boolean[] buf);

    /**
     * <p>Read an array of bytes from the stream.</p>
     * <p>This method first reads a VarInt {@code length} from the buffer. If the VarInt is -1, null is returned. Otherwise
     * {@code length} bytes are read and an array containing those bytes in order is returned.</p>
     *
     * @return a byte array or null
     */
    byte[] readBytes();

    /**
     * <p>Read an array of bytes from the stream.</p>
     * <p>This method acts similar to {@link #readBytes()}, but uses the given buffer if possible.</p>
     *
     * @param buf an existing array to use
     * @return a byte array or null
     */
    byte[] readBytes(@Nullable byte[] buf);

    /**
     * <p>Read an array of shorts from the stream.</p>
     * <p>This method first reads a VarInt {@code length} from the buffer. If the VarInt is -1, null is returned. Otherwise
     * {@code length} shorts are read as if by the {@link #readShort()} method.</p>
     *
     * @return a short array or null
     */
    short[] readShorts();

    /**
     * <p>Read an array of shorts from the stream.</p>
     * <p>This method acts similar to {@link #readShorts()}, but uses the given buffer if possible.</p>
     *
     * @param buf an existing array to use
     * @return a short array or null
     */
    short[] readShorts(@Nullable short[] buf);

    /**
     * <p>Read an array of ints from the stream.</p>
     * <p>This method first reads a VarInt {@code length} from the buffer. If the VarInt is -1, null is returned. Otherwise
     * {@code length} ints are read as if by the {@link #readInt()} method.</p>
     *
     * @return an int array or null
     */
    int[] readInts();

    /**
     * <p>Read an array of ints from the stream.</p>
     * <p>This method acts similar to {@link #readInts()}, but uses the given buffer if possible.</p>
     *
     * @param buf an existing array to use
     * @return an int array or null
     */
    int[] readInts(@Nullable int[] buf);

    /**
     * <p>Read an array of longs from the stream.</p>
     * <p>This method first reads a VarInt {@code length} from the buffer. If the VarInt is -1, null is returned. Otherwise
     * {@code length} longs are read as if by the {@link #readLong()} method.</p>
     *
     * @return a long array or null
     */
    long[] readLongs();

    /**
     * <p>Read an array of longs from the stream.</p>
     * <p>This method acts similar to {@link #readLongs()}, but uses the given buffer if possible.</p>
     *
     * @param buf an existing array to use
     * @return a long array or null
     */
    long[] readLongs(@Nullable long[] buf);

    /**
     * <p>Read an array of chars from the stream.</p>
     * <p>This method first reads a VarInt {@code length} from the buffer. If the VarInt is -1, null is returned. Otherwise
     * {@code length} chars are read as if by the {@link #readChar()} method.</p>
     *
     * @return a char array or null
     */
    char[] readChars();

    /**
     * <p>Read an array of shorts from the stream.</p>
     * <p>This method acts similar to {@link #readChars()}, but uses the given buffer if possible.</p>
     *
     * @param buf an existing array to use
     * @return a char array or null
     */
    char[] readChars(@Nullable char[] buf);

    /**
     * <p>Read an array of floats from the stream.</p>
     * <p>This method first reads a VarInt {@code length} from the buffer. If the VarInt is -1, null is returned. Otherwise
     * {@code length} floats are read as if by the {@link #readFloat()} method.</p>
     *
     * @return a float array or null
     */
    float[] readFloats();

    /**
     * <p>Read an array of floats from the stream.</p>
     * <p>This method acts similar to {@link #readFloats()}, but uses the given buffer if possible.</p>
     *
     * @param buf an existing array to use
     * @return a float array or null
     */
    float[] readFloats(@Nullable float[] buf);

    /**
     * <p>Read an array of doubles from the stream.</p>
     * <p>This method first reads a VarInt {@code length} from the buffer. If the VarInt is -1, null is returned. Otherwise
     * {@code length} doubles are read as if by the {@link #readDouble()} method.</p>
     *
     * @return a double array or null
     */
    double[] readDoubles();

    /**
     * <p>Read an array of doubles from the stream.</p>
     * <p>This method acts similar to {@link #readDoubles()}, but uses the given buffer if possible.</p>
     *
     * @param buf an existing array to use
     * @return a double array or null
     */
    double[] readDoubles(@Nullable double[] buf);
}

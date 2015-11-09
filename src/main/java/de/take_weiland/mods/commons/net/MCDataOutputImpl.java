package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.SCReflector;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.util.EnumUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkPositionIndexes;

/**
 * @author diesieben07
 */
final class MCDataOutputImpl extends OutputStream implements MCDataOutput, WritableByteChannel {

    private boolean locked = false;
    private byte[] buf;
    private int count;

    MCDataOutputImpl(int initialCap) {
        checkArgument(initialCap >= 0, "negative initial size");
        buf = new byte[initialCap];
    }

    @Override
    public int length() {
        return count;
    }

    @Override
    public void lock() {
        locked = true;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public OutputStream asOutputStream() {
        return this;
    }

    @Override
    public WritableByteChannel asByteChannel() {
        return this;
    }

    @Override
    public byte[] backingArray() {
        return buf;
    }

    @Override
    public byte[] toByteArray() {
        return Arrays.copyOf(buf, count);
    }

    @Override
    public void writeTo(OutputStream stream) throws IOException {
        stream.write(buf, 0, count);
    }

    @Override
    public void writeTo(DataOutput out) throws IOException {
        out.write(buf, 0, count);
    }

    @Override
    public void writeTo(ByteBuffer buf) {
        buf.put(this.buf, 0, count);
    }

    @Override
    public void writeTo(WritableByteChannel channel) throws IOException {
        channel.write(ByteBuffer.wrap(buf, 0, count));
    }

    @Override
    public int copyFrom(InputStream in) throws IOException {
        int numRead = 0;
        int read;
        do {
            ensureWritable(Math.min(Network.DEFAULT_BUFFER_SIZE, in.available()));
            read = in.read(buf, count, buf.length - count);
            if (read == -1) {
                return numRead;
            } else {
                numRead += read;
            }
        } while (true);
    }

    @Override
    public void copyFrom(InputStream in, int bytes) throws IOException {
        int numRead = 0;
        while (numRead < bytes) {
            int avail = in.available();
            ensureWritable(avail);
            int read = in.read(buf, count, buf.length - count);
            if (read == -1) {
                throw new EOFException(String.format("Expected to find %d bytes, but only got %d", bytes, numRead));
            } else {
                numRead += read;
            }
        }
    }

    @Override
    public synchronized int write(ByteBuffer src) throws IOException {
        int toWrite = src.remaining();
        ensureWritable(toWrite);
        src.get(buf, count, toWrite);
        count += toWrite;
        return toWrite;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    final void ensureWritable(int bytesToWrite) {
        if (locked) {
            throw new IllegalStateException("Output locked!");
        }
        if ((count + bytesToWrite) - buf.length > 0) {
            grow(bytesToWrite + count);
        }
    }

    private void grow(int minCapacity) {
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if (newCapacity < 0) {
            if (minCapacity < 0) {
                throw new OutOfMemoryError();
            }
            newCapacity = Integer.MAX_VALUE;
        }
        buf = Arrays.copyOf(buf, newCapacity);
    }

    @Override
    public void writeNulls(int n) {
        ensureWritable(n);
        count += n;
    }

    @Override
    public void write(int b) {
        writeByte(b);
    }

    @Override
    public void write(@NotNull byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public void write(@NotNull byte[] b, int off, int len) {
        ensureWritable(len);
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    @Override
    public void writeBoolean(boolean v) {
        write(v ? BufferConstants.BOOLEAN_TRUE : BufferConstants.BOOLEAN_FALSE);
    }

    @Override
    public void writeBooleanBox(Boolean b) {
        writeByte(b == null ? BufferConstants.BOOLEAN_NULL : (b ? BufferConstants.BOOLEAN_TRUE : BufferConstants.BOOLEAN_FALSE));
    }

    @Override
    public void writeByte(int b) {
        ensureWritable(1);
        writeByteNBC(b);
    }

    private void writeByteNBC(int b) {
        buf[count++] = (byte) b;
    }

    @Override
    public void writeByteBox(Byte b) {
        if (b == null) {
            writeByte(BufferConstants.BOX_NULL);
        } else {
            ensureWritable(2);
            writeByteNBC(BufferConstants.BOX_NONNULL);
            writeByteNBC(b);
        }
    }

    @Override
    public void writeShort(int s) {
        ensureWritable(2);
        writeShortNBC(s);
    }

    private void writeShortNBC(int s) {
        writeByteNBC(s);
        writeByteNBC(s >>> 8);
    }

    @Override
    public void writeShortBox(Short s) {
        if (s == null) {
            writeByte(BufferConstants.BOX_NULL);
        } else {
            ensureWritable(3);
            writeByteNBC(BufferConstants.BOX_NONNULL);
            writeShortNBC(s);
        }
    }

    @Override
    public void writeChar(int c) {
        writeShort(c);
    }

    private void writeCharNBC(int c) {
        writeShortNBC(c);
    }

    @Override
    public void writeCharBox(Character c) {
        if (c == null) {
            writeByte(BufferConstants.BOX_NULL);
        } else {
            ensureWritable(3);
            writeByteNBC(BufferConstants.BOX_NONNULL);
            writeCharNBC(c);
        }
    }

    @Override
    public void writeInt(int i) {
        ensureWritable(4);
        writeIntNBC(i);
    }

    private void writeIntNBC(int i) {
        writeByteNBC(i);
        writeByteNBC(i >>> 8);
        writeByteNBC(i >>> 16);
        writeByteNBC(i >>> 24);
    }

    @Override
    public void writeIntBox(Integer i) {
        if (i == null) {
            writeByte(BufferConstants.BOX_NULL);
        } else {
            ensureWritable(5);
            writeByteNBC(BufferConstants.BOX_NONNULL);
            writeIntNBC(i);
        }
    }

    @Override
    public void writeLong(long v) {
        ensureWritable(8);
        writeLongNBC(v);
    }

    private void writeLongNBC(long v) {
        writeByteNBC((int) v);
        writeByteNBC((int) (v >>> 8));
        writeByteNBC((int) (v >>> 16));
        writeByteNBC((int) (v >>> 24));
        writeByteNBC((int) (v >>> 32));
        writeByteNBC((int) (v >>> 40));
        writeByteNBC((int) (v >>> 48));
        writeByteNBC((int) (v >>> 56));
    }

    @Override
    public void writeLongBox(Long l) {
        if (l == null) {
            writeByte(BufferConstants.BOX_NULL);
        } else {
            ensureWritable(9);
            writeByteNBC(BufferConstants.BOX_NONNULL);
            writeLongNBC(l);
        }
    }

    @Override
    public void writeFloat(float v) {
        writeInt(Float.floatToIntBits(v));
    }

    private void writeFloatNBC(float f) {
        writeIntNBC(Float.floatToIntBits(f));
    }

    @Override
    public void writeFloatBox(Float f) {
        if (f == null) {
            writeByte(BufferConstants.BOX_NULL);
        } else {
            ensureWritable(5);
            writeByteNBC(BufferConstants.BOX_NONNULL);
            writeFloatNBC(f);
        }
    }

    @Override
    public void writeDouble(double v) {
        writeLong(Double.doubleToLongBits(v));
    }

    private void writeDoubleNBC(double v) {
        writeLongNBC(Double.doubleToLongBits(v));
    }

    @Override
    public void writeDoubleBox(Double d) {
        if (d == null) {
            writeByte(BufferConstants.BOX_NULL);
        } else {
            ensureWritable(9);
            writeByteNBC(BufferConstants.BOX_NONNULL);
            writeDoubleNBC(d);
        }
    }

    @Override
    public void writeChars(@NotNull String s) {
        int len = s.length();
        ensureWritable(len << 1);
        for (int i = 0; i < len; ++i) {
            writeShortNBC(s.charAt(i));
        }
    }

    @Override
    public void writeUTF(@NotNull String s) {
        char c;
        int i;
        int utfLen = 0;
        int strLen = s.length();
        for (i = 0; i < strLen; i++) {
            c = s.charAt(i);
            if (c >= 0x0001 && c <= 0x007f) {
                utfLen++;
            } else if (c >= 0x800) {
                utfLen += 3;
            } else {
                utfLen += 2;
            }
        }
        if (utfLen > 65535) {
            throw new IllegalStateException(new UTFDataFormatException("Encoded String too long. " + utfLen + " > 65535"));
        }
        writeShort(utfLen);
        ensureWritable(utfLen);

        if (utfLen == strLen) {
            for (i = 0; i < strLen; i++) {
                writeByteNBC(s.charAt(i));
            }
        } else {
            for (i = 0; i < strLen; i++) {
                c = s.charAt(i);
                if (c >= 0x0001 && c <= 0x007f) {
                    writeByteNBC(c);
                } else if (c >= 0x800) {
                    writeByteNBC(0xE0 | ((c >> 12) & 0x0F));
                    writeByteNBC(0x80 | ((c >> 6) & 0x3F));
                    writeByteNBC(0x80 | (c & 0x3F));
                } else {
                    writeByteNBC(0xC0 | ((c >> 6) & 0x1F));
                    writeByteNBC(0x80 | (c & 0x3F));
                }
            }
        }
    }

    @Override
    @Deprecated
    public void writeBytes(@NotNull String s) {
        int len = s.length();
        ensureWritable(len);
        for (int i = 0; i < len; i++) {
            writeByteNBC(s.charAt(i));
        }
    }

    private static int varIntLen(int i) {
        return i < 0
                ? 5 // less than 0 always takes the full 32 bits (=5 bytes in VarInt)
                : positiveVarIntLen(i);
    }

    private static int positiveVarIntLen(int i) {
        // divide by 7 and round up see http://stackoverflow.com/a/7446742
        // actually ((32 - Integer.nOLZ(i)) + 6) / 7
        return Math.max(1, 38 - Integer.numberOfLeadingZeros(i) / 7);
    }

    @Override
    public void writeVarInt(int i) {
        ensureWritable(varIntLen(i));
        writeVarIntNBC(i);
    }

    @Override
    public void writeMedium(int i) {
        ensureWritable(3);
        writeByteNBC(i);
        writeByteNBC(i >>> 8);
        writeByteNBC(i >>> 16);
    }

    private void writePositiveVarInt(int i) {
        ensureWritable(positiveVarIntLen(i));
        writeVarIntNBC(i);
    }

    private void writeNegativeVarInt(int i) {
        ensureWritable(5);
        writeVarIntNBC(i);
    }

    private void writeVarIntNBC(int i) {
        while ((i & ~BufferConstants.SEVEN_BITS) != 0) {
            writeByteNBC(i & BufferConstants.SEVEN_BITS);
            i >>>= 7;
        }
        writeByteNBC(i | (BufferConstants.BYTE_MSB));
    }

    @Override
    public void writeString(String s) {
        if (s == null) {
            writeNegativeVarInt(-1);
        } else {
            int len = s.length();
            ensureWritable(positiveVarIntLen(len) + len << 1);

            writeVarIntNBC(len);
            for (int i = 0; i < len; i++) {
                writeShortNBC(s.charAt(i));
            }
        }
    }

    @Override
    public void writeItemStack(ItemStack stack) {
        if (stack == null) {
            writeShort(-1);
        } else {
            ensureWritable(6); // 2 +2 + 1 + 1 (NBT needs at least 1 byte)
            writeShortNBC(Item.getIdFromItem(stack.getItem()));
            writeShortNBC(stack.getMetadata());
            writeByteNBC(stack.stackSize);
            writeNBT(stack.stackTagCompound);
        }
    }

    @Override
    public void writeFluidStack(FluidStack stack) {
        if (stack == null) {
            writeVarInt(-1);
        } else {
            ensureWritable(varIntLen(stack.getFluidID())
                    + varIntLen(stack.amount) // technically amount is always >= 0, but we can't be sure
                    + 1 /* NBT needs at least 1 byte*/);

            writeVarIntNBC(stack.getFluidID());
            writeVarIntNBC(stack.amount);
            writeNBT(stack.tag);
        }
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        if (nbt == null) {
            writeByte(-1);
        } else {
            try {
                for (Map.Entry<String, NBTBase> entry : NBT.asMap(nbt).entrySet()) {
                    NBTBase tag = entry.getValue();

                    writeByte(tag.getId());
                    writeString(entry.getKey());
                    SCReflector.instance.write(tag, this);
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void writeItem(Item item) {
        writePositiveVarInt(item == null ? BufferConstants.ITEM_NULL_ID : Item.getIdFromItem(item));
    }

    @Override
    public void writeBlock(Block block) {
        writePositiveVarInt(block == null ? BufferConstants.BLOCK_NULL_ID : Block.getIdFromBlock(block));
    }

    private static final int X_Z_MASK = (1 << 26) - 1;
    private static final int Y_MASK = (1 << 8) - 1;

    @Override
    public void writeCoords(int x, int y, int z) {
        long l = (long) x & X_Z_MASK
                | ((long) y & Y_MASK) << 26
                | ((long) z & X_Z_MASK) << 34L;
        writeLong(l);
    }

    @Override
    public void writeCoords(ChunkPosition pos) {
        if (pos == null) {
            writeLong(BufferConstants.NULL_COORDS);
        } else {
            writeCoords(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
        }
    }

    @Override
    public void writeCoords(ChunkCoordinates pos) {
        if (pos == null) {
            writeLong(BufferConstants.NULL_COORDS);
        } else {
            writeCoords(pos.posX, pos.posY, pos.posZ);
        }
    }

    @Override
    public void writeChunkCoords(int chunkX, int chunkZ) {
        writeLong((long) chunkX | (long) chunkZ << 32);
    }

    @Override
    public void writeChunkCoords(ChunkCoordIntPair pos) {
        if (pos == null) {
            writeLong(BufferConstants.NULL_COORDS);
        } else {
            writeLong((long) pos.chunkXPos | (long) pos.chunkZPos << 32);
        }
    }

    @Override
    public void writeUUID(UUID uuid) {
        if (uuid == null) {
            writeLong(BufferConstants.UUID_NULL_MSB);
        } else {
            ensureWritable(16);
            writeLongNBC(uuid.getMostSignificantBits());
            writeLongNBC(uuid.getLeastSignificantBits());
        }
    }

    @Override
    public <E extends Enum<E>> void writeEnum(E e) {
        if (e != null) {
            writePositiveVarInt(e.ordinal());
        } else {
            writeNegativeVarInt(-1);
        }
    }

    @Override
    public void writeBitSet(BitSet bitSet) {
        if (bitSet == null) {
            writeLongs(null);
        } else {
            writeLongs(bitSet.toLongArray());
        }
    }

    @Override
    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumSet) {
        if (enumSet == null) {
            writeByte(0);
        } else {
            int numEnums = EnumUtils.getEnumConstantsShared(EnumUtils.getType(enumSet)).length;

            if (numEnums <= 7) {
                int b = 0;
                for (E e : enumSet) {
                    b |= 2 << e.ordinal(); // actually: 1 << (ordinal + 1)
                }
                writeByte(b);
            } else {
                int numBytes = 1 + (numEnums >>> 3);
                ensureWritable(numBytes);
                int off = count;
                byte[] buf = this.buf;

                for (E e : enumSet) {
                    int ord = e.ordinal() + 1;
                    buf[off + (ord & 7)] |= 1 << ord;
                }

                count += numBytes;
            }
        }
    }

    @Override
    public void writeBooleans(boolean[] booleans) {
        if (booleans == null) {
            writeNegativeVarInt(-1);
        } else {
            writeBooleans0(booleans, 0, booleans.length);
        }
    }

    @Override
    public void writeBooleans(boolean[] booleans, int off, int len) {
        checkPositionIndexes(off, off + len, booleans.length);
        writeBooleans0(booleans, off, len);
    }

    private void writeBooleans0(boolean[] booleans, int off, int len) {
        int bytesNeeded = (len + 7) >>> 3; // division by 8 and round up
        ensureWritable(bytesNeeded + positiveVarIntLen(len));
        writeVarIntNBC(len);

        int currentByte = 0;
        for (int idx = 0; idx < len; idx++) {
            int bit = idx & 7;
            currentByte |= (booleans[off + idx] ? 1 << bit : 0);

            if (bit == 7) {
                writeByteNBC(currentByte);
                currentByte = 0;
            }
        }
        if ((len & 7) != 0) {
            writeByteNBC(currentByte);
        }
    }

    @Override
    public void writeBytes(byte[] bytes) {
        if (bytes == null) {
            writeNegativeVarInt(-1);
        } else {
            writeBytes0(bytes, 0, bytes.length);
        }
    }

    @Override
    public void writeBytes(byte[] bytes, int off, int len) {
        checkArgument(len >= 0, "len must be >= 0");
        checkPositionIndexes(off, off + len, bytes.length);

        writeBytes0(bytes, off, len);
    }

    private void writeBytes0(byte[] bytes, int off, int len) {
        ensureWritable(len + positiveVarIntLen(len));
        writeVarIntNBC(len);
        System.arraycopy(bytes, off, buf, count, len);
        count += len;
    }

    @Override
    public void writeShorts(short[] shorts) {
        if (shorts == null) {
            writeNegativeVarInt(-1);
        } else {
            writeShorts0(shorts, 0, shorts.length);
        }
    }

    @Override
    public void writeShorts(short[] shorts, int off, int len) {
        checkArgument(len >= 0, "len must be >= 0");
        checkPositionIndexes(off, off + len, shorts.length);
        writeShorts0(shorts, off, len);
    }

    private void writeShorts0(short[] shorts, int off, int len) {
        ensureWritable(positiveVarIntLen(len) + len << 1);
        writeVarIntNBC(len);

        for (int i = 0; i < len; i++) {
            writeShortNBC(shorts[off + i]);
        }
    }

    @Override
    public void writeInts(int[] ints) {
        if (ints == null) {
            writeNegativeVarInt(-1);
        } else {
            writeInts0(ints, 0, ints.length);
        }
    }

    @Override
    public void writeInts(int[] ints, int off, int len) {
        checkArgument(len >= 0, "len must be >= 0");
        checkPositionIndexes(off, off + len, ints.length);
        writeInts0(ints, off, len);
    }

    private void writeInts0(int[] ints, int off, int len) {
        ensureWritable(positiveVarIntLen(len) + len << 2);
        writeVarIntNBC(len);
        for (int i = 0; i < len; i++) {
            writeIntNBC(ints[i + off]);
        }
    }

    @Override
    public void writeLongs(long[] longs) {
        if (longs == null) {
            writeNegativeVarInt(-1);
        } else {
            writeLongs0(longs, 0, longs.length);
        }
    }

    @Override
    public void writeLongs(long[] longs, int off, int len) {
        checkArgument(len >= 0, "len must be >= 0");
        checkPositionIndexes(off, off + len, longs.length);
        writeLongs0(longs, off, len);
    }

    private void writeLongs0(long[] longs, int off, int len) {
        ensureWritable(positiveVarIntLen(len) + len << 3);
        writeVarIntNBC(len);

        for (int i = 0; i < len; i++) {
            writeLongNBC(longs[i + off]);
        }
    }

    @Override
    public void writeChars(char[] chars) {
        if (chars == null) {
            writeNegativeVarInt(-1);
        } else {
            writeChars0(chars, 0, chars.length);
        }
    }

    @Override
    public void writeChars(char[] chars, int off, int len) {
        checkArgument(len >= 0, "len must be >= 0");
        checkPositionIndexes(off, off + len, chars.length);
        writeChars0(chars, off, len);
    }

    private void writeChars0(char[] chars, int off, int len) {
        ensureWritable(positiveVarIntLen(len) + len << 1);
        writeVarIntNBC(len);

        for (int i = 0; i < len; i++) {
            writeCharNBC(chars[i + off]);
        }
    }

    @Override
    public void writeFloats(float[] floats) {
        if (floats == null) {
            writeNegativeVarInt(-1);
        } else {
            writeFloats0(floats, 0, floats.length);
        }
    }

    @Override
    public void writeFloats(float[] floats, int off, int len) {
        checkArgument(len >= 0, "len must be >= 0");
        checkPositionIndexes(off, off + len, floats.length);
        writeFloats0(floats, off, len);
    }

    private void writeFloats0(float[] floats, int off, int len) {
        ensureWritable(positiveVarIntLen(len) + len << 2);
        writeVarIntNBC(len);

        for (int i = 0; i < len; i++) {
            writeFloatNBC(floats[i + off]);
        }
    }

    @Override
    public void writeDoubles(double[] doubles) {
        if (doubles == null) {
            writeVarInt(-1);
        } else {
            writeDoubles0(doubles, 0, doubles.length);
        }
    }

    @Override
    public void writeDoubles(double[] doubles, int off, int len) {
        checkArgument(len >= 0, "len must be >= 0");
        checkPositionIndexes(off, off + len, doubles.length);
        writeDoubles0(doubles, off, len);
    }

    private void writeDoubles0(double[] doubles, int off, int len) {
        ensureWritable(positiveVarIntLen(len) + len << 3);
        writeVarIntNBC(len);

        for (int i = 0; i < len; i++) {
            writeDoubleNBC(doubles[i + off]);
        }
    }

}

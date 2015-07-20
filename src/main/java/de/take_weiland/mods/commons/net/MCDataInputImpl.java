package de.take_weiland.mods.commons.net;

import com.google.common.primitives.Ints;
import de.take_weiland.mods.commons.internal.SCReflector;
import de.take_weiland.mods.commons.internal.sync.SyncCompanion;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.util.EnumUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkPositionIndexes;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
class MCDataInputImpl extends InputStream implements MCDataInput, SyncCompanion.ChangeIterator {

    private final byte[] buf;
    private final int maxLen;
    private final int initialPos;
    private int pos;
    private int markedPos = BufferConstants.NO_MARK;

    MCDataInputImpl(byte[] buf, int off, int len) {
        checkPositionIndexes(off, off + len, buf.length);
        this.buf = buf;
        this.pos = this.initialPos = off;
        this.maxLen = len;
    }

    @Override
    public InputStream asInputStream() {
        return this;
    }

    @Override
    public int pos() {
        return pos - initialPos;
    }

    @Override
    public int len() {
        return maxLen;
    }

    @Override
    public int available() {
        return maxLen - pos;
    }

    @Override
    public void seek(int pos) {
        if (pos < 0) {
            throw new IllegalArgumentException("pos must be >= 0");
        } else if (pos > maxLen) {
            throw new IndexOutOfBoundsException("pos must be < length");
        } else {
            this.pos = (pos + initialPos);
        }
    }

    @Override
    public int skipBytes(int n) {
        if (n <= 0) {
            return 0;
        }
        int avail = maxLen - pos;
        if (n > avail) {
            n = avail;
        }
        pos += n;
        return n;
    }

    @Override
    public long skip(long n) {
        return skipBytes(Ints.saturatedCast(n));
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readlimit) {
        markedPos = pos;
    }

    @Override
    public void reset() {
        if (markedPos == BufferConstants.NO_MARK) {
            pos = initialPos;
        } else {
            pos = markedPos;
        }
    }

    @Override
    public void close() {
    }

    // actual IO

    final void checkAvailable(int bytes) {
        if (maxLen - pos < bytes) {
            throw new IllegalStateException("Read past end of buffer");
        }
    }

    @Override
    public int read() {
        return (pos + initialPos) < maxLen ? buf[pos++] & 0xFF : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) {
        checkPositionIndexes(off, off + len, buf.length);
        int avail = maxLen - pos;
        if (avail == 0) {
            return -1;
        }
        int actualLen = Math.min(len, avail);
        if (actualLen != 0) {
            System.arraycopy(buf, pos, b, off, actualLen);
            pos += actualLen;
        }
        return actualLen;
    }

    @Override
    public int read(byte[] b) {
        return read(b, 0, b.length);
    }

    @Override
    @Nonnull
    public String readUTF() {
        try {
            return DataInputStream.readUTF(this);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String readLine() {
        StringBuilder sb = new StringBuilder();

        int c;

        loop:
        while (true) {
            switch (c = read()) {
                case -1:
                case '\n':
                    break loop;
                case '\r':
                    // \r\n counts as one line break
                    if (pos != maxLen && buf[pos] == '\n') {
                        pos++;
                    }
                    break loop;
                default:
                    sb.append((char) c);
                    break;
            }
        }

        return c == -1 && sb.length() == 0 ? null : sb.toString();
    }

    // primitives

    @Override
    public boolean readBoolean() {
        return readByte() != BufferConstants.BOOLEAN_FALSE;
    }

    @Override
    public byte readByte() {
        checkAvailable(1);
        return buf[pos++];
    }

    @Override
    public int readUnsignedByte() {
        return readByte() & 0xFF;
    }

    @Override
    public short readShort() {
        checkAvailable(2);
        return readShortNBC();
    }

    final short readShortNBC() {
        int pos = this.pos;
        this.pos = pos + 2;
        return (short) (this.buf[pos] & 0xFF | this.buf[pos + 1] << 8);
    }

    @Override
    public int readUnsignedShort() {
        return readShort() & 0xFFFF;
    }

    @Override
    public char readChar() {
        checkAvailable(2);
        return readCharNBC();
    }

    final char readCharNBC() {
        return (char) ((this.buf[pos++] & 0xFF) | (this.buf[pos++] & 0xFF) << 8);
    }

    @Override
    public int readInt() {
        checkAvailable(4);
        return readIntNBC();
    }

    final int readIntNBC() {
        return (this.buf[pos++] & 0xFF)
                | (this.buf[pos++] & 0xFF) << 8
                | (this.buf[pos++] & 0xFF) << 16
                | (this.buf[pos++] & 0xFF) << 24;
    }

    @Override
    public long readLong() {
        checkAvailable(8);
        return readLongNBC();
    }

    final long readLongNBC() {
        return (long) this.buf[this.pos++] & 0xFF
                | (long) (this.buf[this.pos++] & 0xFF) << 8
                | (long) (this.buf[this.pos++] & 0xFF) << 16
                | (long) (this.buf[this.pos++] & 0xFF) << 24
                | (long) (this.buf[this.pos++] & 0xFF) << 32
                | (long) (this.buf[this.pos++] & 0xFF) << 40
                | (long) (this.buf[this.pos++] & 0xFF) << 48
                | (long) (this.buf[this.pos++] & 0xFF) << 56;
    }

    @Override
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    final float readFloatNBC() {
        return Float.intBitsToFloat(readIntNBC());
    }

    @Override
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    final double readDoubleNBC() {
        return Double.longBitsToDouble(readLongNBC());
    }

    @Override
    public int readVarInt() {
        int res = 0;
        int read;
        int step = 0;

        do {
            read = readByte();
            res |= (read & BufferConstants.SEVEN_BITS) << step;
            step += 7;
        } while ((read & BufferConstants.BYTE_MSB) == 0);
        return res;
    }

    // primitive boxes

    @Override
    public Boolean readBooleanBox() {
        int b = readByte();
        return b == BufferConstants.BOOLEAN_NULL ? null : b != BufferConstants.BOOLEAN_FALSE;
    }

    @Override
    public Byte readByteBox() {
        if (readByte() == BufferConstants.BOX_NULL) {
            return null;
        } else {
            return readByte();
        }
    }

    @Override
    public Short readShortBox() {
        if (readByte() == BufferConstants.BOX_NULL) {
            return null;
        } else {
            return readShort();
        }
    }

    @Override
    public Character readCharBox() {
        if (readByte() == BufferConstants.BOX_NULL) {
            return null;
        } else {
            return readChar();
        }
    }

    @Override
    public Integer readIntBox() {
        if (readByte() == BufferConstants.BOX_NULL) {
            return null;
        } else {
            return readInt();
        }
    }

    @Override
    public Long readLongBox() {
        if (readByte() == BufferConstants.BOX_NULL) {
            return null;
        } else {
            return readLong();
        }
    }

    @Override
    public Float readFloatBox() {
        if (readByte() == BufferConstants.BOX_NULL) {
            return null;
        } else {
            return readFloat();
        }
    }

    @Override
    public Double readDoubleBox() {
        if (readByte() == BufferConstants.BOX_NULL) {
            return null;
        } else {
            return readDouble();
        }
    }

    // array stuff

    @Override
    public void readFully(@Nonnull byte[] b) {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(@Nonnull byte[] b, int off, int len) {
        checkPositionIndexes(off, off + len, b.length);
        checkAvailable(len);
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
    }

    @Override
    public boolean[] readBooleans() {
        return readBooleans(null);
    }

    @Override
    public boolean[] readBooleans(@Nullable boolean[] b) {
        int len = readVarInt();
        if (len < 0) {
            return null;
        } else if (len == 0) {
            return ArrayUtils.nullToEmpty(b);
        } else {
            if (b == null || b.length != len) {
                b = new boolean[len];
            }
            checkAvailable(((len - 1) >> 3) + 1); // works for len > 0

            int currentByte = 0;
            for (int idx = 0; idx < len; idx++) {
                int bit = (idx & 7); // idx % 8
                if (bit == 0) {
                    currentByte = readUnsignedByte();
                }
                b[idx] = (currentByte & (1 << bit)) != 0;
            }

            return b;
        }
    }

    @Override
    public byte[] readBytes() {
        return readBytes(null);
    }

    @Override
    public byte[] readBytes(@Nullable byte[] b) {
        int len = readVarInt();
        if (len < 0) {
            return null;
        } else if (len == 0) {
            return ArrayUtils.nullToEmpty(b);
        } else {
            if (b == null || b.length != len) {
                b = new byte[len];
            }
            checkAvailable(len);
            System.arraycopy(buf, pos, b, 0, len);
            pos += len;
            return b;
        }
    }

    @Override
    public short[] readShorts() {
        return readShorts(null);
    }

    @Override
    public short[] readShorts(@Nullable short[] arr) {
        int len = readVarInt();
        if (len < 0) {
            return null;
        } else if (len == 0) {
            return ArrayUtils.nullToEmpty(arr);
        } else {
            checkAvailable(len << 1);
            if (arr == null || arr.length < len) {
                arr = new short[len];
            }
            for (int i = 0; i < len; i++) {
                arr[i] = readShortNBC();
            }
            return arr;
        }
    }

    @Override
    public int[] readInts() {
        return readInts(null);
    }

    @Override
    public int[] readInts(@Nullable int[] arr) {
        int len = readVarInt();
        if (len < 0) {
            return null;
        } else if (len == 0) {
            return ArrayUtils.nullToEmpty(arr);
        } else {
            checkAvailable(len << 2);
            if (arr == null || arr.length < len) {
                arr = new int[len];
            }
            for (int i = 0; i < len; i++) {
                arr[i] = readIntNBC();
            }
            return arr;
        }
    }

    @Override
    public long[] readLongs() {
        return readLongs(null);
    }

    @Override
    public long[] readLongs(@Nullable long[] arr) {
        int len = readVarInt();
        if (len < 0) {
            return null;
        } else if (len == 0) {
            return ArrayUtils.nullToEmpty(arr);
        } else {
            checkAvailable(len << 3);
            if (arr == null || arr.length < len) {
                arr = new long[len];
            }
            for (int i = 0; i < len; i++) {
                arr[i] = readLongNBC();
            }
            return arr;
        }
    }

    @Override
    public char[] readChars() {
        return readChars(null);
    }

    @Override
    public char[] readChars(@Nullable char[] arr) {
        int len = readVarInt();
        if (len < 0) {
            return null;
        } else if (len == 0) {
            return ArrayUtils.nullToEmpty(arr);
        } else {
            checkAvailable(len << 1);
            if (arr == null || arr.length < len) {
                arr = new char[len];
            }
            for (int i = 0; i < len; ++i) {
                arr[i] = readCharNBC();
            }
            return arr;
        }
    }

    @Override
    public float[] readFloats() {
        return readFloats(null);
    }

    @Override
    public float[] readFloats(@Nullable float[] arr) {
        int len = readVarInt();
        if (len < 0) {
            return null;
        } else if (len == 0) {
            return ArrayUtils.nullToEmpty(arr);
        } else {
            checkAvailable(len << 2);
            if (arr == null || arr.length < len) {
                arr = new float[len];
            }
            for (int i = 0; i < len; ++i) {
                arr[i] = readFloatNBC();
            }
            return arr;
        }
    }

    @Override
    public double[] readDoubles() {
        return readDoubles(null);
    }

    @Override
    public double[] readDoubles(@Nullable double[] b) {
        int len = readVarInt();
        if (len < 0) {
            return null;
        } else if (len == 0) {
            return ArrayUtils.nullToEmpty(b);
        } else {
            checkAvailable(len << 3);
            if (b == null || b.length < len) {
                b = new double[len];
            }
            for (int i = 0; i < len; ++i) {
                b[i] = readDoubleNBC();

            }
            return b;
        }
    }

    // misc stuff
    @Override
    public <E extends Enum<E>> E readEnum(Class<E> clazz) {
        int e = readVarInt();
        return e < 0 ? null : EnumUtils.byOrdinal(clazz, e);
    }

    @Override
    public BitSet readBitSet() {
        long[] words = readLongs();
        if (words == null) {
            return null;
        } else {
            return BitSet.valueOf(words);
        }
    }

    @Override
    public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> enumClass) {
        int readByte = readByte();
        if (readByte == 0) {
            return null;
        } else {
            E[] universe = EnumUtils.getEnumConstantsShared(enumClass);
            EnumSet<E> set = EnumSet.noneOf(enumClass);

            int numEnums = universe.length;
            int numExtraBytes = numEnums >>> 3;
            readByte >>= 1;
            if (numExtraBytes == 0) {
                for (int i = 0; i < numEnums; i++) {
                    if ((readByte & (1 << i)) != 0) {
                        set.add(universe[i]);
                    }
                }
            } else {
                for (int i = 0; i < 7; i++) {
                    if ((readByte & (1 << i)) != 0) {
                        set.add(universe[i]);
                    }
                }
                numEnums -= 7;
                for (int b = 0; b < numExtraBytes; b++) {
                    readByte = readByte();
                    int left = Math.min(8, numEnums);
                    for (int i = 0; i < left; i++) {
                        if ((readByte & (1 << i)) != 0) {
                            set.add(universe[(b << 3) + i + 1]);
                        }
                    }

                    numEnums -= 8;
                }
            }
            return set;
        }
    }

    @Override
    public String readString() {
        int len = readVarInt();
        if (len < 0) {
            return null;
        } else if (len == 0) {
            return "";
        } else {
            checkAvailable(len << 1);
            byte[] buf = this.buf;
            int pos = this.pos;
            char[] chars = new char[len];
            for (int i = 0; i < len; ++i) {
                chars[i] = (char) (buf[pos] | buf[pos + 1] << 8);
                pos += 2;
            }
            this.pos = pos;
            return SCReflector.instance.createStringShared(chars, true);
        }
    }

    @Override
    public ItemStack readItemStack() {
        int id = readShort();
        if (id < 0) {
            return null;
        } else {
            int dmg = readShort();
            int size = readByte();
            ItemStack stack = new ItemStack(Item.getItemById(id), size, dmg);
            stack.stackTagCompound = readNBT();
            return stack;
        }
    }

    @Override
    public FluidStack readFluidStack() {
        int id = readVarInt();
        if (id < 0) {
            return null;
        } else {
            return new FluidStack(FluidRegistry.getFluid(id), readVarInt(), readNBT());
        }
    }

    @Override
    public UUID readUUID() {
        long msb = readLong();
        if (msb == BufferConstants.UUID_NULL_MSB) {
            return null;
        } else {
            return new UUID(msb, readLong());
        }
    }

    @Override
    public NBTTagCompound readNBT() {
        int id = readByte();
        if (id == -1) {
            return null;
        } else {
            NBTTagCompound nbt = new NBTTagCompound();
            Map<String, NBTBase> map = NBT.asMap(nbt);
            try {
                while (id != 0) {
                    String name = readString();
                    NBTBase tag = SCReflector.instance.newNBTTag((byte) id);
                    SCReflector.instance.load(nbt, this, 1, NBTSizeTracker.INFINITE);

                    map.put(name, tag);
                    id = readByte();
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return nbt;
        }
    }

    @Override
    public Item readItem() {
        int id = readVarInt();
        return id == BufferConstants.ITEM_NULL_ID ? null : Item.getItemById(id);
    }

    @Override
    public Block readBlock() {
        int id = readVarInt();
        return id == BufferConstants.BLOCK_NULL_ID ? null : Block.getBlockById(id);
    }

    private static final long X_NUM_MASK = (1 << 25) - 1;
    private static final long X_SIGN_MASK = 1 << 25;
    private static final long Y_MASK = ((1 << 8) - 1) << 26;
    private static final long Z_NUM_MASK = ((1L << 25L) - 1L) << 34L;
    private static final long Z_SIGN_MASK = 1L << 59;

    @Override
    public ChunkPosition readCoords() {
        // x and z use 26 bits each
        // y uses 8 bits
        long l = readLong();

        // first the number part, extend the possible sign bit using "-" operator
        int x = (int) ((l & X_NUM_MASK) | -(l & X_SIGN_MASK));
        // simply mask and shift Y, it's unsigned
        int y = (int) ((l & Y_MASK) >>> 26);
        // same as for x, but also shift left by 34, use >> instead of >>> to preserve sign bit
        int z = (int) (((l & Z_NUM_MASK) | -(l & Z_SIGN_MASK)) >> 34L);

        return new ChunkPosition(x, y, z);
    }

    @Override
    public ChunkCoordIntPair readChunkCoords() {
        long l = readLong();
        if (l == BufferConstants.NULL_COORDS) {
            return null;
        } else {
            return new ChunkCoordIntPair((int) l, (int) (l >>> 32));
        }
    }

    @Override
    public int fieldId() {
        return readVarInt();
    }

    @Override
    public <T_DATA, T_VAL, T_COM> void apply(Syncer<T_VAL, T_COM, T_DATA> syncer, Object obj, PropertyAccess<T_VAL> property, Object cObj, PropertyAccess<T_COM> companion) {
        syncer.apply(this, obj, property, cObj, companion);
    }

}

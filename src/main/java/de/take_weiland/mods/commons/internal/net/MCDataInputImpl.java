package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.util.EnumUtils;
import de.take_weiland.mods.commons.util.SCReflector;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.EnumSet;

/**
 * @author diesieben07
 */
public final class MCDataInputImpl extends InputStream implements DataInput, MCDataInput {

    private final ByteBuf buf;

    public MCDataInputImpl(ByteBuf buf) {
        this.buf = buf;
    }

    @Override
    public ByteBuf getNettyBuffer() {
        return buf;
    }

    @Override
    public InputStream asInputStream() {
        return this;
    }

    @Override
    public DataInput asDataInput() {
        return this;
    }

    @Override
    public int read() throws IOException {
        return buf.isReadable() ? buf.readByte() & 0xFF : -1;
    }

    @Override
    public int read(@Nonnull byte[] b, int off, int len) throws IOException {
        int toRead = Math.min(len, buf.readableBytes());
        buf.readBytes(b, off, toRead);
        return toRead;
    }

    @Override
    public boolean readBoolean() {
        return buf.readBoolean();
    }

    @Override
    public byte readByte() {
        return buf.readByte();
    }

    @Override
    public short readShort() {
        return buf.readShort();
    }

    @Override
    public char readChar() {
        return buf.readChar();
    }

    @Override
    public int readMedium() {
        return buf.readMedium();
    }

    @Override
    public int readInt() {
        return buf.readInt();
    }

    @Override
    public long readLong() {
        return buf.readLong();
    }

    @Override
    public float readFloat() {
        return buf.readFloat();
    }

    @Override
    public double readDouble() {
        return buf.readDouble();
    }

    @Override
    public int readVarInt() {
        int result = 0;
        int step = 0;
        byte read;

        do {
            read = buf.readByte();
            result |= (read & 0b0111_1111) << step;
            step += 7;
        } while ((read & 0b1000_0000) != 0);

        return result;
    }

    @Override
    public int readUnsignedByte() {
        return buf.readUnsignedByte();
    }

    @Override
    public int readUnsignedShort() {
        return buf.readUnsignedShort();
    }

    @Override
    public int readUnsignedMedium() {
        return buf.readUnsignedMedium();
    }

    @Override
    public long readUnsignedInt() {
        return buf.readUnsignedInt();
    }

    @Override
    public String readString() {
        int len = readVarInt();
        if (len < 0) {
            return null;
        } else if (len == 0) {
            return "";
        } else {
            try {
                if (buf.hasArray()) {
                    int readerIdx = buf.readerIndex();
                    buf.readerIndex(readerIdx + len);
                    return new String(buf.array(), buf.arrayOffset() + readerIdx, len, StandardCharsets.UTF_8);
                } else {
                    byte[] arr = new byte[len];
                    buf.readBytes(arr);
                    return new String(arr, StandardCharsets.UTF_8);
                }
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalStateException("invalid string length received, expected more bytes", e);
            }
        }
    }

    @Override
    public <E extends Enum<E>> E readEnum(Class<E> enumClass) {
        int ord = readVarInt();
        return ord < 0 ? null : EnumUtils.byOrdinal(enumClass, ord);
    }

    @Override
    public ItemStack readItemStack() {
        int id = buf.readShort();
        if (id == MCDataOutputImpl.NULL_ID) {
            return null;
        } else {
            int stackSize = buf.readByte();
            int damage = buf.readShort();
            NBTTagCompound nbt = readNBT();
            ItemStack stack = new ItemStack(Item.getItemById(id), stackSize, damage);
            stack.setTagCompound(nbt);
            return stack;
        }
    }

    @Override
    public Item readItem() {
        int id = buf.readShort();
        if (id == MCDataOutputImpl.NULL_ID) {
            return null;
        } else {
            return Item.getItemById(id);
        }
    }

    @Override
    public Block readBlock() {
        int id = buf.readShort();
        if (id == MCDataOutputImpl.NULL_ID) {
            return null;
        } else {
            return Block.getBlockById(id);
        }
    }

    @Override
    public NBTTagCompound readNBT() {
        byte id = buf.readByte();
        if (id == MCDataOutputImpl.NBT_NULL_ID) {
            return null;
        } else {
            NBTTagCompound nbt = new NBTTagCompound();
            while (id != 0) {
                NBTBase tag = SCReflector.instance.newNBTTag(id);
                String key = readString();

                nbt.setTag(key, tag);
                id = buf.readByte();
            }
            return nbt;
        }
    }

    @Override
    public BitSet readBitSet() {
        long[] longs = readLongs();
        if (longs == null) {
            return null;
        } else {
            return BitSet.valueOf(longs);
        }
    }

    @Override
    public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> enumClass) {
        return null;
    }

    @Override
    public byte[] readBytes() {
        int len = readVarInt();
        if (len > 0) {
            byte[] bytes = new byte[len];
            buf.readBytes(bytes);
            return bytes;
        } else if (len == 0) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        } else {
            return null;
        }
    }

    @Override
    public short[] readShorts() {
        int len = readVarInt();
        if (len > 0) {
            short[] shorts = new short[len];
            for (int i = 0; i < len; i++) {
                shorts[i] = buf.readShort();
            }
            return shorts;
        } else if (len == 0) {
            return ArrayUtils.EMPTY_SHORT_ARRAY;
        } else {
            return null;
        }
    }

    @Override
    public char[] readChars() {
        int len = readVarInt();
        if (len > 0) {
            char[] chars = new char[len];
            for (int i = 0; i < len; i++) {
                chars[i] = buf.readChar();
            }
            return chars;
        } else if (len == 0) {
            return ArrayUtils.EMPTY_CHAR_ARRAY;
        } else {
            return null;
        }
    }

    @Override
    public int[] readInts() {
        int len = readVarInt();
        if (len > 0) {
            int[] ints = new int[len];
            for (int i = 0; i < len; i++) {
                ints[i] = buf.readInt();
            }
            return ints;
        } else if (len == 0) {
            return ArrayUtils.EMPTY_INT_ARRAY;
        } else {
            return null;
        }
    }

    @Override
    public long[] readLongs() {
        int len = readVarInt();
        if (len > 0) {
            long[] longs = new long[len];
            for (int i = 0; i < len; i++) {
                longs[i] = readLong();
            }
            return longs;
        } else if (len == 0) {
            return ArrayUtils.EMPTY_LONG_ARRAY;
        } else {
            return null;
        }
    }

    @Override
    public float[] readFloats() {
        int len = readVarInt();
        if (len > 0) {
            float[] floats = new float[len];
            for (int i = 0; i < len; i++) {
                floats[i] = buf.readFloat();
            }
            return floats;
        } else if (len == 0) {
            return ArrayUtils.EMPTY_FLOAT_ARRAY;
        } else {
            return null;
        }
    }

    @Override
    public double[] readDoubles() {
        int len = readVarInt();
        if (len > 0) {
            double[] doubles = new double[len];
            for (int i = 0; i < len; i++) {
                doubles[i] = readDouble();
            }
            return doubles;
        } else if (len == 0) {
            return ArrayUtils.EMPTY_DOUBLE_ARRAY;
        } else {
            return null;
        }
    }

    @Override
    public void readFully(@Nonnull byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(@Nonnull byte[] b, int off, int len) throws IOException {
        buf.readBytes(b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int toSkip = Math.min(n, buf.readableBytes());
        buf.skipBytes(toSkip);
        return toSkip;
    }

    @Override
    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        loop:
        while (true) {
            switch (c = read()) {
                case -1:
                case '\n':
                    break loop;

                case '\r':
                    int c2 = read();
                    if ((c2 != '\n') && (c2 != -1)) {
                        buf.readerIndex(buf.readerIndex() - 1);
                    }
                    break loop;

                default:
                    sb.append((char) c);
                    break;
            }
        }
        if ((c == -1) && (sb.length() == 0)) {
            return null;
        }
        return sb.toString();
    }

    @Nonnull
    @Override
    public String readUTF() throws IOException {
        return new DataInputStream(this).readUTF();
    }
}

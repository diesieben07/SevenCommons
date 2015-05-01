package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.util.EnumUtils;
import de.take_weiland.mods.commons.util.SCReflector;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.util.BitSet;
import java.util.EnumSet;

/**
 * @author diesieben07
 */
public final class MCDataOutputImpl extends OutputStream implements MCDataOutput, DataOutput {

    private final ByteBuf buf;

    public MCDataOutputImpl(ByteBuf buf) {
        this.buf = buf;
    }

    @Override
    public ByteBuf getNettyBuffer() {
        return buf;
    }

    @Override
    public OutputStream asOutputStream() {
        return this;
    }

    @Override
    public DataOutput asDataOutput() {
        return this;
    }

    @Override
    public void write(int b) {
        buf.writeByte(b);
    }

    @Override
    public void writeBoolean(boolean b) {
        buf.writeBoolean(b);
    }

    @Override
    public void writeByte(int b) {
        buf.writeByte(b);
    }

    @Override
    public void writeShort(int s) {
        buf.writeShort(s);
    }

    @Override
    public void writeChar(int c) {
        buf.writeChar(c);
    }

    @Override
    public void writeMedium(int m) {
        buf.writeMedium(m);
    }

    @Override
    public void writeInt(int i) {
        buf.writeInt(i);
    }

    @Override
    public void writeLong(long l) {
        buf.writeLong(l);
    }

    @Override
    public void writeFloat(float f) {
        buf.writeFloat(f);
    }

    @Override
    public void writeDouble(double d) {
        buf.writeDouble(d);
    }

    private static final int BYTE_MSB = 0b1000_0000;
    private static final int BYTE_LSBS = 0b0111_1111;

    @Override
    public void writeVarInt(int i) {
        while ((i & ~BYTE_LSBS) != 0) {
            buf.writeByte(i & BYTE_LSBS | BYTE_MSB);
            i >>>= 7;
        }

        buf.writeByte(i);
    }

    @Override
    public void writeString(String s) {
        int len = s.length();

        int lenIdx = buf.writerIndex();

        buf.ensureWritable(2 + s.length())
           .writerIndex(lenIdx + 2);

        int utfLen = len;

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c >= '\u0001' && c <= '\u007f') {
                buf.writeByte(c);
            } else if (c >= '\u0800') {
                buf.writeByte((0xe0 | (0x0f & (c >> 12))))
                   .writeByte(0x80 | (0x3f & c >> 6))
                   .writeByte(0x80 | 0x3f & c);

                utfLen += 2;
            } else {
                buf.writeByte(0xc0 | 0x1f & c >> 6)
                   .writeByte(0x80 | 0x3f & c);

                utfLen++;
            }
        }
        buf.setShort(lenIdx, utfLen);
    }

    @Override
    public <E extends Enum<E>> void writeEnum(E e) {
        writeVarInt(e == null ? -1 : e.ordinal());
    }

    private static final short NULL_ID = (short) 0x8000;

    @Override
    public void writeItemStack(ItemStack stack) {
        if (stack == null) {
            buf.writeShort((int) NULL_ID);
        } else {
            buf.writeShort(Item.getIdFromItem(stack.getItem()))
               .writeByte(stack.stackSize)
               .writeShort(stack.getItemDamage());
            writeNBT(stack.stackTagCompound);
        }
    }

    @Override
    public void writeItem(Item item) {
        buf.writeShort(item == null ? NULL_ID : Item.getIdFromItem(item));
    }

    @Override
    public void writeBlock(Block block) {
        buf.writeShort(block == null ? NULL_ID : Block.getIdFromBlock(block));
    }

    private static final byte NBT_NULL_ID = (byte) 0b1000;

    @Override
    public void writeNBT(NBTTagCompound nbtEntry) {
        if (nbtEntry == null) {
            writeByte(NBT_NULL_ID);
        } else {
            NBT.asMap(nbtEntry).forEach((key, nbtBase) -> {
                writeByte(nbtEntry.getId());
                writeString(key);
                try {
                    SCReflector.instance.write(nbtEntry, this);
                } catch (IOException e) {
                    throw new IllegalStateException("NBT write threw IOException");
                }
            });
        }
    }

    @Override
    public void writeBitSet(BitSet set) {
        writeLongs(set == null ? null : set.toLongArray());
    }

    @Override
    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> set) {
        writeLong(EnumUtils.encodeAsLong(set));
    }

    @Override
    public void writeBytes(byte[] arr, int off, int len) {
        buf.writeBytes(arr, off, len);
    }

    @Override
    public void writeShorts(short[] arr, int off, int len) {
        buf.ensureWritable(len << 1);

        for (; off < len; off++) {
            buf.writeShort(arr[off]);
        }
    }

    @Override
    public void writeChars(char[] arr, int off, int len) {
        buf.ensureWritable(len << 1);
        for (; off < len; off++) {
            buf.writeChar(arr[off]);
        }
    }

    @Override
    public void writeInts(int[] arr, int off, int len) {
        buf.ensureWritable(len << 2);
        for (; off < len; off++) {
            buf.writeInt(arr[off]);
        }
    }

    @Override
    public void writeLongs(long[] arr, int off, int len) {
        buf.ensureWritable(len << 3);
        for (; off < len; off++) {
            buf.writeLong(arr[off]);
        }
    }

    @Override
    public void writeFloats(float[] arr, int off, int len) {
        buf.ensureWritable(len << 2);
        for (; off < len; off++) {
            buf.writeFloat(arr[off]);
        }
    }

    @Override
    public void writeDoubles(double[] arr, int off, int len) {
        buf.ensureWritable(len << 3);
        for (; off < len; off++) {
            buf.writeDouble(arr[off]);
        }
    }

    @Override
    public void writeBytes(@Nonnull String s) {
        int len = s.length();

        buf.ensureWritable(len);
        for (int i = 0; i < len; i++) {
            buf.writeByte(s.charAt(i));
        }
    }

    @Override
    public void writeChars(@Nonnull String s) {
        int len = s.length();

        buf.ensureWritable(len << 1);
        for (int i = 0; i < len; i++) {
            buf.writeChar(s.charAt(i));
        }
    }

    @Override
    public void writeUTF(@Nonnull String s) throws IOException {
        if (s.length() >= 65535) {
            throw new UTFDataFormatException("String longer than maximum length");
        }
        writeString(s);
    }
}

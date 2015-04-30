package de.take_weiland.mods.commons.internal.net;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.util.EnumUtils;
import de.take_weiland.mods.commons.util.SCReflector;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.BitSet;
import java.util.EnumSet;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

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

    private static RuntimeException makeEnumSetTooBigException(EnumSet<?> enumSet) {
        String enumType = EnumUtils.getType(enumSet).getName();
        throw new UnsupportedOperationException(String.format("Cannot encode EnumSet of EnumType %s. Only Enum types with <= 64 values are supported", enumType));
    }

    private static final ESEncoder enumSetEncoder;

    static {
        ESEncoder encoder;
        try {
            encoder = (ESEncoder) Class.forName("de.take_weiland.mods.commons.internal.net.MCDataOutputImpl$ESEncoderFast").newInstance();
        } catch (Throwable e) {
            encoder = new ESEncoderPureJava();
        }
        enumSetEncoder = encoder;
    }

    private static abstract class ESEncoder {

        abstract <E extends Enum<E>> long encode(EnumSet<E> set);

    }

    private static final class ESEncoderFast extends ESEncoder {

        private static final MethodHandle getter;

        static {
            try {
                Field longField = null;
                Class<?> regES = Class.forName("java.util.RegularEnumSet");
                for (Field field : regES.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers()) && field.getType() == long.class) {
                        longField = field;
                        break;
                    }
                }
                if (longField != null) {
                    longField.setAccessible(true);
                    getter = publicLookup().unreflectGetter(longField).asType(methodType(long.class, EnumSet.class));
                } else {
                    throw new RuntimeException("");
                }
            } catch (ClassNotFoundException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        <E extends Enum<E>> long encode(EnumSet<E> set) {
            try {
                return (long) getter.invokeExact(set);
            } catch (ClassCastException cce) {
                throw makeEnumSetTooBigException(set);
            } catch (Throwable e) {
                throw Throwables.propagate(e);
            }
        }
    }

    private static final class ESEncoderPureJava extends ESEncoder {

        @Override
        <E extends Enum<E>> long encode(EnumSet<E> set) {
            if (EnumUtils.getEnumConstantsShared(EnumUtils.getType(set)).length > 64) {
                throw makeEnumSetTooBigException(set);
            }
            long result = 0;
            for (Enum<?> e : set){
                result |= 1 << e.ordinal();
            }
            return result;
        }
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
    public void writeBytes(String s) {
        int len = s.length();

        buf.ensureWritable(len);
        for (int i = 0; i < len; i++) {
            buf.writeByte(s.charAt(i));
        }
    }

    @Override
    public void writeChars(String s) {
        int len = s.length();

        buf.ensureWritable(len << 1);
        for (int i = 0; i < len; i++) {
            buf.writeChar(s.charAt(i));
        }
    }

    @Override
    public void writeUTF(String s) throws IOException {
        if (s.length() >= 65535) {
            throw new UTFDataFormatException("String longer than maximum length");
        }
        writeString(s);
    }
}

package de.take_weiland.mods.commons.internal.net;

import com.google.common.base.Throwables;
import com.google.common.base.Utf8;
import de.take_weiland.mods.commons.internal.CommonMethodHandles;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.util.EnumUtils;
import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.FastThreadLocal;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author diesieben07
 */
@MethodsReturnNonnullByDefault
public final class MCDataOutputImpl extends OutputStream implements MCDataOutput, GatheringByteChannel {

    private final ByteBuf buf;

    public MCDataOutputImpl(ByteBuf buf) {
        this.buf = buf;
    }

    @Override
    public int length() {
        return buf.readableBytes();
    }

    @Override
    public OutputStream asOutputStream() {
        return this;
    }

    @Override
    public GatheringByteChannel asByteChannel() {
        return this;
    }

    @Override
    public ByteBuf asByteBuf() {
        return buf;
    }

    @Override
    public void copyInto(OutputStream stream) throws IOException {
        buf.getBytes(buf.readerIndex(), stream, buf.readableBytes());
    }

    @Override
    public void copyInto(DataOutput out) throws IOException {
        if (buf.hasArray()) {
            out.write(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
        } else if (out instanceof OutputStream) {
            copyInto((OutputStream) out);
        } else {
            byte[] arr = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), arr);
            out.write(arr);
        }
    }

    @Override
    public void copyInto(WritableByteChannel channel) throws IOException {
        ByteBuffer nioBuffer = buf.internalNioBuffer(buf.readerIndex(), buf.readableBytes());
        while (nioBuffer.remaining() > 0) {
            channel.write(nioBuffer);
        }
    }

    @Override
    public void copyInto(ByteBuffer nioBuffer) {
        if (buf.hasArray()) {
            nioBuffer.put(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
        } else {
            nioBuffer.put(buf.internalNioBuffer(buf.readerIndex(), buf.readableBytes()));
        }
    }

    @Override
    public int writeBytes(InputStream in) throws IOException {
        return writeBytes(in, Integer.MAX_VALUE);
    }

    @Override
    public int writeBytes(InputStream in, int max) throws IOException {
        return buf.writeBytes(in, max);
    }

    @Override
    public void writeBytes(DataInput in, int bytes) throws IOException {
        buf.ensureWritable(bytes);
        if (buf.hasArray()) {
            in.readFully(buf.array(), buf.arrayOffset() + buf.writerIndex(), bytes);
            buf.writerIndex(buf.writerIndex() + bytes);
        } else {
            byte[] arr = new byte[bytes];
            in.readFully(arr);
            buf.writeBytes(arr);
        }
    }

    @Override
    public int writeBytes(ReadableByteChannel in) throws IOException {
        return writeBytes(in, Integer.MAX_VALUE);
    }

    @Override
    public int writeBytes(ReadableByteChannel in, int bytes) throws IOException {
        ByteBuffer nioBuf = buf.internalNioBuffer(buf.writerIndex(), Math.min(buf.writableBytes(), bytes));
        int read = Math.max(0, in.read(nioBuf));
        buf.writerIndex(buf.writerIndex() + read);
        return read;
    }

    @Override
    public void writeBytes(ByteBuffer nioBuf) {
        buf.ensureWritable(nioBuf.remaining());
        buf.writeBytes(nioBuf);
    }

    @Override
    public void writeBytes(ByteBuf buf) {
        writeBytes(buf, buf.readableBytes());
    }

    @Override
    public void writeBytes(ByteBuf buf, int len) {
        this.buf.ensureWritable(len);
        this.buf.writeBytes(buf, len);
    }

    @Override
    public void write(@Nonnull byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public void write(@Nonnull byte[] b, int off, int len) {
        buf.ensureWritable(len);
        buf.writeBytes(b, off, len);
    }

    @Override
    public void write(int b) {
        buf.writeByte(b);
    }

    @Override
    public synchronized int write(ByteBuffer src) throws IOException {
        int n = src.remaining();
        writeBytes(src);
        return n;
    }

    @Override
    public synchronized long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        long l = 0;
        for (int i = 0; i < length; i++) {
            l += write(srcs[i]);
        }
        return l;
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException {
        return write(srcs, 0, srcs.length);
    }

    @Override
    public void writeBoolean(boolean v) {
        buf.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) {
        buf.writeByte(v);
    }

    @Override
    public void writeShort(int v) {
        buf.writeShort(v);
    }

    @Override
    public void writeChar(int v) {
        buf.writeChar(v);
    }

    @Override
    public void writeInt(int v) {
        buf.writeInt(v);
    }

    @Override
    public void writeLong(long v) {
        buf.writeLong(v);
    }

    @Override
    public void writeFloat(float v) {
        buf.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) {
        buf.writeDouble(v);
    }

    @Override
    public void writeMedium(int v) {
        buf.writeMedium(v);
    }

    @Override
    public void writeVarInt(int i) {
        while ((i & ~0x7F) != 0) {
            writeByte(i | 0x80);
            i >>>= 7;
        }
        writeByte(i);
    }

    @Override
    public void writeShiftedVarInt(int i) {
        writeVarInt(i + 1);
    }

    @Override
    public void writeVarLong(long l) {
        while ((l & 0x7F) != 0) {
            writeByte(((int) l) | 0x80);
            l >>>= 7;
        }
        writeByte((int) l);
    }

    @Override
    public void writeShiftedVarLong(long l) {
        writeVarLong(l + 1);
    }

    @Override
    public void writeNullableBoolean(Boolean b) {
        writeByte(b == null ? BufferConstants.BOOLEAN_NULL : (b ? BufferConstants.BOOLEAN_TRUE : BufferConstants.BOOLEAN_FALSE));
    }

    @Override
    public void writeNullableByte(Byte b) {
        if (b == null) {
            writeShiftedVarInt(-1);
        } else {
            writeShiftedVarInt(b & 0xFF);
        }
    }

    @Override
    public void writeNullableShort(Short s) {
        if (s == null) {
            writeShiftedVarInt(-1);
        } else {
            writeShiftedVarInt(s & 0xFFFF);
        }
    }

    @Override
    public void writeNullableChar(Character c) {
        if (c == null) {
            writeShiftedVarInt(-1);
        } else {
            writeShiftedVarInt(c);
        }
    }

    @Override
    public void writeNullableInt(Integer i) {
        if (i == null) {
            writeShiftedVarLong(-1);
        } else {
            writeShiftedVarLong(i & 0xFFFF_FFFFL);
        }
    }

    @Override
    public void writeNullableFloat(Float f) {
        if (f == null) {
            writeShiftedVarLong(-1); // see writeNullableInt above
        } else {
            writeShiftedVarLong(Float.floatToRawIntBits(f) & 0xFFFF_FFFFL);
        }
    }

    @Override
    public void writeNullableLong(Long l) {
        // like a VarLong but the first byte has only 6 bits of payload

        // highest bit set: read more bytes (like varLong)
        // first byte 2nd highest bit: value is null then 6 bits payload
        // rest of the bytes: 7 bits payload plus "read more" flag

        if (l == null) {
            writeByte(0b0100_0000); // don't read more and value is null
        } else {
            writeNullableLongNonnull(l);
        }
    }

    private void writeNullableLongNonnull(long l) {
        if ((l & ~0x3F) == 0) { // we only need one byte!
            writeByte((int) l); // don't need to set "read more" or "null" flag
        } else { // we need at least more than one byte
            writeByte(0b1000_0000 | (int) l & 0x3F); // set "read more" flag and write first 6 bits of payload

            l >>>= 6;

            while ((l & ~0x7F) != 0) {
                writeByte(0b1000_0000 | (int) l & 0x7F); // set "read more" flag and write 7 bits of payload
                l >>>= 7;
            }
            writeByte((int) l);
        }
    }

    @Override
    public void writeNullableDouble(Double d) {
        if (d == null) {
            writeByte(0b0100_0000); // see writeNullableLong above
        } else {
            writeNullableLongNonnull(Double.doubleToRawLongBits(d));
        }
    }

    @Override
    public void writeChars(@Nonnull String s) {
        buf.ensureWritable(s.length() << 1);
        buf.internalNioBuffer(buf.writerIndex(), buf.writableBytes())
                .asCharBuffer().put(s);
    }

    @Override
    public void writeUTF(@Nonnull String s) {
        try {
            new DataOutputStream(this).writeUTF(s);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    @Deprecated
    public void writeBytes(@Nonnull String s) {
        for (int i = 0; i < s.length(); i++) {
            writeByte(s.charAt(i));
        }
    }

    @Override
    public byte[] toByteArray() {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);
        return bytes;
    }

    @Override
    public void writeString(String s) {
        if (s == null) {
            writeShiftedVarInt(-1);
        } else if (s.isEmpty()) {
            writeShiftedVarInt(0);
        } else {
            int encodedLength = Utf8.encodedLength(s);
            writeShiftedVarInt(encodedLength);
            buf.ensureWritable(encodedLength);

            ByteBuffer nioBuffer = buf.internalNioBuffer(buf.writerIndex(), buf.writableBytes());
            CharsetEncoder encoder = UTF8_ENCODER.get().reset();
            CoderResult result = encoder.encode(CharBuffer.wrap(s), nioBuffer, true);
            try {
                if (!result.isUnderflow()) {
                    result.throwException();
                }
                result = encoder.flush(nioBuffer);
                if (!result.isUnderflow()) {
                    result.throwException();
                }
            } catch (CharacterCodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void writeUUID(UUID uuid) {
        if (uuid == null) {
            writeLong(BufferConstants.UUID_NULL_MSB);
        } else {
            writeLong(uuid.getMostSignificantBits());
            writeLong(uuid.getLeastSignificantBits());
        }
    }

    @Override
    public <E extends Enum<E>> void writeEnum(E e) {
        if (e == null) {
            writeShiftedVarInt(-1);
        } else {
            writeShiftedVarInt(e.ordinal());
        }
    }

    @Override
    public void writeBitSet(BitSet bitSet) {
        if (bitSet == null) {
            writeShiftedVarInt(-1);
        } else {
            byte[] bytes = bitSet.toByteArray();
            writeShiftedVarInt(bytes.length);
            write(bytes);
        }
    }

    @Override
    public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumSet) {
        if (enumSet == null) {
            writeByte(0);
        } else {
            E[] universe = EnumUtils.getEnumConstantsShared(EnumUtils.getType(enumSet));
            int numEnums = universe.length;

            int b = 0b1; // signal for non-null

            for (int i = 0, m = Math.min(7, numEnums); i < m; i++) {
                if (enumSet.contains(universe[i])) {
                    b |= 2 << i;
                }
            }
            writeByte(b);

            int i;
            for (i = 7; i < numEnums; i++) {
                if (enumSet.contains(universe[i])) {
                    int offset = i + 1 & 7;
                    b |= 1 << offset;
                    if (offset == 7) {
                        writeByte(b);
                        b = 0;
                    }
                }
            }
            if ((i & 7) != 7) { // if the 2nd loop wrote not a multiple of 8
                writeByte(b);
            }
        }
    }

    private static final FastThreadLocal<CharsetEncoder> UTF8_ENCODER = new FastThreadLocal<CharsetEncoder>() {
        @Override
        protected CharsetEncoder initialValue() throws Exception {
            return StandardCharsets.UTF_8.newEncoder();
        }
    };


    @SuppressWarnings("deprecation")
    @Override
    public void writeFluidStack(FluidStack stack) {
        if (stack == null) {
            writeShiftedVarInt(-1);
        } else {
            writeShiftedVarInt(FluidRegistry.getFluidID(stack.getFluid()));
            if (stack.tag == null) {
                writeVarInt(stack.amount << 1);
            } else {
                writeVarInt((stack.amount << 1) | 0x1);
                writeNBT(stack.tag);
            }
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
                    try {
                        CommonMethodHandles.nbtBaseWrite.invokeExact((NBTBase) tag, (DataOutput) this);
                    } catch (IOException io) {
                        throw io;
                    } catch (Throwable x) {
                        throw Throwables.propagate(x);
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void writeBlock(Block block) {
        if (block == null) {
            writeShiftedVarInt(-1);
        } else {
            writeShiftedVarInt(Block.getIdFromBlock(block));
        }
    }

    @Override
    public void writeItem(Item item) {
        if (item == null) {
            writeShiftedVarInt(-1);
        } else {
            writeShiftedVarInt(Item.getIdFromItem(item));
        }
    }

    @Override
    public void writeItemStack(ItemStack stack) {
        if (stack == null) {
            writeItem(null);
        } else {
            writeItem(stack.getItem());
            writeVarInt(stack.getItemDamage());
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null) {
                writeByte(stack.stackSize & 0x7F);
            } else {
                writeByte(stack.stackSize & 0x7F | 0x80);
                writeNBT(nbt);
            }

        }
    }

    @Override
    public <T extends IForgeRegistryEntry<T>> void writeRegistryEntry(T entry) {
        if (entry == null) {
            writeShiftedVarInt(-1);
        } else {
            //noinspection unchecked
            FMLControlledNamespacedRegistry<T> registry = (FMLControlledNamespacedRegistry<T>) GameRegistry.findRegistry((Class<T>) entry.getRegistryType());
            writeShiftedVarInt(registry.getId(entry));
        }
    }

    @Override
    public void writeBlockPos(BlockPos pos) {
        if (pos == null) {
            writeLong(0xF000_0000_0000_0000L);
        } else {
            writeBlockPos(pos.getX(), pos.getY(), pos.getZ());
        }
    }

    @Override
    public void writeBlockPos(int x, int y, int z) {
        // x and z get 26 bits each, y gets 8, leaving top 4 bits empty
        // this differs from the vanilla format in that it does not give y the 12 remaining bits, but just the 8 it needs
        // leaves space for null marker
        long l = (x & 0x3FF_FFFFL)
                | ((z & 0x3FF_FFFFL) << 26)
                | ((y & 0xFFL) << 26 + 26);
        writeLong(l);
    }

    @Override
    public void writeChunkPos(ChunkPos pos) {
        // chunk coordinates can be [-30_000_000 >> 4, 30_000_000 >> 4)
        // so only 0x3F_FFFF is needed

        if (pos == null) {
            writeMedium(0x40_0000);
        } else {
            writeMedium(pos.chunkXPos);
            writeMedium(pos.chunkZPos);
        }
    }

    @Override
    public void writeChunkPos(int chunkX, int chunkZ) {
        writeMedium(chunkX);
        writeMedium(chunkZ);
    }

    @Override
    public void writeBlockState(IBlockState state) {
        if (state == null) {
            writeShiftedVarInt(-1);
        } else {
            writeShiftedVarInt(Block.getStateId(state));
        }
    }

    @Override
    public void writeRichBlockState(IBlockState state) {
        if (state == null) {
            writeShiftedVarInt(-1);
        } else {
            Block block = state.getBlock();
            int meta = block.getMetaFromState(state);
            writeShiftedVarInt(Block.getIdFromBlock(block) | (meta << 12));

            @SuppressWarnings("deprecation")
            IBlockState stateFromMeta = block.getStateFromMeta(meta);
            for (IProperty<?> property : state.getPropertyNames()) {
                maybeWriteProperty(property, state, stateFromMeta);
            }
            writeString(null);
        }
    }

    private <T extends Comparable<T>> void maybeWriteProperty(IProperty<T> property, IBlockState state, IBlockState stateFromMeta) {
        T value = state.getValue(property);
        if (!Objects.equals(value, stateFromMeta.getValue(property)) || !Objects.equals(value, state.getBlock().getDefaultState().getValue(property))) {
            writeString(property.getName());
            if (property.getValueClass().isEnum()) {
                //noinspection rawtypes
                writeEnum((Enum) value);
            } else if (property.getValueClass() == Integer.class) {
                writeVarInt((Integer) value);
            } else if (property.getValueClass() == Boolean.class) {
                writeBoolean((Boolean) value);
            } else {
                writeString(property.getName(value));
            }
        }
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() {
    }


}

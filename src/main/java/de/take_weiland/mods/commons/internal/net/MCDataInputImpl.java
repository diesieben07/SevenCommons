package de.take_weiland.mods.commons.internal.net;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.internal.CommonMethodHandles;
import de.take_weiland.mods.commons.internal.sync_olds.SyncCompanion;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import de.take_weiland.mods.commons.util.EnumUtils;
import io.netty.buffer.ByteBuf;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MCDataInputImpl extends InputStream implements MCDataInput, ScatteringByteChannel, SyncCompanion.ChangeIterator {

    private final ByteBuf buf;

    public MCDataInputImpl(ByteBuf buf) {
        this.buf = buf;
    }

    @Override
    public int size() {
        return buf.writerIndex();
    }

    @Override
    public int position() {
        return buf.readerIndex();
    }

    @Override
    public void position(int pos) {
        buf.readerIndex(pos);
    }

    @Override
    public int remaining() {
        return buf.readableBytes();
    }

    @Override
    public InputStream asInputStream() {
        return this;
    }

    @Override
    public ScatteringByteChannel asByteChannel() {
        return this;
    }

    @Override
    public ByteBuf asByteBuf() {
        return buf;
    }

    @Override
    public void readBytes(OutputStream out, int bytes) throws IOException {
        buf.getBytes(buf.readerIndex(), out, bytes);
        buf.readerIndex(buf.readerIndex() + bytes);
    }

    @Override
    public void readBytes(DataOutput out, int bytes) throws IOException {
        if (buf.hasArray()) {
            out.write(buf.array(), buf.arrayOffset() + buf.readerIndex(), bytes);
        } else if (out instanceof OutputStream) {
            readBytes((OutputStream) out, bytes);
        } else {
            byte[] arr = new byte[bytes];
            buf.getBytes(buf.readerIndex(), arr);
            out.write(arr);
        }
        buf.readerIndex(buf.readerIndex() + bytes);
    }

    @Override
    public void readBytes(WritableByteChannel out, int bytes) throws IOException {
        ByteBuffer nioBuffer = buf.internalNioBuffer(buf.readerIndex(), bytes);
        while (nioBuffer.remaining() > 0) {
            out.write(nioBuffer);
        }
        buf.readerIndex(buf.readerIndex() + bytes);
    }

    @Override
    public void readBytes(ByteBuffer buf) {
        this.buf.readBytes(buf);
    }

    @Override
    public void readBytes(ByteBuf buf) {
        this.buf.readBytes(buf);
    }

    @Override
    public void readBytes(ByteBuf buf, int bytes) {
        this.buf.readBytes(buf, bytes);
    }

    // ReadableByteChannel

    @Override
    public void close() {
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public synchronized int read(ByteBuffer dst) throws IOException {
        int available = buf.readableBytes();
        if (available == 0) {
            return -1;
        }
        int toTransfer = Math.min(dst.remaining(), available);
        if (buf.hasArray()) {
            dst.put(buf.array(), buf.arrayOffset() + buf.readerIndex(), toTransfer);
        } else {
            dst.put(buf.nioBuffer());
        }

        buf.readerIndex(buf.readerIndex() + toTransfer);

        return toTransfer;
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }

    @Override
    public synchronized long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        if (!buf.isReadable()) {
            return -1;
        }

        long read = 0;
        for (; offset <= length; offset++) {
            int r = read(dsts[offset]);
            if (r < 0) {
                break;
            }
            read += r;
        }
        return read;
    }

    // end ReadableByteChannel

    @Override
    @Nullable
    public Boolean readNullableBoolean() {
        int b = readByte();
        return b == BufferConstants.BOOLEAN_NULL ? null : b != BufferConstants.BOOLEAN_FALSE;
    }

    @Override
    @Nullable
    public Byte readNullableByte() {
        int i = readShiftedVarInt();
        if (i == -1) {
            return null;
        } else {
            return (byte) i;
        }
    }

    @Override
    @Nullable
    public Short readNullableShort() {
        int i = readShiftedVarInt();
        if (i == -1) {
            return null;
        } else {
            return (short) i;
        }
    }

    @Override
    @Nullable
    public Character readNullableChar() {
        int i = readShiftedVarInt();
        if (i == -1) {
            return null;
        } else {
            return (char) i;
        }
    }

    @Override
    @Nullable
    public Integer readNullableInt() {
        long l = readShiftedVarLong();
        if (l < 0) {
            return null;
        } else {
            return (int) l;
        }
    }

    @Override
    @Nullable
    public Float readNullableFloat() {
        long l = readShiftedVarLong();
        if (l < 0) {
            return null;
        } else {
            return Float.intBitsToFloat((int) l);
        }
    }

    @Override
    @Nullable
    public Long readNullableLong() {
        byte b = readByte();
        if ((b & 0b0100_0000) != 0) {
            return null;
        } else {
            return readNullableLongNonnull(b);
        }
    }

    private long readNullableLongNonnull(byte b) {
        long l = b & 0b0011_1111;
        if ((b & 0b1000_0000) != 0) {
            l <<= 6;

            b = readByte();
            l |= b & 0b0111_1111;

            while ((b & 0b1000_0000) == 0) {
                b = readByte();
                l <<= 7;
                l |= b & 0b0111_1111;
            }

        }

        return l;
    }

    @Override
    @Nullable
    public Double readNullableDouble() {
        byte b = readByte();
        if ((b & 0b0100_0000) != 0) {
            return null;
        } else {
            return Double.longBitsToDouble(readNullableLongNonnull(b));
        }
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
    public int readUnsignedByte() {
        return buf.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return buf.readShort();
    }

    @Override
    public int readUnsignedShort() {
        return buf.readUnsignedShort();
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
    public int readUnsignedMedium() {
        return buf.readUnsignedMedium();
    }

    @Override
    public int readInt() {
        return buf.readInt();
    }

    @Override
    public long readUnsignedInt() {
        return buf.readUnsignedInt();
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

        for (int shift = 0; shift < 35; shift += 7) {
            byte read = readByte();
            result |= (read & 0x7f) << shift;

            if ((read & 0x80) == 0x80) {
                return result;
            }
        }
        throw new RuntimeException("VarInt too big");
    }

    @Override
    public int readShiftedVarInt() {
        return readVarInt() - 1;
    }

    @Override
    public long readVarLong() {
        long result = 0;

        for (int shift = 0; shift < 70; shift += 7) {
            byte read = readByte();
            result |= (read & 0x7fL) << shift;

            if ((read & 0x80) == 0x80) {
                return result;
            }
        }
        throw new RuntimeException("VarLong too big");
    }

    @Override
    public long readShiftedVarLong() {
        return readVarLong() + 1;
    }

    @Override
    public void readFully(byte[] b) {
        buf.readBytes(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) {
        buf.readBytes(b, off, len);
    }

    @Override
    public int skipBytes(int n) {
        if (n > buf.readableBytes()) {
            n = buf.readableBytes();
        }
        buf.skipBytes(n);
        return n;
    }

    @Override
    @Deprecated
    public String readLine() {
        StringBuilder sb = new StringBuilder();

        loop:
        while (buf.isReadable()) {
            char c = (char) readByte();
            switch (c) {
                case '\r':
                    if (buf.isReadable() && buf.getByte(buf.readerIndex()) == '\n') {
                        buf.skipBytes(1);
                    }
                    // fall-through
                case '\n':
                    break loop;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    @Override
    public String readUTF() {
        try {
            return DataInputStream.readUTF(this);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int read() throws IOException {
        if (buf.isReadable()) {
            return buf.readUnsignedByte();
        } else {
            return -1;
        }
    }

    @Override
    public int read(@Nonnull byte[] b, int off, int len) throws IOException {
        if (len > buf.readableBytes()) {
            len = buf.readableBytes();
        }
        buf.readBytes(b, off, len);
        return len;
    }

    // misc stuff

    @Override
    public String readString() {
        int nBytes = readShiftedVarInt();
        if (nBytes < 0) {
            return null;
        } else if (nBytes == 0) {
            return "";
        } else {
            int idx = buf.readerIndex();
            buf.readerIndex(idx + nBytes);
            return buf.toString(idx, nBytes, StandardCharsets.UTF_8);
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
    @Nullable
    public <E extends Enum<E>> E readEnum(Class<E> clazz) {
        int i = readShiftedVarInt();
        return i < 0 ? null : EnumUtils.byOrdinal(clazz, i);
    }

    @Override
    public BitSet readBitSet() {
        int len = readShiftedVarInt();
        if (len < 0) {
            return null;
        } else {
            ByteBuffer nioBuffer = buf.internalNioBuffer(buf.readerIndex(), len);
            buf.readerIndex(buf.readerIndex() + len);
            return BitSet.valueOf(nioBuffer);
        }
    }

    @Override
    public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> enumClass) {
        int readByte = buf.readByte();
        if (readByte == 0) {
            return null;
        } else {
            E[] universe = EnumUtils.getEnumConstantsShared(enumClass);
            EnumSet<E> set = EnumSet.noneOf(enumClass);

            int numEnums = universe.length;

            for (int i = 0, m = Math.min(7, numEnums); i < m; i++) {
                if ((readByte & 2 << i) != 0) {
                    set.add(universe[i]);
                }
            }

            for (int i = 7; i < numEnums; i++) {
                if ((i & 7) == 7) {
                    readByte = buf.readByte();
                }
                if ((readByte & 1 << ((i + 1) & 7)) != 0) {
                    set.add(universe[i]);
                }
            }
            return set;
        }
    }

    @Override
    public IBlockState readBlockState() {
        int id = readShiftedVarInt();
        if (id < 0) {
            return null;
        } else {
            return Block.getStateById(id);
        }
    }

    @Nullable
    @Override
    public IBlockState readRichBlockState() {
        int stateId = readShiftedVarInt();
        if (stateId < 0) {
            return null;
        } else {
            IBlockState state = Block.getStateById(stateId);
            BlockStateContainer stateContainer = state.getBlock().getBlockState();

            while (true) {
                String property = readString();
                if (property == null) {
                    return state;
                }
                state = setProperty(state, stateContainer.getProperty(property));
            }
        }


    }

    private <T extends Comparable<T>> IBlockState setProperty(IBlockState state, @Nullable IProperty<T> prop) {
        if (prop != null) {
            T value = null;
            if (prop.getValueClass().isEnum()) {
                //noinspection unchecked,rawtypes,ConstantConditions
                value = (T) readEnum((Class) prop.getValueClass());
            } else if (prop.getValueClass() == Integer.class) {
                //noinspection unchecked
                value = (T) (Integer) readVarInt();
            } else if (prop.getValueClass() == Boolean.class) {
                //noinspection unchecked
                value = (T) (Boolean) readBoolean();
            } else {
                String s = readString();
                for (T v : prop.getAllowedValues()) {
                    if (prop.getName(v).equals(s)) {
                        value = v;
                        break;
                    }
                }
            }
            if (value != null) {
                return state.withProperty(prop, value);
            }
        }
        return state;
    }

    @Override
    public <T extends IForgeRegistryEntry<T>> T readRegistryEntry(IForgeRegistry<T> registry) {
        int id = readShiftedVarInt();
        if (id < 0) {
            return null;
        } else {
            return ((FMLControlledNamespacedRegistry<T>) registry).getObjectById(id);
        }
    }

    @Nullable
    @Override
    public BlockPos readBlockPos() {
        long l = readLong();
        if ((l & 0xF000_0000_0000_0000L) != 0) {
            return null;
        }
        int x = (int) l << 32 - 26 >> 32 - 26; // sign-extend
        int z = (int) (l << 64 - 26 - 26 >> 64 - 26); // sign-extend and shift-right by 26
        int y = (int) (l >>> 64 - 26 - 26) & 0xFF;
        return new BlockPos(x, y, z);
    }

    @Override
    public ChunkPos readChunkPos() {
        int x = readMedium();
        if (x == 0x40_0000) {
            return null;
        } else {
            int z = readMedium();
            return new ChunkPos(x, z);
        }
    }

    @Nullable
    @Override
    public Block readBlock() {
        int id = readShiftedVarInt();
        if (id < 0) {
            return null;
        } else {
            return Block.getBlockById(id);
        }
    }

    @Override
    public Item readItem() {
        int id = readShiftedVarInt();
        if (id < 0) {
            return null;
        } else {
            return Item.REGISTRY.getObjectById(id);
        }
    }

    @Override
    public ItemStack readItemStack() {
        Item item = readItem();
        if (item == null) {
            return null;
        } else {
            int dmg = readVarInt();
            int size = readByte();
            //noinspection ConstantConditions
            ItemStack stack = new ItemStack(item, size & 0x7F, dmg);
            if ((size & 0x80) != 0) {
                //noinspection ConstantConditions
                stack.setTagCompound(readNBT());
            }
            return stack;
        }
    }

    @Override
    public FluidStack readFluidStack() {
        int id = readShiftedVarInt();
        if (id < 0) {
            return null;
        } else {
            int amount = readVarInt();
            @SuppressWarnings("deprecation")
            FluidStack stack = new FluidStack(FluidRegistry.getFluid(id), amount >>> 1);
            if ((amount & 0x1) != 0) {
                stack.tag = readNBT();
            }
            return stack;
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
                    NBTBase tag;
                    try {
                        tag = (NBTBase) CommonMethodHandles.newNbt.invokeExact((byte) id);
                        CommonMethodHandles.nbtBaseRead.invokeExact((NBTBase) tag, (DataInput) this, 1, NBTSizeTracker.INFINITE);
                    } catch (IOException io) {
                        throw io;
                    } catch (Throwable x) {
                        throw Throwables.propagate(x);
                    }

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
    public int nextFieldId() {
        return readVarInt();
    }

    @Override
    public <T_DATA, T_VAL, T_COM> void apply(TypeSyncer<T_VAL, T_COM, T_DATA> syncer, Object obj, PropertyAccess<T_VAL> property, Object cObj, PropertyAccess<T_COM> companion) {
        syncer.apply(this, obj, property, cObj, companion);
    }

}

package de.take_weiland.mods.commons.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.io.DataInput;
import java.io.InputStream;
import java.util.BitSet;
import java.util.EnumSet;

/**
 * @author diesieben07
 */
public interface MCDataInput {

    ByteBuf getNettyBuffer();

    InputStream asInputStream();
    DataInput asDataInput();

    boolean readBoolean();

    byte readByte();

    short readShort();

    char readChar();

    int readMedium();

    int readInt();

    long readLong();

    float readFloat();

    double readDouble();

    int readVarInt();

    int readUnsignedByte();

    int readUnsignedShort();

    int readUnsignedMedium();

    long readUnsignedInt();

    String readString();

    <E extends Enum<E>> E readEnum(Class<E> enumClass);

    ItemStack readItemStack();

    Item readItem();

    Block readBlock();

    NBTTagCompound readNBT();

    BitSet readBitSet();

    <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> enumClass);

    byte[] readBytes();

    short[] readShorts();

    char[] readChars();

    int[] readInts();

    long[] readLongs();

    float[] readFloats();

    double[] readDoubles();

}

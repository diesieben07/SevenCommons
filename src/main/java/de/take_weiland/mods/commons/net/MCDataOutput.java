package de.take_weiland.mods.commons.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.io.DataOutput;
import java.io.OutputStream;
import java.util.BitSet;
import java.util.EnumSet;

/**
 * @author diesieben07
 */
public interface MCDataOutput {

    ByteBuf getNettyBuffer();

    OutputStream asOutputStream();
    DataOutput asDataOutput();

    void writeBoolean(boolean b);

    void writeByte(int b);

    void writeShort(int s);

    void writeChar(int c);

    void writeMedium(int m);

    void writeInt(int i);

    void writeLong(long l);

    void writeFloat(float f);

    void writeDouble(double d);

    void writeVarInt(int i);

    void writeString(String s);

    <E extends Enum<E>> void writeEnum(E e);

    void writeItemStack(ItemStack stack);

    void writeItem(Item item);

    void writeBlock(Block block);

    void writeNBT(NBTTagCompound nbt);

    void writeBitSet(BitSet set);

    <E extends Enum<E>> void writeEnumSet(EnumSet<E> set);

    default void writeBytes(byte[] arr) {
        if (arr == null) {
            writeVarInt(-1);
        } else {
            writeBytes(arr, 0, arr.length);
        }
    }

    default void writeShorts(short[] arr) {
        if (arr == null) {
            writeVarInt(-1);
        } else {
            writeShorts(arr, 0, arr.length);
        }
    }

    default void writeChars(char[] arr) {
        if (arr == null) {
            writeVarInt(-1);
        } else {
            writeChars(arr, 0, arr.length);
        }
    }

    default void writeInts(int[] arr) {
        if (arr == null) {
            writeVarInt(-1);
        } else {
            writeInts(arr, 0, arr.length);
        }
    }

    default void writeLongs(long[] arr) {
        if (arr == null) {
            writeVarInt(-1);
        } else {
            writeLongs(arr, 0, arr.length);
        }
    }

    default void writeFloats(float[] arr) {
        if (arr == null) {
            writeVarInt(-1);
        } else {
            writeFloats(arr, 0, arr.length);
        }
    }

    default void writeDoubles(double[] arr) {
        if (arr == null) {
            writeVarInt(-1);
        } else {
            writeDoubles(arr, 0, arr.length);
        }
    }

    void writeBytes(byte[] arr, int off, int len);
    void writeShorts(short[] arr, int off, int len);
    void writeChars(char[] arr, int off, int len);
    void writeInts(int[] arr, int off, int len);
    void writeLongs(long[] arr, int off, int len);
    void writeFloats(float[] arr, int off, int len);
    void writeDoubles(double[] arr, int off, int len);

}

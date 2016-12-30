package de.take_weiland.mods.commons.internal.serialize;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.serialize.Deserializer;
import de.take_weiland.mods.commons.serialize.Serializer;

/**
 * @author diesieben07
 */
public final class DefaultSerializers {

    @Serializer
    public static void writeZ(MCDataOutput out, boolean b) {
        out.writeBoolean(b);
    }

    @Deserializer
    public static boolean readZ(MCDataInput in) {
        return in.readBoolean();
    }

    @Serializer
    public static void writeB(MCDataOutput out, byte b) {
        out.writeByte(b);
    }

    @Deserializer
    public static byte readB(MCDataInput in) {
        return in.readByte();
    }

    @Serializer
    public static void writeS(MCDataOutput out, short b) {
        out.writeShort(b);
    }

    @Deserializer
    public static short readS(MCDataInput in) {
        return in.readShort();
    }

    @Serializer
    public static void writeC(MCDataOutput out, char b) {
        out.writeChar(b);
    }

    @Deserializer
    public static char readC(MCDataInput in) {
        return in.readChar();
    }

    @Serializer
    public static void writeI(MCDataOutput out, int b) {
        out.writeInt(b);
    }

    @Deserializer
    public static int readI(MCDataInput in) {
        return in.readInt();
    }

    @Serializer
    public static void writeL(MCDataOutput out, long b) {
        out.writeLong(b);
    }

    @Deserializer
    public static long readL(MCDataInput in) {
        return in.readLong();
    }

    @Serializer
    public static void writeF(MCDataOutput out, float b) {
        out.writeFloat(b);
    }

    @Deserializer
    public static float readF(MCDataInput in) {
        return in.readFloat();
    }

    @Serializer
    public static void writeD(MCDataOutput out, double b) {
        out.writeDouble(b);
    }

    @Deserializer
    public static double readD(MCDataInput in) {
        return in.readDouble();
    }

}

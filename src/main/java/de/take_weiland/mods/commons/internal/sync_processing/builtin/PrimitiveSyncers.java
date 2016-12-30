package de.take_weiland.mods.commons.internal.sync_processing.builtin;

import de.take_weiland.mods.commons.sync.EqualityCheck;
import io.netty.buffer.ByteBuf;

/**
 * @author diesieben07
 */
public class PrimitiveSyncers {

    @EqualityCheck
    public static boolean eq(boolean a, boolean b) {
        return a == b;
    }

    public static void serialize(boolean b, ByteBuf out) {
        out.writeBoolean(b);
    }

    public static boolean deserialize(ByteBuf in) {
        return in.readBoolean();
    }

    @EqualityCheck
    public static boolean eq(byte a, byte b) {
        return a == b;
    }

    @EqualityCheck
    public static boolean eq(short a, short b) {
        return a == b;
    }

    @EqualityCheck
    public static boolean eq(char a, char b) {
        return a == b;
    }

    @EqualityCheck
    public static boolean eq(int a, int b) {
        return a == b;
    }

    @EqualityCheck
    public static boolean eq(long a, long b) {
        return a == b;
    }

    @EqualityCheck
    public static boolean eq(float a, float b) {
        return a == b;
    }

    @EqualityCheck
    public static boolean eq(double a, double b) {
        return a == b;
    }

}
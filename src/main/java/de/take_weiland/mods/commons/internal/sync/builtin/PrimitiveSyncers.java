package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;

enum BooleanSyncer implements Syncer.ForImmutable<Boolean> {

    INSTANCE;

    @Override
    public Boolean decode(MCDataInput in) {
        return in.readBoolean();
    }

    @Override
    public void encode(Boolean val, MCDataOutput out) {
        out.writeBoolean(val);
    }

    @Override
    public Class<Boolean> companionType() {
        return boolean.class;
    }
}

enum ByteSyncer implements Syncer.ForImmutable<Byte> {

    INSTANCE;

    @Override
    public Byte decode(MCDataInput in) {
        return in.readByte();
    }

    @Override
    public void encode(Byte val, MCDataOutput out) {
        out.writeByte(val);
    }

    @Override
    public Class<Byte> companionType() {
        return byte.class;
    }
}

enum ShortSyncer implements Syncer.ForImmutable<Short> {

    INSTANCE;

    @Override
    public Short decode(MCDataInput in) {
        return in.readShort();
    }

    @Override
    public void encode(Short val, MCDataOutput out) {
        out.writeShort(val);
    }

    @Override
    public Class<Short> companionType() {
        return short.class;
    }
}

enum CharSyncer implements Syncer.ForImmutable<Character> {

    INSTANCE;

    @Override
    public Character decode(MCDataInput in) {
        return in.readChar();
    }

    @Override
    public void encode(Character val, MCDataOutput out) {
        out.writeChar(val);
    }

    @Override
    public Class<Character> companionType() {
        return char.class;
    }
}

enum IntSyncer implements Syncer.ForImmutable<Integer> {

    INSTANCE;

    @Override
    public Integer decode(MCDataInput in) {
        return in.readInt();
    }

    @Override
    public void encode(Integer val, MCDataOutput out) {
        out.writeInt(val);
    }
    @Override
    public Class<Integer> companionType() {
        return int.class;
    }
}

enum LongSyncer implements Syncer.ForImmutable<Long> {

    INSTANCE;

    @Override
    public Long decode(MCDataInput in) {
        return in.readLong();
    }

    @Override
    public void encode(Long val, MCDataOutput out) {
        out.writeLong(val);
    }

    @Override
    public Class<Long> companionType() {
        return long.class;
    }
}

enum FloatSyncer implements Syncer.ForImmutable<Float> {

    INSTANCE;

    @Override
    public Float decode(MCDataInput in) {
        return in.readFloat();
    }

    @Override
    public void encode(Float val, MCDataOutput out) {
        out.writeFloat(val);
    }

    @Override
    public Class<Float> companionType() {
        return float.class;
    }
}

enum DoubleSyncer implements Syncer.ForImmutable<Double> {

    INSTANCE;

    @Override
    public Double decode(MCDataInput in) {
        return in.readDouble();
    }

    @Override
    public void encode(Double val, MCDataOutput out) {
        out.writeDouble(val);
    }

    @Override
    public Class<Double> companionType() {
        return double.class;
    }
}

enum BooleanBoxSyncer implements Syncer.ForImmutable<Boolean> {

    INSTANCE;

    @Override
    public Boolean decode(MCDataInput in) {
        return in.readBooleanBox();
    }

    @Override
    public void encode(Boolean val, MCDataOutput out) {
        out.writeBooleanBox(val);
    }

    @Override
    public Class<Boolean> companionType() {
        return Boolean.class;
    }
}

enum ByteBoxSyncer implements Syncer.ForImmutable<Byte> {

    INSTANCE;

    @Override
    public Byte decode(MCDataInput in) {
        return in.readByteBox();
    }

    @Override
    public void encode(Byte val, MCDataOutput out) {
        out.writeByteBox(val);
    }

    @Override
    public Class<Byte> companionType() {
        return Byte.class;
    }
}

enum ShortBoxSyncer implements Syncer.ForImmutable<Short> {

    INSTANCE;

    @Override
    public Short decode(MCDataInput in) {
        return in.readShortBox();
    }

    @Override
    public void encode(Short val, MCDataOutput out) {
        out.writeShortBox(val);
    }

    @Override
    public Class<Short> companionType() {
        return Short.class;
    }
}

enum CharBoxSyncer implements Syncer.ForImmutable<Character> {

    INSTANCE;

    @Override
    public Character decode(MCDataInput in) {
        return in.readCharBox();
    }

    @Override
    public void encode(Character val, MCDataOutput out) {
        out.writeCharBox(val);
    }

    @Override
    public Class<Character> companionType() {
        return Character.class;
    }
}

enum IntBoxSyncer implements Syncer.ForImmutable<Integer> {

    INSTANCE;

    @Override
    public Integer decode(MCDataInput in) {
        return in.readIntBox();
    }

    @Override
    public void encode(Integer val, MCDataOutput out) {
        out.writeIntBox(val);
    }

    @Override
    public Class<Integer> companionType() {
        return Integer.class;
    }
}

enum LongBoxSyncer implements Syncer.ForImmutable<Long> {

    INSTANCE;

    @Override
    public Long decode(MCDataInput in) {
        return in.readLongBox();
    }

    @Override
    public void encode(Long val, MCDataOutput out) {
        out.writeLongBox(val);
    }

    @Override
    public Class<Long> companionType() {
        return Long.class;
    }
}

enum FloatBoxSyncer implements Syncer.ForImmutable<Float> {

    INSTANCE;

    @Override
    public Float decode(MCDataInput in) {
        return in.readFloatBox();
    }

    @Override
    public void encode(Float val, MCDataOutput out) {
        out.writeFloatBox(val);
    }

    @Override
    public Class<Float> companionType() {
        return Float.class;
    }
}

enum DoubleBoxSyncer implements Syncer.ForImmutable<Double> {

    INSTANCE;

    @Override
    public Double decode(MCDataInput in) {
        return in.readDoubleBox();
    }

    @Override
    public void encode(Double val, MCDataOutput out) {
        out.writeDoubleBox(val);
    }

    @Override
    public Class<Double> companionType() {
        return Double.class;
    }
}

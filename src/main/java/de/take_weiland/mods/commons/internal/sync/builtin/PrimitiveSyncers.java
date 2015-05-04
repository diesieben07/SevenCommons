package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;

enum BooleanSyncer implements Syncer.ForImmutable<Boolean> {

    INSTANCE;

    @Override
    public Class<Boolean> getCompanionType() {
        return boolean.class;
    }

    @Override
    public void write(Boolean value, MCDataOutput out) {
        out.writeBoolean(value);
    }

    @Override
    public Boolean read(MCDataInput in) {
        return in.readBoolean();
    }

}
enum ByteSyncer implements Syncer.ForImmutable<Byte> {

    INSTANCE;

    @Override
    public Class<Byte> getCompanionType() {
        return byte.class;
    }

    @Override
    public void write(Byte value, MCDataOutput out) {
        out.writeByte(value);
    }

    @Override
    public Byte read(MCDataInput in) {
        return in.readByte();
    }

}
enum ShortSyncer implements Syncer.ForImmutable<Short> {

    INSTANCE;

    @Override
    public Class<Short> getCompanionType() {
        return short.class;
    }

    @Override
    public void write(Short value, MCDataOutput out) {
        out.writeShort(value);
    }

    @Override
    public Short read(MCDataInput in) {
        return in.readShort();
    }

}

enum CharSyncer implements Syncer.ForImmutable<Character> {

    INSTANCE;

    @Override
    public Class<Character> getCompanionType() {
        return char.class;
    }

    @Override
    public void write(Character value, MCDataOutput out) {
        out.writeChar(value);
    }

    @Override
    public Character read(MCDataInput in) {
        return in.readChar();
    }
}

enum IntSyncer implements Syncer.ForImmutable<Integer> {
    INSTANCE;

    @Override
    public Class<Integer> getCompanionType() {
        return int.class;
    }

    @Override
    public void write(Integer value, MCDataOutput out) {
        out.writeInt(value);
    }

    @Override
    public Integer read(MCDataInput in) {
        return in.readInt();
    }
}

enum LongSyncer implements Syncer.ForImmutable<Long> {

    INSTANCE;

    @Override
    public Class<Long> getCompanionType() {
        return long.class;
    }

    @Override
    public void write(Long value, MCDataOutput out) {
        out.writeLong(value);
    }

    @Override
    public Long read(MCDataInput in) {
        return in.readLong();
    }
}

enum FloatSyncer implements Syncer.ForImmutable<Float> {

    INSTANCE;

    @Override
    public Class<Float> getCompanionType() {
        return float.class;
    }

    @Override
    public void write(Float value, MCDataOutput out) {
        out.writeFloat(value);
    }

    @Override
    public Float read(MCDataInput in) {
        return in.readFloat();
    }
}

enum DoubleSyncer implements Syncer.ForImmutable<Double> {

    INSTANCE;

    @Override
    public Class<Double> getCompanionType() {
        return double.class;
    }

    @Override
    public void write(Double value, MCDataOutput out) {
        out.writeDouble(value);
    }

    @Override
    public Double read(MCDataInput in) {
        return in.readDouble();
    }
}

enum BooleanBoxSyncer implements Syncer.ForImmutable<Boolean> {

    INSTANCE;

    @Override
    public Class<Boolean> getCompanionType() {
        return boolean.class;
    }

    @Override
    public void write(Boolean value, MCDataOutput out) {
        out.writeBooleanBox(value);
    }

    @Override
    public Boolean read(MCDataInput in) {
        return in.readBoolean();
    }

}
enum ByteBoxSyncer implements Syncer.ForImmutable<Byte> {

    INSTANCE;

    @Override
    public Class<Byte> getCompanionType() {
        return byte.class;
    }

    @Override
    public void write(Byte value, MCDataOutput out) {
        out.writeByteBox(value);
    }

    @Override
    public Byte read(MCDataInput in) {
        return in.readByte();
    }

}
enum ShortBoxSyncer implements Syncer.ForImmutable<Short> {

    INSTANCE;

    @Override
    public Class<Short> getCompanionType() {
        return short.class;
    }

    @Override
    public void write(Short value, MCDataOutput out) {
        out.writeShortBox(value);
    }

    @Override
    public Short read(MCDataInput in) {
        return in.readShort();
    }

}

enum CharBoxSyncer implements Syncer.ForImmutable<Character> {

    INSTANCE;

    @Override
    public Class<Character> getCompanionType() {
        return char.class;
    }

    @Override
    public void write(Character value, MCDataOutput out) {
        out.writeCharBox(value);
    }

    @Override
    public Character read(MCDataInput in) {
        return in.readChar();
    }
}

enum IntBoxSyncer implements Syncer.ForImmutable<Integer> {
    INSTANCE;

    @Override
    public Class<Integer> getCompanionType() {
        return int.class;
    }

    @Override
    public void write(Integer value, MCDataOutput out) {
        out.writeIntBox(value);
    }

    @Override
    public Integer read(MCDataInput in) {
        return in.readInt();
    }
}

enum LongBoxSyncer implements Syncer.ForImmutable<Long> {

    INSTANCE;

    @Override
    public Class<Long> getCompanionType() {
        return long.class;
    }

    @Override
    public void write(Long value, MCDataOutput out) {
        out.writeLongBox(value);
    }

    @Override
    public Long read(MCDataInput in) {
        return in.readLong();
    }
}

enum FloatBoxSyncer implements Syncer.ForImmutable<Float> {

    INSTANCE;

    @Override
    public Class<Float> getCompanionType() {
        return float.class;
    }

    @Override
    public void write(Float value, MCDataOutput out) {
        out.writeFloatBox(value);
    }

    @Override
    public Float read(MCDataInput in) {
        return in.readFloat();
    }
}

enum DoubleBoxSyncer implements Syncer.ForImmutable<Double> {

    INSTANCE;

    @Override
    public Class<Double> getCompanionType() {
        return double.class;
    }

    @Override
    public void write(Double value, MCDataOutput out) {
        out.writeDoubleBox(value);
    }

    @Override
    public Double read(MCDataInput in) {
        return in.readDouble();
    }
}
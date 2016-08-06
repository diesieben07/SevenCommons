package de.take_weiland.mods.commons.internal.sync_olds.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.sync.TypeSyncer;

enum BooleanSyncer implements TypeSyncer.ForImmutable<Boolean> {

    INSTANCE;

    @Override
    public Change<Boolean> check(Object obj, PropertyAccess<Boolean> property, Object cObj, PropertyAccess<Boolean> companion) {
        boolean value = property.getBoolean(obj);
        if (value == companion.getBoolean(cObj)) {
            return noChange();
        } else {
            companion.setBoolean(cObj, value);
            return newValue(value);
        }
    }

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

enum ByteSyncer implements TypeSyncer.ForImmutable<Byte> {

    INSTANCE;

    @Override
    public Change<Byte> check(Object obj, PropertyAccess<Byte> property, Object cObj, PropertyAccess<Byte> companion) {
        byte value = property.getByte(obj);
        if (value == companion.getByte(cObj)) {
            return noChange();
        } else {
            companion.setByte(cObj, value);
            return newValue(value);
        }
    }

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

enum ShortSyncer implements TypeSyncer.ForImmutable<Short> {

    INSTANCE;

    @Override
    public Change<Short> check(Object obj, PropertyAccess<Short> property, Object cObj, PropertyAccess<Short> companion) {
        short value = property.getShort(obj);
        if (value == companion.getShort(cObj)) {
            return noChange();
        } else {
            companion.setShort(cObj, value);
            return newValue(value);
        }
    }

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

enum CharSyncer implements TypeSyncer.ForImmutable<Character> {

    INSTANCE;

    @Override
    public Change<Character> check(Object obj, PropertyAccess<Character> property, Object cObj, PropertyAccess<Character> companion) {
        char value = property.getChar(obj);
        if (value == companion.getChar(cObj)) {
            return noChange();
        } else {
            companion.setChar(cObj, value);
            return newValue(value);
        }
    }

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

enum IntSyncer implements TypeSyncer.ForImmutable<Integer> {

    INSTANCE;

    @Override
    public Change<Integer> check(Object obj, PropertyAccess<Integer> property, Object cObj, PropertyAccess<Integer> companion) {
        int value = property.getInt(obj);
        if (value == companion.getInt(cObj)) {
            return noChange();
        } else {
            companion.setInt(cObj, value);
            return newValue(value);
        }
    }

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

enum LongSyncer implements TypeSyncer.ForImmutable<Long> {

    INSTANCE;

    @Override
    public Change<Long> check(Object obj, PropertyAccess<Long> property, Object cObj, PropertyAccess<Long> companion) {
        long value = property.getLong(obj);
        if (value == companion.getLong(cObj)) {
            return noChange();
        } else {
            companion.setLong(cObj, value);
            return newValue(value);
        }
    }

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

enum FloatSyncer implements TypeSyncer.ForImmutable<Float> {

    INSTANCE;

    @Override
    public Change<Float> check(Object obj, PropertyAccess<Float> property, Object cObj, PropertyAccess<Float> companion) {
        float value = property.getFloat(obj);
        if (value == companion.getFloat(cObj)) {
            return noChange();
        } else {
            companion.setFloat(cObj, value);
            return newValue(value);
        }
    }

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

enum DoubleSyncer implements TypeSyncer.ForImmutable<Double> {

    INSTANCE;

    @Override
    public Change<Double> check(Object obj, PropertyAccess<Double> property, Object cObj, PropertyAccess<Double> companion) {
        double value = property.getDouble(obj);
        if (value == companion.getDouble(cObj)) {
            return noChange();
        } else {
            companion.setDouble(cObj, value);
            return newValue(value);
        }
    }

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

enum BooleanBoxSyncer implements TypeSyncer.ForImmutable<Boolean> {

    INSTANCE;

    @Override
    public Boolean decode(MCDataInput in) {
        return in.readNullableBoolean();
    }

    @Override
    public void encode(Boolean val, MCDataOutput out) {
        out.writeNullableBoolean(val);
    }

    @Override
    public Class<Boolean> companionType() {
        return Boolean.class;
    }
}

enum ByteBoxSyncer implements TypeSyncer.ForImmutable<Byte> {

    INSTANCE;

    @Override
    public Byte decode(MCDataInput in) {
        return in.readNullableByte();
    }

    @Override
    public void encode(Byte val, MCDataOutput out) {
        out.writeNullableByte(val);
    }

    @Override
    public Class<Byte> companionType() {
        return Byte.class;
    }
}

enum ShortBoxSyncer implements TypeSyncer.ForImmutable<Short> {

    INSTANCE;

    @Override
    public Short decode(MCDataInput in) {
        return in.readNullableShort();
    }

    @Override
    public void encode(Short val, MCDataOutput out) {
        out.writeNullableShort(val);
    }

    @Override
    public Class<Short> companionType() {
        return Short.class;
    }
}

enum CharBoxSyncer implements TypeSyncer.ForImmutable<Character> {

    INSTANCE;

    @Override
    public Character decode(MCDataInput in) {
        return in.readNullableChar();
    }

    @Override
    public void encode(Character val, MCDataOutput out) {
        out.writeNullableChar(val);
    }

    @Override
    public Class<Character> companionType() {
        return Character.class;
    }
}

enum IntBoxSyncer implements TypeSyncer.ForImmutable<Integer> {

    INSTANCE;

    @Override
    public Integer decode(MCDataInput in) {
        return in.readNullableInt();
    }

    @Override
    public void encode(Integer val, MCDataOutput out) {
        out.writeNullableInt(val);
    }

    @Override
    public Class<Integer> companionType() {
        return Integer.class;
    }
}

enum LongBoxSyncer implements TypeSyncer.ForImmutable<Long> {

    INSTANCE;

    @Override
    public Long decode(MCDataInput in) {
        return in.readNullableLong();
    }

    @Override
    public void encode(Long val, MCDataOutput out) {
        out.writeNullableLong(val);
    }

    @Override
    public Class<Long> companionType() {
        return Long.class;
    }
}

enum FloatBoxSyncer implements TypeSyncer.ForImmutable<Float> {

    INSTANCE;

    @Override
    public Float decode(MCDataInput in) {
        return in.readNullableFloat();
    }

    @Override
    public void encode(Float val, MCDataOutput out) {
        out.writeNullableFloat(val);
    }

    @Override
    public Class<Float> companionType() {
        return Float.class;
    }
}

enum DoubleBoxSyncer implements TypeSyncer.ForImmutable<Double> {

    INSTANCE;

    @Override
    public Double decode(MCDataInput in) {
        return in.readNullableDouble();
    }

    @Override
    public void encode(Double val, MCDataOutput out) {
        out.writeNullableDouble(val);
    }

    @Override
    public Class<Double> companionType() {
        return Double.class;
    }
}

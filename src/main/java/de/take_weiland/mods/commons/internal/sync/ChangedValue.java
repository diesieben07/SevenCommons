package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * @author diesieben07
 */
public abstract class ChangedValue<T> {

    public final int fieldId;

    ChangedValue(int fieldId) {
        this.fieldId = fieldId;
    }

    public abstract void writeData(MCDataOutput out);

    public static abstract class OfRef<T> extends ChangedValue<T> {

        private final T v;

        public OfRef(int fieldId, T v) {
            super(fieldId);
            this.v = v;
        }

        public T get() {
            return v;
        }
    }

    public static final class OfInt<T> extends ChangedValue<T> {

        private final int v;

        public OfInt(int fieldId, int v) {
            super(fieldId);
            this.v = v;
        }

        public OfInt(int fieldId, float v) {
            super(fieldId);
            this.v = Float.floatToRawIntBits(v);
        }

        public int get() {
            return v;
        }

        public float getFloat() {
            return Float.intBitsToFloat(v);
        }

        @Override
        public void writeData(MCDataOutput out) {
            out.writeInt(v);
        }
    }

    public static final class OfLong<T> extends ChangedValue<T> {

        private final long v;

        public OfLong(int fieldId, long v) {
            super(fieldId);
            this.v = v;
        }

        public OfLong(int fieldId, double v) {
            super(fieldId);
            this.v = Double.doubleToRawLongBits(v);
        }

        public long get() {
            return v;
        }

        public double getDouble() {
            return Double.longBitsToDouble(v);
        }

        @Override
        public void writeData(MCDataOutput out) {
            out.writeLong(v);
        }
    }


}

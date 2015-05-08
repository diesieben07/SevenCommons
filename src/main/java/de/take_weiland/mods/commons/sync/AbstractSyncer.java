package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInput;

/**
 * @author diesieben07
 */
public abstract class AbstractSyncer<T_DATA, T_VAL> implements Syncer<T_DATA> {

    private final Object obj;
    private final PropertyAccess<Object, T_VAL> property;

    protected <OBJ> AbstractSyncer(OBJ obj, PropertyAccess<OBJ, T_VAL> property) {
        this.obj = obj;
        //noinspection unchecked
        this.property = (PropertyAccess<Object, T_VAL>) property;
    }

    protected abstract Change<T_DATA> check(T_VAL value);

    @Override
    public final Change<T_DATA> check() {
        return check(property.get(obj));
    }

    protected final T_VAL get() {
        return property.get(obj);
    }

    protected final void set(T_VAL value) {
        property.set(obj, value);
    }

    protected static <T_DATA> Change<T_DATA> noChange() {
        return Syncer.Change.noChange();
    }

    protected static <T_DATA> Change<T_DATA> newValue(T_DATA data) {
        return Syncer.Change.newValue(data);
    }

    public static abstract class WithCompanion<T_DATA, T_VAL, T_COM> extends AbstractSyncer<T_DATA, T_VAL> {

        protected T_COM companion;

        protected <OBJ> WithCompanion(OBJ obj, PropertyAccess<OBJ, T_VAL> property) {
            super(obj, property);
        }
    }

    public static abstract class ForImmutable<T_VAL> extends WithCompanion<T_VAL, T_VAL, T_VAL> {

        protected <OBJ> ForImmutable(OBJ obj, PropertyAccess<OBJ, T_VAL> property) {
            super(obj, property);
        }

        @Override
        protected Change<T_VAL> check(T_VAL value) {
            if (value == companion) {
                return noChange();
            } else {
                companion = value;
                return newValue(value);
            }
        }

        @Override
        public void apply(MCDataInput in) {
            set(decode(in));
        }

        @Override
        public void apply(T_VAL value) {
            set(value);
        }

        protected abstract T_VAL decode(MCDataInput in);
    }

}

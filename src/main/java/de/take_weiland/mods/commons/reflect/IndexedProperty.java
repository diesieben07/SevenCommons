package de.take_weiland.mods.commons.reflect;

import java.util.List;

/**
 * @author diesieben07
 */
public interface IndexedProperty<T> extends PropertyAccess<T> {

    T get(Object o, int i);

    void set(Object o, int i, T val);

    @Override
    default T get(Object o) {
        return get(((ObjectIndexWrapper) o).obj, ((ObjectIndexWrapper) o).idx);
    }

    @Override
    default void set(Object o, T val) {
        set(((ObjectIndexWrapper) o).obj, ((ObjectIndexWrapper) o).idx, val);
    }

    static Object wrappedIndex(Object o, int i) {
        return new ObjectIndexWrapper(o, i);
    }

    final class ObjectIndexWrapper {

        final Object obj;
        final int idx;

        public ObjectIndexWrapper(Object obj, int idx) {
            this.obj = obj;
            this.idx = idx;
        }
    }

    static <T> IndexedProperty<T> indexed(Property<? extends List<T>> listProperty) {
        return new IndexedProperty<T>() {
            @Override
            public T get(Object o, int i) {
                return listProperty.get(o).get(i);
            }

            @Override
            public void set(Object o, int i, T val) {
                listProperty.get(o).set(i, val);
            }
        };
    }

}

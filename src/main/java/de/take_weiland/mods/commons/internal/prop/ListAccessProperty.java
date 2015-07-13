package de.take_weiland.mods.commons.internal.prop;

import de.take_weiland.mods.commons.reflect.PropertyAccess;

import java.util.List;

/**
 * @author diesieben07
 */
public final class ListAccessProperty<T> implements PropertyAccess<T> {

    private final PropertyAccess<? extends List<T>> listProp;

    public ListAccessProperty(PropertyAccess<? extends List<T>> listProp) {
        this.listProp = listProp;
    }

    @Override
    public T get(Object o) {
        ObjectIntBox box = (ObjectIntBox) o;
        List<T> list = listProp.get(box.obj);
        return list.get(box.idx);
    }

    @Override
    public void set(Object o, T val) {
        ObjectIntBox box = (ObjectIntBox) o;
        listProp.get(box.obj).set(box.idx, val);
    }

    public static final class ObjectIntBox {

        final Object obj;
        final int idx;

        public ObjectIntBox(Object obj, int idx) {
            this.obj = obj;
            this.idx = idx;
        }
    }
}

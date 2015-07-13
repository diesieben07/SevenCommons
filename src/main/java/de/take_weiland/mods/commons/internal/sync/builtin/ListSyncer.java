package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.sync.Syncer;
import gnu.trove.map.TIntObjectMap;

import java.util.List;

/**
 * @author diesieben07
 */
public class ListSyncer<E, VALUE_COMP> implements Syncer<List<E>, List<VALUE_COMP>, TIntObjectMap<E>> {

    private final Syncer<E, VALUE_COMP, ?> valueSyncer;

    public ListSyncer(Syncer<E, VALUE_COMP, ?> valueSyncer) {
        this.valueSyncer = valueSyncer;
    }

    @Override
    public Class<List<VALUE_COMP>> companionType() {
        //noinspection unchecked
        return (Class<List<VALUE_COMP>>) (Object) List.class;
    }

    @Override
    public Change<TIntObjectMap<E>> check(Object obj, PropertyAccess<List<E>> property, Object cObj, PropertyAccess<List<VALUE_COMP>> companion) {
        List<E> list = property.get(obj);
        List<VALUE_COMP> compList = companion.get(cObj);
        PropertyIntBox<E> box = new PropertyIntBox<>(property, obj, 0);
    }

    @Override
    public void encode(List<E> es, MCDataOutput out) {

    }

    @Override
    public void apply(List<E> es, Object obj, PropertyAccess<List<E>> property, Object cObj, PropertyAccess<List<E>> companion) {

    }

    @Override
    public void apply(MCDataInput in, Object obj, PropertyAccess<List<E>> property, Object cObj, PropertyAccess<List<E>> companion) {

    }

    enum ListAccessProperty implements PropertyAccess<Object> {

        INSTANCE;

        @Override
        public Object get(Object o) {
            PropertyIntBox<?> box = (PropertyIntBox<?>) o;
            return box.property.get(box.obj).get(box.idx);
        }

        @Override
        public void set(Object o, Object val) {
            @SuppressWarnings("unchecked")
            PropertyIntBox<Object> box = (PropertyIntBox<Object>) o;
            box.property.get(box.obj).set(box.idx, val);
        }
    }

    private static final class PropertyIntBox<T> {

        final PropertyAccess<? extends List<T>> property;
        final Object obj;
        int idx;

        PropertyIntBox(PropertyAccess<? extends List<T>> property, Object obj, int idx) {
            this.property = property;
            this.obj = obj;
            this.idx = idx;
        }
    }


}

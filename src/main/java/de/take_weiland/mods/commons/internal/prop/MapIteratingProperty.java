package de.take_weiland.mods.commons.internal.prop;

import de.take_weiland.mods.commons.reflect.PropertyAccess;

import java.util.Iterator;
import java.util.Map;

/**
 * @author diesieben07
 */
public final class MapIteratingProperty<K, V> extends AbstractIteratingProperty<V, Map.Entry<? super K, V>, Iterator<? extends Map.Entry<? super K, V>>> {

    private final PropertyAccess<? extends Map<? super K, V>> mapProp;

    public MapIteratingProperty(PropertyAccess<? extends Map<? super K, V>> mapProp) {
        this.mapProp = mapProp;
    }

    @Override
    public void start(Object o) {
        it = mapProp.get(o).entrySet().iterator();
    }

    @Override
    public V get(Object o) {
        checkIterating("get");
        return curr.getValue();
    }

    @Override
    public void set(Object o, V val) {
        checkIterating("set");
        curr.setValue(val);
    }

}

package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.util.Listenable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author diesieben07
 */
public interface ListenableInternal<T> extends Listenable<T> {

    String GET = "_sc$getListeners";
    String SET = "_sc$setListeners";

    List<Consumer<? super T>> _sc$getListeners();

    void _sc$setListeners(List<Consumer<? super T>> list);

    static <T> void add(ListenableInternal<T> self, Consumer<? super T> handler) {
        List<Consumer<? super T>> list = self._sc$getListeners();
        if (list == null) {
            list = new ArrayList<>(3);
            self._sc$setListeners(list);
        }
        list.add(handler);
    }

    static <T> void remove(ListenableInternal<T> self, Consumer<? super T> handler) {
        List<Consumer<? super T>> list = self._sc$getListeners();
        if (list != null) {
            list.remove(handler);
        }
    }

    static <T> void doDispatch(ListenableInternal<T> self, T event) {
        List<Consumer<? super T>> list = self._sc$getListeners();
        if (list != null) {
            int i = list.size();
            do {
                if (--i < 0) {
                    return;
                }
                list.get(i).accept(event);
            } while (true);
        }
    }

    static <T> void doDispatch(ListenableInternal<T> self, Supplier<? extends T> eventSupplier) {
        List<Consumer<? super T>> list = self._sc$getListeners();
        int i;
        if (list != null && (i = list.size()) != 0) {
            T event = eventSupplier.get();
            do {
                if (--i < 0) {
                    return;
                }
                list.get(i).accept(event);
            } while (true);
        }
    }

}

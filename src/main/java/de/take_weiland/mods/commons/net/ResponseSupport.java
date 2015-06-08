package de.take_weiland.mods.commons.net;

import com.google.common.collect.MapMaker;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.entity.player.EntityPlayer;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author diesieben07
 */
final class ResponseSupport {

    static final ClassValue<ResponseSupport> cv = new ClassValue<ResponseSupport>() {
        @Override
        protected ResponseSupport computeValue(Class<?> type) {
            return new ResponseSupport();
        }
    };

    private static final Unsafe U = JavaUtils.getUnsafe();
    private static final long NEXT_ID_OFFSET;

    static {
        try {
            Field field = ResponseSupport.class.getDeclaredField("nextId");
            NEXT_ID_OFFSET = U.objectFieldOffset(field);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    private int nextId;
    private final Map<Integer, BiConsumer<?, ? super EntityPlayer>> handlers = new MapMaker().concurrencyLevel(2).makeMap();

    int register(BiConsumer<?, ? super EntityPlayer> handler) {
        int id = nextId();
        handlers.put(id, handler);
        return id;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    void fire(int id, Object response, EntityPlayer player) {
        ((BiConsumer) handlers.remove(id)).accept(response, player);
    }

    private int nextId() {
        int prev, next;
        do {
            prev = nextId;
            next = prev + 1;
        } while (!U.compareAndSwapInt(this, NEXT_ID_OFFSET, prev, next));
        return prev;
    }

}

package de.take_weiland.mods.commons.sync;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

/**
 * @author diesieben07
 */
public class TTest {

    private static final MethodHandle H = PropertyAccessorCache.INSTANCE.get(Test.class);

    public static void main(String[] args) throws Throwable {
        Test t = new Test();
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < 20000; i++) {
            SyncedProperty<?> result = (SyncedProperty<?>) H.invokeExact((Object) t, (int) 1);
            list.add(result);
        }
        System.out.println(list.hashCode());
    }

}

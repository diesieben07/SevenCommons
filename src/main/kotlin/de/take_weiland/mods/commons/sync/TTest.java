package de.take_weiland.mods.commons.sync;

import java.lang.invoke.MethodHandle;

/**
 * @author diesieben07
 */
public class TTest {

    public static void main(String[] args) throws Throwable {
        MethodHandle h = PropertyAccessorsKt.computeAccessors(Test.class);
        SyncedProperty<?> result = (SyncedProperty<?>) h.invokeExact((int) 0);
        System.out.println(result);
    }

}

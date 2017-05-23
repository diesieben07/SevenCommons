package de.take_weiland.mods.commons.net;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * @author diesieben07
 */
public class Test {

    static class Level1 {

        public void print() {
            System.out.println("Level1");
        }

    }

    static class Level2 extends Level1 {

        @Override
        public void print() {
            super.print();
            System.out.println("Level2");
        }
    }

    static class Level3 extends Level1 {

        private static final MethodHandle invokeLevel1Print;

        static {
            try {
                invokeLevel1Print = MethodHandles.lookup().in(Level1.class).findSpecial(Level1.class, "print", MethodType.methodType(void.class), Level1.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void print() {
            printLevel1Super();
            System.out.println("Level3");
        }

        private void printLevel1Super() {
            try {
                invokeLevel1Print.invokeExact((Level1) this);
            } catch (Throwable x) {
                throw new RuntimeException(x);
            }
        }
    }

    public static void main(String[] args) {
        new Level3().print();
    }

}

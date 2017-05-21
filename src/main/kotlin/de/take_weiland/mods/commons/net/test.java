package de.take_weiland.mods.commons.net;

interface JavaProperty<T extends Comparable<T>> { }

class Test {

    public static void main(String[] args) {
        JavaProperty<?> property = null;
        Comparable<?> value = parsePropertyValue(property, "test")
    }

    private static <T extends Comparable<T>> T parsePropertyValue(JavaProperty<T> property, String value) {
        return null;
    }

}
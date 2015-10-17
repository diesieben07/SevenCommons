package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * @author diesieben07
 */
public interface ASMProperty {

    void loadValue(MethodVisitor mv, Consumer<MethodVisitor> instanceLoader);

    default void loadValue(MethodVisitor mv) {
        loadValue(mv, ASMFieldProperty.getThisLoader());
    }

    void setValue(MethodVisitor mv, Consumer<MethodVisitor> valueLoader, Consumer<MethodVisitor> instanceLoader);

    default void setValue(MethodVisitor mv, Consumer<MethodVisitor> valueLoader) {
        setValue(mv, valueLoader, ASMFieldProperty.getThisLoader());
    }

    static ASMProperty forField(Field field) {
        return new ASMFieldProperty(field);
    }

    static ASMProperty forGetterSetter(Method getter, Method setter) {
        return new ASMGetterSetterProperty(getter, setter);
    }

}

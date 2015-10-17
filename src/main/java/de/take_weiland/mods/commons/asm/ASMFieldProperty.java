package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.PUTFIELD;

/**
 * @author diesieben07
 */
final class ASMFieldProperty implements ASMProperty {

    private final String className;
    private final String fieldName;
    private final String fieldDesc;

    ASMFieldProperty(Field field) {
        className = Type.getInternalName(field.getDeclaringClass());
        fieldName = field.getName();
        fieldDesc = Type.getDescriptor(field.getType());
    }

    static Consumer<MethodVisitor> getThisLoader() {
        return m -> m.visitVarInsn(ALOAD, 0);
    }

    @Override
    public void loadValue(MethodVisitor mv, Consumer<MethodVisitor> instanceLoader) {
        instanceLoader.accept(mv);
        mv.visitFieldInsn(GETFIELD, className, fieldName, fieldDesc);
    }

    @Override
    public void setValue(MethodVisitor mv, Consumer<MethodVisitor> valueLoader, Consumer<MethodVisitor> instanceLoader) {
        instanceLoader.accept(mv);
        valueLoader.accept(mv);
        mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldDesc);
    }
}

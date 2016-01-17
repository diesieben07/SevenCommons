package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.base.Strings;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class FieldAdder extends ClassVisitor {

    private       String          className;
    final         FieldInstance[] fields;
    private final Class<?>        clazz;

    private FieldAdder(ClassVisitor cv, Class<?> clazz) {
        super(ASM5, cv);
        this.clazz = clazz;
        this.fields = Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(FieldGetter.class))
                .map(FieldInstance::new)
                .toArray(FieldInstance[]::new);
    }

    public static Function<ClassVisitor, ClassVisitor> cstr(Class<?> clazz) {
        return cv -> new FieldAdder(cv, clazz);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        interfaces = ArrayUtils.add(interfaces, Type.getInternalName(clazz));
        super.visit(V1_8, access, name, signature, superName, interfaces);

        className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        if (name.equals("<init>")) {
            return new CstrHook(mv);
        }

        return mv;
    }

    @Override
    public void visitEnd() {
        for (FieldInstance field : fields) {
            int op = ACC_PRIVATE | (field.setter.equals("") ? ACC_FINAL : 0);
            FieldVisitor fv = cv.visitField(op, field.field, field.type.getDescriptor(), null, null);
            if (fv != null) {
                fv.visitEnd();
            }

            MethodVisitor mv = cv.visitMethod(ACC_PUBLIC | ACC_FINAL, field.getter, Type.getMethodDescriptor(field.type), null, null);
            if (mv != null) {
                mv.visitCode();

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, field.field, field.type.getDescriptor());
                mv.visitInsn(field.type.getOpcode(IRETURN));

                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            if (!Strings.isNullOrEmpty(field.setter)) {
                mv = cv.visitMethod(ACC_PUBLIC | ACC_FINAL, field.setter, Type.getMethodDescriptor(Type.VOID_TYPE, field.type), null, null);
                if (mv != null) {
                    mv.visitCode();

                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitVarInsn(field.type.getOpcode(ILOAD), 1);
                    mv.visitFieldInsn(PUTFIELD, className, field.field, field.type.getDescriptor());
                    mv.visitInsn(RETURN);

                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                }
            }
        }

        super.visitEnd();
    }

    private final class CstrHook extends MethodVisitor {

        CstrHook(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (opcode == INVOKESPECIAL && name.equals("<init>") && !owner.equals(className)) { // non-delegating constructor, e.g. calling super
                for (FieldInstance field : fields) {
                    if (!Strings.isNullOrEmpty(field.creator)) {
                        super.visitVarInsn(ALOAD, 0);
                        super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(clazz), field.creator, Type.getMethodDescriptor(field.type), true);
                        super.visitFieldInsn(PUTFIELD, className, field.field, field.type.getDescriptor());
                    }
                }
            }
        }

    }

    private static final class FieldInstance {

        final String field;
        final String getter;
        final String setter;
        final String creator;
        final Type   type;

        FieldInstance(Method getter) {
            FieldGetter annotation = getter.getAnnotation(FieldGetter.class);

            this.getter = getter.getName();
            this.field = annotation.field();
            this.setter = annotation.setter();
            this.creator = annotation.creator();
            this.type = Type.getType(getter.getReturnType());
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface FieldGetter {

        String field();

        String setter() default "";

        String creator() default "";

    }
}

package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.take_weiland.mods.commons.asm.ClassInfoClassWriter;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ASM4;

/**
 * @author diesieben07
 */
public abstract class VisitorBasedTransformer implements IClassTransformer {

    private List<Entry> entries;

    public VisitorBasedTransformer() {
        entries = new ArrayList<>();
        addEntries();
        entries = ImmutableList.copyOf(entries);
    }

    protected abstract void addEntries();

    public final void addEntry(Class<? extends ClassVisitor> visitor) {
        addEntry0(visitor, Predicates.alwaysTrue());
    }

    public final void addEntry(Class<? extends ClassVisitor> visitor, String clazz) {
        addEntry0(visitor, Predicates.equalTo(clazz));
    }

    public final void addEntry(Class<? extends ClassVisitor> visitor, String... classes) {
        addEntry0(visitor, Predicates.in(ImmutableSet.copyOf(classes)));
    }

    public final void addEntry(Class<? extends ClassVisitor> visitor, Predicate<? super String> nameMatcher) {
        addEntry0(visitor, nameMatcher);
    }

    public final void addEntry(Class<? extends MethodVisitor> methodVisitor, String className, String methodName) {
        addEntry(methodVisitor, Predicates.equalTo(className), Predicates.equalTo(methodName));
    }

    public final void addEntry(Class<? extends MethodVisitor> methodVisitor, Predicate<? super String> classMatcher, String methodName) {
        addEntry(methodVisitor, classMatcher, Predicates.equalTo(methodName));
    }

    public final void addEntry(Class<? extends MethodVisitor> methodVisitor, Predicate<? super String> classMatcher, Predicate<? super String> methodMatcher) {
        MethodHandle mvCstr = makeCstr(methodVisitor, MethodVisitor.class);
        MethodHandle cvCstr = MethodHandles.insertArguments(SingleMethodTransformer.CONSTRUCTOR, 1, mvCstr, methodMatcher);
        entries.add(new Entry(classMatcher, cvCstr));
    }

    private void addEntry0(Class<? extends ClassVisitor> visitor, Predicate<? super String> predicate) {
        entries.add(new Entry(predicate, makeCstr(visitor, ClassVisitor.class)));
    }

    private static <T> MethodHandle makeCstr(Class<? extends T> visitor, Class<T> clazz) {
        try {
            return publicLookup().findConstructor(visitor, methodType(void.class, clazz))
                    .asType(methodType(clazz, clazz));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException("No matching public constructor found", e);
        }
    }

    @Override
    public final byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        String internalName = transformedName.replace('.', '/');

        ClassReader cr = null;
        ClassWriter cw = null;
        ClassVisitor cv = null;
        for (Entry entry : entries) {
            if (entry.nameMatcher.apply(internalName)) {
                if (cv == null) {
                    cr = new ClassReader(bytes);
                    cv = cw = new ClassInfoClassWriter(cr, COMPUTE_FRAMES);
                }
                cv = entry.newVisitor(cv);
            }
        }
        if (cv != null) {
            cr.accept(cv, ClassReader.SKIP_FRAMES); // skip frames since we compute them again anyways
            return cw.toByteArray();
        } else {
            return bytes;
        }
    }

    private static final class Entry {

        final Predicate<? super String> nameMatcher;
        final MethodHandle constructor;

        Entry(Predicate<? super String> nameMatcher, MethodHandle constructor) {
            this.nameMatcher = nameMatcher;
            this.constructor = constructor;
        }

        ClassVisitor newVisitor(ClassVisitor cv) {
            try {
                return (ClassVisitor) constructor.invokeExact(cv);
            } catch (Throwable t) {
                throw Throwables.propagate(t);
            }
        }
    }

    static final class SingleMethodTransformer extends ClassVisitor {

        static final MethodHandle CONSTRUCTOR;

        static {
            try {
                CONSTRUCTOR = lookup().findConstructor(SingleMethodTransformer.class, methodType(void.class, ClassVisitor.class, MethodHandle.class, Predicate.class))
                        .asType(methodType(ClassVisitor.class, ClassVisitor.class, MethodHandle.class, Predicate.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new AssertionError(e); // impossible
            }
        }

        private final MethodHandle methodVisitorCstr;
        private final Predicate<? super String> methodNameMatcher;

        private SingleMethodTransformer(ClassVisitor cv, MethodHandle methodVisitorCstr, Predicate<? super String> methodNameMatcher) {
            super(ASM4, cv);
            this.methodVisitorCstr = methodVisitorCstr;
            this.methodNameMatcher = methodNameMatcher;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            if (methodNameMatcher.apply(name)) {
                return newMethodVisitor(mv);
            } else {
                return mv;
            }
        }

        private MethodVisitor newMethodVisitor(MethodVisitor mv) {
            try {
                return (MethodVisitor) methodVisitorCstr.invokeExact(mv);
            } catch (Throwable t) {
                throw Throwables.propagate(t);
            }
        }
    }
}

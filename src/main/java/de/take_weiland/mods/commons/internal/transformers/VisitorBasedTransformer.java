package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.take_weiland.mods.commons.asm.ClassInfoClassWriter;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;

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

    public final void addEntry(Function<? super ClassVisitor, ? extends ClassVisitor> constructor) {
        addEntry0(constructor, s -> true);
    }

    public final void addEntry(Function<? super ClassVisitor, ? extends ClassVisitor> constructor, String clazz) {
        addEntry0(constructor, Predicate.isEqual(clazz));
    }

    public final void addEntry(Function<? super ClassVisitor, ? extends ClassVisitor> constructor, String... classes) {
        addEntry0(constructor, ImmutableSet.copyOf(classes)::contains);
    }

    public final void addEntry(Function<? super ClassVisitor, ? extends ClassVisitor> constructor, Predicate<? super String> nameMatcher) {
        addEntry0(constructor, nameMatcher);
    }

    public final void addEntry(Function<? super MethodVisitor, ? extends MethodVisitor> constructor, String className, String methodName) {
        addEntry(constructor, Predicate.isEqual(className), Predicate.isEqual(methodName));
    }

    public final void addEntry(Function<? super MethodVisitor, ? extends MethodVisitor> constructor, Predicate<? super String> classMatcher, String methodName) {
        addEntry(constructor, classMatcher, Predicate.isEqual(methodName));
    }

    public final void addEntry(Function<? super MethodVisitor, ? extends MethodVisitor> constructor, Predicate<? super String> classMatcher, Predicate<? super String> methodNameMatcher) {
        addEntry0(classVisitor -> new SingleMethodTransformer(classVisitor, constructor, methodNameMatcher), classMatcher);
    }

    private void addEntry0(Function<? super ClassVisitor, ? extends ClassVisitor> cstr, Predicate<? super String> predicate) {
        entries.add(new Entry(predicate, cstr));
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
            if (entry.nameMatcher.test(internalName)) {
                if (cv == null) {
                    cr = new ClassReader(bytes);
                    cv = cw = new ClassInfoClassWriter(cr, COMPUTE_FRAMES);
                }
                cv = entry.constructor.apply(cv);
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
        final Function<? super ClassVisitor, ? extends ClassVisitor> constructor;

        Entry(Predicate<? super String> nameMatcher, Function<? super ClassVisitor, ? extends ClassVisitor> constructor) {
            this.nameMatcher = nameMatcher;
            this.constructor = constructor;
        }

    }

    private static final class SingleMethodTransformer extends ClassVisitor {

        private final Function<? super MethodVisitor, ? extends MethodVisitor> methodVisitorCstr;
        private final Predicate<? super String> methodNameMatcher;

        SingleMethodTransformer(ClassVisitor cv, Function<? super MethodVisitor, ? extends MethodVisitor> methodVisitorCstr, Predicate<? super String> methodNameMatcher) {
            super(ASM4, cv);
            this.methodVisitorCstr = methodVisitorCstr;
            this.methodNameMatcher = methodNameMatcher;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            if (methodNameMatcher.test(name)) {
                return methodVisitorCstr.apply(mv);
            } else {
                return mv;
            }
        }

    }
}

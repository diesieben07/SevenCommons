package de.take_weiland.mods.commons.internal.reflect;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.take_weiland.mods.commons.reflect.SCReflection;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.Iterator;
import java.util.Map;

import static de.take_weiland.mods.commons.util.JavaUtils.unsafe;

/**
 * @author diesieben07
 */
public class CompileContext {

    final ClassWriter cw;
    private int placeholderCount = 0;
    private final Map<Object, Pair<String, Integer>> patches = HashBiMap.create();

    CompileContext(ClassWriter cw) {
        this.cw = cw;
    }

    void pushAsConstant(MethodVisitor mv, Object obj) {
        Pair<String, Integer> entry = patches.get(obj);
        if (entry != null) {
            mv.visitLdcInsn(entry.getLeft());
        } else {
            String ph = nextPlaceholder();
            mv.visitLdcInsn(ph);
            int idx = cw.newConst(ph);
            patches.put(obj, Pair.of(ph, idx));
        }
    }

    Class<?> link(Class<?> holder) {
        cw.visitEnd();
        byte[] classFile = cw.toByteArray();

        int cpSize = getConstantPoolSize(classFile);

        Object[] patchesArr = new Object[cpSize];
        for (Map.Entry<Object, Pair<String, Integer>> entry : patches.entrySet()) {
            Object val = entry.getKey();
            int idx = entry.getValue().getRight();

            patchesArr[idx] = val;
        }

        return SCReflection.defineAnonymousClass(classFile, holder, patchesArr);
    }

    private static int getConstantPoolSize(byte[] classFile) {
        // snagged from JDK

        // The first few bytes:
        // u4 magic;
        // u2 minor_version;
        // u2 major_version;
        // u2 constant_pool_count;
        return ((classFile[8] & 0xFF) << 8) | (classFile[9] & 0xFF);
    }

    private String nextPlaceholder() {
        return "__sc$ph$" + (placeholderCount++);
    }

}

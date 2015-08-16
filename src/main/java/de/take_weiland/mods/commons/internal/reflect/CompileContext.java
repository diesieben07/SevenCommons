package de.take_weiland.mods.commons.internal.reflect;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author diesieben07
 */
public class CompileContext {

    final ClassWriter cw;
    private int placeholderCount = 0;
    private final TIntObjectMap<Object> patches = new TIntObjectHashMap<>();

    CompileContext(ClassWriter cw) {
        this.cw = cw;
    }

    void pushAsConstant(MethodVisitor mv, Object obj) {
        String ph = nextPlaceholder();
        mv.visitLdcInsn(ph);
        int idx = cw.newConst(ph);
        addPatch(idx, obj);
    }

    String nextPlaceholder() {
        return "__sc$ph$" + (placeholderCount++);
    }

    void addPatch(int idx, Object obj) {
        patches.put(idx, obj);
    }

}

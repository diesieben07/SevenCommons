package de.take_weiland.mods.commons.asm;

import de.take_weiland.mods.commons.util.JavaUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.BitSet;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * @author diesieben07
 */
public final class MethodContext {

    private final BitSet freeLocals = new BitSet();
    private final MethodNode method;
    private LabelNode globalStart;
    private LabelNode globalEnd;

    public MethodContext(MethodNode method) {
        this.method = method;

        boolean isStatic = (method.access & ACC_STATIC) == 0;
        if (isStatic) {
            freeLocals.set(0);
        }
        int firstParam = isStatic ? 0 : 1;
        freeLocals.set(firstParam, firstParam + ASMUtils.argumentCount(method.desc));
        for (LocalVariableNode localVar : JavaUtils.nullToEmpty(method.localVariables)) {
            freeLocals.set(localVar.index);
        }
    }

    public ASMVariable newLocal(Type type) {
        return newLocal(type, defName(type));
    }

    private static String defName(Type type) {
        String simpleName;
        String className = type.getClassName();
        int idx = className.lastIndexOf('.');
        if (idx < 0) {
            simpleName = className;
        } else {
            simpleName = className.substring(idx + 1);
        }
        return StringUtils.uncapitalize(simpleName);
    }

    public ASMVariable newLocal(Type type, String name) {
        if (globalStart == null) {
            globalStart = new LabelNode();
            globalEnd = new LabelNode();
        }
        return newLocal(type, name, globalStart, globalEnd);
    }

    public ASMVariable newLocal(Type type, String name, LabelNode start, LabelNode end) {
        int idx = freeLocals.nextClearBit(0);
        name = makeUnique(name);
        String desc = type.getDescriptor();
        LocalVariableNode var = new LocalVariableNode(name, desc, null, start, end, idx);
        method.localVariables.add(var);
        return ASMVariables.of(var);
    }

    public void finish() {
        if (globalStart != null) {
            method.instructions.insert(globalStart);
            method.instructions.add(globalEnd);
        }
    }

    private String makeUnique(String name) {
        for (LocalVariableNode var : JavaUtils.nullToEmpty(method.localVariables)) {
            if (var.name.equals(name)) {
                return makeUnique(name + '_');
            }
        }
        return name;
    }


}

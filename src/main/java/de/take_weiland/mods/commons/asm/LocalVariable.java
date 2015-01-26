package de.take_weiland.mods.commons.asm;

import de.take_weiland.mods.commons.util.JavaUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * <p>Representation of a local variable in ASM code. The slot will not be assigned until the code is created,
 * making the management of local variable slots unnecessary.</p>
 *
 * @author diesieben07
 */
public final class LocalVariable {

    private final Type type;
    private final Map<MethodNode, MethodData> cache = new WeakHashMap<>();

    public LocalVariable(Type type) {
        this.type = type;
    }

    /**
     * <p>Create a CodePiece that gets the value of this variable in the given method.</p>
     * @param method the MethodNode
     * @return a CodePiece
     */
    public CodePiece get(MethodNode method) {
        return CodePieces.of(new AccessNode(method, type.getOpcode(ILOAD), this));
    }

    /**
     * <p>Create a CodePiece that sets the value of this variable to the given value in the given method.</p>
     * @param method the MethodNode
     * @param val the value
     * @return a CodePiece
     */
    public CodePiece set(MethodNode method, CodePiece val) {
        return val.append(new AccessNode(method, type.getOpcode(ISTORE), this));
    }


    MethodData getData(MethodNode method) {
        MethodData data = cache.get(method);
        if (data == null) {
            data = new MethodData(method);
            cache.put(method, data);
        }
        return data;
    }

    private final class AccessNode extends VarInsnNode {

        private final Object id;
        private final MethodNode method;

        AccessNode(MethodNode method, int opcode, Object id) {
            super(opcode, -1);
            this.method = method;
            this.id = id;
        }

        private void resolve() {
            var = getData(method).slotFor(id);
        }

        @Override
        public void accept(MethodVisitor mv) {
            if (var < 0) {
                resolve();
            }
            super.accept(mv);
        }

    }

    private static class MethodData {

        private final BitSet freeLocals;
        private final Map<Object, Integer> assignedSlots;

        MethodData(MethodNode method) {
            BitSet free = new BitSet();
            boolean isStatic = (method.access & ACC_STATIC) != 0;
            if (!isStatic) {
                free.set(0);
            }
            free.set(isStatic ? 0 : 1, ASMUtils.argumentCount(method.desc) + (isStatic ? 0 : 1));


            for (LocalVariableNode var : JavaUtils.nullToEmpty(method.localVariables)) {
                free.set(var.index);
            }
            Iterator<AbstractInsnNode> it = method.instructions.iterator();
            while (it.hasNext()) {
                AbstractInsnNode insn = it.next();
                if (insn instanceof VarInsnNode) {
                    int var = ((VarInsnNode) insn).var;
                    if (var >= 0 || !(insn instanceof AccessNode)) {
                        free.set(var);
                    }
                }
            }

            this.freeLocals = free;
            this.assignedSlots = new HashMap<>();
        }

        int slotFor(Object id) {
           Integer assigned = assignedSlots.get(id);
            if (assigned == null) {
                assigned = freeLocals.nextClearBit(0);
                freeLocals.set(assigned);
            }
            return assigned;
        }
    }

}

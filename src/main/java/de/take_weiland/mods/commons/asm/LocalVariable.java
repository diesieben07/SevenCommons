package de.take_weiland.mods.commons.asm;

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
public final class LocalVariable extends AbstractASMVariable {

    final MethodNode method;
    final Type type;
    BitSet takenSlots;
    private int slot = -1;

    public LocalVariable(MethodNode method, Type type) {
        this.method = method;
        this.type = type;
    }

    private BitSet resolveFreeSlots() {
        if (takenSlots == null) {
            takenSlots = new BitSet();
            int params = ((method.access & ACC_STATIC) == 0 ? 0 : 1) + ASMUtils.argumentCount(method.desc);
            takenSlots.set(0, params);

            Iterator<AbstractInsnNode> it = method.instructions.iterator();
            while (it.hasNext()) {
                AbstractInsnNode insn = it.next();
                if (insn instanceof VarInsnNode && !(insn instanceof AccessNode)) {
                    takenSlots.set(((VarInsnNode) insn).var);
                }
            }
        }
        return takenSlots;
    }

    private void resolve() {
        if (slot >= 0) {
            return;
        }

        if (takenSlots == null) {
            takenSlots = findFreeSlots();
        }
        slot = tryFindSelfSlot();
        if (slot >= 0) {
            return;
        }
        slot = takenSlots.nextClearBit(0);
        takenSlots.set(slot);
    }

    private int tryFindSelfSlot() {
        Iterator<AbstractInsnNode> it = method.instructions.iterator();
        while (it.hasNext()) {
            AbstractInsnNode insn = it.next();
            if (insn instanceof AccessNode) {
                AccessNode an = (AccessNode) insn;
                if (an.getVar() == this && an.getVar().slot >= 0) {
                    return an.getVar().slot;
                }
            }
        }
        return -1;
    }

    private BitSet findFreeSlots() {
        Iterator<AbstractInsnNode> it = method.instructions.iterator();
        while (it.hasNext()) {
            AbstractInsnNode insn = it.next();
            if (insn instanceof AccessNode) {
                return ((AccessNode) insn).getVar().resolveFreeSlots();
            }
        }
        throw new IllegalStateException("Could not find self in instructions!");
    }

    int getSlot() {
        if (slot < 0) {
            resolve();
        }
        return slot;
    }

    private CodePiece getCache;

    @Override
    public CodePiece get() {
        if (getCache == null) {
            getCache = CodePieces.of(new AccessNode(method, type.getOpcode(ILOAD), this));
        }
        return getCache;
    }

    @Override
    public CodePiece set(CodePiece val) {
        return val.append(new AccessNode(method, type.getOpcode(ISTORE), this));
    }

    public CodePiece increment(int amount) {
        return CodePieces.of(new AccessNodeInc(method, this, amount));
    }

    @Override
    public Type getType() {
        return type;
    }

    private final class AccessNode extends VarInsnNode {

        private final Object id;
        private final MethodNode method;

        LocalVariable getVar() {
            return LocalVariable.this;
        }

        AccessNode(MethodNode method, int opcode, Object id) {
            super(opcode, -1);
            this.method = method;
            this.id = id;
        }

        @Override
        public void accept(MethodVisitor mv) {
            if (var < 0) {
                var = getSlot();
            }
            super.accept(mv);
        }

        @Override
        public AbstractInsnNode clone(Map<LabelNode, LabelNode> labels) {
            return new AccessNode(method, opcode, id);
        }
    }

    private final class AccessNodeInc extends IincInsnNode {

        private final MethodNode method;
        private final Object id;

        AccessNodeInc(MethodNode method, Object id, int incr) {
            super(-1, incr);
            this.method = method;
            this.id = id;
        }

        @Override
        public AbstractInsnNode clone(Map<LabelNode, LabelNode> labels) {
            return new AccessNodeInc(method, var, incr);
        }

        @Override
        public void accept(MethodVisitor mv) {
            if (var < 0) {
                var = getSlot();
            }
            super.accept(mv);
        }
    }

    @Override
    List<AnnotationNode> getterAnns(boolean visible) {
        return null;
    }

    @Override
    int setterModifiers() {
        return 0;
    }

    @Override
    int getterModifiers() {
        return 0;
    }

    @Override
    public String name() {
        return "local_unknown";
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isField() {
        return false;
    }

    @Override
    public boolean isMethod() {
        return false;
    }

}

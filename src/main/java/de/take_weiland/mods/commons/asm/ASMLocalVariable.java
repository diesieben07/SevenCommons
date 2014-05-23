package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.annotation.ElementType;
import java.util.List;

import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;

/**
 * @author diesieben07
 */
class ASMLocalVariable extends AbstractASMVariable {

	private final LocalVariableNode var;

	ASMLocalVariable(LocalVariableNode var) {
		this.var = var;
	}

	@Override
	protected List<AnnotationNode> getterAnns(boolean visible) {
		return null;
	}

	@Override
	protected List<AnnotationNode> setterAnns(boolean visible) {
		return null;
	}

	@Override
	protected int setterModifiers() {
		return 0;
	}

	@Override
	protected int getterModifiers() {
		return 0;
	}

	@Override
	protected ElementType annotationType() {
		return ElementType.LOCAL_VARIABLE;
	}

	@Override
	public CodePiece get() {
		return CodePieces.of(new VarInsnNode(getType().getOpcode(ILOAD), var.index));
	}

	@Override
	public CodePiece set(CodePiece loadValue) {
		InsnList insns = new InsnList();
		loadValue.appendTo(insns);
		insns.add(new VarInsnNode(getType().getOpcode(ISTORE), var.index));
		return CodePieces.of(insns);
	}

	@Override
	public Type getType() {
		return Type.getType(var.desc);
	}

	@Override
	public String name() {
		return var.name;
	}

	@Override
	public boolean isWritable() {
		// cannot detect if final
		return true;
	}

}

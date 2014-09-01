package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
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

	private CodePiece getCache;

	@Override
	public CodePiece get() {
		if (getCache == null) {
			getCache = CodePieces.of(new VarInsnNode(getType().getOpcode(ILOAD), var.index));
		}
		return getCache;
	}

	private CodePiece setCache;

	@Override
	public CodePiece set(CodePiece newValue) {
		if (setCache == null) {
			setCache = CodePieces.of(new VarInsnNode(getType().getOpcode(ISTORE), var.index));
		}
		return newValue.append(setCache);
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

	@Override
	public boolean isField() {
		return false;
	}

	@Override
	public boolean isMethod() {
		return false;
	}
}

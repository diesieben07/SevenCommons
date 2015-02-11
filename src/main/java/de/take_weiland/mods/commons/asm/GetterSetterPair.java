package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID;

/**
 * @author diesieben07
 */
class GetterSetterPair extends ClassBoundASMVariable {

	private final MethodNode getter;

	@Nullable
	private final MethodNode setter;

	GetterSetterPair(ClassNode clazz, MethodNode getter, @Nullable MethodNode setter, @Nullable CodePiece instance) {
		super(clazz, instance);
		Type valueType = Type.getReturnType(getter.desc);
		checkArgument(valueType.getSort() != VOID, "getter must not return void!");
		checkArgument(Type.getArgumentTypes(getter.desc).length == 0, "getter must not take arguments");

		if (setter != null) {
			checkArgument((getter.access & ACC_STATIC) == (setter.access & ACC_STATIC), "setter and getter must have the same static-state");
			Type[] setterArgs = Type.getArgumentTypes(setter.desc);
			checkArgument(setterArgs.length == 1, "setter must only take one argument");
			checkArgument(Type.getReturnType(setter.desc).getSort() == VOID, "setter must return void");
			checkArgument(setterArgs[0].equals(valueType), "setter takes wrong argument!");
		}

		this.getter = getter;
		this.setter = setter;
	}

	private CodePiece getCache;

	@Override
	public CodePiece get() {
		if (getCache == null) {
			if (isStatic()) {
				getCache = CodePieces.invoke(clazz, getter);
			} else {
				getCache = CodePieces.invoke(clazz, getter, instance);
			}
		}
		return getCache;
	}

	@Override
	public CodePiece set(CodePiece newValue) {
		checkWritable();
		assert setter != null;
		if (instance == null) {
			return CodePieces.invoke(clazz, setter, newValue);
		} else {
			return CodePieces.invoke(clazz, setter, instance, newValue);
		}
	}

	@Override
	public CodePiece setAndGet(CodePiece newValue) {
		checkWritable();
		assert setter != null;

		CodeBuilder cb = new CodeBuilder();
		if (instance == null) {
			cb.add(newValue);
			cb.add(new InsnNode(DUP));
			cb.add(new MethodInsnNode(INVOKESTATIC, clazz.name, setter.name, setter.desc));
		} else {
			cb.add(instance);
			cb.add(newValue);
			cb.add(new InsnNode(DUP_X1));

			int invokeOpcode;
			if (hasSetterModifier(ACC_PRIVATE)) {
				invokeOpcode = INVOKESPECIAL;
			} else if ((clazz.access & ACC_INTERFACE) != 0) {
				invokeOpcode = INVOKEINTERFACE;
			} else {
				invokeOpcode = INVOKEVIRTUAL;
			}

			cb.add(new MethodInsnNode(invokeOpcode, clazz.name, setter.name, setter.desc));
		}
		return cb.build();
	}

	@Override
	public boolean isWritable() {
		return setter != null;
	}

	@Override
	public Type getType() {
		return Type.getReturnType(getter.desc);
	}

	@Override
	protected List<AnnotationNode> getterAnns(boolean visible) {
		return visible ? getter.visibleAnnotations : getter.invisibleAnnotations;
	}

	@Override
	protected int setterModifiers() {
		assert setter != null;
		return setter.access;
	}

	@Override
	protected int getterModifiers() {
		return getter.access;
	}

	@Override
	public String name() {
		if (getter.name.startsWith("get")) {
			return Character.toLowerCase(getter.name.charAt(3)) + getter.name.substring(4);
		} else {
			return getter.name;
		}
	}

	@Override
	public String rawName() {
		return getter.name;
	}

	@Override
	public String setterName() {
		if (setter != null) {
			return setter.name;
		}
		return super.setterName();
	}

	@Override
	public String toString() {
		if (setter != null) {
			return String.format("Getter/Setter Pair (getter=\"%s\", setter=\"%s\"", getter.name, setter.name);
		} else {
			return "Getter \"" + getter.name + "\"";
		}
	}

	@Override
	public boolean isField() {
		return false;
	}

	@Override
	public boolean isMethod() {
		return true;
	}

    @Override
    public Handle getterHandle() {
        return new Handle(invokeOp(getter), clazz.name, getter.name, getter.desc);
    }

    @Override
    public Handle setterHandle() {
        if (setter == null) {
            throw new IllegalStateException("No setter!");
        }
        return new Handle(invokeOp(setter), clazz.name, setter.name, setter.desc);
    }

    private int invokeOp(MethodNode method) {
        if ((method.access & ACC_STATIC) != 0) {
            return H_INVOKESTATIC;
        } else if ((method.access & ACC_PRIVATE) != 0) {
            return H_INVOKESPECIAL;
        } else if ((clazz.access & ACC_INTERFACE) != 0) {
            return H_INVOKEINTERFACE;
        } else {
            return H_INVOKEVIRTUAL;
        }
    }
}

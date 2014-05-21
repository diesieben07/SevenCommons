package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public abstract class MethodInfo {

	private final String className;
	ClassInfo clazz;

	MethodInfo(ClassInfo clazz) {
		this.clazz = clazz;
		this.className = clazz.internalName();
	}

	MethodInfo(String className) {
		this.className = className;
	}

	public static MethodInfo unbound(String className, String name, String desc, int modifiers) {
		return new UnboundMethodInfo(className, name, desc, modifiers);
	}

	public abstract String name();

	public abstract String desc();

	public abstract int modifiers();

	public String containingClassName() {
		return className;
	}

	public ClassInfo containingClass() {
		return clazz == null ? (clazz = ClassInfo.of(className)) : clazz;
	}

	public boolean hasModifier(int mod) {
		return (modifiers() & mod) == mod;
	}

	public boolean isAbstract() {
		return hasModifier(ACC_ABSTRACT);
	}

	public boolean isStatic() {
		return hasModifier(ACC_STATIC);
	}

	public boolean isPrivate() {
		return hasModifier(ACC_PRIVATE);
	}

	public boolean isConstructor() {
		return name().equals("<init>");
	}

	public boolean isStaticInit() {
		return name().equals("<clinit>");
	}

	public boolean isEditable() {
		return false;
	}

	public MethodNode asmNode() {
		throw new UnsupportedOperationException();
	}

	public boolean exists() {
		return true;
	}

	/**
	 * <p>calls this method</p>
	 * <p>All parameters, including the {@code instance} to call on (for non-static methods) must be loaded onto the stack manually.</p>
	 * @return a CodePiece that calls this method
	 */
	public CodePiece call() {
		checkCall();
		return ASMUtils.asCodePiece(new MethodInsnNode(getDefaultOpcode(), containingClass().internalName(), name(), desc()));
	}

	/**
	 * <p>calls this method with the given parameters</p>
	 * <p>The {@code parameters} array must have the same number of elements than the parameter list of this method.</p>
	 * <p>Each element in the array must be one of the following:</p>
	 * <ul>
	 *     <li>a {@link de.take_weiland.mods.commons.asm.CodePiece}</li>
	 *     <li>an {@link org.objectweb.asm.tree.AbstractInsnNode}</li>
	 *     <li>an {@link org.objectweb.asm.tree.InsnList}</li>
	 *     <li>a {@link de.take_weiland.mods.commons.asm.ClassProperty}</li>
	 *     <li>a valid LDC constant (see {@link org.objectweb.asm.tree.LdcInsnNode})</li>
	 * </ul>
	 * <p>This method may only be used for static methods.</p>
	 * @param parameters the parameters for the method
	 * @return a CodePiece that calls this method with the given parameters
	 */
	public CodePiece callWith(Object... parameters) {
		checkCall();
		checkArgument(isStatic(), "Use callOnWith for non-static methods!");
		return callWith0(parameters, getDefaultOpcode(), containingClass().internalName(), ASMUtils.emptyCodePiece());
	}

	/**
	 * <p>calls this method on the current {@code this} instance with the given parameters.</p>
	 * <p>This method may only be used for non-static methods.</p>
	 * @param parameters the parameters for the method, see {@link #callWith(Object...)} for more information
	 * @return a code piece that calls this method on the current {@code this} instance with the given parameters
	 */
	public CodePiece callOnThisWith(Object... parameters) {
		checkCall();
		checkArgument(!isStatic(), "Use callWith for static methods!");
		return callWith0(parameters, getDefaultOpcode(), containingClass().internalName(), thisLoader());
	}

	/**
	 * <p>calls this method on the instance loaded by {@code instanceLoader} with the given parameters.</p>
	 * <p>This method may only be used for non-static methods.</p>
	 * @param instanceLoader a CodePiece that loads the instance to call this method on
	 * @param parameters the parameters for the method, see {@link #callWith(Object...)} for more information
	 * @return a code piece that calls this method on the given instance with the given parameters
	 */
	public CodePiece callOnWith(CodePiece instanceLoader, Object... parameters) {
		checkCall();
		checkArgument(!isStatic(), "Use callWith for static methods!");
		return callWith0(parameters, getDefaultOpcode(), containingClass().internalName(), instanceLoader);
	}

	/**
	 * <p>calls this method on the instance loaded by {@code instanceLoader} with the given parameters.</p>
	 * <p>This method may only be used for non-static methods.</p>
	 * @param instanceLoader an instruction that loads the instance to call this method on
	 * @param parameters the parameters for the method, see {@link #callWith(Object...)} for more information
	 * @return a code piece that calls this method on the given instance with the given parameters
	 */
	public CodePiece callOnWith(AbstractInsnNode instanceLoader, Object... parameters) {
		checkCall();
		checkArgument(!isStatic(), "Use callWith for static methods!");
		return callWith0(parameters, getDefaultOpcode(), containingClass().internalName(), ASMUtils.asCodePiece(instanceLoader));
	}

	/**
	 * <p>calls this method on the instance loaded by {@code instanceLoader} with the given parameters.</p>
	 * <p>This method may only be used for non-static methods.</p>
	 * @param instanceLoader an InsnList that loads the instance to call this method on
	 * @param parameters the parameters for the method, see {@link #callWith(Object...)} for more information
	 * @return a code piece that calls this method on the given instance with the given parameters
	 */
	public CodePiece callOnWith(InsnList instanceLoader, Object... parameters) {
		checkCall();
		checkArgument(!isStatic(), "Use callWith for static methods!");
		return callWith0(parameters, getDefaultOpcode(), containingClass().internalName(), ASMUtils.asCodePiece(instanceLoader));
	}

	/**
	 * <p>calls this method on the instance represented by {@code instanceLoader} with the given parameters.</p>
	 * <p>This method may only be used for non-static methods.</p>
	 * @param instanceLoader a ClassProperty that represents the instance to call this method on
	 * @param parameters the parameters for the method, see {@link #callWith(Object...)} for more information
	 * @return a code piece that calls this method on the given instance with the given parameters
	 */
	public CodePiece callOnWith(ClassProperty instanceLoader, Object... parameters) {
		checkCall();
		checkArgument(!isStatic(), "Use callWith for static methods!");
		return callWith0(parameters, getDefaultOpcode(), containingClass().internalName(), instanceLoader.getFromThis());
	}

	/**
	 * <p>Calls the super method of this method without loading any parameters.</p>
	 * <p>This method may only be used for non-static methods.</p>
	 * All parameters, including the {@code this} reference must be loaded onto the stack manually.
	 * @return a code piece that calls the super method
	 */
	public CodePiece callSuper() {
		checkCallSuper();
		return ASMUtils.asCodePiece(new MethodInsnNode(INVOKESPECIAL, containingClass().superName(), name(), desc()));
	}

	/**
	 * <p>calls the super method of this method with the given parameters</p>
	 * <p>This method may only be used for non-static methods.</p>
	 * @param parameters the parameters for the method, see {@link #callWith(Object...)} for more information
	 * @return a CodePiece that calls the super method with the given parameters
	 */
	public CodePiece callSuperWith(Object... parameters) {
		checkCallSuper();
		return callWith0(parameters, INVOKESPECIAL, containingClass().superName(), thisLoader());
	}

	private static CodePiece thisLoader() {
		return ASMUtils.asCodePiece(new VarInsnNode(ALOAD, 0));
	}

	private static CodePiece toCodePiece(Object o) {
		AbstractInsnNode possibleIntInsn;
		if (o instanceof CodePiece) {
			return (CodePiece) o;
		} else if (o instanceof AbstractInsnNode) {
			return ASMUtils.asCodePiece((AbstractInsnNode) o);
		} else if (o instanceof InsnList) {
			return ASMUtils.asCodePiece((InsnList) o);
		} else if (o instanceof ClassProperty) {
			return ((ClassProperty) o).getFromThis();
		} else if ((possibleIntInsn = tryAsIntInsn(o)) != null) {
			return ASMUtils.asCodePiece(possibleIntInsn);
		} else if (isValidLdc(o)) {
			return ASMUtils.asCodePiece(new LdcInsnNode(o));
		} else {
			throw new IllegalArgumentException("Cannot convert type " + o.getClass().getName() + " into a CodePiece");
		}
	}

	private static AbstractInsnNode tryAsIntInsn(Object o) {
		if (o instanceof Integer || o instanceof Byte || o instanceof Short) {
			int i = ((Number) o).intValue();
			if (i <= Byte.MAX_VALUE && i >= Byte.MIN_VALUE) {
				return new IntInsnNode(BIPUSH, i);
			} else if (i <= Short.MAX_VALUE && i >= Short.MIN_VALUE) {
				return new IntInsnNode(SIPUSH, i);
			} else {
				return new LdcInsnNode(i);
			}
		}
		return null;
	}

	private static boolean isValidLdc(Object o) {
		return o instanceof String || o instanceof Integer || o instanceof Long ||  o instanceof Float || o instanceof Double || o instanceof Type;
	}

	private CodePiece callWith0(Object[] parameters, int opcode, String className, CodePiece instanceLoader) {
		String desc = desc();
		Type methodType = Type.getMethodType(desc);
		checkArgument(methodType.getArgumentTypes().length == parameters.length, "Invalid parameter count");

		InsnList call = new InsnList();

		instanceLoader.appendTo(call); // empty for static methods

		for (Object parameter : parameters) {
			toCodePiece(parameter).appendTo(call);
		}
		call.add(new MethodInsnNode(opcode, className, name(), desc));
		return ASMUtils.asCodePiece(call);
	}

	private int getDefaultOpcode() {
		if (isStatic()) {
			return INVOKESTATIC;
		} else if (isPrivate() || isConstructor()) {
			return INVOKESPECIAL;
		} else {
			return INVOKEVIRTUAL;
		}
	}

	private void checkCallSuper() {
		// don't checkCall() here, because abstract methods can have a valid super method
		// instead check if the method is in an interface, then it definitely has no (callable) super method
		checkState(!containingClass().isInterface(), "cannot call abstract method!");
		checkState(!isStatic(), "static methods have no super method");
		checkState(!isPrivate(), "private method have no super method");
		checkState(!isConstructor(), "constructor have no super method");
	}

	private void checkCall() {
		checkState(!isAbstract(), "Cannot call abstract method!");
	}

}

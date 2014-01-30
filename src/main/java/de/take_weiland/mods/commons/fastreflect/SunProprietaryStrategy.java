package de.take_weiland.mods.commons.fastreflect;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import sun.misc.Unsafe;
import de.take_weiland.mods.commons.util.JavaUtils;

@SuppressWarnings("restriction")
class SunProprietaryStrategy extends AbstractStrategy {

	private static final Unsafe unsafe = (Unsafe) JavaUtils.getUnsafe();
	@Override
	public <T> T createAccessor(Class<T> iface) {
		InterfaceInfo info = analyze(iface);
		ClassWriter cw = new ClassWriter(0);
		
		cw.visit(V1_6, ACC_PUBLIC, Fastreflect.nextDynamicClassName(), null, "sun/reflect/MagicAccessorImpl", new String[] { getInternalName(iface) });
		cw.visitSource(".dynamic", null);
		
		makeConstructor(cw);
		
		for (Map.Entry<Method, Field> entry : info.getters.entrySet()) {
			makeGetter(cw, entry.getKey(), entry.getValue());
		}
		
		for (Map.Entry<Method, Field> entry : info.setters.entrySet()) {
			makeSetter(cw, entry.getKey(), entry.getValue());
		}
		
		for (Map.Entry<Method, Method> entry : info.invokers.entrySet()) {
			makeInvoker(cw, entry.getKey(), entry.getValue());
		}
		
		cw.visitEnd();
		
		Class<?> clazz = Fastreflect.defineDynamicClass(cw.toByteArray());
		try {
			return clazz.asSubclass(iface).newInstance();
		} catch (Exception e) {
			throw JavaUtils.throwUnchecked(e);
		}
	}
	
	@Override
	public Class<?> defineDynClass(byte[] clazz, Class<?> context) {
		return unsafe.defineAnonymousClass(context, clazz, null);
	}

	private void makeConstructor(ClassWriter cw) {
		String name = "<init>";
		String desc = getMethodDescriptor(VOID_TYPE);
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, name, desc, null, null);
		mv.visitCode();
		
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "sun/reflect/MagicAccessorImpl", name, desc);
		mv.visitInsn(RETURN);
		
		mv.visitMaxs(1, 1);
		
		mv.visitEnd();
	}

	private void makeGetter(ClassWriter cw, Method getter, Field field) {
		boolean isStatic = Modifier.isStatic(field.getModifiers());
		
		String owner;
		String name = getter.getName();
		String desc = getMethodDescriptor(getter);
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, name, desc, null, null);
		mv.visitCode();
		
		owner = getInternalName(field.getDeclaringClass());
		name = field.getName();
		desc = getDescriptor(field.getType());
		if (isStatic) {
			mv.visitFieldInsn(GETSTATIC, owner, name, desc);
		} else {
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(GETFIELD, owner, name, desc);
		}
		
		mv.visitInsn(getType(field.getType()).getOpcode(IRETURN));
		
		mv.visitMaxs(1, 2);
		
		mv.visitEnd();
	}
	
	private void makeSetter(ClassWriter cw, Method setter, Field field) {
		boolean isStatic = Modifier.isStatic(field.getModifiers());
		
		String owner;
		String name = setter.getName();
		String desc = getMethodDescriptor(setter);
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, name, desc, null, null);
		mv.visitCode();
		
		owner = getInternalName(field.getDeclaringClass());
		name = field.getName();
		desc = getDescriptor(field.getType());
		if (isStatic) {
			mv.visitVarInsn(ALOAD, 2);
			mv.visitFieldInsn(PUTSTATIC, owner, name, desc);
		} else {
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitFieldInsn(PUTFIELD, owner, name, desc);
		}
		
		mv.visitInsn(RETURN);
		
		mv.visitMaxs(isStatic ? 1 : 2, 3);
		
		mv.visitEnd();
	}
	
	private void makeInvoker(ClassWriter cw, Method invoker, Method target) {
		boolean isStatic = Modifier.isStatic(target.getModifiers());
		
		String owner;
		String name = invoker.getName();
		String desc = getMethodDescriptor(invoker);
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, name, desc, null, null);
		mv.visitCode();
		
		Class<?>[] params = invoker.getParameterTypes();
		int len = params.length;
		
		owner = getInternalName(target.getDeclaringClass());
		name = target.getName();
		desc = getMethodDescriptor(target);
		for (int i = isStatic ? 1 : 0; i < len; ++i) {
			Type paramType = getType(params[i]);
			mv.visitVarInsn(paramType.getOpcode(ILOAD), i);
		}
		mv.visitMethodInsn(isStatic ? INVOKESTATIC : INVOKEVIRTUAL, owner, name, desc);
		
		mv.visitInsn(getType(target.getReturnType()).getOpcode(IRETURN));
		
		mv.visitMaxs(isStatic ? len - 1 : len, len + 1);
		
		mv.visitEnd();
	}

}

package de.take_weiland.mods.commons.reflect;

import de.take_weiland.mods.commons.util.JavaUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

class SunProprietaryStrategy extends AbstractStrategy {

	private static final Unsafe unsafe = (Unsafe) JavaUtils.getUnsafe();

	@Override
	public <T> T createAccessor(Class<T> iface) {
		InterfaceInfo info = analyze(iface);
		ClassWriter cw = new ClassWriter(0);

		cw.visit(V1_6, ACC_PUBLIC, SCReflection.nextDynamicClassName(iface.getPackage()), null, "sun/reflect/MagicAccessorImpl", new String[] { getInternalName(iface) });
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

		for (Map.Entry<Method, Constructor<?>> entry : info.cstrs.entrySet()) {
			makeConstructorBouncer(cw, entry.getKey(), entry.getValue());
		}

		cw.visitEnd();

		Class<?> clazz = SCReflection.defineDynamicClass(cw.toByteArray());
		try {
			return clazz.asSubclass(iface).newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Internal error in Fastreflect!", e);
		}
	}

	@Override
	public Class<?> defineDynClass(byte[] clazz, Class<?> context) {
		return unsafe.defineAnonymousClass(context, clazz, null);
	}

	private void makeConstructorBouncer(ClassWriter cw, Method boncer, Constructor<?> cstr) {
		String name = boncer.getName();
		String desc = Type.getMethodDescriptor(boncer);

		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, name, desc, null, null);
		mv.visitCode();

		mv.visitTypeInsn(NEW, Type.getInternalName(cstr.getDeclaringClass()));
		mv.visitInsn(DUP);

		Class<?>[] params = cstr.getParameterTypes();

		int idx = 1;
		for (int i = 0; i < params.length; i++) {
			Type type = Type.getType(params[i]);
			mv.visitVarInsn(type.getOpcode(ILOAD), idx);
			idx += type.getSize();
		}

		mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(cstr.getDeclaringClass()), "<init>", Type.getConstructorDescriptor(cstr));
		mv.visitInsn(ARETURN);

		mv.visitMaxs(1 + idx, idx);
		mv.visitEnd();
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
			if (getter.getParameterTypes()[0] != field.getDeclaringClass()) {
				mv.visitTypeInsn(CHECKCAST, getInternalName(field.getDeclaringClass()));
			}
			mv.visitFieldInsn(GETFIELD, owner, name, desc);
		}

		mv.visitInsn(getType(field.getType()).getOpcode(IRETURN));

		mv.visitMaxs(getType(field.getType()).getSize(), 2);
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
			mv.visitVarInsn(getType(field.getType()).getOpcode(ILOAD), 2);
			mv.visitFieldInsn(PUTSTATIC, owner, name, desc);
		} else {
			mv.visitVarInsn(ALOAD, 1);
			if (setter.getParameterTypes()[0] != field.getDeclaringClass()) {
				mv.visitTypeInsn(CHECKCAST, getInternalName(field.getDeclaringClass()));
			}
			mv.visitVarInsn(getType(field.getType()).getOpcode(ILOAD), 2);
			mv.visitFieldInsn(PUTFIELD, owner, name, desc);
		}

		mv.visitInsn(RETURN);

		mv.visitMaxs(getType(field.getType()).getSize() + (isStatic ? 0 : 1), 2 + getType(field.getType()).getSize());
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

		if (!isStatic) {
			mv.visitVarInsn(ALOAD, 1);
			if (params[0] != target.getDeclaringClass()) {
				mv.visitTypeInsn(CHECKCAST, getInternalName(target.getDeclaringClass()));
			}
		}

		int idx = 2;
		for (int i = 1; i < len; ++i) {
			Type paramType = getType(params[i]);
			mv.visitVarInsn(paramType.getOpcode(ILOAD), idx);
			idx += paramType.getSize();
		}
		mv.visitMethodInsn(isStatic ? INVOKESTATIC : INVOKEVIRTUAL, owner, name, desc);

		mv.visitInsn(getType(target.getReturnType()).getOpcode(IRETURN));

		mv.visitMaxs(isStatic ? idx - 2 : idx - 1, idx);

		mv.visitEnd();
	}

}

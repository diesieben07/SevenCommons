package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.asm.ClassInfoClassWriter;
import de.take_weiland.mods.commons.reflect.SCReflection;
import de.take_weiland.mods.commons.sync.PropertyMetadata;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getType;

/**
 * @author diesieben07
 */
public final class SyncASMHooks {

	public static Watcher<?> findWatcher(PropertyMetadata<?> metadata) throws ReflectiveOperationException {
		return WatcherRegistry.findWatcher(metadata);
	}

	private static final List<Class<?>> keepLoaded = Collections.synchronizedList(new ArrayList<Class<?>>());

	public static SyncableProperty<?, ?> makeProperty(Field field, Field dataField) {
		return new FieldProperty(field, dataField);
	}

	public static SyncableProperty<?, ?> makeProperty(Method getter, Method setter, Field dataField) {
		return new GetterSetterProperty(getter, setter, dataField);
	}

	public static Class<?> makePropertyClass(ClassNode targetClass, String getter, String setter, Type type) {
		Class<?> clazz = compilePropertyClass(targetClass, getter, setter, type);
		keepLoaded.add(clazz);
		return clazz;
	}

	private static final String TARGET_FIELD_NAME = "target";

	private static Class<?> compilePropertyClass(ClassNode targetClass, String getter, String setter, Type type) {
		String myName = targetClass.name + "$_sc_property_m_" + getter;
		String superName = Type.getInternalName(AbstractProperty.class);

		ClassWriter cw = new ClassInfoClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.visit(V1_6, ACC_PUBLIC | ACC_FINAL, myName, null, superName, null);

		addCstrAndField(cw, myName, superName, targetClass);
		addGet(cw, myName, targetClass, getter, type);

		if (setter == null) {
			addThrowingSet(cw, getter, targetClass);
		} else {
			addSet(cw, myName, targetClass, setter, type);
		}

		return SCReflection.defineDynamicClass(cw.toByteArray());
	}

	private static void addThrowingSet(ClassWriter cw, String getter, ClassNode targetClass) {
		String name = "set";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(Object.class));
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_FINAL, name, desc, null, null);
		mv.visitCode();

		String exceptionClass = Type.getInternalName(UnsupportedOperationException.class);

		mv.visitTypeInsn(NEW, exceptionClass);
		mv.visitInsn(DUP);

		mv.visitLdcInsn("set called on immutable property " + getter + " in " + targetClass.name);
		name = "<init>";
		desc = Type.getMethodDescriptor(VOID_TYPE, getType(String.class));
		mv.visitMethodInsn(INVOKESPECIAL, exceptionClass, name, desc);

		mv.visitInsn(ATHROW);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private static void addCstrAndField(ClassWriter cw, String myName, String superName, ClassNode targetClass) {
		cw.visitField(ACC_PRIVATE | ACC_FINAL, TARGET_FIELD_NAME, Type.getObjectType(targetClass.name).getDescriptor(), null, null);

		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(VOID_TYPE, getType(Object.class)), null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, superName, "<init>", Type.getMethodDescriptor(VOID_TYPE));

		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitTypeInsn(CHECKCAST, targetClass.name);
		mv.visitFieldInsn(PUTFIELD, myName, TARGET_FIELD_NAME, Type.getObjectType(targetClass.name).getDescriptor());

		mv.visitInsn(RETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private static void addGet(ClassWriter cw, String myClass, ClassNode targetClass, String getter, Type type) {
		String name = "get";
		String desc = Type.getMethodDescriptor(getType(Object.class));

		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_FINAL, name, desc, null, null);
		mv.visitCode();

		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, myClass, TARGET_FIELD_NAME, Type.getObjectType(targetClass.name).getDescriptor());

		mv.visitMethodInsn(INVOKEVIRTUAL, targetClass.name, getter, Type.getMethodDescriptor(type));

		mv.visitInsn(ARETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private static void addSet(ClassWriter cw, String myClass, ClassNode targetClass, String setter, Type type) {
		String name = "set";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(Object.class));

		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_FINAL, name, desc, null, null);
		mv.visitCode();

		getTargetField(mv, myClass, targetClass);
		mv.visitVarInsn(ALOAD, 1); // load parameter to set

		if (!type.getInternalName().equals("java/lang/Object")) {
			mv.visitTypeInsn(CHECKCAST, type.getInternalName());
		}

		mv.visitMethodInsn(INVOKEVIRTUAL, targetClass.name, setter, Type.getMethodDescriptor(VOID_TYPE, type));

		mv.visitInsn(RETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private static void getTargetField(MethodVisitor mv, String myClass, ClassNode targetClass) {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, myClass, TARGET_FIELD_NAME, Type.getObjectType(targetClass.name).getDescriptor());
	}
}

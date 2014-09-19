package de.take_weiland.mods.commons.sync;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.reflect.SCReflection;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.V1_6;
import static org.objectweb.asm.Type.VOID_TYPE;

/**
 * @author diesieben07
 */
public class PropertyCompiler {

	public static Property compile(Method getter, Method setter) {
		ClassWriter cw = new ClassWriter(COMPUTE_FRAMES);
		String name = SCReflection.nextDynamicClassName();
		cw.visit(V1_6, ACC_PUBLIC | ACC_FINAL, name, null, Type.getInternalName(Property.class), null);

		cw.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(VOID_TYPE), null, null);

		cw.visitEnd();

		try {
			return (Property) SCReflection.defineDynamicClass(cw.toByteArray()).newInstance();
		} catch (ReflectiveOperationException e) {
			throw Throwables.propagate(e);
		}
	}


}

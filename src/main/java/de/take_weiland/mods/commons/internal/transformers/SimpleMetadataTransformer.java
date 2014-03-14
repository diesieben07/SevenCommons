package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ClassInfo;
import de.take_weiland.mods.commons.internal.SimpleMetadataProxy;
import de.take_weiland.mods.commons.metadata.Metadata;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class SimpleMetadataTransformer extends AbstractAnalyzingTransformer {

	private static final ClassInfo simpleMetadataCI = ASMUtils.getClassInfo(Metadata.Simple.class);

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		if (!simpleMetadataCI.isAssignableFrom(classInfo)) {
			return false;
		}
		if (!classInfo.isEnum()) {
			throw new IllegalArgumentException("Must implement Metadata.Simple on an Enum class! Class: " + clazz.name);
		}

		FieldNode field = new FieldNode(ACC_PRIVATE | ACC_STATIC, "_sc$metadataHolder", getDescriptor(Object.class), null, null);
		clazz.fields.add(field);

		MethodNode method = new MethodNode(ACC_PUBLIC, SimpleMetadataProxy.SETTER, getMethodDescriptor(VOID_TYPE, getType(Object.class)), null, null);
		method.instructions.add(new VarInsnNode(ALOAD, 1));
		method.instructions.add(new FieldInsnNode(PUTSTATIC, clazz.name, field.name, field.desc));
		method.instructions.add(new InsnNode(RETURN));
		clazz.methods.add(method);

		method = new MethodNode(ACC_PUBLIC, SimpleMetadataProxy.GETTER, getMethodDescriptor(getType(Object.class)), null, null);
		method.instructions.add(new FieldInsnNode(GETSTATIC, clazz.name, field.name, field.desc));
		method.instructions.add(new InsnNode(ARETURN));
		clazz.methods.add(method);

		clazz.interfaces.add("de/take_weiland/mods/commons/internal/SimpleMetadataProxy");

		return true;
	}
}

package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.nbt.ToNbt;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Map;
import java.util.Set;

import static de.take_weiland.mods.commons.internal.ASMConstants.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
final class NBTTransformer {

	private static final Type classType = getType(Class.class);
	private static final Type string = getType(String.class);
	private static final Type enumType = getType(Enum.class);
	private static final Type enumArr = getType(Enum[].class);
	private static final Type nbtTagCompound = getObjectType("net/minecraft/nbt/NBTTagCompound");
	private static final String entityPropsName = "net/minecraftforge/common/IExtendedEntityProperties";
	private static final String nbtAsmHooks = "de/take_weiland/mods/commons/internal/NBTASMHooks";

	private static final ASMUtils.ClassInfo enumClassInfo = ASMUtils.getClassInfo(Enum.class);
	private static final ASMUtils.ClassInfo tileEntity = ASMUtils.getClassInfo("net/minecraft/tileentity/TileEntity");
	private static final ASMUtils.ClassInfo entity = ASMUtils.getClassInfo("net/minecraft/entity/Entity");
	private static final ASMUtils.ClassInfo entityProps = ASMUtils.getClassInfo(entityPropsName);

	static void transform(ClassNode clazz) {
		clazz.access &= ~(ACC_PRIVATE | ACC_PROTECTED);
		clazz.access |= ACC_PUBLIC;

		ClassType type;
		ASMUtils.ClassInfo me = ASMUtils.getClassInfo(clazz);
		if (ASMUtils.isAssignableFrom(tileEntity, me)) {
			type = ClassType.TILE_ENTITY;
		} else if (ASMUtils.isAssignableFrom(entity, me)) {
			type = ClassType.ENTITY;
		} else if (ASMUtils.isAssignableFrom(entityProps, me)) {
			type = ClassType.ENTITY_PROPS;
		} else {
			throw new IllegalArgumentException(String.format("Don't know how to save @ToNbt fields in class %s!", clazz.name));
		}

		Map<FieldNode, String> fields = Maps.newHashMap();
		Set<String> names = Sets.newHashSet();
		Set<FieldNode> enumFields = Sets.newHashSet();
		for (FieldNode field : clazz.fields) {
			AnnotationNode nbt = ASMUtils.getAnnotation(field, ToNbt.class);
			if (nbt == null) {
				continue;
			}

			if ((field.access & ACC_STATIC) == ACC_STATIC || (field.access & ACC_FINAL) == ACC_FINAL) {
				throw new IllegalArgumentException(String.format("@ToNbt field %s in class %s may not be static or final!", field.name, clazz.name));
			}
			String name = nbt.values == null ? "" : ((String) nbt.values.get(1)).trim();
			if (name.isEmpty()) {
				name = field.name;
			}

			if (names.contains(name)) {
				throw new IllegalArgumentException(String.format("Duplicate Key for @ToNbt field %s in class %s!", field.name, clazz.name));
			}
			names.add(name);
			fields.put(field, name);

			Type fieldType = getType(field.desc);
			Type actualType;
			boolean isArray = fieldType.getSort() == ARRAY;
			if (isArray) {
				actualType = fieldType.getElementType();
			} else {
				actualType = fieldType;
			}
			if (!ASMUtils.isPrimitive(actualType) && ASMUtils.isAssignableFrom(enumClassInfo, ASMUtils.getClassInfo(actualType.getInternalName()))) {
				enumFields.add(field);
			}
		}

		boolean callSuper = type != ClassType.ENTITY_PROPS || !isEntityPropsDirect(clazz);

		InsnList readHook = createReadHook(clazz, fields, enumFields);
		InsnList writeHook = createWriteHook(clazz, fields, enumFields);

		makeHandlerCall(clazz, callSuper, type.writeMethod, writeHook);
		makeHandlerCall(clazz, callSuper, type.readMethod, readHook);

		for (FieldNode field : clazz.fields) {
			if (ASMUtils.hasAnnotation(field, ToNbt.class)) {
				field.access &= ~(ACC_PRIVATE | ACC_PROTECTED);
				field.access |= ACC_PUBLIC;
			}
		}
	}

	private static InsnList createWriteHook(ClassNode clazz, Map<FieldNode, String> fields, Set<FieldNode> enumFields) {
		InsnList insns = new InsnList();

		for (Map.Entry<FieldNode, String> entry : fields.entrySet()) {
			FieldNode field = entry.getKey();
			String nbtKey = entry.getValue();
			insns.add(new VarInsnNode(ALOAD, 1));
			insns.add(new LdcInsnNode(nbtKey));
			insns.add(new VarInsnNode(ALOAD, 0));
			insns.add(new FieldInsnNode(GETFIELD, clazz.name, field.name, field.desc));

			Type fieldType = getType(field.desc);
			boolean isArray = fieldType.getSort() == ARRAY;
			boolean isEnum = enumFields.contains(field);
			Type typeToUse;
			if (isEnum) {
				typeToUse = isArray ? enumArr : enumType;
			} else {
				typeToUse = fieldType;
			}

			String desc = getMethodDescriptor(VOID_TYPE, nbtTagCompound, string, typeToUse);
			insns.add(new MethodInsnNode(INVOKESTATIC, nbtAsmHooks, "set", desc));
		}

		return insns;
	}

	private static InsnList createReadHook(ClassNode clazz, Map<FieldNode, String> fields, Set<FieldNode> enumFields) {
		InsnList insns = new InsnList();

		for (Map.Entry<FieldNode, String> entry : fields.entrySet()) {
			FieldNode field = entry.getKey();
			String nbtKey = entry.getValue();
			Type fieldType = getType(field.desc);
			boolean isArray = fieldType.getSort() == ARRAY;
			boolean isEnum = enumFields.contains(field);

			Type typeToUse;
			if (isEnum) {
				typeToUse = isArray ? enumArr : enumType;
			} else {
				typeToUse = fieldType;
			}

			insns.add(new VarInsnNode(ALOAD, 0));
			insns.add(new VarInsnNode(ALOAD, 1));
			insns.add(new LdcInsnNode(nbtKey));
			String desc;
			if (isEnum) {
				insns.add(new LdcInsnNode(isArray ? fieldType.getElementType() : fieldType));
				desc = getMethodDescriptor(typeToUse, nbtTagCompound, string, classType);
			} else {
				desc = getMethodDescriptor(typeToUse, nbtTagCompound, string);
			}
			String name;
			if (isArray) {
				name = "get_" + typeToUse.getElementType().getClassName().replace('.', '_') + "_arr";
			} else {
				name = "get_" + typeToUse.getClassName().replace('.', '_');
			}
			insns.add(new MethodInsnNode(INVOKESTATIC, nbtAsmHooks, name, desc));
			if (isEnum) {
				// need to cast for Enums because the return type is just Enum / Enum[]
				// but the actual type matches
				insns.add(new TypeInsnNode(CHECKCAST, fieldType.getInternalName()));
			}
			insns.add(new FieldInsnNode(PUTFIELD, clazz.name, field.name, field.desc));
		}

		return insns;
	}

	private static void makeHandlerCall(ClassNode clazz, boolean callSuper, String methodName, InsnList hook) {
		MethodNode method = ASMUtils.findMethod(clazz, methodName);
		String desc;
		if (method == null) {
			desc = getMethodDescriptor(VOID_TYPE, nbtTagCompound);
			method = new MethodNode(ACC_PUBLIC, methodName, desc, null, null);
			InsnList insns = method.instructions;
			if (callSuper) {
				insns.add(new VarInsnNode(ALOAD, 0));
				insns.add(new VarInsnNode(ALOAD, 1));
				insns.add(new MethodInsnNode(INVOKESPECIAL, clazz.superName, methodName, desc));
			}

			insns.add(new InsnNode(RETURN));
			clazz.methods.add(method);
		}

		method.instructions.insert(hook);
	}

	private static boolean isEntityPropsDirect(ClassNode clazz) {
		if (clazz.interfaces.contains(entityPropsName)) {
			return true;
		}
		for (String iface : clazz.interfaces) {
			if (checkHasProps(ASMUtils.getClassInfo(iface))) {
				return true;
			}
		}
		return false;
	}

	private static boolean checkHasProps(ASMUtils.ClassInfo clazz) {
		for (String iface : clazz.interfaces()) {
			if (iface.equals(entityPropsName) || checkHasProps(ASMUtils.getClassInfo(iface))) {
				return true;
			}
		}
		return false;
	}

	private static enum ClassType {

		TILE_ENTITY(M_WRITE_TO_NBT_TILEENTITY_MCP, M_WRITE_TO_NBT_TILEENTITY_SRG, M_READ_FROM_NBT_TILEENTITY_MCP, M_READ_FROM_NBT_TILEENTITY_SRG),
		ENTITY(M_WRITE_ENTITY_TO_NBT_MCP, M_WRITE_ENTITY_TO_NBT_SRG, M_READ_ENTITY_FROM_NBT_MCP, M_READ_ENTITY_FROM_NBT_SRG),
		ENTITY_PROPS("saveNBTData", "loadNBTData");

		final String writeMethod;
		final String readMethod;

		private ClassType(String writeMethod, String readMethod) {
			this.writeMethod = writeMethod;
			this.readMethod = readMethod;
		}

		private ClassType(String writeMcp, String writeSrg, String readMcp, String readSrg) {
			this(ASMUtils.useMcpNames() ? writeMcp : writeSrg, ASMUtils.useMcpNames() ? readMcp : readSrg);
		}

	}

}

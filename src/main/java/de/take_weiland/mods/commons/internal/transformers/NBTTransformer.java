package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.NBTASMHooks;
import de.take_weiland.mods.commons.nbt.NBTSerializable;
import de.take_weiland.mods.commons.nbt.NBTSerializer;
import de.take_weiland.mods.commons.nbt.ToNbt;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.ListIterator;

import static de.take_weiland.mods.commons.asm.ASMUtils.isPrimitive;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class NBTTransformer {

	private static final int READ = 0;
	private static final int WRITE = 1;

	private static final ClassInfo nbtASMHooks = ClassInfo.of(NBTASMHooks.class);

	private static final Type nbtBaseType = getType(NBTBase.class);
	private static final Type nbtTagCompoundType = getType(NBTTagCompound.class);
	private static final Type stringType = getType(String.class);

	private static final ClassInfo tileEntityClassInfo = ClassInfo.of(TileEntity.class);
	private static final ClassInfo entityClassInfo = ClassInfo.of(Entity.class);
	private static final ClassInfo entityPropsClassInfo = ClassInfo.of(IExtendedEntityProperties.class);
	private static final ClassInfo nbtSerializableClassInfo = ClassInfo.of(NBTSerializable.class);
	private static final ClassInfo nbtSerializerClassInfo = ClassInfo.of(NBTSerializer.class);

	static void transform(ClassNode clazz, ClassInfo classInfo, ListIterator<MethodNode> methods) {
		ClassType type = findType(classInfo);
		Collection<ClassProperty> properties = ASMUtils.propertiesWith(clazz, ToNbt.class);

		MethodNode readMethod = new MethodNode(ACC_PUBLIC, type.getNbtRead(), getMethodDescriptor(VOID_TYPE, nbtTagCompoundType), null, null);
		MethodNode writeMethod = new MethodNode(ACC_PUBLIC, type.getNbtWrite(), getMethodDescriptor(VOID_TYPE, nbtTagCompoundType), null, null);

		InsnList read = readMethod.instructions;
		InsnList write = writeMethod.instructions;

		// find any present NBT read/write method and rename them
		// this is needed because it is basically impossible to find all the possible exitpoints
		// (return, throw, etc.) of the method, which we would need to make sure our code always get's executed
		// instead we rename the present method and call it instead
		MethodInfo presentReadMethod = classInfo.getMethod(type.getNbtRead());
		if (presentReadMethod != null) {
			MethodNode methodNode = presentReadMethod.asmNode();
			methodNode.name = "_sc$renamedNbtRead";
			methodNode.access = (methodNode.access & ~(ACC_PUBLIC | ACC_PROTECTED)) | ACC_PRIVATE;

			presentReadMethod.callOnThisWith(new VarInsnNode(ALOAD, 1)).appendTo(read);
		}

		MethodInfo presentWriteMethod = classInfo.getMethod(type.getNbtWrite());
		if (presentWriteMethod != null) {
			MethodNode methodNode = presentWriteMethod.asmNode();
			methodNode.name = "_sc$renamedNbtWrite";
			methodNode.access = (methodNode.access & ~(ACC_PUBLIC | ACC_PROTECTED)) | ACC_PRIVATE;

			presentWriteMethod.callOnThisWith(new VarInsnNode(ALOAD, 1)).appendTo(write);
		}

		// determine if our method needs to call the super method
		// this is the case if
		// a) there wasn't already a read/write method in the class
		// b) the super method actually exists and is not abstract
		boolean[] callSuper = type.shouldCallSuper(classInfo, presentReadMethod != null, presentWriteMethod != null);

		if (callSuper[READ]) {
			insertSuperCall(clazz, read, type.getNbtRead());
		}

		if (callSuper[WRITE]) {
			insertSuperCall(clazz, write, type.getNbtWrite());
		}

		for (ClassProperty property : properties) {
			AnnotationNode ann = property.getterAnnotation(ToNbt.class);
			String propName = ASMUtils.getAnnotationProperty(ann, "value", property.propertyName());

			Type propType = property.getType();
			Type rawType;
			Type callType;
			String mName = "convert";
			if (propType.getSort() == ARRAY) {
				rawType = propType.getElementType();
				if (propType.getDimensions() == 1) {
					callType = ASMUtils.asArray(findBaseType(rawType), propType.getDimensions());
				} else {
					Type baseType = findBaseType(rawType);
					mName = "convert_deep_" + baseType.getClassName().replace('.', '_');
					callType = getType(Object[].class);
				}
			} else {
				rawType = propType;
				callType = findBaseType(propType);
			}

			boolean passClass = !ASMUtils.isPrimitive(rawType) && rawType.getInternalName().equals("java/lang/Enum");

			// call the convert method to convert the value to a NBTBase
			String desc = getMethodDescriptor(nbtBaseType, callType);
			CodePiece converted = nbtASMHooks.getMethod(mName, desc).callWith(property);

			nbtASMHooks
					.getMethod("putInto")
					.callWith(new VarInsnNode(ALOAD, type.nbtIdxWrite),
							propName,
							converted)
					.appendTo(write);
		}

		read.add(new InsnNode(RETURN));

		write.add(new InsnNode(RETURN));

		// add this at the end to not interfer with the findMethod calls above
		methods.add(readMethod);
		methods.add(writeMethod);
	}

	private static void insertSuperCall(ClassInfo clazz, InsnList insns, String methodName) {
		clazz.getMethod(methodName).callSuperWith(new VarInsnNode(ALOAD, 1)).appendTo(insns);
	}

	private static Type findBaseType(Type type) {
		if (isPrimitive(type)) {
			return type;
		}
		if (type.equals(stringType)) {
			return stringType;
		}
		ClassInfo ci = ClassInfo.of(type);
		if (ci.isEnum()) {
			return getType(Enum.class);
		}
		if (ci.isAssignableFrom(nbtSerializableClassInfo)) {
			return getType(NBTSerializable.class);
		}
		throw new IllegalArgumentException(String.format("Cannot auto-save objects of class %s to NBT!", type.getInternalName()));
	}

	private static ClassType findType(ClassInfo classInfo) {
		if (tileEntityClassInfo.isAssignableFrom(classInfo)) {
			return ClassType.TILE_ENTITY;
		} else if (entityClassInfo.isAssignableFrom(classInfo)) {
			return ClassType.ENTITY;
		} else if (entityPropsClassInfo.isAssignableFrom(classInfo)) {
			return ClassType.ENTITY_PROPS;
		} else {
			throw new IllegalStateException("Cannot save to NBT automatically in " + classInfo.internalName());
		}
	}

	private static enum ClassType {

		TILE_ENTITY(1, 1, "func_70307_a", "func_70310_b"), // readFromNBT, writeToNBT
		ENTITY(1, 1, "func_70037_a", "func_70014_b"), // readEntityFromNBT, writeEntityToNBT
		ENTITY_PROPS(1, 1, "loadNBTData", "saveNBTData");

		final int nbtIdxWrite;
		final int nbtIdxRead;
		private final String nbtReadSrg;
		private final String nbtWriteSrg;

		private ClassType(int nbtIdxWrite, int nbtIdxRead, String nbtReadSrg, String nbtWriteSrg) {
			this.nbtIdxWrite = nbtIdxWrite;
			this.nbtIdxRead = nbtIdxRead;
			this.nbtReadSrg = nbtReadSrg;
			this.nbtWriteSrg = nbtWriteSrg;
		}

		public String getNbtRead() {
			return this != ENTITY_PROPS ? MCPNames.method(nbtReadSrg) : nbtReadSrg;
		}

		public String getNbtWrite() {
			return this != ENTITY_PROPS ? MCPNames.method(nbtWriteSrg) : nbtWriteSrg;
		}

		public boolean[] shouldCallSuper(ClassInfo me, boolean readPresent, boolean writePresent) {
			String read = getNbtRead();
			String write = getNbtWrite();
			boolean readFound = readPresent;
			boolean writeFound = writePresent;
			// only need to compute if we have neither a read nor a write method present in the class
			if (!readFound || !writeFound) {
				switch (this) {
					case ENTITY:
						ClassInfo clazz = me;
						do {
							clazz = clazz.superclass();
							if (!readFound && clazz.hasMethod(read)) {
								readFound = true;
							}
							if (!writeFound && clazz.hasMethod(write)) {
								writeFound = true;
							}
						} while ((!readFound || !writeFound) && !clazz.superName().equals("java/lang/Object"));
						break;
					case ENTITY_PROPS:
						if (me.superName().equals("java/lang/Object")) {
							readFound = writeFound = false;
						} else {
							clazz = me;
							do {
								clazz = clazz.superclass();
								if (!readFound && clazz.hasMethod(read)) {
									readFound = true;
								}
								if (!writeFound && clazz.hasMethod(write)) {
									writeFound = true;
								}
							} while ((!readFound || !writeFound) && !clazz.superName().equals("java/lang/Object"));
						}
						break;
					case TILE_ENTITY:
						readFound = writeFound = false;
						break;
				}
			}
			// only call super if the method is not present in the current class
			return new boolean[] { !readPresent && !readFound, !writePresent && !writeFound };
		}
	}

}

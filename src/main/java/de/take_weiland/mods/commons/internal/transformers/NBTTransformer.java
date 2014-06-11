package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.NBTASMHooks;
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

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public class NBTTransformer {

	private static final int READ = 0;
	private static final int WRITE = 1;

	private static final Type nbtBaseType = getType(NBTBase.class);
	private static final Type nbtTagCompoundType = getType(NBTTagCompound.class);

	private static final ClassInfo tileEntityClassInfo = ClassInfo.of(TileEntity.class);
	private static final ClassInfo entityClassInfo = ClassInfo.of(Entity.class);
	private static final ClassInfo entityPropsClassInfo = ClassInfo.of(IExtendedEntityProperties.class);

	static void transform(ClassNode clazz, ClassInfo classInfo, ListIterator<MethodNode> methods) {
		ClassType type = findType(classInfo);
		Collection<ASMVariable> variables = ASMVariables.allWith(clazz, ToNbt.class, CodePieces.getThis());

		MethodNode readMethod = new MethodNode(ACC_PUBLIC, type.getNbtRead(), getMethodDescriptor(VOID_TYPE, nbtTagCompoundType), null, null);
		MethodNode writeMethod = new MethodNode(ACC_PUBLIC, type.getNbtWrite(), getMethodDescriptor(VOID_TYPE, nbtTagCompoundType), null, null);

		InsnList read = readMethod.instructions;
		InsnList write = writeMethod.instructions;

		// find any present NBT read/write method and rename them
		// this is needed because it is basically impossible to find all the possible exitpoints
		// (return, throw, etc.) of the method, which we would need to make sure our code always gets executed
		// instead we rename the present method and call it instead
		MethodNode presentReadMethod = ASMUtils.findMethod(clazz, type.getNbtRead());
		if (presentReadMethod != null) {
			presentReadMethod.name = "_sc$renamedNbtRead";
			presentReadMethod.access = (presentReadMethod.access & ~(ACC_PUBLIC | ACC_PROTECTED)) | ACC_PRIVATE;

			CodePieces.invoke(clazz, presentReadMethod,
					CodePieces.getThis(),
					CodePieces.of(new VarInsnNode(ALOAD, 1))).appendTo(read);
		}

		MethodNode presentWriteMethod = ASMUtils.findMethod(clazz, type.getNbtWrite());
		if (presentWriteMethod != null) {
			presentWriteMethod.name = "_sc$renamedNbtWrite";
			presentWriteMethod.access = (presentWriteMethod.access & ~(ACC_PUBLIC | ACC_PROTECTED)) | ACC_PRIVATE;

			CodePieces.invoke(clazz, presentWriteMethod,
					CodePieces.getThis(),
					CodePieces.of(new VarInsnNode(ALOAD, 1))).appendTo(write);
		}

		methods.add(readMethod);
		methods.add(writeMethod);

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

		for (ASMVariable variable : variables) {
			AnnotationNode ann = variable.getterAnnotation(ToNbt.class);
			String propName = ASMUtils.getAnnotationProperty(ann, "value", variable.name());

			CodePiece nbtValue = CodePieces.invokeStatic(
					NBTASMHooks.CLASS_NAME,	"getFrom", ASMUtils.getMethodDescriptor(NBTBase.class, NBTTagCompound.class, String.class),
					CodePieces.of(new VarInsnNode(ALOAD, 1)), CodePieces.constant(propName));


			ConvertInfo info = ConvertType.get(variable).createInfo(variable, nbtValue);
			info.convert().appendTo(write);

			CodePieces.invokeStatic(
					NBTASMHooks.CLASS_NAME, "putInto",
					ASMUtils.getMethodDescriptor(void.class, NBTTagCompound.class, String.class, NBTBase.class),
					CodePieces.of(new VarInsnNode(ALOAD, 1)),
					CodePieces.constant(propName),
					info.convert()).appendTo(write);

			variable.set(info.get()).appendTo(read);
		}

		read.add(new InsnNode(RETURN));
		write.add(new InsnNode(RETURN));

	}

	private static void insertSuperCall(ClassNode clazz, InsnList insns, String methodName) {
		CodePieces.invokeSuper(clazz,
				ASMUtils.findMethod(clazz, methodName),
				CodePieces.of(new VarInsnNode(ALOAD, 1))).appendTo(insns);
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

	private static enum ConvertType {

		DEFAULT {
			@Override
			ConvertInfo createInfo(ASMVariable property, CodePiece nbtValue) {
				Type type = property.getType();
				String getMethod = "get_" + type.getClassName().replace('.', '_');
				String getDesc = getMethodDescriptor(type, nbtBaseType);

				String convertMethod = "convert";
				String convertDesc = getMethodDescriptor(nbtBaseType, type);

				Object[] getArgs = { nbtValue };
				Object[] convertArgs = { property.get() };
				return new ConvertInfo(getMethod, convertMethod, getDesc, convertDesc, getArgs, convertArgs);
			}
		},
		ENUM {
			@Override
			ConvertInfo createInfo(ASMVariable property, CodePiece nbtValue) {
				Type type = property.getType();
				String getMethod = "get_java_lang_Enum";
				String getDesc = getMethodDescriptor(getType(Enum.class), nbtBaseType, getType(Class.class));

				String convertMethod = "convert";
				String convertDesc = getMethodDescriptor(nbtBaseType, getType(Enum.class));

				Object[] getArgs = { nbtValue, type };
				Object[] convertArgs = { property.get() };
				return new ConvertInfo(getMethod, convertMethod, getDesc, convertDesc, getArgs, convertArgs, property.getType());
			}
		},
		ONE_DIM_DEFAULT {
			@Override
			ConvertInfo createInfo(ASMVariable property, CodePiece nbtValue) {
				Type type = property.getType();
				Type elemType = type.getElementType();

				String getMethod = "get_" + elemType.getClassName().replace('.', '_') + "_arr";
				String getDesc = getMethodDescriptor(type, nbtBaseType);

				String convertMethod = "convert";
				String convertDesc = getMethodDescriptor(type, nbtBaseType);

				Object[] getArgs = { nbtValue };
				Object[] convertArgs = { property.get() };
				return new ConvertInfo(getMethod, convertMethod, getDesc, convertDesc, getArgs, convertArgs);
			}
		},
		ONE_DIM_ENUM {
			@Override
			ConvertInfo createInfo(ASMVariable property, CodePiece nbtValue) {
				Type type = property.getType();
				Type elemType = type.getElementType();

				String getMethod = "get_java_lang_Enum_arr";
				String getDesc = getMethodDescriptor(getType(Enum[].class), nbtBaseType, getType(Class.class));
				Object[] getArgs = { nbtValue, elemType };

				String convertMethod = "convert";
				String convertDesc = getMethodDescriptor(nbtBaseType, getType(Enum[].class));
				Object[] convertArgs = { property.get() };
				return new ConvertInfo(getMethod, convertMethod, getDesc, convertDesc, getArgs, convertArgs, property.getType());
			}
		},
		MULTI_DIM_DEFAULT {
			@Override
			ConvertInfo createInfo(ASMVariable property, CodePiece nbtValue) {
				Type type = property.getType();
				Type elemType = type.getElementType();

				String convClassName = elemType.getClassName().replace('.', '_');
				String getMethod = "get_deep_" + convClassName;
				String getDesc = getMethodDescriptor(getType(Object[].class), nbtBaseType, getType(Class.class), INT_TYPE);
				Object[] getArgs = { nbtValue, type, type.getDimensions() };

				String convertMethod = "convert_deep_" + convClassName;
				String convertDesc = getMethodDescriptor(nbtBaseType, getType(Object[].class), INT_TYPE);
				Object[] convertArgs = { property.get(), type.getDimensions() };
				return new ConvertInfo(getMethod, convertMethod, getDesc, convertDesc, getArgs, convertArgs, property.getType());
			}
		},
		MULTI_DIM_ENUM {
			@Override
			ConvertInfo createInfo(ASMVariable property, CodePiece nbtValue) {
				Type type = property.getType();

				String getMethod = "get_deep_java_lang_Enum";
				String getDesc = getMethodDescriptor(getType(Object[].class), nbtBaseType, getType(Class.class), INT_TYPE);
				Object[] getArgs = { nbtValue, type, type.getDimensions() };

				String convertMethod = "convert_deep_java_lang_Enum";
				String convertDesc = getMethodDescriptor(nbtBaseType, getType(Object[].class), INT_TYPE);
				Object[] convertArgs = { property.get(), type.getDimensions() };
				return new ConvertInfo(getMethod, convertMethod, getDesc, convertDesc, getArgs, convertArgs, property.getType());
			}
		};

		abstract ConvertInfo createInfo(ASMVariable property, CodePiece nbtValue);

		static ConvertType get(ASMVariable property) {
			Type type = property.getType();
			if (type.getSort() == ARRAY) {
				Type elemType = type.getElementType();
				boolean multiDim = type.getDimensions() != 1;
				if (isBaseType(elemType)) {
					return multiDim ? MULTI_DIM_DEFAULT : ONE_DIM_DEFAULT;
				} else if (isEnum(elemType)) {
					return multiDim ? MULTI_DIM_ENUM : ONE_DIM_ENUM;
				}
			} else if (isBaseType(type)) {
				return DEFAULT;
			} else if (isEnum(type)) {
				return ENUM;
			}
			throw new IllegalArgumentException("Cannot serialize class " + type.getClassName() + " to NBT!");
		}

		private static boolean isBaseType(Type t) {
			return ASMUtils.isPrimitive(t) || t.getInternalName().equals("java/lang/String");
		}

		private static boolean isEnum(Type t) {
			return ClassInfo.of(t).isEnum();
		}

	}

	private static class ConvertInfo {

		final String getMethod;
		final String convertMethod;
		final String getDesc;
		final String convertDesc;
		final Object[] getArgs;
		final Object[] convertArgs;
		final Type cast;

		ConvertInfo(String getMethod, String convertMethod, String getDesc, String convertDesc, Object[] getArgs, Object[] convertArgs) {
			this(getMethod, convertMethod, getDesc, convertDesc, getArgs, convertArgs, null);
		}

		ConvertInfo(String getMethod, String convertMethod, String getDesc, String convertDesc, Object[] getArgs, Object[] convertArgs, Type cast) {
			this.cast = cast;
			this.getMethod = getMethod;
			this.convertMethod = convertMethod;
			this.getDesc = getDesc;
			this.convertDesc = convertDesc;
			this.getArgs = getArgs;
			this.convertArgs = convertArgs;
		}

		CodePiece convert() {
			return CodePieces.invokeStatic(NBTASMHooks.CLASS_NAME, convertMethod, convertDesc,
					CodePieces.parse(convertArgs));
		}

		CodePiece get() {
			CodePiece piece = CodePieces.invokeStatic(NBTASMHooks.CLASS_NAME, getMethod, getDesc, CodePieces.parse(convertArgs));
			if (cast != null) {
				return CodePieces.castTo(cast, piece);
			} else {
				return piece;
			}
		}
	}

}

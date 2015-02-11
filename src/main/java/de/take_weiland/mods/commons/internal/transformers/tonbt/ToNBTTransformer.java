package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.asm.info.HasAnnotations;
import de.take_weiland.mods.commons.internal.NBTDynCallback;
import de.take_weiland.mods.commons.internal.transformers.ClassWithProperties;
import de.take_weiland.mods.commons.internal.transformers.PropertyBasedTransformer;
import de.take_weiland.mods.commons.internal.transformers.TransformerUtil;
import de.take_weiland.mods.commons.nbt.ToNbt;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Collection;
import java.util.List;

import static de.take_weiland.mods.commons.asm.MCPNames.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
public final class ToNBTTransformer implements PropertyBasedTransformer {

	@Override
	public boolean transform(ClassWithProperties clazz) {
		List<ASMVariable> vars = ASMVariables.allWith(clazz.clazz, ToNbt.class, CodePieces.getThis());
		if (vars.isEmpty()) {
			return false;
		}

		ClassType type = ClassType.typeOf(clazz.classInfo);

		int level = determineLevel(clazz.classInfo);
		MethodNode levelMethod = makeLevelMethod(clazz.clazz, level);
		ASMCondition isCurrentLvl = isCurrentLevel(clazz.clazz, levelMethod, level);

		MethodNode writeMethod = makeWriteMethod(clazz.clazz, level > 0);
		MethodNode readMethod = makeReadMethod(clazz.clazz, level > 0);

		CodeBuilder writeCode = new CodeBuilder();
		CodeBuilder readCode = new CodeBuilder();

		CodePiece nbt = CodePieces.getLocal(1);
		for (ASMVariable var : vars) {
            String varKey = var.name();

            Handle get = var.getterHandle();
            Handle set = var.setterHandle();

            String name = var.rawName();
            String desc = Type.getMethodDescriptor(getType(NBTBase.class), getObjectType(clazz.clazz.name));

            CodePiece writtenNbt = CodePieces.invokeDynamic(name, desc, CodePieces.getThis())
                    .withBootstrap(getInternalName(NBTDynCallback.class), "makeNBTWrite", var.isMethod() ? 1 : 0, get, set);

			writeCode.add(CodePieces.invokeVirtual(NBTTagCompound.class, "setTag", nbt, void.class,
                    String.class, varKey,
                    NBTBase.class, writtenNbt));
		}
		writeCode.add(new InsnNode(RETURN));
		readCode.add(new InsnNode(RETURN));

		writeCode.build().appendTo(writeMethod.instructions);

		readCode.build().appendTo(readMethod.instructions);

		CodePiece callWrite = CodePieces.invoke(clazz.clazz.name, writeMethod, CodePieces.getThis(), nbt);
		CodePiece callRead = CodePieces.invoke(clazz.clazz.name, readMethod, CodePieces.getThis(), nbt);

		type.injectWrite(clazz, isCurrentLvl.doIfTrue(callWrite), level > 0);
		type.injectRead(clazz, isCurrentLvl.doIfTrue(callRead), level > 0);

		return true;
	}

	private static int determineLevel(ClassInfo clazz) {
		clazz = clazz.superclass();
		int level = 0;

		while (clazz != null) {
			if (hasToNbt(clazz)) {
				level++;
			}
			clazz = clazz.superclass();
		}
		return level;
	}

	private static boolean hasToNbt(ClassInfo clazz) {
		return anyNBT(clazz.getFields()) || anyNBT(clazz.getMethods());
	}

	private static boolean anyNBT(Collection<? extends HasAnnotations> members) {
		for (HasAnnotations member : members) {
			if (member.hasAnnotation(ToNbt.class)) {
				return true;
			}
		}
		return false;
	}

	private static MethodNode makeLevelMethod(ClassNode clazz, int level) {
		String name = "_sc$tonbt$lvl";
		String desc = Type.getMethodDescriptor(INT_TYPE);
		MethodNode method = new MethodNode(ACC_PROTECTED, name, desc, null, null);
		clazz.methods.add(method);
		CodePieces.constant(level)
				.append(new InsnNode(IRETURN))
				.appendTo(method.instructions);
		return method;
	}

	private static ASMCondition isCurrentLevel(ClassNode clazz, MethodNode levelMethod, int myLevel) {
		CodePiece currentLevel = CodePieces.invokeVirtual(clazz.name, levelMethod.name,
				CodePieces.getThis(), int.class);
		return ASMCondition.isSame(currentLevel, CodePieces.constant(myLevel), Type.INT_TYPE);
	}

	private static MethodNode makeWriteMethod(ClassNode clazz, boolean callSuper) {
		String name = "_sc$tonbt$write";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(NBTTagCompound.class));
		MethodNode method = new MethodNode(ACC_PROTECTED, name, desc, null, null);
		clazz.methods.add(method);
		if (callSuper) {
			method.instructions.add(new VarInsnNode(ALOAD, 0));
			method.instructions.add(new VarInsnNode(ALOAD, 1));
			method.instructions.add(new MethodInsnNode(INVOKESPECIAL, clazz.superName, name, desc));
		}
		return method;
	}

	private static MethodNode makeReadMethod(ClassNode clazz, boolean callSuper) {
		String name = "_sc$tonbt$read";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(NBTTagCompound.class));
		MethodNode method = new MethodNode(ACC_PROTECTED, name, desc, null, null);
		clazz.methods.add(method);
		if (callSuper) {
			method.instructions.add(new VarInsnNode(ALOAD, 0));
			method.instructions.add(new VarInsnNode(ALOAD, 1));
			method.instructions.add(new MethodInsnNode(INVOKESPECIAL, clazz.superName, name, desc));
		}
		return method;
	}

	enum ClassType {

		TILE_ENTITY(MCPNames.method(M_WRITE_TO_NBT_TILEENTITY), MCPNames.method(M_READ_FROM_NBT_TILEENTITY)),
		ENTITY(MCPNames.method(M_WRITE_ENTITY_TO_NBT), MCPNames.method(M_READ_ENTITY_FROM_NBT)),
		ENTITY_PROPS("saveNBTData", "loadNBTData");

		private final String writeMethod;
		private final String readMethod;

		private ClassType(String writeMethod, String readMethod) {
			this.writeMethod = writeMethod;
			this.readMethod = readMethod;
		}

		static ClassType typeOf(ClassInfo clazz) {
			if (ClassInfo.of(TileEntity.class).isAssignableFrom(clazz)) {
				return TILE_ENTITY;
			} else if (ClassInfo.of(Entity.class).isAssignableFrom(clazz)) {
				return ENTITY;
			} else if (ClassInfo.of(IExtendedEntityProperties.class).isAssignableFrom(clazz)) {
				return ENTITY_PROPS;
			} else {
				throw new IllegalStateException("Don't know how to save to NBT in class " + clazz.internalName());
			}
		}

		final void injectWrite(ClassWithProperties clazz, CodePiece code, boolean superCertain) {
			inject(writeMethod, clazz, code, superCertain);
		}

		final void injectRead(ClassWithProperties clazz, CodePiece code, boolean superCertain) {
			inject(readMethod, clazz, code, superCertain);
		}

		private void inject(String name, ClassWithProperties clazz, CodePiece code, boolean superCertain) {
			String desc = Type.getMethodDescriptor(VOID_TYPE, getType(NBTTagCompound.class));
			TransformerUtil.addOrOverride(clazz.clazz, clazz.classInfo, name, desc, code, superCertain);
		}

	}
}

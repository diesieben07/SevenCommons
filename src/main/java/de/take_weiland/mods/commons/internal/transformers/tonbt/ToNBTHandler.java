package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.transformers.ClassWithProperties;
import de.take_weiland.mods.commons.nbt.ToNbt;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidTank;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import static de.take_weiland.mods.commons.asm.MCPNames.M_GET_TAG;
import static de.take_weiland.mods.commons.asm.MCPNames.M_SET_TAG;

/**
 * @author diesieben07
 */
abstract class ToNBTHandler {

	static ToNBTHandler create(ClassWithProperties clazz, ASMVariable var) {
		Type type = var.getType();
		String internalName = type.getInternalName();
		if (ASMUtils.isPrimitive(type)) {
			return new PrimitiveHandler(var);
		} else if (SimpleIntrinsicsHandler.isIntrinsic(type)) {
			return new SimpleIntrinsicsHandler(type, var);
		} else if (internalName.equals("java/util/EnumSet")) {
			return new EnumSetHandler(clazz, var);
		}
		if (type.getSort() == Type.ARRAY) {
			return new ArrayHandler(clazz, var);
		}
		ClassInfo ci = ClassInfo.of(type);
		if (ci.isEnum()) {
			return new EnumHandler(var);
		} else if (ClassInfo.of(FluidTank.class).isAssignableFrom(ci)) {
			return new FluidTankHandler(var);
		} else {
			return new DelegatingHandler(clazz, var);
		}
	}

	final String key;
	ASMVariable var;
	final ToNbt annotation;

	ToNBTHandler(ASMVariable var) {
		this.var = var;
		annotation = var.getAnnotation(ToNbt.class);

		String key = annotation.key();
		if (key.isEmpty()) {
			key = var.name();
		}
		this.key = key;
	}

	void overrideVar(ASMVariable var) {
		this.var = var;
	}

	void initialTransform(MethodNode readMethod, MethodNode writeMethod) { }

	final CodePiece write(CodePiece nbtCompound, MethodNode writeMethod) {
		String setTagMethod = MCPNames.method(M_SET_TAG);
		return CodePieces.invokeVirtual(NBTTagCompound.class, setTagMethod, nbtCompound, void.class,
				String.class, CodePieces.constant(key),
				NBTBase.class, makeNBT(writeMethod));
	}

	final CodePiece read(CodePiece nbtCompound, MethodNode readMethod) {
		String getTagMethod = MCPNames.method(M_GET_TAG);
		CodePiece tag = CodePieces.invokeVirtual(NBTTagCompound.class, getTagMethod, nbtCompound, NBTBase.class,
				String.class, CodePieces.constant(key));
		return consumeNBT(tag, readMethod);
	}

	abstract CodePiece makeNBT(MethodNode writeMethod);

	abstract CodePiece consumeNBT(CodePiece nbt, MethodNode readMethod);
}

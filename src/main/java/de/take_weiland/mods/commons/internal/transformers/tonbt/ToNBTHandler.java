package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.transformers.ClassWithProperties;
import de.take_weiland.mods.commons.nbt.ToNbt;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidTank;
import org.objectweb.asm.Type;

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

	void initialTransform() { }

	final CodePiece write(CodePiece nbtCompound, MethodContext context) {
		String setTagMethod = MCPNames.method(M_SET_TAG);
		return CodePieces.invokeVirtual(NBTTagCompound.class, setTagMethod, nbtCompound, void.class,
				String.class, CodePieces.constant(key),
				NBTBase.class, makeNBT(context));
	}

	final CodePiece read(CodePiece nbtCompound) {
		String getTagMethod = MCPNames.method(M_GET_TAG);
		CodePiece tag = CodePieces.invokeVirtual(NBTTagCompound.class, getTagMethod, nbtCompound, NBTBase.class,
				String.class, CodePieces.constant(key));
		return consumeNBT(tag);
	}

	abstract CodePiece makeNBT(MethodContext context);

	abstract CodePiece consumeNBT(CodePiece nbt);
}

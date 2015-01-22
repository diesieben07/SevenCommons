package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.transformers.ClassWithProperties;
import de.take_weiland.mods.commons.nbt.ToNbt;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.objectweb.asm.Type;

import static de.take_weiland.mods.commons.asm.MCPNames.M_GET_TAG;
import static de.take_weiland.mods.commons.asm.MCPNames.M_SET_TAG;

/**
 * @author diesieben07
 */
abstract class ToNBTHandler {

	static ToNBTHandler create(ClassWithProperties clazz, ASMVariable var) {
		Type type = var.getType();
		if (ASMUtils.isPrimitive(type)) {
			return new PrimitiveHandler(var);
		} else if (SimpleIntrinsicsHandler.isIntrinsic(type)) {
			return new SimpleIntrinsicsHandler(type, var);
		} else if (type.getInternalName().equals("java/util/EnumSet")) {
			return new EnumSetHandler(clazz, var);
		} else if (EnumHandler.isEnum(type)) {
			return new EnumHandler(var);
		} else {
			return new DelegatingHandler(clazz, var);
		}
	}

	final String key;
	final ASMVariable var;
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

	final CodePiece write(CodePiece nbtCompound) {
		String setTagMethod = MCPNames.method(M_SET_TAG);
		return CodePieces.invokeVirtual(NBTTagCompound.class, setTagMethod, nbtCompound, void.class,
				String.class, CodePieces.constant(key),
				NBTBase.class, makeNBT());
	}

	final CodePiece read(CodePiece nbtCompound) {
		String getTagMethod = MCPNames.method(M_GET_TAG);
		CodePiece tag = CodePieces.invokeVirtual(NBTTagCompound.class, getTagMethod, nbtCompound, NBTBase.class,
				String.class, CodePieces.constant(key));
		return consumeNBT(tag);
	}

	abstract CodePiece makeNBT();

	abstract CodePiece consumeNBT(CodePiece nbt);
}

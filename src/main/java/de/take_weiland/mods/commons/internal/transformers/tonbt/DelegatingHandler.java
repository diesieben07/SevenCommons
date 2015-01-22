package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.SerializerRegistry;
import de.take_weiland.mods.commons.internal.transformers.ClassWithProperties;
import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.sync.Property;
import net.minecraft.nbt.NBTBase;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
final class DelegatingHandler extends ToNBTHandler {

	private final ClassWithProperties properties;
	private CodePiece property;
	private ASMVariable serializer;

	DelegatingHandler(ClassWithProperties properties, ASMVariable var) {
		super(var);
		this.properties = properties;
	}

	@Override
	void initialTransform() {
		property = properties.getProperty(var, ClassWithProperties.PropertyType.NORMAL);

		String name = "_sc$tonbt$ser$" + ClassWithProperties.identifier(var);
		String desc = Type.getDescriptor(NBTSerializer.class);
		FieldNode serializerField = new FieldNode(ACC_PRIVATE | ACC_STATIC, name, desc, null, null);
		properties.clazz.fields.add(serializerField);
		serializer = ASMVariables.of(properties.clazz, serializerField);

		ASMUtils.initializeLate(properties.clazz, serializer.set(
				CodePieces.invokeStatic(SerializerRegistry.class, "getNBTSerializer", NBTSerializer.class,
						TypeSpecification.class, property))
		);
	}

	@Override
	CodePiece makeNBT() {
		return CodePieces.invokeInterface(NBTSerializer.class, "serialize", serializer.get(), NBTBase.class,
				Property.class, property,
				Object.class, CodePieces.getThis());
	}

	@Override
	CodePiece consumeNBT(CodePiece nbt) {
		return CodePieces.invokeStatic(ASMHooks.class, ASMHooks.READ_NBT, void.class,
				NBTBase.class, nbt,
				NBTSerializer.class, serializer.get(),
				Property.class, property,
				Object.class, CodePieces.getThis());
	}
}

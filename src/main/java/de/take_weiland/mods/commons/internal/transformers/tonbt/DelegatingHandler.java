package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.internal.SerializerRegistry;
import de.take_weiland.mods.commons.internal.transformers.ClassWithProperties;
import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.sync.Property;
import net.minecraft.nbt.NBTBase;
import org.objectweb.asm.Type;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getType;

/**
 * @author diesieben07
 */
final class DelegatingHandler extends ToNBTHandler {

	private final ClassWithProperties properties;
	private CodePiece property;

	DelegatingHandler(ClassWithProperties properties, ASMVariable var) {
		super(var);
		this.properties = properties;
	}

	@Override
	void initialTransform() {
		property = properties.getProperty(var, ClassWithProperties.PropertyType.NORMAL);
	}

	@Override
	CodePiece makeNBT() {
		return CodePieces.invokeInterface(NBTSerializer.class, "serialize", getSerializer(), NBTBase.class,
				Property.class, property,
				Object.class, CodePieces.getThis());
	}

	private CodePiece getSerializer() {
		return CodePieces.invokeDynamic(SerializerRegistry.GET_NBT_SERIALIZER, Type.getMethodDescriptor(getType(NBTSerializer.class)))
			.withBootstrap(getInternalName(SerializerRegistry.class), "indyBootstrap",
					Type.getObjectType(properties.clazz.name),
					properties.getFieldName(var));
	}

}

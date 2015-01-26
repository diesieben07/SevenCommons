package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.SerializerRegistry;
import de.take_weiland.mods.commons.internal.transformers.ClassWithProperties;
import de.take_weiland.mods.commons.nbt.NBTData;
import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.sync.Property;
import net.minecraft.nbt.NBTBase;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

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
	void initialTransform(MethodNode readMethod, MethodNode writeMethod) {
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
	CodePiece makeNBT(MethodNode writeMethod) {
		ASMCondition notNull = ASMCondition.isNotNull(var.get());

		CodePiece invokeSerializer = CodePieces.invokeInterface(NBTSerializer.class, "serialize", serializer.get(), NBTBase.class,
				Property.class, property,
				Object.class, CodePieces.getThis());

		CodePiece getNull = CodePieces.invokeStatic(NBTData.class, "serializedNull", NBTBase.class);

		return notNull.doIfElse(invokeSerializer, getNull);
	}

	@Override
	CodePiece consumeNBT(CodePiece nbt, MethodNode readMethod) {
		LocalVariable nbtCache = new LocalVariable(readMethod, Type.getType(NBTBase.class));
		ASMCondition isNull = ASMCondition.isTrue(CodePieces.invokeStatic(NBTData.class, "isSerializedNull", boolean.class,
				NBTBase.class, nbtCache.get()));

		CodeBuilder cb = new CodeBuilder();
		cb.add(nbtCache.set(nbt));

		CodePiece setNull = var.set(CodePieces.constantNull());

		CodePiece unserialize = CodePieces.invokeInterface(NBTSerializer.class, "deserialize", serializer.get(), void.class,
				NBTBase.class, nbtCache.get(),
				Property.class, property,
				Object.class, CodePieces.getThis());

		cb.add(isNull.doIfElse(setNull, unserialize));
		return cb.build();
	}
}

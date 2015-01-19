package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.sync.SyncASMHooks;
import de.take_weiland.mods.commons.sync.Property;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class ClassWithProperties {

	public final ClassNode clazz;
	public final ClassInfo classInfo;
	private final Map<String, PropertyType> types = new HashMap<>();
	private final Map<String, ASMVariable> variables = new HashMap<>();

	ClassWithProperties(ClassNode clazz, ClassInfo classInfo) {
		this.clazz = clazz;
		this.classInfo = classInfo;
	}

	public CodePiece getProperty(ASMVariable var, PropertyType type) {
		String ident = identifier(var);

		PropertyType presentType = types.get(ident);
		if (presentType == null || presentType == PropertyType.NORMAL && type == PropertyType.SYNCED) {
			types.put(ident, type);
		}
		if (!variables.containsKey(ident)) {
			variables.put(ident, var);
		}
		return CodePieces.getField(clazz.name, propertyFieldName(ident), type.propertyClass);
	}

	public String getFieldName(ASMVariable var) {
		return propertyFieldName(identifier(var));
	}

	void createFields() {
		CodeBuilder init = new CodeBuilder();

		for (Map.Entry<String, ASMVariable> entry : variables.entrySet()) {
			String ident = entry.getKey();

			PropertyType propertyType = types.get(ident);
			ASMVariable variable = entry.getValue();

			String name = propertyFieldName(ident);
			String desc = Type.getDescriptor(propertyType.propertyClass);
			FieldNode field = new FieldNode(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, name, desc, null, null);
			clazz.fields.add(field);

			CodePiece newProp;
			switch (propertyType) {
				case NORMAL:
					if (variable.isField()) {
						CodePiece fieldObj = getReflectField(variable.rawName());

						newProp = CodePieces.invokeStatic(SyncASMHooks.class, "makeProperty", Property.class,
								Field.class, fieldObj);
					} else {
						CodePiece getterObj = getReflectMethod(variable.rawName());
						CodePiece setterObj = getReflectMethod(variable.setterName());

						newProp = CodePieces.invokeStatic(SyncASMHooks.class, "makeProperty", Property.class,
								Method.class, getterObj,
								Method.class, setterObj);
					}
					break;
				case SYNCED:
					name = companionName(ident);
					desc = Type.getDescriptor(Object.class);
					FieldNode companion = new FieldNode(ACC_PRIVATE, name, desc, null, null);
					clazz.fields.add(companion);

					CodePiece companionFieldObj = getReflectField(name);
					if (variable.isField()) {
						CodePiece fieldObj = getReflectField(variable.rawName());

						newProp = CodePieces.invokeStatic(SyncASMHooks.class, "makeSyncableProperty", SyncableProperty.class,
								Field.class, fieldObj,
								Field.class, companionFieldObj);
					} else {
						CodePiece getterObj = getReflectMethod(variable.rawName());
						CodePiece setterObj = getReflectMethod(variable.setterName());

						newProp = CodePieces.invokeStatic(SyncASMHooks.class, "makeSyncableProperty", SyncableProperty.class,
								Method.class, getterObj,
								Method.class, setterObj,
								Field.class, companion);
					}
					break;
				default:
					throw new AssertionError();
			}

			init.add(CodePieces.setField(clazz, field, newProp));
		}

		ASMUtils.initializeStatic(clazz, init.build());
	}

	private CodePiece getReflectMethod(String name, Class<?>... args) {
		CodePiece myClass = CodePieces.constant(Type.getObjectType(clazz.name));
		return CodePieces.invokeVirtual(Class.class, "getDeclaredMethod", myClass, Method.class,
				String.class, name,
				Class[].class, CodePieces.constant(args));
	}

	private CodePiece getReflectField(String name) {
		CodePiece myClass = CodePieces.constant(Type.getObjectType(clazz.name));
		return CodePieces.invokeVirtual(Class.class, "getDeclaredField", myClass, Field.class,
				String.class, name);
	}
	private static String propertyFieldName(String ident) {
		return "_sc$prop$" + ident;
	}

	private static String companionName(String ident) {
		return "_sc$prop$c$" + ident;
	}

	private static String identifier(ASMVariable var) {
		return (var.isField() ? "f$" : "m$") + var.rawName();
	}

	public enum PropertyType {

		NORMAL(Property.class, "makeProperty"),
		SYNCED(SyncableProperty.class, "makeSyncedProperty");

		final Class<?> propertyClass;
		final String newPropertyMethod;

		private PropertyType(Class<?> propertyClass, String newPropertyMethod) {
			this.propertyClass = propertyClass;
			this.newPropertyMethod = newPropertyMethod;
		}

	}

}

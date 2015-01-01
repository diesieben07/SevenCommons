package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.EntityProxy;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static de.take_weiland.mods.commons.asm.MCPNames.F_IS_REMOTE;
import static de.take_weiland.mods.commons.asm.MCPNames.F_WORLD_OBJ_ENTITY;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getType;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class EntityTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		FieldNode syncPropsField = addPropsFieldAndAccessors(clazz);

		patchAddExtProp(clazz);
		patchOnUpdate(clazz, syncPropsField);

		clazz.interfaces.add(EntityProxy.CLASS_NAME);

		return true;
	}

	private static void patchAddExtProp(ClassNode clazz) {
		MethodNode method = ASMUtils.requireMethod(clazz, "registerExtendedProperties", ASMUtils.getMethodDescriptor(String.class, String.class, IExtendedEntityProperties.class));
		AbstractInsnNode ret = ASMUtils.findLastReturn(method);

		CodePiece ident = CodePieces.of(new VarInsnNode(ALOAD, 1));
		CodePiece props = CodePieces.of(new VarInsnNode(ALOAD, 2));

		CodePieces.invokeStatic(ASMHooks.class, ASMHooks.ON_NEW_ENTITY_PROPS, void.class,
				clazz, CodePieces.getThis(),
				IExtendedEntityProperties.class, props,
				String.class, ident)
				.insertBefore(method.instructions, ret);
	}

	private static void patchOnUpdate(ClassNode clazz, FieldNode syncPropsField) {
		MethodNode method = ASMUtils.requireMinecraftMethod(clazz, MCPNames.M_ON_UPDATE);
		CodePiece invoke = CodePieces.invokeStatic(ASMHooks.class, ASMHooks.TICK_SYNC_PROPS, void.class,
				List.class, CodePieces.getField(clazz, syncPropsField, CodePieces.getThis()));

		Type worldType = Type.getObjectType("net/minecraft/world/World");
		CodePiece world = CodePieces.getField(clazz.name, MCPNames.field(F_WORLD_OBJ_ENTITY), worldType, CodePieces.getThis());
		CodePiece isRemote = CodePieces.getField(worldType.getInternalName(), MCPNames.field(F_IS_REMOTE), BOOLEAN_TYPE, world);

		ASMCondition isServer = ASMCondition.isFalse(isRemote);

		isServer.doIfTrue(invoke).prependTo(method.instructions);
	}

	private static FieldNode addPropsFieldAndAccessors(ClassNode clazz) {
		FieldNode field = new FieldNode(ACC_PRIVATE, "_sc$syncedprops", Type.getDescriptor(List.class), null, null);
		clazz.fields.add(field);

		String name = EntityProxy.GET_PROPERTIES;
		String desc = Type.getMethodDescriptor(getType(List.class));
		MethodNode get = new MethodNode(ACC_PUBLIC | ACC_FINAL, name, desc, null, null);
		clazz.methods.add(get);

		CodePieces.getField(clazz, field, CodePieces.getThis()).appendTo(get.instructions);
		get.instructions.add(new InsnNode(ARETURN));

		name = EntityProxy.SET_PROPERTIES;
		desc = Type.getMethodDescriptor(VOID_TYPE, getType(List.class));
		MethodNode set = new MethodNode(ACC_PUBLIC | ACC_FINAL, name, desc, null, null);
		clazz.methods.add(set);

		CodePieces.setField(clazz, field, CodePieces.getThis(), CodePieces.of(new VarInsnNode(ALOAD, 1)))
				.appendTo(set.instructions);

		set.instructions.add(new InsnNode(RETURN));
		return field;
	}

	@Override
	public boolean transforms(String internalName) {
		return "net/minecraft/entity/Entity".equals(internalName);
	}
}

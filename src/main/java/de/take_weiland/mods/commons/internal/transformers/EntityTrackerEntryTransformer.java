package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.util.JavaUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static de.take_weiland.mods.commons.asm.MCPNames.M_TRY_START_WATCHING_THIS_MCP;
import static de.take_weiland.mods.commons.asm.MCPNames.M_TRY_START_WATCHING_THIS_SRG;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

public class EntityTrackerEntryTransformer implements ASMClassTransformer {

	@Override
	public boolean transform(ClassNode clazz, ClassInfo classInfo) {
		MethodNode method = ASMUtils.requireMinecraftMethod(clazz, M_TRY_START_WATCHING_THIS_MCP, M_TRY_START_WATCHING_THIS_SRG);

		generateEventCall(clazz).insertBefore(findInsertionHook(method));

		return true;
	}

	private CodePiece generateEventCall(ClassNode clazz) {
		Type entityPlayer = getObjectType("net/minecraft/entity/player/EntityPlayer");
		Type entity = getObjectType("net/minecraft/entity/Entity");

		String myEntity = MCPNames.field(MCPNames.F_MY_ENTITY_SRG);

		String methodDesc = getMethodDescriptor(VOID_TYPE, entityPlayer, entity);

		return CodePieces.invokeStatic(ASMHooks.CLASS_NAME, "onStartTracking", methodDesc,
				CodePieces.of(new VarInsnNode(ALOAD, 1)),
				CodePieces.getField(clazz.name, myEntity, entity.getDescriptor(), CodePieces.getThis()));
	}

	private CodeLocation findInsertionHook(MethodNode method) {
		String sendPacketToPlayer = MCPNames.method("func_72567_b");

		// oh how i wish for Java8...

		// extracts the name of a MethodInsnNode
		Function<MethodInsnNode, String> methodInsnName = new Function<MethodInsnNode, String>() {
			@Override
			public String apply(MethodInsnNode input) {
				return input.name;
			}
		};

		// checks if the MethodInsnNode invokes the sendPacketToPlayer method
		Predicate<MethodInsnNode> isPacketToPlayer = Predicates.compose(
				Predicates.equalTo(sendPacketToPlayer),
				methodInsnName
		);

		// checks if the instruction is a MethodInsnNode and invokes the sendPacketToPlayer method
		Predicate<AbstractInsnNode> invokePacketToPlayer =
				JavaUtils.instanceOfAnd(
						MethodInsnNode.class,
						isPacketToPlayer);

		return ASMUtils.searchIn(method.instructions)
				.backwards()
				.jumpToEnd()
				.find(invokePacketToPlayer)
				.forwards()
				.find(GOTO)
				.find(GOTO)
				.startHere()
				.endHere();
	}

	@Override
	public boolean transforms(String className) {
		return "net/minecraft/entity/EntityTrackerEntry".equals(className);
	}

}

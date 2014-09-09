package de.take_weiland.mods.commons.internal.transformers.nbt;

import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.nbt.ToNbt;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.List;

import static de.take_weiland.mods.commons.asm.CodePieces.constant;
import static de.take_weiland.mods.commons.asm.MCPNames.M_SET_TAG;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
class AutoNBTImpl {

	final ClassNode clazz;
	final List<NBTPropHandler> handlers;

	AutoNBTImpl(ClassNode clazz) {
		this.clazz = clazz;

		List<ASMVariable> vars = ASMVariables.allWith(clazz, ToNbt.class, CodePieces.getThis());
		handlers = Lists.newArrayListWithCapacity(vars.size());
		for (ASMVariable var : vars) {
			handlers.add(NBTPropHandler.create(var));
		}
	}

	void transform() {
		addReadMethod();
		addWriteMethod();
	}

	private void addReadMethod() {
		String name = "_sc$nbt$read";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(NBTTagCompound.class));
		MethodNode method = new MethodNode(ACC_PRIVATE | ACC_TRANSIENT, name, desc, null, null);

		LabelNode start = new LabelNode();
		LabelNode end = new LabelNode();

		method.localVariables.add(new LocalVariableNode("this", getObjectType(clazz.name).getDescriptor(), null, start, end, 0));
		method.localVariables.add(new LocalVariableNode("nbt", getDescriptor(NBTTagCompound.class), null, start, end, 1));
		method.localVariables.add(new LocalVariableNode("tag", getDescriptor(NBTBase.class), null, start, end, 2));

		method.instructions.add(start);

		ASMVariable tagVar = ASMVariables.local(method, 2);

		CodePiece compound = CodePieces.of(new VarInsnNode(ALOAD, 1));

		for (NBTPropHandler handler : handlers) {
			String owner = Type.getInternalName(NBTTagCompound.class);
			name = "getTag";
			desc = Type.getMethodDescriptor(getType(NBTBase.class), getType(String.class));
			CodePiece tag = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, compound, constant(handler.getKey()));

			tagVar.set(tag).appendTo(method.instructions);

			handler.readFromNbt(tagVar.get()).appendTo(method.instructions);
		}

		method.instructions.add(new InsnNode(RETURN));
		method.instructions.add(end);

		clazz.methods.add(method);
	}

	private void addWriteMethod() {
		String name = "_sc$nbt$write";
		String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(NBTTagCompound.class));
		MethodNode method = new MethodNode(ACC_PRIVATE | ACC_TRANSIENT, name, desc, null, null);

		LabelNode start = new LabelNode();
		LabelNode end = new LabelNode();

		method.localVariables.add(new LocalVariableNode("this", getObjectType(clazz.name).getDescriptor(), null, start, end, 0));
		method.localVariables.add(new LocalVariableNode("nbt", getDescriptor(NBTTagCompound.class), null, start, end, 1));

		ASMVariable nbt = ASMVariables.local(method, 1);

		method.instructions.add(start);

		String owner = getInternalName(NBTTagCompound.class);
		name = MCPNames.method(M_SET_TAG);
		desc = Type.getMethodDescriptor(VOID_TYPE, getType(String.class), getType(NBTBase.class));

		for (NBTPropHandler handler : handlers) {
			CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, nbt.get(), constant(handler.getKey()), handler.toNbt()).appendTo(method.instructions);
		}

		method.instructions.add(new InsnNode(RETURN));

		method.instructions.add(end);
		clazz.methods.add(method);
	}

}

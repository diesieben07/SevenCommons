package de.take_weiland.mods.commons.internal.transformers.tonbt;

import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.asm.*;
import de.take_weiland.mods.commons.internal.transformers.ClassWithProperties;
import de.take_weiland.mods.commons.internal.transformers.PropertyBasedTransformer;
import de.take_weiland.mods.commons.nbt.ToNbt;
import net.minecraft.nbt.NBTTagCompound;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getType;

/**
 * @author diesieben07
 */
public class ToNBTTransformer implements PropertyBasedTransformer {

	@Override
	public boolean transform(ClassWithProperties clazz) {
		List<ASMVariable> vars = ASMVariables.allWith(clazz.clazz, ToNbt.class, CodePieces.getThis());
		if (vars.isEmpty()) {
			return false;
		}

		List<ToNBTHandler> handlers = Lists.newArrayListWithCapacity(vars.size());
		for (ASMVariable var : vars) {
			handlers.add(ToNBTHandler.create(clazz, var));
		}

		MethodNode method = new MethodNode(ACC_PUBLIC, "_sc$tonbt", Type.getMethodDescriptor(VOID_TYPE, getType(NBTTagCompound.class)), null, null);
		clazz.clazz.methods.add(method);

		CodePiece nbt = CodePieces.getLocal(1);
		for (ToNBTHandler handler : handlers) {
			handler.initialTransform();
			handler.write(nbt).appendTo(method.instructions);
		}

		method.instructions.add(new InsnNode(RETURN));

		return true;
	}
}

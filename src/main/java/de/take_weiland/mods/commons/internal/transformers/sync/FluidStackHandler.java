package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMCondition;
import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getType;

/**
 * @author diesieben07
 */
class FluidStackHandler extends PropertyHandler {

	private ASMVariable companion;

	FluidStackHandler(ASMVariable var, int idx) {
		super(var, idx);
	}

	@Override
	void initialTransform(TransformState state) {
		companion = createCompanion(state);
	}

	@Override
	ASMCondition hasChanged() {
		String owner = Type.getInternalName(Fluids.class);
		String name = "identical";
		String desc = Type.getMethodDescriptor(BOOLEAN_TYPE, getType(FluidStack.class), getType(FluidStack.class));
		return ASMCondition.ifTrue(CodePieces.invokeStatic(owner, name, desc, var.get(), companion.get()));
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		String owner = Type.getInternalName(MCDataOutputStream.class);
		String name = "writeFluidStack";
		String desc = Type.getMethodDescriptor(VOID_TYPE, getType(FluidStack.class));
		CodePiece write = CodePieces.invoke(INVOKEVIRTUAL, owner, name, desc, stream, var.get());

		owner = Type.getInternalName(Fluids.class);
		name = "clone";
		desc = Type.getMethodDescriptor(getType(FluidStack.class), getType(FluidStack.class));
		CodePiece copy = companion.set(CodePieces.invokeStatic(owner, name, desc, var.get()));
		return write.append(copy);
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = Type.getInternalName(MCDataInputStream.class);
		String name = "readFluidStack";
		String desc = Type.getMethodDescriptor(getType(FluidStack.class));
		return var.set(CodePieces.invokeVirtual(owner, name, desc, stream));
	}
}

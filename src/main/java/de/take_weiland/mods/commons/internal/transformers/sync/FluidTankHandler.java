package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMCondition;
import de.take_weiland.mods.commons.asm.ASMVariable;
import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.asm.CodePieces;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.Watch;
import de.take_weiland.mods.commons.util.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;

import static org.objectweb.asm.Type.*;

/**
 * @author diesieben07
 */
class FluidTankHandler extends PropertyHandler {

	private ASMVariable companion;

	FluidTankHandler(ASMVariable var, int idx) {
		super(var, idx);
	}

	@Override
	void initialTransform(TransformState state) {
		companion = createCompanion(state, Type.getType(FluidStack.class));
	}

	@Override
	ASMCondition hasChanged() {
		CodePiece tankFluid = getTankFluid();
		CodePiece oldFluid = companion.get();

		String owner = getInternalName(Fluids.class);
		String name = "identical";
		String desc = Type.getMethodDescriptor(BOOLEAN_TYPE, getType(FluidStack.class), getType(FluidStack.class));
		return ASMCondition.ifTrue(CodePieces.invokeStatic(owner, name, desc, tankFluid, oldFluid)).negate();
	}

	@Override
	CodePiece writeAndUpdate(CodePiece stream) {
		CodePiece tankFluid = getTankFluid();
		String owner = getInternalName(Fluids.class);
		String name = "clone";
		String desc = Type.getMethodDescriptor(getType(FluidStack.class), getType(FluidStack.class));
		CodePiece update = companion.set(CodePieces.invokeStatic(owner, name, desc, tankFluid));

		owner = getInternalName(MCDataOutputStream.class);
		name = "writeFluidStack";
		desc = Type.getMethodDescriptor(VOID_TYPE, getType(FluidStack.class));
		CodePiece write = CodePieces.invokeVirtual(owner, name, desc, stream, tankFluid);
		return write.append(update);
	}

	private CodePiece getTankFluid() {
		String owner = getInternalName(FluidTank.class);
		String name = "getFluid";
		String desc = Type.getMethodDescriptor(getType(FluidStack.class));
		return CodePieces.invokeVirtual(owner, name, desc, var.get());
	}

	@Override
	CodePiece read(CodePiece stream) {
		String owner = getInternalName(MCDataInputStream.class);
		String name = "readFluidStack";
		String desc = Type.getMethodDescriptor(getType(FluidStack.class));
		CodePiece read = CodePieces.invokeVirtual(owner, name, desc, stream);

		owner = getInternalName(FluidTank.class);
		name = "setFluid";
		desc = Type.getMethodDescriptor(VOID_TYPE, getType(FluidStack.class));
		return CodePieces.invokeVirtual(owner, name, desc, var.get(), read);
	}

	@Override
	Class<? extends Annotation> getAnnotation() {
		return Watch.class;
	}
}

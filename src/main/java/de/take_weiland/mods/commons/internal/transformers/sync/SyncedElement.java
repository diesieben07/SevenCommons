package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import static org.objectweb.asm.Opcodes.*;

/**
* @author diesieben07
*/
class SyncedElement {

	final int index;
	final ASMVariable variable;
	final Syncer syncer;
	final SyncGroup group;
	ASMVariable companion;

	SyncedElement(int index, ASMVariable variable, Syncer syncer, SyncGroup group) {
		this.index = index;
		this.variable = variable;
		this.syncer = syncer;
		this.group = group;
	}

	void setup() {
		companion = makeCompanion();
	}

	CodePiece writeData(CodePiece packetBuilder) {
		return syncer.equals(variable.get(), companion.get())
				.otherwise(syncer.write(variable.get(), packetBuilder)
						.append(companion.set(variable.get())))
				.build();
	}

	private ASMVariable makeCompanion() {
		ClassNode clazz = group.handler.clazz;
		String name = "_sc$sync$companion$" + variable.name();
		String desc = variable.getType().getDescriptor();
		FieldNode field = new FieldNode(ACC_PRIVATE | ACC_TRANSIENT, name, desc, null, null);
		clazz.fields.add(field);
		return ASMVariables.of(clazz, field, CodePieces.getThis());
	}

}

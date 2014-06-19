package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.ASMVariable;

/**
* @author diesieben07
*/
class SyncedElement {

	final int index;
	final ASMVariable variable;
	final ASMVariable companion;
	final Syncer syncer;

	SyncedElement(int index, ASMVariable variable, ASMVariable companion, Syncer syncer) {
		this.index = index;
		this.variable = variable;
		this.companion = companion;
		this.syncer = syncer;
	}
}

package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformerWrapper;

/**
 * @author diesieben07
 */
public class SCTransformerWrapper extends ASMClassTransformerWrapper {

	@Override
	protected void setup() {
		register(new GuiScreenTransformer());
		register(new EntityZombieTransformer());
		register(new EntityPlayerTransformer());
		register(new EntityTransformer());
		register(new PacketTransformer());
		register(new EntityTrackerEntryTransformer());
		register(new SyncingTransformer());
		register(new TraitImplTransformer());
		register(new HasTraitTransformer());
		register(new TraitTransformer());
	}

}

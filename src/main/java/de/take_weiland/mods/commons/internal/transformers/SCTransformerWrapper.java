package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.ASMClassTransformerWrapper;
import de.take_weiland.mods.commons.internal.transformers.sync.SyncingTransformer;

/**
 * @author diesieben07
 */
public class SCTransformerWrapper extends ASMClassTransformerWrapper {

	@Override
	protected void setup() {
		// vanilla stuff
		register(new GuiScreenTransformer());
		register(new EntityZombieTransformer());
		register(new EntityPlayerTransformer());
		register(new EntityTransformer());
		register(new EntityTrackerEntryTransformer());
		register(new Packet250Transformer());


		// packets
		register(new SimplePacketTypeTransformer());
		register(new PacketTransformer());

		// @ToNbt
		register(new AnnotationFindingTransformer());

		// @Synced
		register(new SyncingTransformer());

		register(new ListenableTransformer());
	}

}

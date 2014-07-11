package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.ImmutableList;
import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ASMClassTransformerWrapper;
import de.take_weiland.mods.commons.internal.transformers.sync.SyncingTransformer;

/**
 * @author diesieben07
 */
public class SCTransformerWrapper extends ASMClassTransformerWrapper {

	@Override
	protected void setup(ImmutableList.Builder<ASMClassTransformer> builder) {
		// vanilla stuff
		builder.add(new GuiScreenTransformer());
		builder.add(new EntityPlayerTransformer());
		builder.add(new EntityTrackerEntryTransformer());
		builder.add(new Packet250Transformer());


		// packets
		builder.add(new SimplePacketTypeTransformer());
		builder.add(new PacketTransformer());

		// @ToNbt
		builder.add(new NBTTransformer());

		builder.add(new SyncingTransformer());

		builder.add(new ListenableTransformer());
	}

}

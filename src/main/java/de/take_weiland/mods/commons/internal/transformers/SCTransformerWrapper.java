package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.ImmutableList;
import de.take_weiland.mods.commons.asm.ASMClassTransformer;
import de.take_weiland.mods.commons.asm.ASMClassTransformerWrapper;
import de.take_weiland.mods.commons.internal.transformers.sync.SyncTransformer;

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
		builder.add(new ContainerTransformer());
		builder.add(new GuiContainerTransformer());

		// packets
		builder.add(new Packet250Transformer());
		builder.add(new ModPacketTransformer());

		builder.add(new SyncTransformer());

		builder.add(new InvokeDynamicTransformer());

		builder.add(new ListenableTransformer());

	}

}

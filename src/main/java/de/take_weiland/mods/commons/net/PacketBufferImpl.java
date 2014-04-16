package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
class PacketBufferImpl<TYPE extends Enum<TYPE>> extends WritableDataBufImpl<PacketBufferImpl<TYPE>> implements PacketBuilder {

	int id;
	EntityPlayer sender;
	PacketResponseHandler responseHandler;
	int transferId;

	private final PacketFactoryInternal<TYPE> factory;
	private boolean locked;

	private PacketBufferImpl(byte[] wrap, PacketFactoryInternal<TYPE> factory, int id, boolean isRead) {
		super(wrap);
		if (isRead) {
			actualLen = wrap.length;
		}
		this.factory = factory;
		this.id = id;
	}

	PacketBufferImpl(byte[] wrap, PacketFactoryInternal<TYPE> factory, int id) {
		this(wrap, factory, id, true);
	}

	PacketBufferImpl(int capacity, PacketFactoryInternal<TYPE> factory, int id) {
		this(DataBuffers.createBuffer(capacity), factory, id, false);
	}

	@Deprecated
	@Override
	public SimplePacket toPacket() {
		return build();
	}

	@Override
	public SimplePacket build() {
		locked = true;
		return factory.build(this);
	}

	@Override
	void grow0(int i) {
		checkState(!locked, "Cannot reuse a PacketBuilder!");
		super.grow0(i);
	}
}

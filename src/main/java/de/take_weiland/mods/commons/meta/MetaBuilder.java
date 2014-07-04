package de.take_weiland.mods.commons.meta;

import net.minecraft.world.World;

/**
 * @author diesieben07
 */
public final class MetaBuilder {

	private static MetaBuilder clientBuilder;
	private static MetaBuilder serverBuilder;

	public static MetaBuilder cached(World world) {
		MetaBuilder b;
		if (world.isRemote) {
			b = clientBuilder;
			if (b == null) {
				return (clientBuilder = new MetaBuilder());
			}
		} else {
			b = serverBuilder;
			if (b == null) {
				return (serverBuilder = new MetaBuilder());
			}
		}
		b.reset();
		return b;
	}

	public static MetaBuilder create() {
		return new MetaBuilder();
	}

	private MetaBuilder() { }

	private int meta;

	public <T> MetaBuilder set(MetadataProperty<? super T> property, T value) {
		meta = property.toMeta(value, meta);
		return this;
	}

	public MetaBuilder set(BooleanProperty property, boolean value) {
		meta = property.toMeta(value, meta);
		return this;
	}

	public MetaBuilder set(IntProperty property, int value) {
		meta = property.toMeta(value, meta);
		return this;
	}

	public int build() {
		return meta;
	}

	private void reset() {
		meta = 0;
	}
}

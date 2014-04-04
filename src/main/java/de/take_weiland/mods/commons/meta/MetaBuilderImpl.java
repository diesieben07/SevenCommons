package de.take_weiland.mods.commons.meta;

/**
 * @author diesieben07
 */
class MetaBuilderImpl implements MetaBuilder {

	private int meta;

	@Override
	public <T> MetaBuilder set(MetadataProperty<? super T> property, T value) {
		meta = property.toMeta(value, meta);
		return this;
	}

	@Override
	public MetaBuilder set(BooleanProperty property, boolean value) {
		meta = property.toMeta(value, meta);
		return this;
	}

	@Override
	public MetaBuilder set(IntProperty property, int value) {
		meta = property.toMeta(value, meta);
		return this;
	}

	@Override
	public int build() {
		return meta;
	}

	void reset() {
		meta = 0;
	}
}

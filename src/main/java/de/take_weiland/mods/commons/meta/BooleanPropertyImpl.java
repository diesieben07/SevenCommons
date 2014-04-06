package de.take_weiland.mods.commons.meta;

/**
 * @author diesieben07
 */
final class BooleanPropertyImpl extends GenericProperty<Boolean> implements BooleanProperty {

	private static final BooleanPropertyImpl[] cache = new BooleanPropertyImpl[32];

	static BooleanPropertyImpl get(int shift) {
		if (cache[shift] == null) {
			cache[shift] = new BooleanPropertyImpl(shift);
		}
		return cache[shift];
	}

	private final int mask;

	private BooleanPropertyImpl(int shift) {
		mask = 1 << shift;
	}

	@Override
	public boolean booleanValue(int metadata) {
		return (metadata & mask) != 0;
	}

	@Override
	public int toMeta(boolean value, int previousMeta) {
		return value ? previousMeta | mask : previousMeta;
	}

	@Override
	public Boolean value(int metadata) {
		return booleanValue(metadata);
	}

	@Override
	public int toMeta(Boolean value, int previousMeta) {
		return toMeta(value.booleanValue(), previousMeta);
	}

}

package de.take_weiland.mods.commons.sync;

/**
 * @author diesieben07
 */
class EnumAdapter extends SyncAdapter<Enum<?>> {

	private Enum<?> value;

	@Override
	public boolean checkAndUpdate(Enum<?> newValue) {
		if (value != newValue) {
			value = newValue;
			return true;
		} else {
			return false;
		}
	}
}

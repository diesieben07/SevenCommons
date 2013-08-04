package net.minecraft.item;

public final class SevenCommonsItemInjector {

	private SevenCommonsItemInjector() { }
	
	public static final void setHasSubtypes(Item item) {
		item.setHasSubtypes(true);
	}
	
}

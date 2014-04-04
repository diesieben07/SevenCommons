package de.take_weiland.mods.commons.meta;

/**
 * @author diesieben07
 */
public interface MetaBuilder {

	<T> MetaBuilder set(MetadataProperty<? super T> property, T value);

	MetaBuilder set(BooleanProperty property, boolean value);

	MetaBuilder set(IntProperty property, int value);

	int build();

}

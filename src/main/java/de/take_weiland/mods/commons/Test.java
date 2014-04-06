package de.take_weiland.mods.commons;

import de.take_weiland.mods.commons.meta.BooleanProperty;
import de.take_weiland.mods.commons.meta.IntProperty;
import de.take_weiland.mods.commons.meta.MetaProperties;
import de.take_weiland.mods.commons.meta.MetadataProperty;

/**
 * @author diesieben07
 */
public class Test {

	public static void main(String... args) {
		System.out.println(Thread.currentThread().getId());


		BooleanProperty prop0 = MetaProperties.newBooleanProperty(0);
		IntProperty prop1 = MetaProperties.newIntProperty(1, 8);
		MetadataProperty<MyEnum> prop2 = MetaProperties.newProperty(9, MyEnum.class);

		int meta = MetaProperties.builder()
				.set(prop0, true)
				.set(prop1, 20)
				.set(prop2, MyEnum.C).build();

		System.out.println(Integer.toBinaryString(meta));
		System.out.println(prop0.value(meta));
		System.out.println(prop1.value(meta));
		System.out.println(prop2.value(meta));
	}

	public static enum MyEnum  {

		A,
		B,
		C

	}

}

package de.take_weiland.mods.commons;

import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.tileentity.TileEntity;

/**
* @author diesieben07
*/
public class TestTE extends TileEntity {

	@Sync
	String string;

	private int tick;

	@Override
	public void updateEntity() {
		System.out.println("string is " + string + " on " + Sides.logical(this));

		if (Sides.logical(this).isServer() && (tick++ % 20) == 0) {
			string = String.valueOf(Math.random());
		}
	}
}

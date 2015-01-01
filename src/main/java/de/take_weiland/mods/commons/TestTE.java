package de.take_weiland.mods.commons;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.sync.Sync;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/**
* @author diesieben07
*/
public class TestTE extends TileEntity {

	@Sync
	ItemStack stack;

	@Override
	public String toString() {
		try {
			return TestTE.class.getDeclaredField("_sc$sync$watcher$f$stack").get(null).getClass().toString();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}
}

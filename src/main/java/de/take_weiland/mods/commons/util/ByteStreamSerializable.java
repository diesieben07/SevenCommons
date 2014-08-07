package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;

/**
 * @author diesieben07
 */
public interface ByteStreamSerializable {

	void write(MCDataOutputStream buf);

	void read(MCDataInputStream buf);

}

package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;

/**
 * @author diesieben07
 */
public interface ByteStreamSerializable {

	void write(WritableDataBuf buf);

	void read(DataBuf buf);

}

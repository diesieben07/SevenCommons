package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.CodePiece;
import de.take_weiland.mods.commons.internal.SyncType;

/**
 * @author diesieben07
 */
abstract class ASMPacketTarget {

	abstract CodePiece sendPacket(SyncType type, CodePiece packet);

	abstract String methodPostfix();

}

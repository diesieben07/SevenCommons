package de.take_weiland.mods.commons.internal.transformers.sync;

import com.google.common.collect.Lists;
import org.objectweb.asm.Type;

import java.util.List;

/**
 * @author diesieben07
 */
class SyncingGroup {

	final Type packetTarget;
	final List<SyncedElement> elements;

	SyncingGroup(Type packetTarget) {
		this.packetTarget = packetTarget;
		this.elements = Lists.newArrayList();
	}
}

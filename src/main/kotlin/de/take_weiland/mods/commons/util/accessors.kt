package de.take_weiland.mods.commons.util

import net.minecraft.inventory.Container
import net.minecraft.inventory.IContainerListener

/**
 * @author diesieben07
 */
val Container.listeners : MutableList<IContainerListener> inline get() = SCMethodHandles.getListeners(this)
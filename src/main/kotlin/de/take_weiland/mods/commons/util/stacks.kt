package de.take_weiland.mods.commons.util

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

/**
 * @author diesieben07
 */
var ItemStack.nbt: NBTTagCompound
    inline get() = tagCompound ?: NBTTagCompound().also { tagCompound = it }
    inline set(value) {
        tagCompound = value
    }

val ItemStack.nbtOrNull: NBTTagCompound?
    inline get() = tagCompound
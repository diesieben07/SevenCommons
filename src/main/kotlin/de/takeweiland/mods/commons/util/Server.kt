package de.takeweiland.mods.commons.util

import net.minecraft.server.MinecraftServer
import net.minecraftforge.fml.common.FMLCommonHandler

/**
 * @author Take Weiland
 */
val minecraftServer: MinecraftServer get() = FMLCommonHandler.instance().minecraftServerInstance
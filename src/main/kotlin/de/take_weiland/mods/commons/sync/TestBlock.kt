package de.take_weiland.mods.commons.sync

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.world.World

/**
 * @author diesieben07
 */
class TestBlock : Block(Material.ROCK) {

    init {
        setRegistryName("testBlock")
    }

    override fun hasTileEntity(state: IBlockState?) = true

    override fun createTileEntity(world: World?, state: IBlockState?) = Foo()

}
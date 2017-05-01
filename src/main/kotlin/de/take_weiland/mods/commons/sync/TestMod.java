package de.take_weiland.mods.commons.sync;

import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * @author diesieben07
 */
@Mod(modid = "sevencommons", name = "SevenCommons", version = "0.1")
public class TestMod {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        TestBlock block = new TestBlock();
        GameRegistry.register(block);
        GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        GameRegistry.registerTileEntity(Foo.class, "sevencommons:foo");
    }

}
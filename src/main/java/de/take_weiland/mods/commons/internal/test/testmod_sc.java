package de.take_weiland.mods.commons.internal.test;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.InvocationTargetException;

import static de.take_weiland.mods.commons.util.Sides.sideOf;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1", dependencies = "required-after:sevencommons")
public class testmod_sc {

    @Mod.Instance
    public static testmod_sc instance;

    private static Block myBlock;

    @Mod.EventHandler
    public void preInit(FMLPostInitializationEvent event) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        myBlock = new Block(Material.rock) {

            @Override
            public boolean hasTileEntity(int metadata) {
                return true;
            }

            @Override
            public TileEntity createTileEntity(World world, int metadata) {
                return new TestTE();
            }

            @Override
            public boolean onBlockActivated(World world, int par2, int par3, int par4, EntityPlayer player, int par6, float par7, float par8, float par9) {
                TestTE te = (TestTE) world.getTileEntity(par2, par3, par4);
                if (sideOf(world).isServer()) {
                    player.addChatMessage(new ChatComponentText("old val: " + te.test));
                    te.test = String.valueOf(world.rand.nextFloat());
                    player.addChatMessage(new ChatComponentText("new val: " + te.test));
                } else {
                    player.addChatMessage(new ChatComponentText("on client: " + te.test));
                }
//                PlayerProps props = (PlayerProps) player.getExtendedProperties("testmod_sc");
//                if (Sides.logical(world).isClient()) {
//                    player.addChatMessage("On client: " + props.getSomeData());
//                } else {
//                    props.setSomeData(String.valueOf(world.rand.nextFloat()));
//                }

                return true;
            }
        };

        myBlock.setCreativeTab(CreativeTabs.tabBlock);

        GameRegistry.registerTileEntity(TestTE.class, "testte");
        GameRegistry.registerBlock(myBlock, "testblock");
        MinecraftForge.EVENT_BUS.register(this);
    }

}

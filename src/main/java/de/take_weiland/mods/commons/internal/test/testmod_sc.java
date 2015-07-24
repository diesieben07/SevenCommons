package de.take_weiland.mods.commons.internal.test;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.util.Scheduler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;

import java.lang.reflect.InvocationTargetException;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1", dependencies = "required-after:sevencommons")
public class testmod_sc {

    @Mod.Instance
    public static testmod_sc instance;

    private static Block myBlock;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
                if (!world.isRemote) {
                    new TestPacket("testus")
                            .sendTo(player)
                            .whenCompleteAsync((response, x) -> System.out.println(response.s), Scheduler.server());
                }
                return true;
            }
        };

        myBlock.setCreativeTab(CreativeTabs.tabBlock);

        GameRegistry.registerTileEntity(TestTE.class, "testte");
        GameRegistry.registerBlock(myBlock, "testblock");
        MinecraftForge.EVENT_BUS.register(this);

        Network.newSimpleChannel("testmod")
                .register(0, TestPacket::new, TestResponse::new, (packet, player, side) -> new TestResponse(packet.s))
                .build();
    }

    @SubscribeEvent
    public void onEntityConstruct(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            event.entity.registerExtendedProperties("testmod_sc", new PlayerProps());
        }
    }

}

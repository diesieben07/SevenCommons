package de.take_weiland.mods.commons.internal.test;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;

import java.io.IOException;
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
                    new TestPacket().sendTo(player);
                }
                System.out.println("hello");
                return true;
            }
        };

        myBlock.setCreativeTab(CreativeTabs.tabBlock);

        GameRegistry.registerTileEntity(TestTE.class, "testte");
        GameRegistry.registerBlock(myBlock, "testblock");
        MinecraftForge.EVENT_BUS.register(this);

        Network.newSimpleChannel("testmod")
                .register(0, TestPacket::new, (packet, player, side) ->
                        System.out.println("packet received"))
                .build();
    }

    @SubscribeEvent
    public void onEntityConstruct(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            event.entity.registerExtendedProperties("testmod_sc", new PlayerProps());
        }
    }

    private static class TestPacket implements Packet {

        private TestPacket(MCDataInput in) {
            try {
                System.out.println("packet from " + in.asInputStream().available());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TestPacket() {
        }

        @Override
        public void writeTo(MCDataOutput out) {
            out.writeNulls(0x1FFFFF);
        }

        @Override
        public int expectedSize() {
            return 0x1FFFFF;
        }
    }

}

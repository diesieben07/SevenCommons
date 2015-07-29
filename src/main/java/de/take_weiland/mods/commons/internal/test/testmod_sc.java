package de.take_weiland.mods.commons.internal.test;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.util.Blocks;
import de.take_weiland.mods.commons.util.ItemStacks;
import de.take_weiland.mods.commons.util.Scheduler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Predicate;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1", dependencies = "required-after:sevencommons")
@GameRegistry.ObjectHolder("testmod_sc")
public class testmod_sc {

    @Mod.Instance
    public static testmod_sc instance;

    @GameRegistry.ObjectHolder("testblock")
    public static Block myBlock;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Block b = new Block(Material.rock) {

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

        b.setCreativeTab(CreativeTabs.tabBlock);
        GameRegistry.registerTileEntity(TestTE.class, "testte");

        Blocks.init(b, "testblock");

        MinecraftForge.EVENT_BUS.register(this);

        Network.newSimpleChannel("testmod")
                .register(0, TestPacket::new, TestResponse::new, (packet, player, side) -> new TestResponse(packet.s))
                .build();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println(myBlock);
    }

    @SubscribeEvent
    public void onEntityConstruct(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            event.entity.registerExtendedProperties("testmod_sc", new PlayerProps());
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        try {
            Predicate<ItemStack> matcher = ItemStacks.parseMatcher("\"pipes|\"");
            System.out.println("stone: " + matcher.test(new ItemStack(Blocks.stone)));
            System.out.println("dirt: " + matcher.test(new ItemStack(Blocks.dirt)));
            System.out.println("dirt@1: " + matcher.test(new ItemStack(Blocks.dirt, 1, 1)));
            System.out.println("wood: " + matcher.test(new ItemStack(Blocks.log)));
            System.out.println("wood2: " + matcher.test(new ItemStack(Blocks.log2)));
        } catch (ItemStacks.InvalidStackDefinition e) {
            e.printStackTrace();
        }

//        FMLCommonHandler.instance().exitJava(0, false);
    }

}

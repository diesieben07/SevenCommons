package de.take_weiland.mods.commons.internal.test;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.util.Scheduler;
import net.minecraft.block.Block;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ForkJoinPool;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1", dependencies = "required-after:sevencommons")
@GameRegistry.ObjectHolder("testmod_sc")
public class testmod_sc {

    @Mod.Instance
    public static testmod_sc instance;

    @GameRegistry.ObjectHolder("testblock")
    public static Block myBlock;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        FMLCommonHandler.instance().bus().register(this);
//        Block b = new Block(Material.rock) {
//
//            @Override
//            public boolean hasTileEntity(int metadata) {
//                return true;
//            }
//
//            @Override
//            public TileEntity createTileEntity(World world, int metadata) {
//                return new TestTE();
//            }
//
//            @Override
//            public boolean onBlockActivated(World world, int par2, int par3, int par4, EntityPlayer player, int par6, float par7, float par8, float par9) {
//                if (!world.isRemote) {
//                    new TestPacket("testus")
//                            .sendTo(player)
//                            .whenCompleteAsync((response, x) -> System.out.println(response.s), Scheduler.server());
//                }
//                return true;
//            }
//        };
//
//        b.setCreativeTab(CreativeTabs.tabBlock);
//        GameRegistry.registerTileEntity(TestTE.class, "testte");
//
//        Blocks.init(b, "testblock");
//
//        MinecraftForge.EVENT_BUS.register(this);
//
//        Network.newSimpleChannel("testmod")
//                .register(0, TestPacket::new, TestResponse::new, (packet, player, side) -> new TestResponse(packet.s))
//                .build();
    }

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        for (int i = 0; i < 100; i++) {
            final int ifin = i;
            ForkJoinPool.commonPool().execute(() -> {
                Scheduler.server().execute(() -> System.out.println("task " + ifin));
            });
        }
    }


}

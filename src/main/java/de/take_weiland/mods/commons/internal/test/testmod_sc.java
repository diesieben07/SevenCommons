package de.take_weiland.mods.commons.internal.test;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.internal.SchedulerInternalTask;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.util.Blocks;
import de.take_weiland.mods.commons.util.Scheduler;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;

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
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println(Sides.environment());
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
            public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
                if (world.isRemote) {
                    System.out.println(((TestTE) world.getTileEntity(x, y, z)).getSyncFoobar());
                }
                return true;
            }
        };

        b.setCreativeTab(CreativeTabs.tabBlock);
        GameRegistry.registerTileEntity(TestTE.class, "testte");

        Blocks.init(b, "testblock");
//
        Network.newSimpleChannel("testmod")
                .registerFutureResponse(0, TestPacket::new, TestResponse::new, (packet, player) -> CompletableFuture.supplyAsync(() -> new TestResponse(packet.s)))
                .build();
    }

    static final int X = 200;

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        SchedulerInternalTask.execute(Scheduler.server(), new SchedulerInternalTask() {

            @Override
            public boolean run() {
                System.out.println("tick");
                return false;
            }
        });

//        for (int i = 0; i < 100000; i++) {
//            final int ifin = i;
//            ForkJoinPool.commonPool().execute(() -> {
//                SchedulerInternalTask.execute(Scheduler.server(), new SchedulerInternalTask() {
//
//                    private int x = new Random().nextInt(X);
//
//                    @Override
//                    public boolean run() {
//                        if (x++ % X == 0)
//                            x = new Random().nextInt(X);
//                        return true;
//                    }
//                });
//            });
//        }
    }


}

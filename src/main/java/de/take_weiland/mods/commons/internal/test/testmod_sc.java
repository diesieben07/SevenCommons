package de.take_weiland.mods.commons.internal.test;

import com.google.common.collect.FluentIterable;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.client.icon.IconManager;
import de.take_weiland.mods.commons.client.icon.IconManagerBuilder;
import de.take_weiland.mods.commons.client.icon.Icons;
import de.take_weiland.mods.commons.internal.SchedulerInternalTask;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.util.Blocks;
import de.take_weiland.mods.commons.util.Scheduler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

@Mod(modid = "testmod_sc", name = "testmod_sc", version = "0.1", dependencies = "required-after:sevencommons")
@GameRegistry.ObjectHolder("testmod_sc")
public class testmod_sc {

    @Mod.Instance
    public static testmod_sc instance;

    @GameRegistry.ObjectHolder("testblock")
    public static Block   myBlock;
    private       MyBlock block;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new IGuiHandler() {
            @Override
            public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                return new TestContainer(world, x, y, z, player);
            }

            @Override
            public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
                return new TestGui((TestContainer) getServerGuiElement(ID, player, world, x, y, z));
            }
        });

        block = new MyBlock();

        block.setCreativeTab(CreativeTabs.tabBlock);
        GameRegistry.registerTileEntity(TestTE.class, "testte");

        Blocks.init(block, "testblock");

//
        Network.newSimpleChannel("testmod")
                .register(0, TestPacket::new, TestResponse::new, (packet, player, side) -> new TestResponse(packet.s))
                .build();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandBase() {
            @Override
            public String getCommandName() {
                return "rot";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return "rot <face>";
            }

            @SuppressWarnings("unchecked")
            @Override
            public void processCommand(ICommandSender sender, String[] args) {
                if (args.length != 2) {
                    throw new CommandException("Missing args");
                } else {
                    try {
                        ForgeDirection dir = Enum.valueOf(ForgeDirection.class, args[0].trim().toUpperCase());
                        int rot = Integer.valueOf(args[1].trim()) & 3;

                        EntityPlayer player = getCommandSenderAsPlayer(sender);
                        Chunk chunk = player.worldObj.getChunkFromBlockCoords((int) player.posX, (int) player.posZ);
                        for (TestTE te : FluentIterable.from((Collection<TileEntity>) chunk.chunkTileEntityMap.values()).filter(TestTE.class)) {
                            te.rotMeta = block.icons.getMeta(dir.ordinal(), rot);
                        }
                    } catch (NumberFormatException nfe) {
                        throw new CommandException("Invalid number");
                    } catch (IllegalArgumentException e) {
                        throw new CommandException("Invalid face");
                    }
                }
            }
        });
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


    private static class MyBlock extends Block {

        public MyBlock() {
            super(Material.rock);
        }

        @Override
        public boolean hasTileEntity(int metadata) {
            return true;
        }

        @Override
        public TileEntity createTileEntity(World world, int metadata) {
            return new TestTE();
        }

        @Override
        public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9) {
            if (!world.isRemote) {
//                if (side > -1) {
//                    TestTE te = (TestTE) world.getTileEntity(x, y, z);
//                    te.rotMeta = icons.getMeta(side, 0);
//                }
            }
            return true;
        }

        @Override
        public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
            return icons.getIcon(side, ((TestTE) world.getTileEntity(x, y, z)).rotMeta);
        }

        IconManager icons;

        @Override
        public void registerIcons(IIconRegister reg) {
            IconManagerBuilder builder = Icons.newBuilder(reg, "sevencommons")
                    .addValidFront(ForgeDirection.SOUTH, 2);
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                builder.texture("test_" + dir.name().toLowerCase(), dir);
            }
            icons = builder.build(false);
        }
    }
}

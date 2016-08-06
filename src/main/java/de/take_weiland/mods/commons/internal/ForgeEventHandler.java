package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.client.ScreenWithParent;
import de.take_weiland.mods.commons.internal.sync_olds.SyncCompanion;
import de.take_weiland.mods.commons.internal.sync_olds.SyncedObjectProxy;
import de.take_weiland.mods.commons.internal.worldview.ServerChunkViewManager;
import gnu.trove.set.hash.TLongHashSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

import static net.minecraft.client.Minecraft.getMinecraft;

/**
 * @author diesieben07
 */
public final class ForgeEventHandler {

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent.Entity event) {

    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent.TileEntity event) {

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @SideOnly(Side.CLIENT)
    public void onScreenInit(GuiScreenEvent.InitGuiEvent.Pre event) {
        ((GuiScreenProxy) event.getGui())._sc$textFields().clear();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onOpenGui(GuiOpenEvent event) {
        if (event.getGui() == null && getMinecraft().currentScreen instanceof ScreenWithParent) {
            event.setGui(((ScreenWithParent) getMinecraft().currentScreen).getParentScreen());
        }
    }


    @SuppressWarnings("ForLoopReplaceableByForEach")
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onScreenDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        List<GuiTextField> fields = ((GuiScreenProxy) event.getGui())._sc$textFields();
        for (int i = 0, fieldsSize = fields.size(); i < fieldsSize; i++) {
            fields.get(i).drawTextBox();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (event.getItemInHand().hasDisplayName() && event.getPlacedBlock().getBlock().hasTileEntity(event.getState())) {
            TileEntity te = event.getWorld().getTileEntity(event.getPos());
//TODO
// if (te instanceof NameableInventory && ((NameableInventory) te).takeItemStackName(event.getPlayer(), event.getItemInHand())) {
//                ((NameableInventory) te).setCustomName(event.getItemInHand().getDisplayName());
//            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void startTracking(PlayerEvent.StartTracking event) {
        SyncCompanion companion = ((SyncedObjectProxy) event.getTarget())._sc$getCompanion();
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();

        if (companion != null) {
            companion.check(event.getTarget(), 0, player);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        TLongHashSet old = ((EntityPlayerMPProxy) event.getOriginal())._sc$viewedChunks();
        ((EntityPlayerMPProxy) event.getEntityPlayer())._sc$viewedChunks().addAll(old);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void startTrackingChunk(ChunkWatchEvent.Watch event) {
        Chunk chunk = event.getPlayer().worldObj.getChunkFromChunkCoords(event.getChunk().chunkXPos, event.getChunk().chunkZPos);
        //noinspection unchecked
        chunk.getTileEntityMap().forEach((key, te) -> {
            SyncCompanion companion = ((SyncedObjectProxy) te)._sc$getCompanion();
            if (companion != null) {
                companion.check(te, 0, event.getPlayer());
            }
        });
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!event.getWorld().isRemote) {
            ServerChunkViewManager.onChunkLoad(event.getChunk());
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            ServerChunkViewManager.onChunkUnload(event.getChunk());
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        event.getWorld().addEventListener(event.getWorld().isRemote ? new WorldAccessImpl() : new WorldAccessImplServer(event.getWorld()));
    }

    private static final class WorldAccessImplServer extends WorldAccessImpl {

        private final World world;

        WorldAccessImplServer(World world) {
            this.world = world;
        }

        @Override
        public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
            super.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
//            ChunkUpdateTracker.onChunkBlockUpdate(world.getChunkFromBlockCoords(x, z), x & 0xF, y, z & 0xF);
        }

    }

    private static class WorldAccessImpl implements IWorldEventListener {

        @Override
        public void onEntityRemoved(Entity entity) {
        }

        @Override
        public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {

        }

        @Override
        public void notifyLightSet(BlockPos pos) {

        }

        @Override
        public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {

        }

        @Override
        public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {

        }

        @Override
        public void playRecord(SoundEvent soundIn, BlockPos pos) {

        }

        @Override
        public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

        }

        @Override
        public void onEntityAdded(Entity entityIn) {

        }

        @Override
        public void broadcastSound(int soundID, BlockPos pos, int data) {

        }

        @Override
        public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {

        }

        @Override
        public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

        }
    }
}

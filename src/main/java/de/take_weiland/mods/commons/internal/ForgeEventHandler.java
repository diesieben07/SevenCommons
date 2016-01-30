package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.client.ScreenWithParent;
import de.take_weiland.mods.commons.internal.sync.SyncCompanion;
import de.take_weiland.mods.commons.internal.sync.SyncedObjectProxy;
import de.take_weiland.mods.commons.internal.worldview.ChunkUpdateTracker;
import de.take_weiland.mods.commons.internal.worldview.ServerChunkViewManager;
import de.take_weiland.mods.commons.inv.NameableInventory;
import gnu.trove.list.array.TLongArrayList;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.Minecraft.getMinecraft;

/**
 * @author diesieben07
 */
public final class ForgeEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @SideOnly(Side.CLIENT)
    public void onScreenInit(GuiScreenEvent.InitGuiEvent.Pre event) {
        ((GuiScreenProxy) event.gui)._sc$textFields().clear();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onOpenGui(GuiOpenEvent event) {
        if (event.gui == null && getMinecraft().currentScreen instanceof ScreenWithParent) {
            event.gui = ((ScreenWithParent) getMinecraft().currentScreen).getParentScreen();
        }
    }


    @SuppressWarnings("ForLoopReplaceableByForEach")
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onScreenDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        List<GuiTextField> fields = ((GuiScreenProxy) event.gui)._sc$textFields();
        for (int i = 0, fieldsSize = fields.size(); i < fieldsSize; i++) {
            fields.get(i).drawTextBox();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (event.itemInHand.hasDisplayName() && event.placedBlock.hasTileEntity(event.blockMetadata)) {
            TileEntity te = event.world.getTileEntity(event.x, event.y, event.z);
            if (te instanceof NameableInventory && ((NameableInventory) te).takeItemStackName(event.player, event.itemInHand)) {
                ((NameableInventory) te).setCustomName(event.itemInHand.getDisplayName());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void startTracking(PlayerEvent.StartTracking event) {
        SyncCompanion companion = ((SyncedObjectProxy) event.target)._sc$getCompanion();
        EntityPlayerMP player = (EntityPlayerMP) event.entityPlayer;

        if (companion != null) {
            companion.check(event.target, SyncCompanion.FORCE_CHECK, player);
        }
        forceIEEPUpdate(player, event.target);
    }

    public static void forceIEEPUpdate(EntityPlayerMP player, Entity target) {
        Collection<IExtendedEntityProperties> ieeps = ((EntityProxy) target)._sc$getIEEPMap().values();
        for (IExtendedEntityProperties ieep : ieeps) {
            SyncCompanion companion = ASMHooks.getIEEPCompanion(ieep);
            if (companion != null) {
                companion.check(ieep, SyncCompanion.FORCE_CHECK, player);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        TLongArrayList old = ((EntityPlayerMPProxy) event.original)._sc$viewedChunks();
        ((EntityPlayerMPProxy) event.entityPlayer)._sc$viewedChunks().addAll(old);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void startTrackingChunk(ChunkWatchEvent.Watch event) {
        Chunk chunk = event.player.worldObj.getChunkFromChunkCoords(event.chunk.chunkXPos, event.chunk.chunkZPos);
        //noinspection unchecked
        ((Map<ChunkPosition, TileEntity>) chunk.chunkTileEntityMap).forEach((key, te) -> {
            SyncCompanion companion = ((SyncedObjectProxy) te)._sc$getCompanion();
            if (companion != null) {
                companion.check(te, SyncCompanion.FORCE_CHECK, event.player);
            }
        });
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!event.world.isRemote) {
            ServerChunkViewManager.onChunkLoad(event.getChunk());
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.world.isRemote) {
            ServerChunkViewManager.onChunkUnload(event.getChunk());
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        event.world.addWorldAccess(event.world.isRemote ? new WorldAccessImpl() : new WorldAccessImplServer(event.world));
    }

    private static final class WorldAccessImplServer extends WorldAccessImpl {

        private final World world;

        WorldAccessImplServer(World world) {
            this.world = world;
        }

        @Override
        public void markBlockForUpdate(int x, int y, int z) {
            ChunkUpdateTracker.onChunkBlockUpdate(world.getChunkFromBlockCoords(x, z), x & 0xF, y, z & 0xF);
        }
    }

    private static class WorldAccessImpl implements IWorldAccess {

        @Override
        public void onEntityDestroy(Entity entity) {
            ASMHooks.onEntityUnload(entity);
        }

        @Override
        public void markBlockForUpdate(int x, int y, int z) {
        }

        @Override
        public void markBlockForRenderUpdate(int p_147588_1_, int p_147588_2_, int p_147588_3_) {

        }

        @Override
        public void markBlockRangeForRenderUpdate(int p_147585_1_, int p_147585_2_, int p_147585_3_, int p_147585_4_, int p_147585_5_, int p_147585_6_) {

        }

        @Override
        public void playSound(String soundName, double x, double y, double z, float volume, float pitch) {

        }

        @Override
        public void playSoundToNearExcept(EntityPlayer p_85102_1_, String p_85102_2_, double p_85102_3_, double p_85102_5_, double p_85102_7_, float p_85102_9_, float p_85102_10_) {

        }

        @Override
        public void spawnParticle(String p_72708_1_, double p_72708_2_, double p_72708_4_, double p_72708_6_, double p_72708_8_, double p_72708_10_, double p_72708_12_) {

        }

        @Override
        public void onEntityCreate(Entity p_72703_1_) {

        }

        @Override
        public void playRecord(String p_72702_1_, int p_72702_2_, int p_72702_3_, int p_72702_4_) {

        }

        @Override
        public void broadcastSound(int p_82746_1_, int p_82746_2_, int p_82746_3_, int p_82746_4_, int p_82746_5_) {

        }

        @Override
        public void playAuxSFX(EntityPlayer p_72706_1_, int p_72706_2_, int p_72706_3_, int p_72706_4_, int p_72706_5_, int p_72706_6_) {

        }

        @Override
        public void destroyBlockPartially(int p_147587_1_, int p_147587_2_, int p_147587_3_, int p_147587_4_, int p_147587_5_) {

        }

        @Override
        public void onStaticEntitiesChanged() {

        }
    }
}

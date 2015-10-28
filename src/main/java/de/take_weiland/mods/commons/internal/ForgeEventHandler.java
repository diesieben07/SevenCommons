package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.internal.sync.SyncCompanion;
import de.take_weiland.mods.commons.internal.sync.SyncedObjectProxy;
import de.take_weiland.mods.commons.inv.NameableInventory;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author diesieben07
 */
public final class ForgeEventHandler implements IWorldAccess {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @SideOnly(Side.CLIENT)
    public void onScreenInit(GuiScreenEvent.InitGuiEvent.Pre event) {
        ((GuiScreenProxy) event.gui)._sc$textFields().clear();
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onScreenDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
        List<GuiTextField> fields = ((GuiScreenProxy) event.gui)._sc$textFields();
        for (int i = 0, fieldsSize = fields.size(); i < fieldsSize; i++) {
            GuiTextField field = fields.get(i);
            field.drawTextBox();
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
            companion.forceUpdate(event.target, false, player);
        }
        forceIEEPUpdate(player, event.target);
    }

    public static void forceIEEPUpdate(EntityPlayerMP player, Entity target) {
        SyncCompanion companion;
        Collection<IExtendedEntityProperties> ieeps = ((EntityProxy) target)._sc$getIEEPMap().values();
        for (IExtendedEntityProperties ieep : ieeps) {
            companion = ASMHooks.getIEEPCompanion(ieep);
            if (companion != null) {
                companion.forceUpdate(ieep, false, player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void startTrackingChunk(ChunkWatchEvent.Watch event) {
        Chunk chunk = event.player.worldObj.getChunkFromChunkCoords(event.chunk.chunkXPos, event.chunk.chunkZPos);
        //noinspection unchecked
        ((Map<ChunkPosition, TileEntity>) chunk.chunkTileEntityMap).forEach((key, te) -> {
            SyncCompanion companion = ((SyncedObjectProxy) te)._sc$getCompanion();
            if (companion != null) {
                companion.forceUpdate(te, false, event.player);
            }
        });
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        event.world.addWorldAccess(this);
    }

    @Override
    public void onEntityDestroy(Entity entity) {
        ASMHooks.onEntityUnload(entity);
    }

    @Override
    public void markBlockForUpdate(int p_147586_1_, int p_147586_2_, int p_147586_3_) {

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

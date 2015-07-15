package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import de.take_weiland.mods.commons.inv.NameableInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IWorldAccess;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;

/**
 * @author diesieben07
 */
public final class ForgeEventHandler implements IWorldAccess {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (event.itemInHand.hasDisplayName() && event.placedBlock.hasTileEntity(event.blockMetadata)) {
            TileEntity te = event.world.getTileEntity(event.x, event.y, event.z);
            if (te instanceof NameableInventory && ((NameableInventory) te).takeItemStackName(event.player, event.itemInHand)) {
                ((NameableInventory) te).setCustomName(event.itemInHand.getDisplayName());
            }
        }
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

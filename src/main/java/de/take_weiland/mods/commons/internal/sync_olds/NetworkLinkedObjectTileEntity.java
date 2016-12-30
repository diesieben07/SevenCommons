package de.take_weiland.mods.commons.internal.sync_olds;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.SimplePacket;
import de.take_weiland.mods.commons.sync.NetworkLinkedObjectType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * @author diesieben07
 */
public final class NetworkLinkedObjectTileEntity implements NetworkLinkedObjectType<TileEntity, BlockPos> {
    @Override
    public void write(TileEntity object, MCDataOutput out) {
        out.writeBlockPos(object.getPos());
    }

    @Override
    public TileEntity read(MCDataInput in, EntityPlayer player) {
        return player.worldObj.getTileEntity(in.readBlockPos());
    }

    @Override
    public BlockPos getData(TileEntity object) {
        return object.getPos();
    }

    @Override
    public TileEntity getObject(BlockPos pos, EntityPlayer player) {
        return player.worldObj.getTileEntity(pos);
    }

    @Override
    public void sendToTracking(TileEntity object, SimplePacket packet) {
        packet.sendToAllTracking(object);
    }
}

package de.take_weiland.mods.commons.internal.tonbt.builtin;

import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.nbt.NBTSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkPosition;

import javax.annotation.Nonnull;

/**
 * @author diesieben07
 */
enum ChunkPositionSerializer implements NBTSerializer.ForValue<ChunkPosition> {
    INSTANCE;

    @Override
    public ChunkPosition read(@Nonnull NBTBase nbt) {
        NBTTagCompound compound = (NBTTagCompound) nbt;
        return new ChunkPosition(compound.getInteger("x"), UnsignedBytes.toInt(compound.getByte("y")), compound.getInteger("z"));
    }

    @Override
    public NBTBase write(@Nonnull ChunkPosition value) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", value.chunkPosX);
        nbt.setByte("y", UnsignedBytes.checkedCast(value.chunkPosY));
        nbt.setInteger("z", value.chunkPosZ);
        return nbt;
    }
}

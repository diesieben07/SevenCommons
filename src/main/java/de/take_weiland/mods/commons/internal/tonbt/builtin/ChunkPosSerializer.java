package de.take_weiland.mods.commons.internal.tonbt.builtin;

import de.take_weiland.mods.commons.serialize.NBTSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nonnull;

/**
 * @author diesieben07
 */
enum ChunkPosSerializer implements NBTSerializer.Instance<ChunkPos> {

    INSTANCE;

    @Override
    public ChunkPos read(@Nonnull NBTBase nbt) {
        NBTTagCompound compound = (NBTTagCompound) nbt;
        return new ChunkPos(compound.getInteger("x"), compound.getInteger("z"));
    }

    @Override
    public NBTBase write(@Nonnull ChunkPos value) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", value.chunkXPos);
        nbt.setInteger("z", value.chunkZPos);
        return null;
    }
}

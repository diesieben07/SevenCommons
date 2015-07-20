package de.take_weiland.mods.commons.internal.tonbt.builtin;

import de.take_weiland.mods.commons.nbt.NBTSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;

import javax.annotation.Nonnull;

/**
 * @author diesieben07
 */
enum ChunkCoordIntPairSerializer implements NBTSerializer.ForValue<ChunkCoordIntPair> {

    INSTANCE;

    @Override
    public ChunkCoordIntPair read(@Nonnull NBTBase nbt) {
        NBTTagCompound compound = (NBTTagCompound) nbt;
        return new ChunkCoordIntPair(compound.getInteger("x"), compound.getInteger("z"));
    }

    @Override
    public NBTBase write(@Nonnull ChunkCoordIntPair value) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", value.chunkXPos);
        nbt.setInteger("z", value.chunkZPos);
        return null;
    }
}

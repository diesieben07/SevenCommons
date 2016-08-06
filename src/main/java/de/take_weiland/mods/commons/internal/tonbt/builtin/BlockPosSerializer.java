package de.take_weiland.mods.commons.internal.tonbt.builtin;

import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.nbt.NBTSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

/**
 * @author diesieben07
 */
enum BlockPosSerializer implements NBTSerializer.ForValue<BlockPos> {
    INSTANCE;

    @Override
    public BlockPos read(@Nonnull NBTBase nbt) {
        NBTTagCompound compound = (NBTTagCompound) nbt;
        return new BlockPos(
                compound.getInteger("x"),
                UnsignedBytes.toInt(compound.getByte("y")),
                compound.getInteger("z"));
    }

    @Override
    public NBTBase write(@Nonnull BlockPos value) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", value.getX());
        nbt.setByte("y", UnsignedBytes.checkedCast(value.getY()));
        nbt.setInteger("z", value.getZ());
        return nbt;
    }
}

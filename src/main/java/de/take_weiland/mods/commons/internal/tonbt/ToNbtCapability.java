package de.take_weiland.mods.commons.internal.tonbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author diesieben07
 */
public class ToNbtCapability implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

    static final Logger log = LogManager.getLogger("SevenCommons|ToNBT");

    private final Object obj;
    private final NBTField[] fields;

    public ToNbtCapability(Object obj, NBTField[] fields) {
        this.obj = obj;
        this.fields = fields;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        for (NBTField field : fields) {
            NBTBase fieldNbt = field.write(obj);
            if (fieldNbt != null) {
                nbt.setTag(field.name, fieldNbt);
            }
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        for (NBTField field : fields) {
            NBTBase fieldNbt = nbt.getTag(field.name);
            if (fieldNbt != null) {
                field.read(fieldNbt, obj);
            }
        }
    }
}

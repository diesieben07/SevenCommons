package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.sync.Sync;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

/**
* @author diesieben07
*/
public class PlayerProps implements IExtendedEntityProperties {

    @Sync
    String someString;

    @Override
    public void saveNBTData(NBTTagCompound compound) {

    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {

    }

    @Override
    public void init(Entity entity, World world) {

    }
}

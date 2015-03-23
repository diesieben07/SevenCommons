package de.take_weiland.mods.commons.internal.test;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

/**
* @author diesieben07
*/
public class PlayerProps implements IExtendedEntityProperties, SyncedInterface {

    private String someString;

    @Override
    public String getSomeData() {
        return someString;
    }

    @Override
    public void setSomeData(String i) {
        this.someString = i;
    }

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

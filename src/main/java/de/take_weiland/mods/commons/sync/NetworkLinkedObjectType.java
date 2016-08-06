package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.SimplePacket;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public interface NetworkLinkedObjectType<T, DATA> {

    void write(T object, MCDataOutput out);

    T read(MCDataInput in, EntityPlayer player);

    DATA getData(T object);

    T getObject(DATA data, EntityPlayer player);

    void send(T object, SimplePacket packet);

}

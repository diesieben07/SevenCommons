package de.take_weiland.mods.commons.internal.tonbt;

import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.serialize.SerializationException;
import de.take_weiland.mods.commons.serialize.nbt.NBTSerializer;
import net.minecraft.nbt.NBTBase;
import org.apache.logging.log4j.message.Message;

/**
 * @author diesieben07
 */
public class DefaultNBTField<T> extends NBTField {

    private final PropertyAccess<T> property;
    private final NBTSerializer<T> serializer;

    public DefaultNBTField(String name, PropertyAccess<T> property, NBTSerializer<T> serializer) {
        super(name);
        this.property = property;
        this.serializer = serializer;
    }

    @Override
    public void read(NBTBase nbt, Object obj) {
        try {
            serializer.read(nbt, property, obj);
        } catch (SerializationException e) {
            Message msg = ToNbtCapability.log.getMessageFactory().newMessage("Field {}.{} could not be read from NBT", obj.getClass().getName(), name);
            ToNbtCapability.log.error(msg, e);
        }
    }

    @Override
    public NBTBase write(Object obj) {
        try {
            return serializer.write(property, obj);
        } catch (SerializationException e) {
            Message msg = ToNbtCapability.log.getMessageFactory().newMessage("Field {}.{} could not be written to NBT", obj.getClass().getName(), name);
            ToNbtCapability.log.error(msg, e);
            return null;
        }
    }

}

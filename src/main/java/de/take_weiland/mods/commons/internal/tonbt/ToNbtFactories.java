package de.take_weiland.mods.commons.internal.tonbt;

import cpw.mods.fml.common.LoaderState;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.internal.TypeToFactoryMap;
import de.take_weiland.mods.commons.internal.prop.AbstractProperty;
import de.take_weiland.mods.commons.nbt.NBTSerializer;
import de.take_weiland.mods.commons.nbt.NBTSerializerFactory;
import de.take_weiland.mods.commons.nbt.ToNbt;
import de.take_weiland.mods.commons.serialize.Property;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author diesieben07
 */
public final class ToNbtFactories {

    private static final ToNbtHandlerFactory factory = new DefaultHandlerFactory();
    private static final ClassValue<ToNbtHandler> handlerCV = new ClassValue<ToNbtHandler>() {
        @Override
        protected synchronized ToNbtHandler computeValue(@Nonnull Class<?> type) {
            return factory.getHandler(type);
        }
    };

    private static final TypeToFactoryMap<NBTSerializerFactory, NBTSerializer<?>> serializerFactories = new TypeToFactoryMap<NBTSerializerFactory, NBTSerializer<?>>() {
        @Override
        protected NBTSerializer<?> applyFactory(NBTSerializerFactory factory, Property<?, ?> type) {
            return factory.get(type);
        }
    };

    static {
        SevenCommons.registerStateCallback(LoaderState.ModState.POSTINITIALIZED, serializerFactories::freeze);
    }

    public static void registerFactory(Class<?> baseType, NBTSerializerFactory factory) {
        serializerFactories.register(baseType, factory);
    }

    static NBTSerializer<?> serializerFor(Property<?, ?> property) {
        return serializerFactories.get(property);
    }

    public static ToNbtHandler handlerFor(Class<?> clazz) {
        return handlerCV.get(clazz);
    }

    static List<Property<?, ?>> getProperties(Class<?> clazz) {
        return AbstractProperty.allProperties(clazz, ToNbt.class);
    }

}

package de.take_weiland.mods.commons.syncx;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import de.take_weiland.mods.commons.internal.MethodHandleHelpers;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import org.apache.commons.lang3.StringUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public class DefaultFactory implements SyncerFactory {
    @Override
    public Handle get(TypeSpecification<?> type) {
        if (type.getRawType().isPrimitive()) {
            return new PrimitiveHandler(type.getRawType());
        } else {
            return null;
        }
    }

    private static class PrimitiveHandler implements Handle {

        private final Class<?> primitive;

        private PrimitiveHandler(Class<?> primitive) {
            this.primitive = primitive;
        }

        @Override
        public Class<?> getCompanionType() {
            return primitive;
        }

        @Override
        public Instance make(MethodHandle getter, MethodHandle setter, MethodHandle companionGet, MethodHandle companionSet) {
            try {
                Class<?> valueClazz = getter.type().parameterType(0);
                Class<?> compClazz = companionGet.type().parameterType(0);

                MethodHandle eq = MethodHandleHelpers.equal(primitive);
                MethodHandle checker = MethodHandles.filterArguments(eq, 0, getter, companionGet);

                String writeMethod = "write" + StringUtils.capitalize(primitive.getSimpleName());
                MethodHandle rawWrite = publicLookup().findVirtual(ByteArrayDataOutput.class, writeMethod, methodType(void.class, primitive))
                        .asType(methodType(void.class, MCDataOutput.class, primitive));

                String readMethod = "read" + StringUtils.capitalize(primitive.getSimpleName());
                MethodHandle rawRead = publicLookup().findVirtual(ByteArrayDataInput.class, readMethod, methodType(primitive))
                        .asType(methodType(primitive, MCDataInput.class));

                return SyncerFactoryUtils.makeSimple(eq, rawWrite, rawRead, null, getter, setter, companionGet, companionSet);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                // impossible
                throw new RuntimeException(e);
            }
        }

    }

}
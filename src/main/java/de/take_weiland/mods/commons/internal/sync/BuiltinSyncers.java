package de.take_weiland.mods.commons.internal.sync;

import com.google.common.base.Objects;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import de.take_weiland.mods.commons.internal.MethodHandleHelpers;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.sync.SyncerFactory;
import de.take_weiland.mods.commons.sync.SyncerFactoryUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.invoke.MethodHandle;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class BuiltinSyncers implements SyncerFactory {
    @Override
    public Handle get(TypeSpecification<?> type) {
        Class<?> rawType = type.getRawType();

        if (rawType.isPrimitive()) {
            return new PrimitiveHandler(rawType);
        } else if (rawType == String.class) {
            return StringHandler.INSTANCE;
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
                MethodHandle eq = MethodHandleHelpers.equal(primitive);

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

    enum StringHandler implements Handle {
        INSTANCE;

        private static final MethodHandle stringEq;

        static {
            try {
                stringEq = publicLookup().findStatic(Objects.class, "equal", methodType(boolean.class, Object.class, Object.class))
                        .asType(methodType(boolean.class, String.class, String.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Class<?> getCompanionType() {
            return String.class;
        }

        @Override
        public Instance make(MethodHandle getter, MethodHandle setter, MethodHandle companionGet, MethodHandle companionSet) {
            MethodHandle write;
            MethodHandle read;
            try {
                write = publicLookup().findVirtual(MCDataOutput.class, "writeString", methodType(void.class, String.class));
                read = publicLookup().findVirtual(MCDataInput.class, "readString", methodType(String.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            return SyncerFactoryUtils.makeSimple(
                    stringEq,
                    write, read,
                    null,
                    getter, setter,
                    companionGet, companionSet
            );
        }
    }

}
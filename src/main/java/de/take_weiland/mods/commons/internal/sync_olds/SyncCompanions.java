package de.take_weiland.mods.commons.internal.sync_olds;

import de.take_weiland.mods.commons.internal.TypeToFactoryMap;
import de.take_weiland.mods.commons.internal.prop.AbstractProperty;
import de.take_weiland.mods.commons.reflect.Property;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import de.take_weiland.mods.commons.sync.SyncerFactory;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.invoke.MethodType.methodType;

/**
 * <p>Factory for SyncCompanions.</p>
 *
 * @author diesieben07
 */
public final class SyncCompanions {

    private static final CompanionFactory companionFactory = new DefaultCompanionFactory();

    private static final ClassValue<MethodHandle> companionConstructors = new ClassValue<MethodHandle>() {
        @Override
        protected synchronized MethodHandle computeValue(@Nonnull Class<?> type) {
            MethodHandle cstr = companionFactory.getCompanionConstructor(type);
            checkState(cstr.type().equals(methodType(SyncCompanion.class)));
            return cstr;
        }
    };

    private static final TypeToFactoryMap<SyncerFactory, TypeSyncer<?, ?, ?>> syncerFactories = new TypeToFactoryMap<SyncerFactory, TypeSyncer<?, ?, ?>>() {
        @Override
        protected TypeSyncer<?, ?, ?> applyFactory(SyncerFactory factory, Property<?> property) {
            if (property.isStatic()) {
                throw new IllegalArgumentException("@Sync cannot be used on static fields");
            } else {
                return factory.getSyncer(property);
            }
        }
    };

    /**
     * <p>Register a new SyncerFactory.</p>
     *
     * @param clazz   the base class
     * @param factory the factory
     */
    public static void registerSyncerFactory(Class<?> clazz, SyncerFactory factory) {
        syncerFactories.register(clazz, factory);
    }

    /**
     * <p>Create a new SyncCompanion for the given class.</p>
     *
     * @param clazz the class
     * @return a SyncCompanion
     */
    public static SyncCompanion newCompanion(Class<?> clazz) throws Throwable {
        return (SyncCompanion) companionConstructors.get(clazz).invokeExact();
    }

    static Map<Property<?>, TypeSyncer<?, ?, ?>> getSyncedMemberInfo(Class<?> clazz) {
        return AbstractProperty.allProperties(clazz, Sync.class)
                .collect(Collectors.toMap(Function.identity(), syncerFactories::get));
    }

    private SyncCompanions() {
    }
}
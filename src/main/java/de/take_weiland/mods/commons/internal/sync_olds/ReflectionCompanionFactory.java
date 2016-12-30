package de.take_weiland.mods.commons.internal.sync_olds;

import de.take_weiland.mods.commons.internal.sync.SyncEvent;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

/**
 * @author diesieben07
 */
public class ReflectionCompanionFactory implements CompanionFactory {

    @Override
    public MethodHandle getCompanionConstructor(Class<?> clazz) {
        return null;
    }

    private static final class ReflectiveSyncCompanion extends SyncCompanion {

        private final List<PropertyEntry<?, ?>> properties;

        private ReflectiveSyncCompanion(List<PropertyEntry<?, ?>> properties) {this.properties = properties;}

        @Override
        public SyncEvent check(Object instance, int flags, EntityPlayerMP player) {
            SyncEvent event = null;
            for (PropertyEntry<?, ?> property : properties) {
                TypeSyncer.Change<?> change = property.invokeCheck(instance);
                if (change != null) {
                    if (event == null) {
                        event = new ArrayList<>(2);
                    }
                    event.add(change);
                }
            }
            if (changes == null) {
                return null;
            } else {
                return new SyncEvent.ForTE()
            }
            return super.check(instance, flags, player);
        }

        @Override
        public SyncEvent checkInContainer(Object instance, int flags, EntityPlayerMP player) {
            return super.checkInContainer(instance, flags, player);
        }

        @Override
        public int applyChanges(Object instance, ChangeIterator values) {
            return super.applyChanges(instance, values);
        }

        static final class PropertyEntry<T, COM> implements PropertyAccess<COM> {

            private final PropertyAccess<T> access;
            private final TypeSyncer<T, COM , ?> syncer;
            private COM companionValue;

            PropertyEntry(PropertyAccess<T> access, TypeSyncer<T, COM, ?> syncer) {
                this.access = access;
                this.syncer = syncer;
            }

            TypeSyncer.Change<?> invokeCheck(Object instance) {
                return syncer.check(instance, access, null, this);
            }

            @Override
            public COM get(Object o) {
                return companionValue;
            }

            @Override
            public void set(Object o, COM val) {
                this.companionValue = val;
            }
        }

    }

}

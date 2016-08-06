package de.take_weiland.mods.commons.internal.sync;

import javax.annotation.Nullable;
import javax.lang.model.type.TypeMirror;

/**
 * @author diesieben07
 */
public abstract class CompileTimeSyncer {

    public static CompileTimeSyncer create(SyncedProperty property) {
        return new BasicCompileTimeSyncer(property);
    }

    public boolean hasCompanion() {
        return getCompanionType() != null;
    }

    public abstract String getSyncMacro();

    @Nullable
    public abstract TypeMirror getCompanionType();

    public static class BasicCompileTimeSyncer extends CompileTimeSyncer {
        private final SyncedProperty property;

        public BasicCompileTimeSyncer(SyncedProperty property) {
            this.property = property;
        }

        @Nullable
        @Override
        public TypeMirror getCompanionType() {
            return property.getGetter().asType();
        }

        @Override
        public String getSyncMacro() {
            if (property.getGetter().asType().getKind().isPrimitive()) {
                return "primitiveSync";
            } else {
                return "basicObjectSync";
            }
        }
    }
}

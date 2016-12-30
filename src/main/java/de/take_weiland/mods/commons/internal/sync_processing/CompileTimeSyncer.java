package de.take_weiland.mods.commons.internal.sync_processing;

import de.take_weiland.mods.commons.internal.sync.ChangedValue;

import javax.annotation.Nullable;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author diesieben07
 */
public abstract class CompileTimeSyncer {

    public boolean hasCompanion() {
        return getCompanionType() != null;
    }

    public abstract boolean supports(TypeMirror typeToSync);

    public abstract ExecutableElement getEqualityCheck();

    public abstract TypeElement getTargetClass();

    @Nullable
    public abstract TypeMirror getCompanionType();

    public abstract Class<? extends ChangedValue> getChangedValueClass();

}

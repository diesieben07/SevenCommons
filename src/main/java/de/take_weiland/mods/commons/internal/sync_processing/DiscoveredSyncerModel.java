package de.take_weiland.mods.commons.internal.sync_processing;

import javax.lang.model.element.TypeElement;

/**
 * @author diesieben07
 */
public class DiscoveredSyncerModel {

    private final String className;
    private final TypeElement targetClass;

    public DiscoveredSyncerModel(String className, TypeElement targetClass) {
        this.className = className;
        this.targetClass = targetClass;
    }

    public String getClassName() {
        return className;
    }

    public TypeElement getTargetClass() {
        return targetClass;
    }
}

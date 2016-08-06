package de.take_weiland.mods.commons.internal.sync;

import javax.lang.model.element.TypeElement;
import java.util.List;

/**
 * @author diesieben07
 */
public final class CompanionTemplateModel {

    private final String pkg;
    private final String companionClass;
    private final TypeElement syncedClass;
    private final List<SyncedProperty> members;
    private final int firstId;

    public CompanionTemplateModel(String pkg, String companionClass, TypeElement syncedClass, List<SyncedProperty> members, int firstId) {
        this.pkg = pkg;
        this.companionClass = companionClass;
        this.syncedClass = syncedClass;
        this.members = members;
        this.firstId = firstId;
    }

    public String getCompanionClass() {
        return companionClass;
    }

    public String getPackage() {
        return pkg;
    }

    public List<SyncedProperty> getMembers() {
        return members;
    }

    public TypeElement getSyncedClass() {
        return syncedClass;
    }

    public int getFirstId() {
        return firstId;
    }

}

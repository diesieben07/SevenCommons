package de.take_weiland.mods.commons.internal.sync_processing;

import javax.lang.model.element.TypeElement;
import java.util.List;

/**
 * @author diesieben07
 */
public final class CompanionTemplateModel {

    private final PendingCompanion pendingCompanion;
    private final String pkg;
    private final String companionClass;
    private final TypeElement syncedClass;
    private final List<SyncedProperty> members;
    private final int firstId;

    public CompanionTemplateModel(PendingCompanion pendingCompanion, String pkg, String companionClass, TypeElement syncedClass, List<SyncedProperty> members, int firstId) {
        this.pendingCompanion = pendingCompanion;
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

    public String getSuperClass() {
        return pendingCompanion.getSuperClassName();
    }

}

<#-- @ftlvariable name="" type="de.take_weiland.mods.commons.internal.sync_processing.CompanionTemplateModel" -->
<#include 'Macros.ftl'>

package ${package};

import de.take_weiland.mods.commons.internal.sync.SyncEvent;
import de.take_weiland.mods.commons.internal.sync.ChangedValue;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import net.minecraft.entity.player.EntityPlayerMP;
import java.util.Objects;

public class ${companionClass} extends ${superClass} {

    <#list members as member>
        <#if member.syncer.hasCompanion()>
            private ${member.syncer.companionType.toString()} ${member.companionFieldName};
        </#if>
    </#list>

    @Override
    public SyncEvent check(Object obj, int flags, EntityPlayerMP player) {
        <@syncMethod inContainer=false/>
    }

    @Override
    public SyncEvent checkInContainer(Object obj, int flags, EntityPlayerMP player) {
        <@syncMethod inContainer=true/>
    }

    @Override
    public int applyChanges(Object instance, ChangeIterator values) {
        int fieldId = super.applyChanges(instance, values);

        loop:
        while (true) {
            switch (fieldId - super.fieldOffset()) {
                <#assign fieldId = 1>
                <#list members as member>
                    case ${fieldId?c}:
                        <@readChange fieldId=fieldId member=member />
                        break;
                    <#assign fieldId++ >
                </#list>
                default: <#-- end of stream or unknown id -->
                    break loop;
            }
            fieldId = values.nextFieldId();
        }

        return fieldId;
    }

    @Override
    public int fieldOffset() {
        return super.fieldOffset() + ${members?size?c};
    }

}



<#-- @ftlvariable name="" type="de.take_weiland.mods.commons.internal.sync.CompanionTemplateModel" -->

<#macro primitiveSync element fieldId>
<#-- @ftlvariable name="element" type="de.take_weiland.mods.commons.internal.sync.SyncedProperty" -->
    if (instance.${element.getter}<#if element.method>()</#if> != this.${element.companionFieldName}) {
        change = new TypeSyncer.Change(null, instance.${element.getter}<#if element.method>()</#if>);
        <@eventAdd fieldId=fieldId/>
        this.${element.companionFieldName} = <@propertyGet property=element/>;
    }
</#macro>

<#macro basicObjectSync element, fieldId>
<#-- @ftlvariable name="element" type="de.take_weiland.mods.commons.internal.sync.SyncedProperty" -->
    if (!Objects.equals(<@propertyGet property=element/>, this.${element.companionFieldName})) {
        change = new TypeSyncer.Change(null, instance.${element.getter}<#if element.method>()</#if>);
        <@eventAdd fieldId=fieldId/>
        this.${element.companionFieldName} = <@propertyGet property=element/>;
    }
</#macro>

<#macro eventAdd fieldId>
    event.add(${fieldId}, change);
</#macro>

<#macro propertyGet property>
<#-- @ftlvariable name="property" type="de.take_weiland.mods.commons.internal.sync.SyncedProperty" -->
    instance.${property.getter.simpleName}<#if property.method>()</#if>
</#macro>

<#macro propertySet property value>
<#-- @ftlvariable name="property" type="de.take_weiland.mods.commons.internal.sync.SyncedProperty" -->
    instance.${property.setter.simpleName}
    <#if property.method>
        (${value})
    <#else>
        =${value}
    </#if>
</#macro>

<#macro syncMethod inContainer>
${syncedClass.qualifiedName} instance = (${syncedClass.qualifiedName}) obj;

SyncEvent event = super.check(instance, flags, player);
TypeSyncer.Change change;

    <#assign fieldId = firstId>
    <#list members as member>
        <#if member.inContainer == inContainer>
            <#assign m = member.syncer.syncMacro>
            <@.vars[m] element=member fieldId=fieldId />
            <#assign fieldId++>
        </#if>
    </#list>

return event;
</#macro>

package ${package};

import de.take_weiland.mods.commons.internal.sync_olds.SyncCompanion;
import de.take_weiland.mods.commons.internal.sync_olds.SyncEvent;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import net.minecraft.entity.player.EntityPlayerMP;
import java.util.Objects;

public class ${companionClass} extends SyncCompanion {

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
        return 0;
    }

    @Override
    public int read(Object instance, MCDataInput in) {
        return 0;
    }
}



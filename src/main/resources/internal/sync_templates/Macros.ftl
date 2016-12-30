<#-- @ftlvariable name="" type="de.take_weiland.mods.commons.internal.sync_processing.CompanionTemplateModel" -->

<#macro eqCheck element fieldId>
    <#-- @ftlvariable name="element" type="de.take_weiland.mods.commons.internal.sync_processing.SyncedProperty" -->
    ${element.syncer.targetClass.qualifiedName}.${element.syncer.equalityCheck.simpleName}(<@propertyGet element />, <@companionGet element />)
</#macro>

<#macro eventAdd>
event.add(change);
</#macro>

<#macro propertyGet property>
<#-- @ftlvariable name="property" type="de.take_weiland.mods.commons.internal.sync_processing.SyncedProperty" -->
instance.${property.getter.simpleName}<#if property.method>()</#if>
</#macro>

<#macro propertySet property value>
<#-- @ftlvariable name="property" type="de.take_weiland.mods.commons.internal.sync_processing.SyncedProperty" -->
instance.${property.setter.simpleName}
    <#if property.method>
    (${value})
    <#else>
    =${value}
    </#if>
</#macro>

<#macro companionGet property>
<#-- @ftlvariable name="property" type="de.take_weiland.mods.commons.internal.sync_processing.SyncedProperty" -->
this.${property.companionFieldName}
</#macro>

<#macro companionSet property value>
<#-- @ftlvariable name="property" type="de.take_weiland.mods.commons.internal.sync_processing.SyncedProperty" -->
this.${property.companionFieldName} = ${value}
</#macro>

<#macro syncMethod inContainer>
${syncedClass.qualifiedName} instance = (${syncedClass.qualifiedName}) obj;

SyncEvent event = super.check<#if inContainer>InContainer</#if>(instance, flags, player);
ChangedValue<?> change;

    <#assign fieldId = 1>
    <#list members as member>
        <#if member.inContainer == inContainer>
            if (!<@eqCheck member fieldId />) {
                change = new ${member.syncer.changedValueClass.canonicalName}(super.fieldOffset() + ${fieldId?c}, <@propertyGet member />);
                <@eventAdd />
                <#--this.${element.companionFieldName} = <@propertyGet property=element/>;-->
            }
        </#if>
        <#assign fieldId++>
    </#list>



return event;
</#macro>

<#macro readChange member fieldId>
<#-- @ftlvariable name="member" type="de.take_weiland.mods.commons.internal.sync_processing.SyncedProperty" -->

</#macro>
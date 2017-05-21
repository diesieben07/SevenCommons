package de.take_weiland.mods.commons.internal

/**
 * @author diesieben07
 */
internal val <E : Enum<E>> Class<E>.sharedEnumConstants
    inline get() = EnumConstantsAccessor.getEnumConstantsShared(this)
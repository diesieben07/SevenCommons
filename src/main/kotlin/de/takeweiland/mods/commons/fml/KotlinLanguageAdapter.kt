package de.takeweiland.mods.commons.fml

import net.minecraftforge.fml.common.FMLModContainer
import net.minecraftforge.fml.common.ILanguageAdapter
import net.minecraftforge.fml.common.ModContainer
import net.minecraftforge.fml.relauncher.Side
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * @author Take Weiland
 */
const val KOTLIN_LANGUAGE_ADAPTER = "de.takeweiland.mods.commons.fml.KotlinLanguageAdapter"

@Suppress("unused")
class KotlinLanguageAdapter : ILanguageAdapter {

    override fun getNewInstance(container: FMLModContainer, objectClass: Class<*>, classLoader: ClassLoader?, factoryMarkedAnnotation: Method?): Any {
        return getObjectInstance(objectClass)
    }

    override fun supportsStatics(): Boolean = false

    override fun setProxy(target: Field, proxyTarget: Class<*>, proxy: Any?) {
        target.set(getObjectInstance(proxyTarget), proxy)
    }

    private fun getObjectInstance(objectClass: Class<*>): Any {
        // avoid dependency on kotlin reflection
        val instanceField = objectClass.getDeclaredField("INSTANCE")
        if (instanceField == null || instanceField.type != objectClass || !Modifier.isStatic(instanceField.modifiers)) {
            throw IllegalArgumentException("Cannot use ${objectClass.name} as a Kotlin mod, it is not an object.")
        }
        return instanceField.get(null)
    }

    override fun setInternalProxies(mod: ModContainer?, side: Side?, loader: ClassLoader?) {}
}
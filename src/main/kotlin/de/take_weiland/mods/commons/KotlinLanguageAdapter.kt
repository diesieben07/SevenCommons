package de.take_weiland.mods.commons

import net.minecraftforge.fml.common.FMLModContainer
import net.minecraftforge.fml.common.ILanguageAdapter
import net.minecraftforge.fml.common.ModContainer
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.LogManager
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * @author diesieben07
 */
@Suppress("unused")
class KotlinLanguageAdapter : ILanguageAdapter {

    companion object {
        const val name = "de.take_weiland.mods.commons.KotlinLanguageAdapter"

        private val log = LogManager.getLogger("KotlinLanguageAdapter")
    }

    override fun getNewInstance(container: FMLModContainer, objectClass: Class<*>, classLoader: ClassLoader?, factoryMethod: Method?): Any {
        return if (factoryMethod != null) {
            log.debug("Using annotated factory method $factoryMethod")
            factoryMethod.invoke(null)
        } else {
            val instanceField = objectClass.getDeclaredField("INSTANCE") ?: throw IllegalStateException("Kotlin mods must be objects, not classes.")

            instanceField.isAccessible = true
            instanceField.get(null)
        }
    }

    override fun setProxy(target: Field?, proxyTarget: Class<*>?, proxy: Any?) {
        throw IllegalStateException("@SidedProxy is not supported in Kotlin mods, use by sidedProxy() instead.")
    }

    override fun supportsStatics(): Boolean = false

    override fun setInternalProxies(mod: ModContainer?, side: Side?, loader: ClassLoader?) = Unit
}
package de.take_weiland.mods.commons.client;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.Locale;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.publicLookup;

/**
 * <p>Utility class for translating.</p>
 *
 * @author diesieben07
 */
public final class I18n {

    /**
     * <p>Translate the given key.</p>
     *
     * @param key the key to translate
     * @return the translation
     */
    public static String translate(String key) {
        try {
            return (String) translate.invokeExact((Locale) currentLocalGet.invokeExact(), key);
        } catch (Throwable x) {
            throw Throwables.propagate(x);
        }
    }

    /**
     * <p>Translate the given key and pass the result to {@link String#format(String, Object...)} with the given args.</p>
     *
     * @param key  the key to translate
     * @param args the formatting arguments
     * @return the translation
     */
    public static String translate(String key, Object... args) {
        try {
            return ((Locale) currentLocalGet.invokeExact()).formatMessage(key, args);
        } catch (Throwable x) {
            throw Throwables.propagate(x);
        }
    }

    private static final MethodHandle currentLocalGet;
    private static final MethodHandle translate;

    static {
        try {
            Field field = LanguageManager.class.getDeclaredField(MCPNames.field(SRGConstants.F_CURRENT_LOCALE));
            field.setAccessible(true);
            currentLocalGet = publicLookup().unreflectGetter(field);

            Method method = Locale.class.getDeclaredMethod(MCPNames.method(SRGConstants.M_TRANSLATE_KEY_PRIVATE), String.class);
            method.setAccessible(true);
            translate = publicLookup().unreflect(method);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private I18n() {
    }
}

package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.Loader;

import static com.google.common.base.Preconditions.checkState;
import static cpw.mods.fml.common.LoaderState.PREINITIALIZATION;

/**
 * @author diesieben07
 */
final class RegistrationUtil {

    static void checkPhase(String type) {
        checkState(Loader.instance().isInState(PREINITIALIZATION), "Mod %s tried to register a %s outside of the preInit phase", Loader.instance().activeModContainer().getModId(), type);
    }

}

package de.take_weiland.mods.commons.function;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.util.JavaUtils;

import java.util.function.Function;

/**
 * @author diesieben07
 */
final class FuncUtils {

    static Function<Throwable, RuntimeException> uncheckedRethrower() {
        return JavaUtils::throwUnchecked;
    }

    static Function<Throwable, RuntimeException> checkedRethrower() {
        return Throwables::propagate;
    }

}


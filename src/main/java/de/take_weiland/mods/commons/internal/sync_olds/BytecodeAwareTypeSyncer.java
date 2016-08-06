package de.take_weiland.mods.commons.internal.sync_olds;

import de.take_weiland.mods.commons.asm.ASMCallable;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import org.objectweb.asm.commons.GeneratorAdapter;

/**
 * @author diesieben07
 */
public interface BytecodeAwareTypeSyncer<VAL, COM, DATA> extends TypeSyncer<VAL, COM, DATA> {

    void emitCheck(GeneratorAdapter gen, ASMCallable addChange,
                   ASMCallable getProperty, ASMCallable setProperty,
                   ASMCallable getCompanion, ASMCallable setCompanion);

}

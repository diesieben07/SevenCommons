package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;

/**
 * @author diesieben07
 */
public interface ASMCallable {

    void emitCall(MethodVisitor mv, Consumer<MethodVisitor> parameterLoader);

}

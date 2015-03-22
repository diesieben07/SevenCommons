package de.take_weiland.mods.commons.internal.transformers.sync;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM4;

/**
 * @author diesieben07
 */
public class PlayerTickHook extends MethodVisitor {

    public PlayerTickHook(MethodVisitor mv) {
        super(ASM4, mv);
    }
}

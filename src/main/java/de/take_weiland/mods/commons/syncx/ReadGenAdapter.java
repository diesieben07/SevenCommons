package de.take_weiland.mods.commons.syncx;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

/**
 * @author diesieben07
 */
public final class ReadGenAdapter extends GeneratorAdapter {

    ReadGenAdapter(ClassVisitor cv, int access, Method method) {
        super(Opcodes.ASM4, cv.visitMethod(access, method.getName(), method.getDescriptor(), null, null), access,
                method.getName(), method.getDescriptor());
    }

    public void loadInStream() {
        loadArg(0);
    }

}

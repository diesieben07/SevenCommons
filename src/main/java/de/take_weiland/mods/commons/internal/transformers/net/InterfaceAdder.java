package de.take_weiland.mods.commons.internal.transformers.net;

import com.google.common.collect.ObjectArrays;
import org.objectweb.asm.ClassVisitor;

import static org.objectweb.asm.Opcodes.ASM5;

/**
 * <p>Add an interface to a class.</p>
 *
 * @author diesieben07
 */
public final class InterfaceAdder extends ClassVisitor {

    private final String interfaceName;

    public InterfaceAdder(ClassVisitor cv, String interfaceName) {
        super(ASM5, cv);
        this.interfaceName = interfaceName;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (interfaces == null) {
            interfaces = new String[]{interfaceName};
        } else {
            interfaces = ObjectArrays.concat(interfaceName, interfaces);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }
}

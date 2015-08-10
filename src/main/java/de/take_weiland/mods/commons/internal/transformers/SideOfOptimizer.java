package de.take_weiland.mods.commons.internal.transformers;

import cpw.mods.fml.relauncher.Side;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * <p>On the dedicated server replaces all Sides.sideOf() methods with just "return Side.SERVER". This should allow
 * the JVM to fold more checks away.</p>
 *
 * @author diesieben07
 */
final class SideOfOptimizer extends MethodVisitor {

    private final MethodVisitor mv;

    SideOfOptimizer(MethodVisitor mv) {
        super(ASM5, null);
        this.mv = mv;
    }

    @Override
    public void visitCode() {
        mv.visitCode();

        mv.visitFieldInsn(GETSTATIC, Type.getInternalName(Side.class), "SERVER", Type.getDescriptor(Side.class));
        mv.visitInsn(ARETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }


}

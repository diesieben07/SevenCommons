package de.take_weiland.mods.commons.asm.info;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
final class ClassInfoArray extends ClassInfo {

    private final int dimensions;
    private final String internalName;
    private final int modifiers;

    ClassInfoArray(int dimensions, ClassInfo rootType) {
        this.dimensions = dimensions;

        String rootIntName = rootType.internalName();

        StringBuilder sb = new StringBuilder(rootIntName.length() + dimensions + 1);
        for (int i = 0; i < dimensions; i++) {
            sb.append('[');
        }
        sb.append('L');
        sb.append(rootIntName);
        this.internalName = sb.toString();

        // see Class.getModifiers
        this.modifiers = rootType.modifiers() & (ACC_PUBLIC | ACC_PROTECTED | ACC_PRIVATE) | ACC_FINAL;
    }

    @Override
    public List<String> interfaces() {
        return ImmutableList.of();
    }

    @Override
    public String superName() {
        // see Class.getSuperClass
        return "java/lang/Object";
    }

    @Override
    public String internalName() {
        return internalName;
    }

    @Override
    public int getDimensions() {
        return dimensions;
    }

    @Override
    public int modifiers() {
        return modifiers;
    }
}

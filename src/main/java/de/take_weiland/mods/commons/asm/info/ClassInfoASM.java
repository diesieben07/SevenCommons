package de.take_weiland.mods.commons.asm.info;

import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
final class ClassInfoASM extends ClassInfo {

    private final ClassNode clazz;
    private final List<String> interfaces;

    ClassInfoASM(ClassNode clazz) {
        this.clazz = checkNotNull(clazz, "ClassNode");
        this.interfaces = Collections.unmodifiableList(clazz.interfaces);
    }

    @Override
    public List<String> interfaces() {
        return interfaces;
    }

    @Override
    public String superName() {
        return clazz.superName;
    }

    @Override
    public String internalName() {
        return clazz.name;
    }

    @Override
    public int modifiers() {
        return clazz.access;
    }

    @Override
    public int getDimensions() {
        // we never load array classes as a ClassNode
        return 0;
    }

}

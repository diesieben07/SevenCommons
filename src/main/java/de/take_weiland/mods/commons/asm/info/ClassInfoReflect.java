package de.take_weiland.mods.commons.asm.info;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
final class ClassInfoReflect extends ClassInfo {

    private final Class<?> clazz;
    private final List<String> interfaces;

    ClassInfoReflect(Class<?> clazz) {
        this.clazz = checkNotNull(clazz, "clazz");
        interfaces = Lists.transform(
                Collections.unmodifiableList(Arrays.asList(clazz.getInterfaces())),
                Type::getInternalName);
    }

    @Override
    public List<String> interfaces() {
        return interfaces;
    }

    @Override
    public String superName() {
        Class<?> s = clazz.getSuperclass();
        return s == null ? null : Type.getInternalName(s);
    }

    @Override
    public String internalName() {
        return Type.getInternalName(clazz);
    }

    @Override
    public int modifiers() {
        return clazz.getModifiers();
    }

    @Override
    public int getDimensions() {
        if (clazz.isArray()) {
            return StringUtils.countMatches(clazz.getName(), "[");
        } else {
            return 0;
        }
    }

    @Override
    boolean callRightAssignableFrom(ClassInfo parent) {
        return parent.isAssignableFromReflect(this);
    }

    @Override
    boolean isAssignableFromReflect(ClassInfoReflect child) {
        // use JDK test if both are reflectively loaded
        return this.clazz.isAssignableFrom(child.clazz);
    }

}

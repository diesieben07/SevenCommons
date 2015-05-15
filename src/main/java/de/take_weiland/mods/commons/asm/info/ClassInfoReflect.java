package de.take_weiland.mods.commons.asm.info;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
final class ClassInfoReflect extends ClassInfo {

    private final Class<?> clazz;
    private List<String> interfaces;

    ClassInfoReflect(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public List<String> interfaces() {
        if (interfaces == null) {
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            for (Class<?> iface : clazz.getInterfaces()) {
                builder.add(Type.getInternalName(iface));
            }
            interfaces = builder.build();
        }
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

    private String sourceFileCache;

    @Override
    public String getSourceFile() {
        if (sourceFileCache == null) {
            InputStream in = clazz.getResourceAsStream('/' + clazz.getName().replace('.', '/') + ".class");
            if (in != null) {
                try {
                    ClassReader cr = new ClassReader(ByteStreams.toByteArray(in));
                    SourceFileExtractor extractor = new SourceFileExtractor();
                    cr.accept(extractor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    sourceFileCache = Strings.nullToEmpty(extractor.sourceFile);
                } catch (IOException e) {
                    sourceFileCache = "";
                } finally {
                    IOUtils.closeQuietly(in);
                }
            }
        }
        return Strings.emptyToNull(sourceFileCache);
    }

    private static final class SourceFileExtractor extends ClassVisitor {

        String sourceFile;

        SourceFileExtractor() {
            super(ASM4, null);
        }

        @Override
        public void visitSource(String source, String debug) {
            sourceFile = source;
            super.visitSource(source, debug);
        }
    }
}

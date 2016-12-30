package de.take_weiland.mods.commons.internal.sync_processing;

import de.take_weiland.mods.commons.internal.sync_olds.SyncCompanion;
import de.take_weiland.mods.commons.internal.sync_processing.description.ObjectDescription;
import de.take_weiland.mods.commons.sync.Sync;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;
import static de.take_weiland.mods.commons.internal.sync_processing.SyncAnnotationProcessor.getPackage;

/**
 * @author diesieben07
 */
public class PendingCompanion {

    private final SyncAnnotationProcessor processor;
    private final PendingCompanion parent;
    final ObjectDescription<TypeElement> clazz;
    private final List<SyncedProperty> properties;

    private String qualifiedName;

    public PendingCompanion(SyncAnnotationProcessor processor, PendingCompanion parent, TypeElement clazz) {
        this.processor = processor;
        this.parent = parent;
        this.clazz = ObjectDescription.forClass(clazz);

        properties = getSyncedElements(clazz)
                .map(e -> SyncedProperty.create(processor, e))
                .collect(Collectors.toList());
    }

    boolean hasProperties() {
        return !properties.isEmpty();
    }

    boolean isReady() {
        if (parent == null || parent.isReady()) {
            for (SyncedProperty property : properties) {
                if (!property.isReady()) {
                    processor.getEnv().getMessager().printMessage(Diagnostic.Kind.NOTE, "property " + property + " is not ready");
                    return false;
                }
            }
            return true;
//            if (properties.stream().allMatch(SyncedProperty::isReady)) {
//                return true;
//            }
        }
        return false;
    }

    String getSuperClassName() {
        return parent == null ? SyncCompanion.class.getCanonicalName() : parent.getClassName();
    }

    String getClassName() {
        if (qualifiedName == null) {
            try {
                generate(SyncAnnotationProcessor.instance().getEnv());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return qualifiedName;
    }

    void generate(ProcessingEnvironment env) throws IOException {
        checkState(isReady(), "not ready");
        checkState(qualifiedName == null, "Already generated");

        if (properties.isEmpty()) {
            qualifiedName = SyncCompanion.class.getCanonicalName();
            return;
        }

        TypeElement resolvedClass = clazz.resolve(env);

        String suffix = "$$SyncCompanion";
        String simpleName = resolvedClass.getSimpleName() + suffix;
        qualifiedName = resolvedClass.getQualifiedName() + suffix;
        String pkg = getPackage(resolvedClass).getQualifiedName().toString();

        int firstId = SyncCompanion.FIRST_USABLE_ID;

        CompanionTemplateModel model = new CompanionTemplateModel(this, pkg, simpleName, resolvedClass, properties, firstId);

        JavaFileObject file = processor.getEnv().getFiler().createSourceFile(qualifiedName, resolvedClass);

        Template template = processor.getTemplate("Companion");

        try (Writer writer = SyncAnnotationProcessor.getWriter(file)) {
            template.process(model, writer);
        } catch (TemplateException e) {
            throw new IOException(e);
        }
    }

    private Stream<? extends Element> getSyncedElements(TypeElement clazz) {
        return clazz.getEnclosedElements().stream()
                .filter(e -> EnumSet.of(ElementKind.FIELD, ElementKind.METHOD).contains(e.getKind()))
                .filter(e -> SyncAnnotationProcessor.isAnnotationPresent(e, Sync.class));
    }

    public String toString() {
        return "Class: " + clazz;
    }
}

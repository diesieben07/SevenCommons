package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import de.take_weiland.mods.commons.sync.Sync;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.*;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Verify.verify;

/**
 * @author diesieben07
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("de.take_weiland.mods.commons.sync.*")
public class SyncAnnotationProcessor extends AbstractProcessor {

    private final Configuration templateConfig;

    public SyncAnnotationProcessor() {
        templateConfig = new Configuration(Configuration.VERSION_2_3_25);
        templateConfig.setTemplateLoader(new ClassTemplateLoader(SyncAnnotationProcessor.class, "/internal/sync_templates/"));
        templateConfig.setDefaultEncoding("UTF-8");
        templateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
        templateConfig.setLogTemplateExceptions(false);
    }

    public static final class ProcessingException extends RuntimeException {

        final Consumer<Messager> handler;

        public ProcessingException(String message) {
            this(message, null);
        }

        public ProcessingException(String message, Throwable cause) {
            this(defaultMessagePrinter(message), message, cause);
        }

        public ProcessingException(Consumer<Messager> handler, String message) {
            this(handler, message, null);
        }

        public ProcessingException(Consumer<Messager> handler) {
            this(handler, null, null);
        }

        public ProcessingException(Consumer<Messager> handler, String message, Throwable cause) {
            super(message, cause);
            this.handler = handler;
        }

        private static Consumer<Messager> defaultMessagePrinter(String msg) {
            return m -> m.printMessage(Diagnostic.Kind.ERROR, msg);
        }
    }

    private final Set<Name> done = new HashSet<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        try {
            roundEnv.getElementsAnnotatedWith(Sync.class).stream()
                    .map(Element::getEnclosingElement)
                    .map(TypeElement.class::cast)
                    .distinct()
                    .forEach(el -> {
                        if (done.add(el.getQualifiedName())) {
                            generateCompanion(el, processingEnv.getFiler());
                        }
                    });
        } catch (ProcessingException e) {
            e.handler.accept(processingEnv.getMessager());
            e.printStackTrace();
        }
        return true;
    }

    private void generateCompanion(TypeElement clazz, Filer filer) {
        try {
            int firstId = 0;

            String suffix = "$$SyncCompanion";
            String simpleName = clazz.getSimpleName() + suffix;
            String qualifiedName = clazz.getQualifiedName() + suffix;
            String pkg = getPackage(clazz).getQualifiedName().toString();

            List<SyncedProperty> syncedProperties = clazz.getEnclosedElements().stream()
                    .filter(e -> EnumSet.of(ElementKind.FIELD, ElementKind.METHOD).contains(e.getKind()))
                    .filter(e -> isAnnotationPresent(e, Sync.class))
                    .map(e -> SyncedProperty.create(processingEnv, e))
                    .collect(Collectors.toList());

            CompanionTemplateModel model = new CompanionTemplateModel(pkg, simpleName, clazz, syncedProperties, firstId);

            JavaFileObject file = filer.createSourceFile(qualifiedName, clazz);

            Formatter formatter = new Formatter();

            StringWriter buf = new StringWriter();
            Template template = getTemplate("Companion");

            template.process(model, buf);

            try (Writer writer = file.openWriter()) {
                try {
                    writer.write(formatter.formatSource(buf.toString()));
                } catch (FormatterException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, fullToString(e));
                    writer.write(buf.toString());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    private static String fullToString(Throwable t) {
        StringWriter w = new StringWriter();
        t.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

    private static boolean isAnnotationPresent(Element element, Class<? extends Annotation> annotation) {
        return element.getAnnotationMirrors().stream()
                .map(AnnotationMirror::getAnnotationType)
                .map(DeclaredType::asElement)
                .map(TypeElement.class::cast)
                .map(TypeElement::getQualifiedName)
                .anyMatch(n -> n.contentEquals(annotation.getName()));

    }

    private String generateChecks(int startId, List<? extends Element> elements) {
        return IntStream.range(startId, startId + elements.size())
                .mapToObj(fieldId -> Pair.of(fieldId, elements.get(fieldId - startId)))
                .map(pair -> getCheck(pair.getLeft(), pair.getRight()))
                .collect(Collectors.joining("\n"));
    }

    private String getCheck(int fieldId, Element element) {
        try {
            Map<String, String> map = ImmutableMap.<String, String>builder()
                    .put("check", getSyncerBasedCheck(element))
                    .put("fieldId", String.valueOf(fieldId))
                    .build();

            return CharStreams.toString(getTemplate("ElementCheck", map));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String getSyncerBasedCheck(Element element) throws IOException {
        Map<String, String> map = ImmutableMap.<String, String>builder()
                .put("syncer", "((TypeSyncer) null)")
                .put("obj", "instance")
                .put("property", "((PropertyAccess) null)")
                .put("cProperty", "((PropertyAccess) null)")
                .put("cObj", "this")
                .build();

        return CharStreams.toString(getTemplate("SyncerBasedCheck", map));
    }

    private static PackageElement getPackage(TypeElement clazz) {
        Element el = clazz.getEnclosingElement();
        while (!(el instanceof PackageElement)) {
            el = el.getEnclosingElement();
        }
        return (PackageElement) el;
    }

    private void generateAccessor(Element element, Filer filer) throws IOException {
        if (validateElement(element)) return;

        String type = element.asType().toString();

        Element enclosing = element.getEnclosingElement();
        verify(enclosing.getKind() == ElementKind.CLASS);

        Name pkg = ((PackageElement) enclosing.getEnclosingElement()).getQualifiedName();
        String simpleName = enclosing.getSimpleName() + "$$Access$" + element.getSimpleName();
        String accessorClassName = pkg + simpleName;

        Map<String, String> replacements = ImmutableMap.<String, String>builder()
                .put("cls", simpleName)
                .put("type", type)
                .put("typevars", "")
                .put("field", element.getSimpleName().toString())
                .put("enclosing", ((TypeElement) enclosing).getQualifiedName().toString())
                .build();


        JavaFileObject file = filer.createSourceFile(accessorClassName);
        try (Writer writer = file.openWriter()) {
            CharStreams.copy(getTemplate("FieldAccessor", replacements), writer);
        }
    }

    private static Reader getTemplate(String template, Map<String, String> replacements) throws IOException {
        String s = Resources.toString(SyncAnnotationProcessor.class.getResource("/internal/sync_templates/" + template + ".template.java"), StandardCharsets.UTF_8);

        StrSubstitutor substitutor = new StrSubstitutor(replacements, "$", "$");

        return new StringReader(substitutor.replace(s));
    }


    private Template getTemplate(String name) {
        try {
            return templateConfig.getTemplate(name + ".ftl");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean validateElement(Element element) {
        if (element.getKind() != ElementKind.FIELD || element.getKind() != ElementKind.METHOD) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Sync can only be used on fields or methods");
            return true;
        }

        if (element.getModifiers().contains(Modifier.STATIC)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Sync cannot be used on static members");
            return true;
        }

        if (element.getKind() == ElementKind.FIELD && element.getModifiers().contains(Modifier.FINAL)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Sync cannot be used on final fields");
        }

        return false;
    }

}

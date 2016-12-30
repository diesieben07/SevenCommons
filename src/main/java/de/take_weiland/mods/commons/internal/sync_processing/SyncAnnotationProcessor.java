package de.take_weiland.mods.commons.internal.sync_processing;

import com.google.common.base.CharMatcher;
import com.google.common.base.Throwables;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.google.googlejavaformat.java.JavaFormatterOptions.JavadocFormatter;
import com.google.googlejavaformat.java.JavaFormatterOptions.SortImports;
import com.google.googlejavaformat.java.JavaFormatterOptions.Style;
import de.take_weiland.mods.commons.sync.Sync;
import de.take_weiland.mods.commons.sync.SyncerDefinition;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.*;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author diesieben07
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("de.take_weiland.mods.commons.sync.*")
public class SyncAnnotationProcessor extends AbstractProcessor {

    private static final String SYNCER_LIST_PACKAGE = "de.take_weiland.mods.commons.internal.sync_list";

    private Configuration templateConfig;
    private CompileTimeSyncerFactory syncerFactory;

    private final Set<String> foundSyncers = new HashSet<>();
    final List<CompileTimeSyncer> knownSyncers = new ArrayList<>();

    private final Set<String> processedSyncs = new HashSet<>();
    private final Set<String> pendingSyncs = new HashSet<>();

    int round = 0;

    private static SyncAnnotationProcessor instance;

    private static synchronized void initInstance(SyncAnnotationProcessor self) {
        if (instance != null) {
            throw new IllegalStateException("More than one SyncAnnotationProcessor per process?");
        }
        instance = self;
    }

    public static synchronized SyncAnnotationProcessor instance() {
        if (instance == null) {
            throw new IllegalStateException("no instance");
        }
        return instance;
    }

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        initInstance(this);

        syncerFactory = new CompileTimeSyncerFactory(this);
        templateConfig = new Configuration(Configuration.VERSION_2_3_25);
        templateConfig.setTemplateLoader(new ClassTemplateLoader(SyncAnnotationProcessor.class, "/internal/sync_templates/"));
        templateConfig.setDefaultEncoding("UTF-8");
        templateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
        templateConfig.setLogTemplateExceptions(false);
    }

    public ProcessingEnvironment getEnv() {
        return processingEnv;
    }


    public CompileTimeSyncerFactory getSyncerFactory() {
        return syncerFactory;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Round: " + round);
        try {
            discoverSyncers(roundEnv);
            loadExistingSyncers();

            discoverPendingSyncs(roundEnv);
            processPendingSyncs();
        } catch (ProcessingException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "ProcessingException?! " + fullToString(e));
            e.handler.accept(processingEnv.getMessager());
        } catch (Throwable x) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "exception! " + fullToString(x));
        } finally {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "End of round " + round);
            if (roundEnv.processingOver() && !pendingSyncs.isEmpty()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not process all @Sync annotations, " +
                        "the following failed:\n" + pendingSyncs);
            }
            round++;
        }

        return true;
    }

    private void discoverPendingSyncs(RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "discovering pending @Syncs...");
        roundEnv.getElementsAnnotatedWith(Sync.class).stream()
                .map(Element::getEnclosingElement)
                .map(TypeElement.class::cast)
                .map(TypeElement::getQualifiedName)
                .map(CharSequence::toString)
                .filter(s -> !processedSyncs.contains(s))
                .distinct()
                .peek(c -> processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "found pending @Sync: " + c))
                .forEach(pendingSyncs::add);
    }

    private void processPendingSyncs() {
        pendingSyncs.stream()
                .map(processingEnv.getElementUtils()::getTypeElement)
                .map(this::getPendingCompanion)
                .collect(Collectors.toList()) // collect into a temporary list so we can remove from pendingSyncs
                .forEach(this::tryProcessPendingCompanion);
    }

    private void tryProcessPendingCompanion(PendingCompanion pendingCompanion) {
        if (pendingCompanion.isReady()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing pending companion: " + pendingCompanion);
            try {
                pendingCompanion.generate(processingEnv);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            String qualifiedName = pendingCompanion.clazz.resolve(processingEnv).getQualifiedName().toString();
            processedSyncs.add(qualifiedName);
            pendingSyncs.remove(qualifiedName);
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Skipping pending companion, not ready yet: " + pendingCompanion);
        }
    }

    private PendingCompanion getPendingCompanion(TypeElement element) {
        TypeMirror superType = element.getSuperclass();
        PendingCompanion parent;
        if (superType.getKind() != TypeKind.DECLARED) {
            parent = null;
        } else {
            parent = getPendingCompanion((TypeElement) processingEnv.getTypeUtils().asElement(superType));
        }
        PendingCompanion companion = new PendingCompanion(this, parent, element);
        return companion.hasProperties() ? companion : null;
    }

    private void loadExistingSyncers() {
        Optional.ofNullable(processingEnv.getElementUtils().getPackageElement(SYNCER_LIST_PACKAGE))
                .map(PackageElement::getEnclosedElements)
                .orElse(Collections.emptyList())
                .stream()
                .map(TypeElement.class::cast)
                .map(this::getReferencedSyncerClass)
                .map(TypeElement::getQualifiedName)
                .map(CharSequence::toString)
                .forEach(foundSyncers::add);

        knownSyncers.clear();
        foundSyncers.stream()
                .map(processingEnv.getElementUtils()::getTypeElement)
                .map(AnnotationBasedSyncer::createFromClass)
                .forEach(knownSyncers::add);
    }

    private TypeElement getReferencedSyncerClass(TypeElement listClass) {
        return listClass.getAnnotationMirrors().stream()
                .filter(a -> ((TypeElement) a.getAnnotationType().asElement()).getQualifiedName().contentEquals(SerializedSyncer.class.getName()))
                .map(AnnotationMirror::getElementValues)
                .flatMap(m -> m.entrySet().stream())
                .filter(e -> e.getKey().getSimpleName().contentEquals("value"))
                .map(Map.Entry::getValue)
                .map(AnnotationValue::getValue)
                .map(TypeMirror.class::cast)
                .map(processingEnv.getTypeUtils()::asElement)
                .map(TypeElement.class::cast)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    private void discoverSyncers(RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(SyncerDefinition.class)
                .forEach(cl -> {
                    TypeElement syncerClass = (TypeElement) cl;

                    try {
                        Filer filer = processingEnv.getFiler();
                        String className = CharMatcher.javaLetterOrDigit().negate().replaceFrom(syncerClass.getQualifiedName(), '_');
                        JavaFileObject sourceFile = filer.createSourceFile(SYNCER_LIST_PACKAGE + '.' + className, syncerClass);

                        DiscoveredSyncerModel model = new DiscoveredSyncerModel(className, syncerClass);

                        try (Writer out = sourceFile.openWriter()) {
                            getTemplate("DiscoveredSyncer").process(model, out);
                        }
                    } catch (IOException | TemplateException e) {
                        throw Throwables.propagate(e);
                    }
                });
    }

    static Writer getWriter(JavaFileObject file) throws IOException {
        Writer original = file.openWriter();
        Formatter formatter = new Formatter(new JavaFormatterOptions(JavadocFormatter.NONE, Style.AOSP, SortImports.ALSO));

        return new StringWriter() {
            @Override
            public void close() throws IOException {
                super.close();
                try {
                    original.write(formatter.formatSource(this.toString()));
                } catch (FormatterException e) {
                    original.write(this.toString());
                } finally {
                    original.close();
                }
            }
        };
    }

    private static String fullToString(Throwable t) {
        StringWriter w = new StringWriter();
        t.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

    static boolean isAnnotationPresent(Element element, Class<? extends Annotation> annotation) {
        return element.getAnnotationMirrors().stream()
                .map(AnnotationMirror::getAnnotationType)
                .map(DeclaredType::asElement)
                .map(TypeElement.class::cast)
                .map(TypeElement::getQualifiedName)
                .anyMatch(n -> n.contentEquals(annotation.getName()));

    }

    static PackageElement getPackage(TypeElement clazz) {
        Element el = clazz.getEnclosingElement();
        while (!(el instanceof PackageElement)) {
            el = el.getEnclosingElement();
        }
        return (PackageElement) el;
    }


    Template getTemplate(String name) {
        try {
            return templateConfig.getTemplate(name + ".ftl");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}

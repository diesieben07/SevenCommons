package de.take_weiland.mods.commons.internal.sync_processing;

import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author diesieben07
 */
public class CompileTimeSyncerFactory {

    private final SyncAnnotationProcessor processor;

    public CompileTimeSyncerFactory(SyncAnnotationProcessor processor) {
        this.processor = processor;
    }

    public Optional<CompileTimeSyncer> getSyncer(TypeMirror type) {
        processor.getEnv().getMessager().printMessage(Diagnostic.Kind.NOTE, "Trying to find syncer for type: " + type);
        processor.getEnv().getMessager().printMessage(Diagnostic.Kind.NOTE, "Candidates: " + processor.knownSyncers);

        List<CompileTimeSyncer> candidates = processor.knownSyncers.stream()
                .filter(syncer -> syncer.supports(type))
                .collect(Collectors.toList());

        switch (candidates.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(candidates.get(0));
            default:
                throw new RuntimeException("Multiple syncers for type " + type);
        }
//        switch (type.getKind()) {
//            case BOOLEAN:
//            case BYTE:
//            case CHAR:
//            case SHORT:
//            case INT:
//            case FLOAT:
//            case LONG:
//            case DOUBLE:
//                // TODO;
////                return new CompileTimeSyncer.BasicCompileTimeSyncer(type);
//            case ARRAY:
//                // TODO
//                return null;
//            case DECLARED:
//                // TODO
//                return null;
//            default:
//                throw new UnsupportedOperationException("Cannot sync type " + type);
//        }
    }

}

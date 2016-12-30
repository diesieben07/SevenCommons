package de.take_weiland.mods.commons.internal.sync_processing;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.util.function.Consumer;

/**
 * @author diesieben07
 */
public final class ProcessingException extends RuntimeException {

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

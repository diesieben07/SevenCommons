package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.asm.MCPNames;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * <p>Detects builders which are created but never built.</p>
 *
 * @author diesieben07
 */
public final class BuilderLeakDetect {

    private static final boolean ACTIVE = MCPNames.use();
    static final ReferenceQueue<Object> queue = ACTIVE ? new ReferenceQueue<>() : null;

    static void init() {
        if (ACTIVE) {
            new PollerThread().start();
        }
    }

    public static Runnable createBuilder(Object o, @Nonnull String description) {
        if (ACTIVE) {
            return new BuilderRef(o, queue, description);
        } else {
            return () -> {
            };
        }
    }

    private static final class BuilderRef extends PhantomReference<Object> implements Runnable {

        @Nullable
        volatile String description;

        BuilderRef(Object referent, ReferenceQueue<? super Object> q, @Nonnull String description) {
            super(referent, q);
            this.description = description;
        }

        @Override
        public void run() {
            description = null;
        }
    }

    private static final class PollerThread extends Thread {

        public PollerThread() {
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    BuilderRef ref = (BuilderRef) queue.remove();
                    if (ref.description != null) {
                        SevenCommons.INSTANCE.getLog().fatal(String.format("Builder %s has been garbage collected without being built", ref.description));
                    }
                } catch (InterruptedException e) {
                    SevenCommons.INSTANCE.getLog().warn("Builder misuse detection thread interrupted, exiting", e);
                    break;
                }
            }
        }
    }

}

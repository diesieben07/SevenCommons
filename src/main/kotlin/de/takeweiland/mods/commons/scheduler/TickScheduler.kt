@file:Mod.EventBusSubscriber

package de.takeweiland.mods.commons.scheduler

import de.takeweiland.mods.commons.SC_LOG
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.timeunit.TimeUnit
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.relauncher.FMLLaunchHandler
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Dispatches execution on the Minecraft client thread, if available.
 * Accessing this property on a dedicated server will throw an exception.
 */
val MinecraftClientThread: CoroutineDispatcher
    get() = if (FMLLaunchHandler.side() == Side.CLIENT)
        clientThread
    else
        throw IllegalStateException("Cannot use MinecraftClientThread on dedicated server")

/**
 * Dispatches execution on the Minecraft server thread.
 */
val MinecraftServerThread: CoroutineDispatcher get() = serverThread

val Side.mainThread: CoroutineDispatcher get() = if (this == Side.CLIENT) clientThread else serverThread

// internal implementation

@SubscribeEvent
@SideOnly(Side.CLIENT)
internal fun clientTick(event: TickEvent.ClientTickEvent) {
    clientThread.tick(event)
}

@SubscribeEvent
internal fun serverTick(event: TickEvent.ServerTickEvent) {
    serverThread.tick(event)
}

private val clientThread = MinecraftThreadDispatcher()
private val serverThread = MinecraftThreadDispatcher()

@JvmField
internal val nanoOrigin = System.nanoTime()

internal class MinecraftThreadDispatcher : CoroutineDispatcher(), Delay {

    private val queue = ConcurrentLinkedQueue<TickTask>()
    private var active = arrayOfNulls<TickTask>(5)
    private var activeSize = 0

    fun tick(event: TickEvent) {
        if (event.phase == TickEvent.Phase.START) return

        var free = -1
        for (idx in active.indices) {
            val task = active[idx] ?: break
            if (!task.guardedExecute()) {
                // remove the task
                if (free == -1) {
                    free = idx
                }
            } else if (free != -1) {
                active[free++] = task
                active[idx] = null
            }
        }

        if (free != -1) {
            activeSize = free
            for (i in free..active.lastIndex) active[i] = null
        }

        while (true) {
            val newTask = queue.poll() ?: break

            if (newTask.guardedExecute()) {
                if (activeSize == active.size) {
                    active = Arrays.copyOf(active, activeSize * 2)
                }
                active[activeSize++] = newTask
            }
        }
    }

    private fun TickTask.guardedExecute(): Boolean {
        return try {
            execute(this@MinecraftThreadDispatcher)
        } catch (x: Exception) {
            SC_LOG.error("Exception thrown during TickTask execution. Task will be removed", x)
            false
        }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        queue.offer(TickTask.forRunnable(block))
    }

    private class ScheduledContinuationTickTask(private val targetNanos: Long, private val continuation: CancellableContinuation<Unit>) : TickTask() {

        override fun execute(dispatcher: MinecraftThreadDispatcher): Boolean {
            return if ((System.nanoTime() - nanoOrigin) >= targetNanos) {
                with(continuation) {
                    with(dispatcher) {
                        resumeUndispatched(Unit)
                    }
                }
                false
            } else {
                true
            }
        }
    }

    internal class ScheduledRunnableTickTask(private val targetNanos: Long, @Volatile private var r: Runnable?) : TickTask(), DisposableHandle {

        override fun execute(dispatcher: MinecraftThreadDispatcher): Boolean {
            val r0 = r ?: return false // return false if disposed
            return if ((System.nanoTime() - nanoOrigin) >= targetNanos) {
                r0.run()
                false
            } else {
                true
            }
        }

        override fun dispose() {
            r = null
        }
    }

    override fun scheduleResumeAfterDelay(time: Long, unit: TimeUnit, continuation: CancellableContinuation<Unit>) {
        val targetNanos = (System.nanoTime() - nanoOrigin) + unit.toNanos(time)
        queue.offer(ScheduledContinuationTickTask(targetNanos, continuation))
    }

    override fun invokeOnTimeout(time: Long, unit: TimeUnit, block: Runnable): DisposableHandle {
        val targetNanos = (System.nanoTime() - nanoOrigin) + unit.toNanos(time)
        val task = ScheduledRunnableTickTask(targetNanos, block)
        queue.offer(task)
        return task
    }
}

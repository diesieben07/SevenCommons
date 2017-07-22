package de.take_weiland.mods.commons.util

import com.google.common.base.Preconditions.checkArgument
import com.google.common.primitives.Ints
import de.take_weiland.mods.commons.internal.SchedulerBase
import de.take_weiland.mods.commons.internal.SevenCommons
import net.minecraft.entity.Entity
import net.minecraft.world.World
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.relauncher.Side
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

/**
 *
 * A highly efficient, thread-safe, lock-free implementation of
 * [com.google.common.util.concurrent.ListeningExecutorService] that uses the
 * main Minecraft thread to execute tasks.
 *
 * Limited scheduling is available via the [.schedule] method.
 *
 * This ExecutorService cannot be shut down or terminated.
 *
 * If tasks are added from inside a task executed by this Scheduler, they will be executed in the same tick as the
 * task adding the new tasks. If a task is scheduled from inside another task, the current tick will count as the first
 * waiting tick.

 * @author diesieben07
 */
class Scheduler private constructor() : SchedulerBase() {

    /**
     *
     * Execute the given task after `tickDelay` ticks have passed.

     * @param r         the task
     * *
     * @param tickDelay the delay, in ticks
     */
    fun schedule(r: Runnable, tickDelay: Long) {
        checkArgument(tickDelay >= 0)
        execute(WaitingTask(r, tickDelay))
    }

    override fun execute(task: Runnable) {
        run(task::run)
    }

    /**
     * Execute the given task once.
     */
    inline fun run(crossinline task: () -> Unit) {
        execute(object : Task {
            override fun execute(): Boolean {
                task()
                return false
            }
        })
    }

    /**
     * Execute the given task until it returns false.
     */
    inline fun runUntil(crossinline task: () -> Boolean) {
        execute(object : Task {
            override fun execute(): Boolean = task()
        })
    }

    /**
     * Execute the given task until it returns false.
     */
    fun execute(task: Task) {
        inputQueue.offer(task)
    }

    // this is the queue that holds new tasks until they are picked up by the main thread
    private val inputQueue = ConcurrentLinkedQueue<Task>()

    // only used by the main thread
    private var activeTasks = arrayOfNulls<Task>(5)
    private var size = 0 // actual number of tasks in the above array, used for adding to the end

    override fun tick() {
        // handle existing tasks

        // move through task list and simultaneously execute tasks and compact the list
        // by moving non-removed tasks to the new end of the list if needed
        var free = -1

        for (idx in activeTasks.indices) {
            val task = activeTasks[idx] ?: break

            if (!task.checkedExecute()) {
                // task needs to be removed, null out it's slot
                activeTasks[idx] = null
                if (free == -1) {
                    // if this is the first task to be removed, set it as the compaction target
                    free = idx
                }
            } else if (free != -1) {
                // we had to remove one or more tasks earlier in the list,
                // move this one there to keep the list continuous
                activeTasks[free++] = task
                activeTasks[idx] = null
            }
        }

        if (free != -1) {
            size = free
        }

        // handle new tasks
        while (true) {
            val task = inputQueue.poll() ?: break

            // only add task to the active list if it wants to keep executing
            // avoids unnecessary work for one-off tasks
            if (task.checkedExecute()) {
                if (size == activeTasks.size) {
                    // we are full
                    val newArr = arrayOfNulls<Task>(size shl 1)
                    System.arraycopy(activeTasks, 0, newArr, 0, size)
                    this.activeTasks = newArr
                    activeTasks = this.activeTasks
                }
                activeTasks[size++] = task
            }
        }
    }

    interface Task {

        /**
         *
         * Execute this task, return true to keep executing.

         * @return true to keep executing
         */
        fun execute(): Boolean

    }

    private class WaitingTask internal constructor(private val r: Runnable, private var ticks: Long) : Task {

        override fun execute(): Boolean {
            if (--ticks == 0L) {
                r.run()
                return false
            } else {
                return true
            }
        }

        override fun toString(): String {
            return String.format("Scheduled task (task=%s, remainingTicks=%s)", r, ticks)
        }
    }

    /**
     * @return always false
     * *
     */
    @Deprecated("always false, this ExecutorService cannot be shut down")
    override fun isShutdown(): Boolean {
        return false
    }

    /**
     * @return always false
     * *
     */
    @Deprecated("always false, this ExecutorService cannot be shut down")
    override fun isTerminated(): Boolean {
        return false
    }


    @Deprecated("this ExecutorService cannot be shut down")
    override fun shutdown() {
    }

    /**
     * @return a list of all waiting tasks
     * *
     */
    @Deprecated("this ExecutorService cannot be shut down")
    override fun shutdownNow(): List<Runnable> {
        return emptyList()
    }

    /**
     * @param timeout the timeout
     * *
     * @param unit    TimeUnit
     * *
     * @return always false
     * *
     * @throws InterruptedException
     * *
     */
    @Deprecated("this ExecutorService cannot be shut down, always returns false after sleeping for the specified\n      amount of time")
    @Throws(InterruptedException::class)
    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        val millis = unit.toMillis(timeout)
        val milliNanos = TimeUnit.MILLISECONDS.toNanos(millis)
        val additionalNanos = Ints.saturatedCast(unit.toNanos(timeout) - milliNanos)
        Thread.sleep(millis, additionalNanos)
        return false
    }

    companion object {

        /**
         * The Scheduler that executes tasks on the server thread.
         */
        val server: Scheduler = Scheduler()

        private val client0: Scheduler? = if (FMLCommonHandler.instance().side.isClient) {
            Scheduler()
        } else {
            null
        }

        /**
         * The Scheduler that executes
         */
        val client: Scheduler
            get() = requireNotNull(client0) { "Client Scheduler not available on the server." }

        /**
         *
         * Return [client] if `side` is `Side.CLIENT`, [server] otherwise.

         * @param side the side
         * *
         * @return a Scheduler for the side
         */
        fun forSide(side: Side): Scheduler {
            return if (side == Side.CLIENT) client else server
        }
    }
}

internal fun Scheduler.Task.checkedExecute(): Boolean {
    try {
        return execute()
    } catch (x: Throwable) {
        SevenCommons.log.error("Exception thrown during execution of $this")
        return false
    }
}

/**
 * Execute the given task on the main server thread.
 */
inline fun serverThread(crossinline task: () -> Unit) {
    Scheduler.server.run(task)
}

/**
 * Execute the given task on the main client thread.
 */
inline fun clientThread(crossinline task: () -> Unit) {
    Scheduler.client.run(task)
}

/**
 * The `Scheduler` corresponding to the side of this `Entity`.
 */
val Entity.thread: Scheduler
    inline get() = world.thread

/**
 * The `Scheduler` corresponding to the side of this `World`.
 */
val World.thread: Scheduler
    inline get() = if (isRemote) Scheduler.client else Scheduler.server
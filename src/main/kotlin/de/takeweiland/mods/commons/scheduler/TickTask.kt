package de.takeweiland.mods.commons.scheduler

/**
 * @author Take Weiland
 */
internal abstract class TickTask {

    @JvmSynthetic
    internal abstract fun execute(dispatcher: MinecraftThreadDispatcher): Boolean

    companion object {
        private class RunnableTickTask(private val r: Runnable) : TickTask() {
            override fun execute(dispatcher: MinecraftThreadDispatcher): Boolean {
                r.run()
                return false
            }
        }

        fun forRunnable(r: Runnable): TickTask {
            return RunnableTickTask(r)
        }
    }

}
package cn.huohuas001.huhobot.mod.tools
import cn.huohuas001.bot.tools.Cancelable
import cn.huohuas001.huhobot.mod.HuHoBotMod
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class HuHoBotScheduler(private val plugin: HuHoBotMod) {

    val logger: Logger = plugin.LOGGER
    val server: MinecraftServer = plugin.serverInstance
    var taskList: MutableMap<Long, ScheduledTask> = ConcurrentHashMap()
    val taskIdGenerator = AtomicLong(0)

    fun startScheduler() {
        val tickThread = Thread({
            while (!server.isStopped) {
                try {
                    processTasks()
                    Thread.sleep(50) // 1 Tick  50ms
                } catch (ignored: InterruptedException) {}
            }
        }, "HuHoBotScheduler-Thread")
        tickThread.isDaemon = true // 守护线程，不阻止 JVM 退出
        tickThread.start()
    }

    private fun processTasks() {
        val iterator = taskList.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val task = entry.value

            if (task.isCancelled) {
                iterator.remove()
                continue
            }

            task.ticksLeft--
            if (task.ticksLeft <= 0) {
                try {
                    task.runnable.run()
                } catch (e: Exception) {
                    logger.error("任务执行异常", e)
                }

                if (task.isLoop) {
                    task.ticksLeft = task.intervalTicks.toLong()
                } else {
                    iterator.remove()
                }
            }
        }
    }

    class ScheduledTask internal constructor(
        private val id: Long,
        val runnable: Runnable,
        delayTicks: Long,
        val intervalTicks: Int,
        val isLoop: Boolean
    ): Cancelable {
        @Volatile
        var ticksLeft: Long = delayTicks

        @Volatile
        private var cancelled = false

        override fun cancel() {
            this.cancelled = true
        }

        val isCancelled: Boolean
            get() = cancelled
    }


    // 一次性延迟任务
    fun runTaskLater(task: Runnable, delayTicks: Long): ScheduledTask {
        val taskId = taskIdGenerator.incrementAndGet()
        val scheduled = ScheduledTask(taskId, task, delayTicks, 0, false)
        taskList[taskId] = scheduled
        return scheduled
    }

    // 循环任务
    fun runDelayedLoop(task: Runnable, delayTicks: Long, intervalTicks: Int): ScheduledTask {
        val taskId = taskIdGenerator.incrementAndGet()
        val scheduled = ScheduledTask(taskId, task, delayTicks, intervalTicks, true)
        taskList[taskId] = scheduled
        return scheduled
    }

    fun runDelayedLoop(task: Runnable, delayTicks: Long): ScheduledTask {
        return runDelayedLoop(task, delayTicks, 20) // 默认间隔20 ticks (1秒)
    }

    // 立即执行
    fun runTask(task: Runnable) {
        if (!server.isStopped) {
            server.submit(task)
        }
    }
}

package cn.huohuas001.huhobot.spigot

import cn.huohuas001.bot.tools.Cancelable
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask

class HuHoBotTask(private val task: MyScheduledTask): Cancelable {
    override fun cancel() {
        task.cancel()
    }
}

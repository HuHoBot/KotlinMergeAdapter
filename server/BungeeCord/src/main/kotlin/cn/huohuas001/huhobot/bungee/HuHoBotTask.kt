package cn.huohuas001.huhobot.bungee

import cn.huohuas001.bot.tools.Cancelable
import net.md_5.bungee.api.scheduler.ScheduledTask

class HuHoBotTask(private val task: ScheduledTask) : Cancelable {
    override fun cancel() {
        task.cancel()
    }
}

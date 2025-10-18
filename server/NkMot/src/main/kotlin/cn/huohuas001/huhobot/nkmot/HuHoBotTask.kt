package cn.huohuas001.huhobot.nkmot

import cn.huohuas001.bot.tools.Cancelable
import cn.nukkit.scheduler.PluginTask
import cn.nukkit.scheduler.TaskHandler

class HuHoBotTask(private val owner: HuHoBotNkMot, private val task: Runnable): PluginTask<HuHoBotNkMot>(owner) {
    override fun onRun(currentTick: Int) {
        task.run()
    }
}

class HuHoBotTaskCancelable(val task: TaskHandler): Cancelable{
    override fun cancel() {
        task.cancel()
    }
}
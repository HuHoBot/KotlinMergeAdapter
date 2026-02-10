package cn.huohuas001.huhobot.allay

import cn.huohuas001.bot.tools.Cancelable
import org.allaymc.api.scheduler.Task

class HuHoBotTask(val task: Runnable): Task{
    override fun onRun(): Boolean {
        task.run();
        return true
    }
}

class HuHoBotTaskCancelable(): Cancelable {
    override fun cancel() {

    }
}
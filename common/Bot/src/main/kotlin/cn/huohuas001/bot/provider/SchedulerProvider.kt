package cn.huohuas001.bot.provider

import cn.huohuas001.bot.tools.Cancelable


interface SchedulerProvider {
    fun submit(task: Runnable): Cancelable

    fun submitLater(delay: Long, task: Runnable): Cancelable

    fun submitTimer(delay: Long, period: Long, task: Runnable): Cancelable
}
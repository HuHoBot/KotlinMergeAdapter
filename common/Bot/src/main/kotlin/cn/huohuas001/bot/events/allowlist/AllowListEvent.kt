package cn.huohuas001.bot.events.allowlist

import cn.huohuas001.bot.HuHoBot
import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.bot.provider.BotShared

abstract class AllowListEvent : BaseEvent() {
    protected abstract fun operateWhiteList(plugin: HuHoBot, id: String)
    protected abstract val operationName: String

    override fun run(): Boolean {
        val plugin = BotShared.getPlugin()
        val id: String = mBody.getString("xboxid")
        operateWhiteList(plugin, id)
        respone("${plugin.getName()}已接受${operationName}名为${id}的白名单请求", "success")
        return true
    }
}

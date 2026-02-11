package cn.huohuas001.huhobot.nkmot.events

import cn.huohuas001.bot.HuHoBot
import cn.huohuas001.bot.events.AbstractQueryOnline
import cn.huohuas001.huhobot.nkmot.HuHoBotNkMot

class QueryOnline(val plugin: HuHoBotNkMot) : AbstractQueryOnline() {

    override fun getPlugin(): HuHoBot = plugin

    override fun getOnlinePlayerNames(): List<String> {
        return plugin.server.onlinePlayers.values.map { it.name }
    }
}

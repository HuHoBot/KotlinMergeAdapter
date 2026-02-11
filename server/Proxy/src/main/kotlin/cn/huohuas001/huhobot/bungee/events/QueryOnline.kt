package cn.huohuas001.huhobot.bungee.events

import cn.huohuas001.bot.HuHoBot
import cn.huohuas001.bot.events.AbstractQueryOnline
import cn.huohuas001.huhobot.bungee.HuHoBotBungee

class QueryOnline(val plugin: HuHoBotBungee) : AbstractQueryOnline() {

    override fun getPlugin(): HuHoBot = plugin

    override fun getOnlinePlayerNames(): List<String> {
        return plugin.proxy.players.map { it.name }
    }
}

package cn.huohuas001.huhobot.velocity.events

import cn.huohuas001.bot.HuHoBot
import cn.huohuas001.bot.events.AbstractQueryOnline
import cn.huohuas001.huhobot.velocity.HuHoBotVelocity

class QueryOnline(val plugin: HuHoBotVelocity) : AbstractQueryOnline() {

    override fun getPlugin(): HuHoBot = plugin

    override fun getOnlinePlayerNames(): List<String> {
        return plugin.server.allPlayers.map { it.username }
    }
}

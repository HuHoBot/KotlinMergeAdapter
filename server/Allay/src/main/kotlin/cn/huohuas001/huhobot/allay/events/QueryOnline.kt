package cn.huohuas001.huhobot.allay.events

import cn.huohuas001.bot.HuHoBot
import cn.huohuas001.bot.events.AbstractQueryOnline
import cn.huohuas001.huhobot.allay.HuHoBotAllay
import org.allaymc.api.server.Server

class QueryOnline(val plugin: HuHoBotAllay) : AbstractQueryOnline() {

    override fun getPlugin(): HuHoBot = plugin

    override fun getOnlinePlayerNames(): List<String> {
        return Server.getInstance().playerManager.players.values.map { it.originName }
    }
}

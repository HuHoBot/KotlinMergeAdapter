package cn.huohuas001.huhobot.allay.events

import cn.huohuas001.bot.events.AbstractQueryAllowList
import org.allaymc.api.server.Server

class QueryAllowList : AbstractQueryAllowList() {
    override fun getWhiteList(): Set<String> {
        return Server.getInstance().playerManager.whitelistedPlayers
    }
}

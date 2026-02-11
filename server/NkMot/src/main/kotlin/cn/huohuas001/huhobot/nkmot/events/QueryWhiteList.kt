package cn.huohuas001.huhobot.nkmot.events

import cn.huohuas001.bot.events.AbstractQueryAllowList
import cn.huohuas001.huhobot.nkmot.HuHoBotNkMot

class QueryWhiteList(private val plugin: HuHoBotNkMot) : AbstractQueryAllowList() {
    override fun getWhiteList(): Set<String> {
        return plugin.server.whitelist.keys
    }
}

package cn.huohuas001.bot.events.allowlist

import cn.huohuas001.bot.HuHoBot

class DelAllowList: AllowListEvent() {
    override val operationName = "删除"
    override fun operateWhiteList(plugin: HuHoBot, id: String) = plugin.delWhiteList(id)
}

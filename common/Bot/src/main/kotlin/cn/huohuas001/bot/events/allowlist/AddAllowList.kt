package cn.huohuas001.bot.events.allowlist

import cn.huohuas001.bot.HuHoBot

class AddAllowList: AllowListEvent() {
    override val operationName = "添加"
    override fun operateWhiteList(plugin: HuHoBot, id: String) = plugin.addWhiteList(id)
}

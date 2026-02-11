package cn.huohuas001.huhobot.spigot.events

import cn.huohuas001.bot.HuHoBot
import cn.huohuas001.bot.events.AbstractQueryOnline
import cn.huohuas001.huhobot.spigot.HuHoBotSpigot
import me.clip.placeholderapi.PlaceholderAPI

class QueryOnline(val plugin: HuHoBotSpigot) : AbstractQueryOnline() {

    override fun getPlugin(): HuHoBot = plugin

    override fun getOnlinePlayerNames(): List<String> {
        return plugin.server.onlinePlayers.map { it.name }
    }

    override fun processText(text: String): String {
        if (!plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            return text
        }
        return PlaceholderAPI.setPlaceholders(null, text)
    }
}

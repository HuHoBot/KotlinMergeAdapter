package cn.huohuas001.huhobot.spigot.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.huhobot.spigot.HuHoBotSpigot
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player

class QueryOnline(val plugin: HuHoBotSpigot): BaseEvent() {
    fun setPlaceholder(oriText: String): String? {
        if (!plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            return oriText
        }
        return PlaceholderAPI.setPlaceholders(null, oriText)
    }

    override fun run(): Boolean {
        val outputOnlineList: Boolean = plugin.getMotd().outputOnlineList
        val text: String = plugin.getMotd().text

        val onlineNameString = StringBuilder()
        var onlineSize = -1
        if (outputOnlineList) {
            onlineNameString.append("\n在线玩家列表：\n")
            val onlineList: MutableCollection<out Player> = plugin.server.onlinePlayers
            for (pl in onlineList) {
                val playerName = pl.name
                onlineNameString.append(playerName).append("\n")
            }
            onlineSize = onlineList.size
        }

        onlineNameString.append(setPlaceholder(text.replace("{online}", onlineSize.toString())))

        ClientManager.postMotd(onlineNameString.toString(),mPackId)

        return true
    }
}
package cn.huohuas001.huhobot.mod.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.huhobot.mod.HuHoBotMod
import java.util.function.Consumer

class QueryOnline(val plugin: HuHoBotMod) : BaseEvent() {
    override fun run(): Boolean {
        val onlinePlayers = plugin.serverInstance.playerList.players
        val outputOnlineList = plugin.getMotd().outputOnlineList
        val text = plugin.getMotd().text

        val onlineNameString = StringBuilder()
        val onlineSize = onlinePlayers.size
        if (outputOnlineList && !onlinePlayers.isEmpty()) {
            onlinePlayers.forEach {
                player -> onlineNameString.append(player!!.name.string).append("\n")
            }
        } else if (outputOnlineList) {
            onlineNameString.append("\n当前没有在线玩家\n")
        }

        onlineNameString.append(text.replace("{online}", onlineSize.toString()))

        ClientManager.postMotd(onlineNameString.toString(), mPackId)
        return true
    }
}
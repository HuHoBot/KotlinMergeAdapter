package cn.huohuas001.huhobot.velocity.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.huhobot.velocity.HuHoBotVelocity

class QueryOnline(val plugin: HuHoBotVelocity) : BaseEvent() {

    override fun run(): Boolean {
        val outputOnlineList: Boolean = plugin.getMotd().outputOnlineList
        val text: String = plugin.getMotd().text

        val onlineNameString = StringBuilder()
        var onlineSize = -1

        if (outputOnlineList) {
            onlineNameString.append("\n在线玩家列表：\n")
            val onlineList = plugin.server.allPlayers
            for (player in onlineList) {
                val playerName = player.username
                onlineNameString.append(playerName).append("\n")
            }
            onlineSize = onlineList.size
        }

        onlineNameString.append(text.replace("{online}", onlineSize.toString()))

        ClientManager.postMotd(onlineNameString.toString(), mPackId)

        return true
    }
}

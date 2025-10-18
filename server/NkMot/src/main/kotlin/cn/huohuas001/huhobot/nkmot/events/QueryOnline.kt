package cn.huohuas001.huhobot.nkmot.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.huhobot.nkmot.HuHoBotNkMot
import cn.nukkit.Player
import java.util.*
import java.util.function.Consumer

class QueryOnline(val plugin: HuHoBotNkMot) : BaseEvent() {
    override fun run(): Boolean {
        val onlinePlayers: MutableMap<UUID?, Player?> = plugin.server.onlinePlayers
        val outputOnlineList = plugin.getMotd().outputOnlineList
        val text = plugin.getMotd().text

        val onlineNameString = StringBuilder()

        val onlineSize = onlinePlayers.size
        if (outputOnlineList && !onlinePlayers.isEmpty()) {
            onlinePlayers.values.forEach(Consumer { player: Player? ->
                onlineNameString.append(player!!.name).append("\n")
            })
        } else if (outputOnlineList) {
            onlineNameString.append("\n当前没有在线玩家\n")
        }

        onlineNameString.append(text.replace("{online}", onlineSize.toString()))

        ClientManager.postMotd(onlineNameString.toString(), mPackId)
        return true
    }
}
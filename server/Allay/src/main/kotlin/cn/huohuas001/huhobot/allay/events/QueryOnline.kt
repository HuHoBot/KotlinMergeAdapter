package cn.huohuas001.huhobot.allay.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.huhobot.allay.HuHoBotAllay
import org.allaymc.api.entity.interfaces.EntityPlayer
import org.allaymc.api.server.Server
import java.util.function.Consumer

class QueryOnline(val plugin: HuHoBotAllay) : BaseEvent() {
    override fun run(): Boolean {
        val onlinePlayers = Server.getInstance().playerManager.players
        val outputOnlineList = plugin.getMotd().outputOnlineList
        val text = plugin.getMotd().text

        val onlineNameString = StringBuilder()

        val onlineSize = onlinePlayers.size
        if (outputOnlineList && !onlinePlayers.isEmpty()) {
            onlinePlayers.values.forEach(Consumer { player: EntityPlayer? ->
                onlineNameString.append(player!!.nameTag).append("\n")
            })
        } else if (outputOnlineList) {
            onlineNameString.append("\n当前没有在线玩家\n")
        }

        onlineNameString.append(text.replace("{online}", onlineSize.toString()))

        ClientManager.postMotd(onlineNameString.toString(), mPackId)
        return true
    }
}
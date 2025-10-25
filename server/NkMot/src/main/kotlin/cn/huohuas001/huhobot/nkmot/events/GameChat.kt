package cn.huohuas001.huhobot.nkmot.events

import cn.huohuas001.bot.ClientManager
import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerChatEvent

class GameChat: Listener {
    @EventHandler
    fun onChat(event: PlayerChatEvent) {
        val message = event.getMessage()
        val playerName = event.getPlayer().name
        ClientManager.postChat(playerName,message)
    }
}
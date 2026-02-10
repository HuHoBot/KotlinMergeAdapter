package cn.huohuas001.huhobot.spigot.events

import cn.huohuas001.bot.ClientManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class GameChat : Listener {
    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val message = event.message
        val playerName = event.player.name

        ClientManager.postChat(playerName, message)
    }
}
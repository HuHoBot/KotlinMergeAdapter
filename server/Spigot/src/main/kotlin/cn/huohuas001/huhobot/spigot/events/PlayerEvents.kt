package cn.huohuas001.huhobot.spigot.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.huhobot.spigot.HuHoBotSpigot
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerEvents(private val plugin: HuHoBotSpigot) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val playerName = event.player.name
        val postEventEnable = plugin.configManager.getPostEventEnable("onJoin")

        if (postEventEnable) {
            val formatString = plugin.configManager.getPostEventFormat("onJoin")
            val msg = formatString.replace("{playerName}", playerName)
            ClientManager.postCustomChat(msg,"进服")
        }
    }

    @EventHandler
    fun onLeft(event: PlayerQuitEvent) {
        val playerName = event.player.name
        val postEventEnable = plugin.configManager.getPostEventEnable("onLeft")

        if (postEventEnable) {
            val formatString = plugin.configManager.getPostEventFormat("onLeft")
            val msg = formatString.replace("{playerName}", playerName)
            ClientManager.postCustomChat(msg,"退服")
        }
    }
}

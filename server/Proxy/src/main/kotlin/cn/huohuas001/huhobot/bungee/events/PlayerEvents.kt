package cn.huohuas001.huhobot.bungee.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.huhobot.bungee.HuHoBotBungee
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class PlayerEvents(private val plugin: HuHoBotBungee) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PostLoginEvent) {
        val playerName = event.player.name
        val postEventEnable = plugin.configManager.getPostEventEnable("onJoin")

        if (postEventEnable) {
            val formatString = plugin.configManager.getPostEventFormat("onJoin")
            val msg = formatString.replace("{playerName}", playerName)
            ClientManager.postCustomChat(msg,"进服")
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerDisconnectEvent) {
        val playerName = event.player.name
        val postEventEnable = plugin.configManager.getPostEventEnable("onLeft")

        if (postEventEnable) {
            val formatString = plugin.configManager.getPostEventFormat("onLeft")
            val msg = formatString.replace("{playerName}", playerName)
            ClientManager.postCustomChat(msg,"退服")
        }
    }
}

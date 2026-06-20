package cn.huohuas001.huhobot.velocity.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.huhobot.velocity.HuHoBotVelocity
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent

class PlayerEvents(val plugin: HuHoBotVelocity) {

    @Subscribe
    fun onPlayerJoin(event: LoginEvent) {
        val playerName = event.player.username
        val postEventEnable = plugin.configManager.getPostEventEnable("onJoin")

        if (postEventEnable) {
            val formatString = plugin.configManager.getPostEventFormat("onJoin")
            val msg = formatString.replace("{playerName}", playerName)
            ClientManager.postCustomChat(msg,"进服")
        }
    }

    @Subscribe
    fun onPlayerQuit(event: DisconnectEvent) {
        val playerName = event.player.username
        val postEventEnable = plugin.configManager.getPostEventEnable("onLeft")

        if (postEventEnable) {
            val formatString = plugin.configManager.getPostEventFormat("onLeft")
            val msg = formatString.replace("{playerName}", playerName)
            ClientManager.postCustomChat(msg,"退服")
        }
    }
}

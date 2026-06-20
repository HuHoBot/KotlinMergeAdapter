package cn.huohuas001.huhobot.nkmot.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.huhobot.nkmot.HuHoBotNkMot
import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerChatEvent
import cn.nukkit.event.player.PlayerJoinEvent
import cn.nukkit.event.player.PlayerQuitEvent
import cn.nukkit.event.server.ServerCommandEvent

class PlayerEvents: Listener {
    private var plugin: HuHoBotNkMot

    constructor(plugin: HuHoBotNkMot) {
        this.plugin = plugin
    }

    @EventHandler
    fun onChat(event: PlayerChatEvent) {
        val message = event.getMessage()
        val playerName = event.getPlayer().name

        //读取配置文件
        val chatFormat = plugin.getChatFormat()
        if(chatFormat.postChat && message.startsWith(chatFormat.postPrefix)){
            ClientManager.postChat(playerName,message)
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val playerName = event.player.name
        val postEvent = plugin.config.postEvent

        if (postEvent.onJoin.enable) {
            val formatString = postEvent.onJoin.formatString
            if (formatString.isNotEmpty()) {
                val formatString = formatString.replace("{playerName}", playerName)
                ClientManager.postCustomChat(formatString, "进服")
            }
        }
    }

    @EventHandler
    fun onLeft(event: PlayerQuitEvent) {
        val playerName = event.player.name
        val postEvent = plugin.config.postEvent

        if (postEvent.onLeft.enable) {
            val formatString = postEvent.onLeft.formatString
            if (formatString.isNotEmpty()) {
                val formatString = formatString.replace("{playerName}", playerName)
                ClientManager.postCustomChat(formatString, "退服")
            }
        }
    }
}
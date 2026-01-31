package cn.huohuas001.huhobot.bungee.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.huhobot.bungee.HuHoBotBungee
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class GameChat(val plugin: HuHoBotBungee) : Listener {

    @EventHandler
    fun onPlayerChat(event: ChatEvent) {
        // Ignore commands
        if (event.isCommand || event.isProxyCommand) {
            return
        }

        val chatFormat = plugin.getChatFormat()

        if (!chatFormat.postChat) {
            return
        }

        val sender = event.sender
        if (sender !is net.md_5.bungee.api.connection.ProxiedPlayer) {
            return
        }

        val message = event.message

        // Check if message starts with the post prefix
        if (chatFormat.postPrefix.isNotEmpty() && !message.startsWith(chatFormat.postPrefix)) {
            return
        }

        // Remove prefix from message if present
        val actualMessage = if (chatFormat.postPrefix.isNotEmpty()) {
            message.removePrefix(chatFormat.postPrefix)
        } else {
            message
        }

        // Send to bot
        ClientManager.postChat(sender.name, actualMessage)
    }
}

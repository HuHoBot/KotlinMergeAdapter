package cn.huohuas001.huhobot.velocity.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.huhobot.velocity.HuHoBotVelocity
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent

class GameChat(val plugin: HuHoBotVelocity) {

    @Subscribe
    fun onPlayerChat(event: PlayerChatEvent) {
        val chatFormat = plugin.getChatFormat()

        if (!chatFormat.postChat) {
            return
        }

        val player = event.player
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

        // Format the message
        val formattedMessage = chatFormat.fromGame
            .replace("{name}", player.username)
            .replace("{msg}", actualMessage)

        // Send to bot
        ClientManager.postChat(player.username,actualMessage)
    }
}

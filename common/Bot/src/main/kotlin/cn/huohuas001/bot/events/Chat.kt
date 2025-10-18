package cn.huohuas001.bot.events

import cn.huohuas001.bot.provider.BotShared

class Chat: BaseEvent() {
    override fun run(): Boolean {
        val plugin = BotShared.getPlugin()

        val nick = mBody.getString("nick")
        val msg = mBody.getString("msg")
        val isPostChat: Boolean = plugin.getChatFormat().postChat
        val message = plugin.getChatFormat().fromGroup.replace("{nick}", nick).replace("{msg}", msg)
        if (isPostChat) {
            plugin.broadcastMessage(message)
        }
        return true
    }
}
package cn.huohuas001.bot.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.provider.BotShared

class SendConfig: BaseEvent() {
    override fun run(): Boolean {
        val plugin = BotShared.getPlugin()
        val hashKey: String = mBody.getString("hashKey")
        plugin.setHashKey(hashKey)
        plugin.log_info("配置文件已接受.")
        plugin.log_info("自动断开连接以刷新配置文件...")
        ClientManager.shutdownClient()
        return true
    }
}
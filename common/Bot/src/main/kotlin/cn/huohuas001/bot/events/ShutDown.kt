package cn.huohuas001.bot.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.provider.BotShared

class ShutDown: BaseEvent() {
    override fun run(): Boolean {
        val plugin = BotShared.getPlugin()
        val msg = mBody.getString("msg")
        plugin.log_error("服务端命令断开连接 原因:$msg")
        plugin.log_error("此错误具有不可容错性!请检查插件配置文件!")
        plugin.log_warning("正在断开连接...")
        ClientManager.setShouldReconnect(false)
        ClientManager.shutdownClient()
        return true
    }
}
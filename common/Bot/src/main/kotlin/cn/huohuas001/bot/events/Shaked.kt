package cn.huohuas001.bot.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.provider.BotShared

class Shaked: BaseEvent() {
    private fun shakedProcess() {
        ClientManager.setShouldReconnect(true)
        ClientManager.cancelCurrentTask()
        ClientManager.setAutoDisConnectTask()
        val plugin = BotShared.getPlugin()
        plugin.submitTimer(0, 10*20){ ClientManager.postHeart()}
    }

    override fun run(): Boolean {
        val code: Int = mBody.getInteger("code")
        val msg: String = mBody.getString("msg")
        val plugin = BotShared.getPlugin()
        when (code) {
            1 -> {
                plugin.log_info("与服务端握手成功.")
                shakedProcess()
            }

            2 -> {
                plugin.log_info("握手完成!,附加消息:$msg")
                shakedProcess()
            }

            3 -> {
                plugin.log_error("握手失败，客户端密钥错误.")
                ClientManager.setShouldReconnect(false)
            }

            6 -> {
                plugin.log_info("与服务端握手成功，服务端等待绑定...")
                plugin.sendBindMessage()
                shakedProcess()
            }

            else -> plugin.log_error("握手失败，原因$msg")
        }
        return true
    }
}
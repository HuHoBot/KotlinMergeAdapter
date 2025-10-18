package cn.huohuas001.bot.events

import cn.huohuas001.bot.provider.BotShared
import com.alibaba.fastjson2.JSONObject

class BindRequest: BaseEvent() {
    private val bindMap = HashMap<String, String>()

    override fun run(): Boolean {
        val bindCode: String = mBody.getString("bindCode")
        val plugin = BotShared.getPlugin()
        plugin.log_info("收到一个新的绑定请求，如确认绑定，请输入\"/huhobot bind $bindCode\"来进行确认")
        bindMap[bindCode] = mPackId
        return true
    }

    fun confirmBind(bindCode: String): Boolean {
        if (bindMap.containsKey(bindCode)) {
            sendMessage("bindConfirm", JSONObject())
            bindMap.remove(bindCode)
            return true
        }
        return false
    }
}
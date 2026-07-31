package cn.huohuas001.bot.events.member

import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.bot.provider.BotShared
import com.alibaba.fastjson2.JSONObject

class MemberAdd : BaseEvent() {
    private val isAdmin = true

    private fun callPluginEvent(command: String, data: JSONObject, packId: String, runByAdmin: Boolean): Boolean {
        val plugin = BotShared.getPlugin()
        return plugin.callPluginEvent(command, data, packId, runByAdmin)
    }

    private fun callEvent(data: JSONObject, packId: String) {
        val keyWord = "#MemberAdd"
        callPluginEvent(keyWord, data, packId, isAdmin)
    }

    override fun run(): Boolean {
        val plugin = BotShared.getPlugin()
        return when (mBody.getString("action")) {
            "add" -> {
                val data = mBody
                val packId = mPackId
                plugin.submit { callEvent(data, packId) }
                true
            }

            "remove" -> MemberRemove().eventCall(mPackId, mBody)
            else -> false
        }
    }
}

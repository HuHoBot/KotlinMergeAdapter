package cn.huohuas001.huhobot.velocity.events

import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.huhobot.velocity.HuHoBotVelocity
import com.alibaba.fastjson2.JSONObject

class QueryAllowList(val plugin: HuHoBotVelocity) : BaseEvent() {

    override fun run(): Boolean {
        plugin.submit { callEvent() }
        return true
    }

    fun callEvent(): Boolean {
        val rBody = JSONObject()
        rBody["list"] = "暂不支持此功能"
        sendMessage("queryWl", rBody)
        return true
    }
}

package cn.huohuas001.bot.events.custom_run

import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.bot.provider.BotShared
import cn.huohuas001.bot.provider.CustomCommandDetail
import com.alibaba.fastjson2.JSONObject


open class CustomRun: BaseEvent() {

    open val isAdmin: Boolean = false

    open fun callPluginEvent(command: String, data:JSONObject, packId:String , runByAdmin: Boolean): Boolean{
        val plugin = BotShared.getPlugin()
        return plugin.callPluginEvent(command, data, packId , runByAdmin)
    }

    private fun callEvent() {
        val keyWord: String = mBody.getString("key")
        val param: MutableList<String?> = mBody.getList("runParams", String::class.java)


        val result: CustomCommandDetail? = BotShared.customCommandMap[keyWord]
        var isEventCancel: Boolean
        if (result == null) {
            isEventCancel = callPluginEvent(keyWord, mBody, mPackId, isAdmin)
        } else {
            var command: String = result.command
            for (i in param.indices) {
                val replaceNum = i + 1
                command = command.replace("&$replaceNum", param[i]!!)
            }
            if (!isAdmin && result.permission > 0) {
                respone("权限不足，若您是管理员，请使用/管理员执行", "error")
                return
            }
            sendCommand(command)
            return
        }

        //执行后判定是否有命令接收
        if (!isEventCancel && !keyWord.startsWith("#")) {
            respone("未找到关键词" + keyWord + "对应的自定义事件", "error")
        }
    }

    override fun run(): Boolean {
        val plugin = BotShared.getPlugin()
        plugin.submit { callEvent() }

        return true
    }
}

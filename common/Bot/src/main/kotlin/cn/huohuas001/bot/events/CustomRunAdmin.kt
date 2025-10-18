package cn.huohuas001.bot.events

import cn.huohuas001.bot.provider.BotShared
import cn.huohuas001.bot.provider.CustomCommandDetail
import com.alibaba.fastjson2.JSONObject


open class CustomRunAdmin: BaseEvent() {

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
            isEventCancel = callPluginEvent(keyWord, mBody, mPackId, true)
        } else {
            var command: String = result.command
            for (i in param.indices) {
                val replaceNum = i + 1
                command = command.replace("&$replaceNum", param[i]!!)
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
package cn.huohuas001.bot.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.provider.BotShared
import cn.huohuas001.bot.WsClient
import com.alibaba.fastjson2.JSONObject

open class BaseEvent {
    lateinit var mPackId: String
    lateinit var mBody: JSONObject

    fun eventCall(packId: String,body: JSONObject): Boolean{
        mPackId = packId
        mBody = body
        return run()
    }

    fun respone(msg: String, type: String) {
        ClientManager.postRespone(msg, type, mPackId)
    }

    fun sendMessage(type: String, body: JSONObject) {
        val client: WsClient = ClientManager.getClient()
        client.sendMessage(type, body, mPackId)
    }

    fun sendCommand(command: String){
        val plugin = BotShared.getPlugin()
        plugin.submit{
            val ret = plugin.sendCommand(command)
            ret.thenAccept {
                val retText = it.getRawString()
                if (retText.isNotEmpty()) {
                    ClientManager.postRespone("已执行.\n$retText", "success", mPackId);
                }else{
                    ClientManager.postRespone("已执行.\n无返回结果", "success", mPackId);
                }
            }

        }
    }

    open fun run(): Boolean{
        return false
    }
}
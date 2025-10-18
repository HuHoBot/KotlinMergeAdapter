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
        val client: WsClient = ClientManager.getClient()
        client.respone(msg, type, mPackId)
    }

    fun sendMessage(type: String, body: JSONObject) {
        val client: WsClient = ClientManager.getClient()
        client.sendMessage(type, body, mPackId)
    }

    fun sendCommand(command: String){
        val plugin = BotShared.getPlugin()
        plugin.submit{
            val ret:String = plugin.sendCommand(command)
            val client: WsClient = ClientManager.getClient()
            client.respone("已执行.\n$ret", "success", mPackId);
        }
    }

    open fun run(): Boolean{
        return false
    }
}
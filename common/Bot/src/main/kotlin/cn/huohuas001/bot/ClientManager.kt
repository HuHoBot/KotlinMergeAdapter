package cn.huohuas001.bot

import cn.huohuas001.bot.provider.BotShared
import cn.huohuas001.bot.tools.Cancelable
import cn.huohuas001.config.WS_SERVER_URL
import com.alibaba.fastjson2.JSONObject
import java.net.URI
import java.net.URISyntaxException

object ClientManager {
    private var client: WsClient? = null
    private val websocketUrl = WS_SERVER_URL

    private const val RECONNECT_DELAY: Long = 5 // 重连延迟时间，单位为秒
    private const val MAX_RECONNECT_ATTEMPTS = 5 // 最大重连尝试次数
    private var ReconnectAttempts = 0
    private var shouldReconnect = true

    private var currentTask: Cancelable? = null
    private var autoDisConnectTask: Cancelable? = null

    /**
     * 设置是否自动重连
     */
    fun setShouldReconnect(should: Boolean){
        shouldReconnect = should
    }

    /**
     * 取消当前所有任务
     */
    fun cancelCurrentTask() {
        if (currentTask != null) {
            currentTask!!.cancel()
            currentTask = null
            ReconnectAttempts = 0
        }
    }

    fun shutdownClient(): Boolean {
        if (isOpen()) {
            client!!.close(1000)
            return true
        }
        return false
    }

    fun getClient(): WsClient{
        return client!!
    }

    fun autoDisConnectClient() {
        val plugin = BotShared.getPlugin()
        plugin.log_info("连接超时，已自动重连")
        shutdownClient()
    }

    fun setAutoDisConnectTask() {
        val plugin = BotShared.getPlugin()
        if (autoDisConnectTask == null) {
            autoDisConnectTask = plugin.submitLater(6 * 60 * 60 * 20L) { this.autoDisConnectClient() }
        } else {
            autoDisConnectTask!!.cancel()
            autoDisConnectTask = null
            setAutoDisConnectTask()
        }
    }

    fun connectServer(): Boolean {
        val plugin = BotShared.getPlugin()
        plugin.log_info(" 正在连接服务端...")
        try {
            val uri = URI(websocketUrl)
            if(client == null || !client!!.isOpen){
                client = WsClient(BotShared.getPlugin(),uri)
                setShouldReconnect(true) // 设置是否重连
                client!!.connect()
            }
            return true
        } catch (e: URISyntaxException) {
            plugin.log_error(e.stackTrace.toString())
        }
        return false
    }

    fun isOpen(): Boolean {
        return client != null && client!!.isOpen
    }

    fun postHeart() {
        val plugin = BotShared.getPlugin()
        client?.sendMessage("heart", JSONObject())
    }

    fun postChat(playerName: String,message: String){
        val plugin = BotShared.getPlugin()
        val chatFormat = plugin.getChatFormat()
        val format = chatFormat.fromGame
        val prefix = chatFormat.postPrefix
        val isPostChat = chatFormat.postChat

        if (message.startsWith(prefix) && isPostChat) {
            val formatted = format.replace("{name}", playerName)
                .replace("{msg}", message.substring(prefix.length))
            val body = JSONObject()
            body["serverId"] = plugin.getServerId()
            body["msg"] = formatted
            client?.sendMessage("chat", body)
        }
    }

    fun postMotd(msg: String,packId: String){
        val plugin = BotShared.getPlugin()
        val serverIP: String = plugin.getMotd().serverIP
        val serverPort: Int = plugin.getMotd().serverPort
        val api: String = plugin.getMotd().api
        val postImg = plugin.getMotd().postImg
        // 构造JSON对象
        val list = JSONObject()
        list["msg"] = msg
        list["url"] = "$serverIP:$serverPort"
        list["imgUrl"] = api.replace("{server_ip}", serverIP).replace("{server_port}", serverPort.toString())
        list["post_img"] = postImg
        list["serverType"] = "java"
        val rBody = JSONObject()
        rBody["list"] = list

        //返回消息
        client?.sendMessage("queryOnline", rBody, packId)
    }

    fun postList(list:String,packId: String){
        val rBody = JSONObject();
        rBody["list"] = list;
        client?.sendMessage("queryWl", rBody, packId);
    }

    fun postRespone(msg: String,type: String,packId: String){
        client?.respone(msg,type,packId)
    }

    fun postRespone(msg: JSONObject,type: String,packId: String){
        client?.respone(msg.toJSONString(),type)
    }

    /**
     * 客户端自动重连循环
     */
    private fun autoReconnect() {
        synchronized(this) {
            val plugin = BotShared.getPlugin()
            ReconnectAttempts++
            if (ReconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                plugin.log_warning(" 重连尝试已达到最大次数，将不再尝试重新连接。")
                cancelCurrentTask()
                return
            }
            if (!shouldReconnect) {
                cancelCurrentTask()
                return
            }
            plugin.log_info(" 正在尝试重新连接,这是第($ReconnectAttempts/$MAX_RECONNECT_ATTEMPTS)次连接")
            this.connectServer()
        }
    }

    fun clientReconnect() {
        if (shouldReconnect && currentTask == null) {
            val plugin = BotShared.getPlugin()
            currentTask = plugin.submitTimer(this.RECONNECT_DELAY * 20L, 0) { this.autoReconnect() }
        }
    }
}
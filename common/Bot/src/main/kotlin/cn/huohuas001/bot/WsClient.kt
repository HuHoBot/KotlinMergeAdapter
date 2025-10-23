package cn.huohuas001.bot

//import cn.huohuas001.huHoBot.Tools.ConfigManager
//import cn.huohuas001.huHoBot.Tools.PackId
import cn.huohuas001.bot.provider.BotShared
import cn.huohuas001.bot.tools.getPackID
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.CompletableFuture

class WsClient(private val plugin: HuHoBot, serverUri: URI) : WebSocketClient(serverUri){
    private val responseFutureList = mutableMapOf<String, CompletableFuture<JSONObject>>()

    override fun onOpen(handshakedata: ServerHandshake) {
        plugin.log_info("服务端连接成功.")
        ClientManager.cancelCurrentTask()
        shakeHand()
    }

    override fun onMessage(message: String) {
        val jsonData = JSON.parseObject(message)
        val header = jsonData.getJSONObject("header")
        val packId = header.getString("id")
        val plugin = BotShared.getPlugin()

        if (responseFutureList.containsKey(packId)) {
            val responseFuture = responseFutureList[packId]
            responseFuture?.takeIf { !it.isDone }?.complete(jsonData)
            responseFutureList.remove(packId)
        } else {
            plugin.onMessage(jsonData)
        }
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        plugin.log_error("连接已断开,错误码:$code 错误信息:$reason")
        if (code != 1000) { // 1000是正常关闭
            ClientManager.clientReconnect()
        }
    }

    override fun onError(ex: Exception) {
        plugin.log_error(ex.printStackTrace().toString())
        ClientManager.clientReconnect()
    }

    /**
     * 向服务端发送一条消息
     *
     * @param type 消息类型
     * @param body 消息数据
     */
    fun sendMessage(type: String, body: JSONObject) {
        val newPackId = getPackID()
        sendMessage(type, body, newPackId)
    }

    /**
     * 向服务端发送一条消息
     *
     * @param type   消息类型
     * @param body   消息数据
     * @param packId 消息Id
     */
    fun sendMessage(type: String, body: JSONObject, packId: String) {
        val data = JSONObject()
        val header = JSONObject()
        header["type"] = type
        header["id"] = packId
        data["header"] = header
        data["body"] = body

        if (this.isOpen) {
            this.send(data.toJSONString())
        }
    }

    /**
     * 向服务端发送一条消息并获取返回值
     *
     * @param type 消息类型
     * @param body 消息数据
     * @return 消息回报体
     */
    fun sendRequestAndAwaitResponse(type: String, body: JSONObject): CompletableFuture<JSONObject> {
        val newPackId = getPackID()
        return sendRequestAndAwaitResponse(type, body, newPackId)
    }

    /**
     * 向服务端发送一条消息并获取返回值
     *
     * @param type   消息类型
     * @param body   消息数据
     * @param packId 消息Id
     * @return 消息回报体
     */
    fun sendRequestAndAwaitResponse(type: String, body: JSONObject, packId: String): CompletableFuture<JSONObject> {
        if (this.isOpen) {
            //打包数据并发送
            val data = JSONObject()
            val header = JSONObject()
            header["type"] = type
            header["id"] = packId
            data["header"] = header
            data["body"] = body
            this.send(data.toJSONString())

            //存储回报
            val responseFuture = CompletableFuture<JSONObject>()
            responseFutureList[packId] = responseFuture

            return responseFuture
        } else {
            throw IllegalStateException("WebSocket connection is not open.")
        }
    }

    /**
     * 向服务端发送一条回报
     *
     * @param msg  回报消息
     * @param type 回报类型：success|error
     */
    fun respone(msg: String, type: String) {
        val newPackId = getPackID()
        respone(msg, type, newPackId)
    }

    /**
     * 向服务端发送一条回报
     *
     * @param msg    回报消息
     * @param type   回报类型：success|error
     * @param packId 回报Id
     */
    fun respone(msg: String, type: String, packId: String) {
        val body = JSONObject()
        body["msg"] = msg
        sendMessage(type, body, packId)
    }

    /**
     * 向服务端握手
     */
    private fun shakeHand() {
        val body = JSONObject()
        body["serverId"] = plugin.getServerId()
        body["hashKey"] = plugin.getHashKey()
        body["name"] = plugin.getName()
        body["version"] = plugin.getPluginVersion()
        body["platform"] = plugin.getPlatform()
        sendMessage("shakeHand", body)
    }
}

package cn.huohuas001.bot

import cn.huohuas001.bot.provider.BotShared
import cn.huohuas001.bot.tools.getPackID
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class WsClient(private val plugin: HuHoBot, val serverUri: URI) : CoroutineScope {
    private val responseFutureList = ConcurrentHashMap<String, CompletableFuture<JSONObject>>()
    private val client: HttpClient = HttpClient {
        install(WebSockets)
    }
    private var webSocketSession: WebSocketSession? = null
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    /**
     * 建立WebSocket连接
     */
    fun connect() {
        launch {
            try {
                val url = serverUri.toString()
                client.webSocket(url) {
                    webSocketSession = this
                    plugin.log_info("服务端连接成功.")
                    ClientManager.cancelCurrentTask()

                    // 发送握手消息
                    shakeHand()

                    // 处理接收消息
                    try {
                        incoming.consumeEach { frame ->
                            when (frame) {
                                is Frame.Text -> {
                                    val message = frame.readText()
                                    handleMessage(message)
                                }
                                else -> {
                                    // 忽略其他类型的消息帧
                                }
                            }
                        }
                    } catch (e: Exception) {
                        plugin.log_error("消息处理异常: ${e.message}")
                    } finally {
                        // 连接断开时的处理
                        handleConnectionClosed()
                    }
                }
            } catch (e: Exception) {
                plugin.log_error("连接失败: ${e.message}")
                ClientManager.clientReconnect()
            }
        }
    }

    /**
     * 处理接收到的消息
     */
    private fun handleMessage(message: String) {
        try {
            val jsonData = JSON.parseObject(message)
            val header = jsonData.getJSONObject("header")
            val packId = header.getString("id")

            if (responseFutureList.containsKey(packId)) {
                val responseFuture = responseFutureList[packId]
                responseFuture?.takeIf { !it.isDone }?.complete(jsonData)
                responseFutureList.remove(packId)
            } else {
                plugin.onMessage(jsonData)
            }
        } catch (e: Exception) {
            plugin.log_error("消息解析失败: ${e.message}")
        }
    }

    /**
     * 处理连接断开
     */
    private fun handleConnectionClosed() {
        plugin.log_error("连接已断开")
        ClientManager.clientReconnect()
    }

    /**
     * 检查连接是否打开
     */
    val isOpen: Boolean
        get() = webSocketSession?.isActive == true

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
        launch {
            try {
                val data = JSONObject()
                val header = JSONObject()
                header["type"] = type
                header["id"] = packId
                data["header"] = header
                data["body"] = body

                if (isOpen) {
                    webSocketSession?.send(Frame.Text(data.toJSONString()))
                }
            } catch (e: Exception) {
                plugin.log_error("发送消息失败: ${e.message}")
            }
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
        if (isOpen) {
            //打包数据并发送
            val data = JSONObject()
            val header = JSONObject()
            header["type"] = type
            header["id"] = packId
            data["header"] = header
            data["body"] = body

            // 存储回报
            val responseFuture = CompletableFuture<JSONObject>()
            responseFutureList[packId] = responseFuture

            // 发送消息
            launch {
                try {
                    webSocketSession?.send(Frame.Text(data.toJSONString()))
                } catch (e: Exception) {
                    responseFuture.completeExceptionally(e)
                    responseFutureList.remove(packId)
                }
            }

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
    fun respone(msg: String, type: String, callbackConvert: Int) {
        val newPackId = getPackID()
        respone(msg, type, callbackConvert, newPackId)
    }

    /**
     * 向服务端发送一条回报
     *
     * @param msg    回报消息
     * @param type   回报类型：success|error
     * @param packId 回报Id
     */
    fun respone(msg: String, type: String, callbackConvert: Int, packId: String) {
        val body = JSONObject()
        body["msg"] = msg
        body["callbackConvert"] = callbackConvert
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

    /**
     * 关闭连接
     */
    fun close(code: Short = 1000, reason: String = "") {
        launch {
            webSocketSession?.close(CloseReason(code, reason))
            client.close()
            job.cancel()
        }
    }
}

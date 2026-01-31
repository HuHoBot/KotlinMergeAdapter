package cn.huohuas001.huhobot.bungee.redis

import cn.huohuas001.huhobot.bungee.HuHoBotBungee
import kotlinx.coroutines.*
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisPubSub
import java.time.Duration
import java.util.logging.Level

class RedisManager(private val plugin: HuHoBotBungee) {

    private var jedisPool: JedisPool? = null
    private val commandChannel: String get() = plugin.configManager.getRedisChannel()
    val callbackChannel: String get() = "${commandChannel}_callback"

    private var subscriberJob: Job? = null
    private var pubSub: JedisPubSub? = null

    // 回调管理器
    var commandCallback: CommandCallback? = null
        private set

    fun connect(host: String, port: Int, password: String?) {
        try {
            val poolConfig = JedisPoolConfig().apply {
                maxTotal = 10
                maxIdle = 5
                minIdle = 1
                testOnBorrow = true
                testOnReturn = true
                testWhileIdle = true
                setMaxWait(Duration.ofSeconds(3))
            }

            jedisPool = if (password.isNullOrEmpty()) {
                JedisPool(poolConfig, host, port)
            } else {
                JedisPool(poolConfig, host, port, 2000, password)
            }

            // 测试连接
            jedisPool?.resource?.use { jedis ->
                jedis.ping()
            }

            plugin.logger.info("Redis 连接成功: $host:$port")

            // 初始化回调管理器
            commandCallback = CommandCallback(plugin)

            // 开始监听回调频道
            startCallbackListener()

        } catch (e: Exception) {
            plugin.logger.severe("Redis 连接失败: ${e.message}")
            jedisPool = null
        }
    }

    /**
     * 开始监听回调频道
     */
    private fun startCallbackListener() {
        subscriberJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val host = plugin.configManager.getRedisHost()
                val port = plugin.configManager.getRedisPort()

                Jedis(host, port).use { jedis ->

                    // 如果有密码，需要验证
                    val password = plugin.configManager.getRedisPassword()
                    if (!password.isNullOrEmpty()) {
                        jedis.auth(password)
                    }

                    pubSub = object : JedisPubSub() {
                        override fun onMessage(channel: String, message: String) {
                            // plugin.logger.info("收到回调消息: $message") // debug
                            commandCallback?.handleCallback(message)
                        }

                        override fun onSubscribe(channel: String, subscribedChannels: Int) {
                            plugin.logger.info("已订阅回调频道: $channel")
                        }

                        override fun onUnsubscribe(channel: String, subscribedChannels: Int) {
                            plugin.logger.info("已取消订阅频道: $channel")
                        }
                    }

                    jedis.subscribe(pubSub, callbackChannel)
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    plugin.logger.severe("Redis 回调监听器异常: ${e.message}")
                }
            }
        }
    }

    /**
     * 停止回调监听器
     */
    private fun stopCallbackListener() {
        try {
            pubSub?.unsubscribe()
        } catch (e: Exception) {
            // 忽略取消订阅时的异常
        }

        try {
            subscriberJob?.cancel()
        } catch (t: Throwable) {
            // 忽略取消任务时的异常，特别是 NoClassDefFoundError
            // 这种情况通常发生在插件卸载时，类加载器可能已经卸载了部分类
        }

        subscriberJob = null
        pubSub = null
    }

    fun disconnect() {
        stopCallbackListener()
        jedisPool?.close()
        jedisPool = null
        commandCallback = null
        plugin.logger.info("Redis 连接已断开")
    }

    fun isConnected(): Boolean {
        return try {
            jedisPool?.resource?.use { jedis ->
                jedis.ping() == "PONG"
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 发送命令到指定子服务器
     * @param serverName 目标服务器名称，"ALL" 表示所有服务器
     * @param command 要执行的命令
     */
    fun sendCommand(serverName: String, command: String): Boolean {
        return try {
            jedisPool?.resource?.use { jedis ->
                val message = "$serverName|$command"
                jedis.publish(commandChannel, message)
                // plugin.logger.info("已发送命令到 $serverName: $command") // debug
                true
            } ?: run {
                plugin.logger.warning("Redis 未连接，无法发送命令")
                false
            }
        } catch (e: Exception) {
            plugin.logger.severe("发送命令失败: ${e.message}")
            false
        }
    }

    /**
     * 发送带回调的命令
     * 消息格式: serverName|taskId|command
     * @param serverName 目标服务器名称
     * @param taskId 任务ID，用于匹配回调
     * @param command 要执行的命令
     */
    fun sendCommandWithCallback(serverName: String, taskId: String, command: String): Boolean {
        return try {
            jedisPool?.resource?.use { jedis ->
                val message = "$serverName|$taskId|$command"
                jedis.publish(commandChannel, message)
                // plugin.logger.info("已发送回调命令到 $serverName [taskId=$taskId]: $command") // debug
                true
            } ?: run {
                plugin.logger.warning("Redis 未连接，无法发送命令")
                false
            }
        } catch (e: Exception) {
            plugin.logger.severe("发送命令失败: ${e.message}")
            false
        }
    }

    /**
     * 发送广播消息到所有子服务器
     * @param message 广播内容
     */
    fun broadcast(message: String): Boolean {
        return sendCommand("ALL", "broadcast $message")
    }

    /**
     * 发送自定义消息到 Redis 频道
     * @param type 消息类型
     * @param data 消息数据
     */
    fun publish(type: String, data: String): Boolean {
        return try {
            jedisPool?.resource?.use { jedis ->
                val message = "$type|$data"
                jedis.publish(commandChannel, message)
                true
            } ?: false
        } catch (e: Exception) {
            plugin.logger.severe("发布消息失败: ${e.message}")
            false
        }
    }

    fun getJedis(): Jedis? {
        return try {
            jedisPool?.resource
        } catch (e: Exception) {
            null
        }
    }
}

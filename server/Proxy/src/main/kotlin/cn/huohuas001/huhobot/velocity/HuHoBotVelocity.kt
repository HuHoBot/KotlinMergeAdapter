package cn.huohuas001.huhobot.velocity

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.HuHoBot
import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.bot.events.BindRequest
import cn.huohuas001.bot.provider.*
import cn.huohuas001.bot.tools.Cancelable
import cn.huohuas001.huhobot.common.HuHoBotProxy
import cn.huohuas001.huhobot.velocity.commands.HuHoBotCommand
import cn.huohuas001.huhobot.velocity.commands.VelocityConsoleSender
import cn.huohuas001.huhobot.velocity.events.GameChat
import cn.huohuas001.huhobot.velocity.events.QueryAllowList
import cn.huohuas001.huhobot.velocity.events.QueryOnline
import cn.huohuas001.huhobot.common.managers.IConfigManager
import cn.huohuas001.huhobot.velocity.managers.ConfigManager
import cn.huohuas001.huhobot.common.redis.RedisManager
import com.alibaba.fastjson2.JSONObject
import com.google.inject.Inject
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.slf4j.Logger
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class HuHoBotVelocity @Inject constructor(
    val server: ProxyServer,
    val logger: Logger,
    @DataDirectory val dataDirectory: Path,
    private val pluginContainer: PluginContainer
) : HuHoBotProxy {

    override lateinit var configManager: IConfigManager
    override var redisManager: RedisManager? = null
    override var bindRequestObj = BindRequest()
    override var eventList: MutableMap<String, BaseEvent> = HashMap()

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        configManager = ConfigManager(this)
        configManager.loadCommandsFromConfig()

        // 初始化 Redis
        initRedis()

        // Register command
        val commandMeta: CommandMeta = server.commandManager.metaBuilder("huhobot")
            .aliases("hb")
            .build()
        server.commandManager.register(commandMeta, HuHoBotCommand(this))

        // Register chat event listener
        server.eventManager.register(this, GameChat(this))

        enableBot()
    }

    private fun initRedis() {
        if (configManager.isRedisEnabled()) {
            redisManager = RedisManager(this)
            redisManager!!.connect(
                configManager.getRedisHost(),
                configManager.getRedisPort(),
                configManager.getRedisPassword()
            )
        } else {
            logger.info("Redis 未启用，命令将在本地执行")
        }
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        // 断开 Redis
        redisManager?.disconnect()

        ClientManager.setShouldReconnect(false)
        ClientManager.shutdownClient()
    }

    override fun broadcastMessage(msg: String) {
        // 先在 Velocity 广播
        server.allPlayers.forEach { player ->
            player.sendMessage(net.kyori.adventure.text.Component.text(msg))
        }

        // 如果 Redis 已连接，也发送到子服务器
        redisManager?.broadcast(msg)
    }

    override fun callPluginEvent(command: String, data: JSONObject, packId: String, runByAdmin: Boolean): Boolean {
        // Velocity doesn't have a built-in event system like Bukkit
        // Custom event handling can be implemented here if needed
        return false
    }

    override fun sendCommand(command: String): CompletableFuture<HExecution> {
        val sender = VelocityConsoleSender(this)
        return sender.execute(command)
    }

    /**
     * 发送命令到指定服务器
     */
    fun sendCommandToServer(serverName: String, command: String): CompletableFuture<HExecution> {
        val sender = VelocityConsoleSender(this)
        return sender.executeOnServer(serverName, command)
    }

    override fun submit(task: Runnable): Cancelable {
        val scheduledTask = server.scheduler.buildTask(this, task).schedule()
        return HuHoBotTask(scheduledTask)
    }

    override fun submitLater(delay: Long, task: Runnable): Cancelable {
        val scheduledTask = server.scheduler.buildTask(this, task)
            .delay(delay * 50, TimeUnit.MILLISECONDS) // Convert ticks to milliseconds
            .schedule()
        return HuHoBotTask(scheduledTask)
    }

    override fun submitTimer(delay: Long, period: Long, task: Runnable): Cancelable {
        val scheduledTask = server.scheduler.buildTask(this, task)
            .delay(delay * 50, TimeUnit.MILLISECONDS)
            .repeat(period * 50, TimeUnit.MILLISECONDS)
            .schedule()
        return HuHoBotTask(scheduledTask)
    }

    override fun getHashKey(): String {
        return configManager.getHashKey()
    }

    override fun getPlatform(): String {
        return "velocity"
    }

    override fun getName(): String {
        return configManager.getName()
    }

    fun getWhiteList(): WhiteList {
        return configManager.getWhiteList()
    }

    override fun getChatFormat(): ChatFormat {
        return configManager.getChatFormat()
    }

    override fun getMotd(): Motd {
        return configManager.getMotd()
    }

    override fun addWhiteList(playerName: String) {
        val command: String = getWhiteList().addCommand.replace("{name}", playerName)
        // 确保发送到所有服务器，除非明确指定了目标
        val finalCommand = if (command.contains(":")) command else "ALL:$command"
        sendCommand(finalCommand)
    }

    override fun delWhiteList(playerName: String) {
        val command: String = getWhiteList().delCommand.replace("{name}", playerName)
        // 确保发送到所有服务器，除非明确指定了目标
        val finalCommand = if (command.contains(":")) command else "ALL:$command"
        sendCommand(finalCommand)
    }

    override fun getPluginVersion(): String {
        return pluginContainer.description.version.orElse("1.0.0")
    }

    override fun getServerId(): String {
        return configManager.getServerId()
    }

    override fun getCallbackConvertImg(): Int {
        return configManager.getCallbackConvertImg()
    }

    override fun loadCustomCommand() {
        configManager.loadCommandsFromConfig()
    }

    override fun setHashKey(hashKey: String) {
        configManager.setHashKey(hashKey)
    }

    override fun setServerId(serverId: String) {
        configManager.setServerId(serverId)
    }

    override fun log_info(msg: String) {
        logger.info(msg)
    }

    override fun log_error(msg: String) {
        logger.error(msg)
    }

    override fun log_warning(msg: String) {
        logger.warn(msg)
    }

    override fun getQueryOnline(): BaseEvent {
        return QueryOnline(this)
    }

    override fun getQueryAllowList(): BaseEvent {
        return QueryAllowList(this)
    }

    /**
     * 重新连接 Redis
     */
    fun reconnectRedis(): Boolean {
        redisManager?.disconnect()
        if (configManager.isRedisEnabled()) {
            redisManager = RedisManager(this)
            redisManager!!.connect(
                configManager.getRedisHost(),
                configManager.getRedisPort(),
                configManager.getRedisPassword()
            )
            return redisManager!!.isConnected()
        }
        return false
    }
}

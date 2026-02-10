package cn.huohuas001.huhobot.bungee

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.bot.events.BindRequest
import cn.huohuas001.bot.provider.*
import cn.huohuas001.bot.providers.HExecution
import cn.huohuas001.bot.tools.Cancelable
import cn.huohuas001.huhobot.bungee.commands.BungeeConsoleSender
import cn.huohuas001.huhobot.bungee.commands.HuHoBotCommand
import cn.huohuas001.huhobot.bungee.events.GameChat
import cn.huohuas001.huhobot.bungee.events.QueryAllowList
import cn.huohuas001.huhobot.bungee.events.QueryOnline
import cn.huohuas001.huhobot.bungee.managers.ConfigManager
import cn.huohuas001.huhobot.common.HuHoBotProxy
import cn.huohuas001.huhobot.common.managers.IConfigManager
import cn.huohuas001.huhobot.common.redis.RedisManager
import com.alibaba.fastjson2.JSONObject
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Plugin
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class HuHoBotBungee : Plugin(), HuHoBotProxy {

    override lateinit var configManager: IConfigManager
    override var redisManager: RedisManager? = null
    override var bindRequestObj = BindRequest()
    override var eventList: MutableMap<String, BaseEvent> = HashMap()

    override fun onEnable() {
        configManager = ConfigManager(this)
        configManager.loadCommandsFromConfig()

        // 初始化 Redis
        initRedis()

        // Register command
        proxy.pluginManager.registerCommand(this, HuHoBotCommand(this))

        // Register chat event listener
        proxy.pluginManager.registerListener(this, GameChat(this))

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

    override fun onDisable() {
        // 断开 Redis
        redisManager?.disconnect()

        ClientManager.setShouldReconnect(false)
        ClientManager.shutdownClient()
    }

    override fun broadcastMessage(msg: String) {
        val component = TextComponent(msg)
        proxy.players.forEach { player ->
            player.sendMessage(*arrayOf(component))
        }

        // 如果 Redis 已连接，也发送到子服务器
        redisManager?.broadcast(msg)
    }

    override fun callPluginEvent(command: String, data: JSONObject, packId: String, runByAdmin: Boolean): Boolean {
        // BungeeCord doesn't have a built-in custom event system like Bukkit
        return false
    }

    override fun sendCommand(command: String): CompletableFuture<HExecution> {
        val sender = BungeeConsoleSender(this)
        return sender.execute(command)
    }

    /**
     * 发送命令到指定服务器
     */
    fun sendCommandToServer(serverName: String, command: String): CompletableFuture<HExecution> {
        val sender = BungeeConsoleSender(this)
        return sender.executeOnServer(serverName, command)
    }

    override fun submit(task: Runnable): Cancelable {
        val scheduledTask = proxy.scheduler.runAsync(this, task)
        return HuHoBotTask(scheduledTask)
    }

    override fun submitLater(delay: Long, task: Runnable): Cancelable {
        val scheduledTask = proxy.scheduler.schedule(this, task, delay * 50, TimeUnit.MILLISECONDS)
        return HuHoBotTask(scheduledTask)
    }

    override fun submitTimer(delay: Long, period: Long, task: Runnable): Cancelable {
        val scheduledTask = proxy.scheduler.schedule(this, task, delay * 50, period * 50, TimeUnit.MILLISECONDS)
        return HuHoBotTask(scheduledTask)
    }

    override fun getHashKey(): String {
        return configManager.getHashKey()
    }

    override fun getPlatform(): String {
        return "bungeecord"
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
        return description.version
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
        logger.severe(msg)
    }

    override fun log_warning(msg: String) {
        logger.warning(msg)
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

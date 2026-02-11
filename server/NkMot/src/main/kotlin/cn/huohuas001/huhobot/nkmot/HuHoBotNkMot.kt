package cn.huohuas001.huhobot.nkmot


import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.HuHoBot
import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.bot.events.BindRequest
import cn.huohuas001.bot.provider.BotShared
import cn.huohuas001.bot.provider.ChatFormat
import cn.huohuas001.bot.provider.CustomCommandDetail
import cn.huohuas001.bot.provider.Motd
import cn.huohuas001.bot.provider.HExecution
import cn.huohuas001.bot.tools.Cancelable
import cn.huohuas001.bot.tools.getPackID
import cn.huohuas001.huhobot.nkmot.commands.HuHoBotCommand
import cn.huohuas001.huhobot.nkmot.events.QueryOnline
import cn.huohuas001.huhobot.nkmot.events.QueryWhiteList
import cn.huohuas001.huhobot.nkmot.managers.ConfigManager
import cn.huohuas001.huhobot.nkmot.tools.ConsoleSender
import cn.nukkit.plugin.PluginBase
import cn.nukkit.plugin.PluginLogger
import java.util.concurrent.CompletableFuture

class HuHoBotNkMot: PluginBase(), HuHoBot {
    override var bindRequestObj = BindRequest()
    override var eventList:MutableMap<String, BaseEvent> = HashMap<String, BaseEvent>()
    lateinit var config: ConfigManager
    lateinit var pluginLogger: PluginLogger

    override fun onLoad() {
        config = ConfigManager(this)
        pluginLogger = getLogger()

        //初始化命令
        this.server.commandMap.register("huhobot", HuHoBotCommand(this))

        //生成serverId
        if (config.getServerId().isEmpty() || config.getServerId() === "null") {
            config.setServerId(getPackID())
            config.save()
        }
    }

    fun reloadBotConfig(): Boolean {
        config = ConfigManager(this)
        loadCustomCommand()
        return true
    }

    override fun onEnable() {
        enableBot()
        pluginLogger.info("HuHoBot Loaded. By HuoHuas001")
    }

    override fun onDisable() {
        ClientManager.setShouldReconnect(false)
        ClientManager.shutdownClient()
    }

    override fun getQueryAllowList(): BaseEvent {
        return QueryWhiteList( this)
    }

    override fun getQueryOnline(): BaseEvent {
        return QueryOnline(this)
    }

    override fun addWhiteList(playerName: String) {
        server.addWhitelist(playerName)
    }

    override fun delWhiteList(playerName: String) {
        server.removeWhitelist(playerName)
    }

    override fun log_info(msg: String) {
        pluginLogger.info(msg)
    }

    override fun log_warning(msg: String) {
        pluginLogger.warning(msg)
    }

    override fun log_error(msg: String) {
        pluginLogger.error(msg)
    }

    override fun getChatFormat(): ChatFormat {
        return ChatFormat(
            config.chatFormat.fromGame,
            config.chatFormat.fromGroup,
            config.chatFormat.postChat,
            config.chatFormat.postPrefix
        )
    }

    override fun getMotd(): Motd {
        return Motd(
            config.motd.serverIp,
            config.motd.serverPort,
            config.motd.api,
            config.motd.text,
            config.motd.outputOnlineList,
            config.motd.postImg
        )
    }

    override fun getServerId(): String {
        return config.getServerId()
    }

    override fun setServerId(serverId: String) {
        config.setServerId(serverId)
    }

    override fun getHashKey(): String? {
        return config.hashKey
    }

    override fun setHashKey(hashKey: String) {
        config.setHashKey(hashKey)
    }

    override fun getPlatform(): String {
        return "nukkit"
    }

    override fun getPluginVersion(): String {
        return description.version
    }

    override fun getCallbackConvertImg(): Int {
        return config.callbackConvertImg
    }

    override fun loadCustomCommand() {
        val commands = config.customCommands

        // 初始化 commandMap
        val commandMap = HashMap<String, CustomCommandDetail>()
        for (commandData in commands) {

            commandMap[commandData.key] = commandData.convert()
        }

        BotShared.customCommandMap = commandMap
    }

    override fun sendCommand(command: String): CompletableFuture<HExecution> {
        class NkExecution: HExecution {
            val sender = ConsoleSender(server.consoleSender)
            override fun getRawString(): String {
                return sender.output.toString()
            }

            override fun execute(command: String): CompletableFuture<HExecution> {
                server.dispatchCommand(sender, command)
                return CompletableFuture.completedFuture(this)
            }
        }
        return NkExecution().execute(command)
    }

    override fun submit(task: Runnable): Cancelable {
        val taskObj = HuHoBotTask(this, task)
        return HuHoBotTaskCancelable(server.scheduler.scheduleTask(taskObj))
    }

    override fun submitLater(delay: Long, task: Runnable): Cancelable {
        val taskObj = HuHoBotTask(this, task)
        return HuHoBotTaskCancelable(server.scheduler.scheduleDelayedTask(taskObj, delay.toInt()))
    }

    override fun submitTimer(
        delay: Long,
        period: Long,
        task: Runnable
    ): Cancelable {
        val taskObj = HuHoBotTask(this, task)
        return HuHoBotTaskCancelable(server.scheduler.scheduleDelayedRepeatingTask(taskObj, delay.toInt(), period.toInt()))
    }

    override fun broadcastMessage(msg: String) {
        server.broadcastMessage(msg)
    }

}
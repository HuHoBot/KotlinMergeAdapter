package cn.huohuas001.huhobot.allay

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
import cn.huohuas001.huhobot.allay.commands.HuHoBotCommand
import cn.huohuas001.huhobot.allay.events.QueryAllowList
import cn.huohuas001.huhobot.allay.events.QueryOnline
import cn.huohuas001.huhobot.allay.utils.HuHoBotCommandSender
import eu.okaeri.configs.ConfigManager
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.OkaeriConfigInitializer
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer
import org.allaymc.api.eventbus.EventHandler
import org.allaymc.api.eventbus.event.player.PlayerChatEvent
import org.allaymc.api.plugin.Plugin
import org.allaymc.api.registry.Registries
import org.allaymc.api.server.Server
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

class HuHoBotAllay: Plugin(), HuHoBot {
    override var bindRequestObj = BindRequest()
    override var eventList:MutableMap<String, BaseEvent> = HashMap<String, BaseEvent>()
    val CONFIG_FILE_NAME: String = "plugins/HuHoBot/config.yml"
    
    lateinit var config: HuHoBotConfig

    fun reloadConfig() {
        config.load()
        loadCustomCommand()
        config.save()
    }

    override fun onLoad() {

        // 创建配置管理器
        config = ConfigManager.create(
            HuHoBotConfig::class.java,
            OkaeriConfigInitializer { it: OkaeriConfig? ->
                // Specify configurer implementation, optionally additional serdes packages
                it!!.withConfigurer(YamlSnakeYamlConfigurer())
                // Specify Path, File or pathname
                it.withBindFile(Path.of(CONFIG_FILE_NAME))
                // Automatic removal of undeclared keys
                it.withRemoveOrphans(true)
                // Save the file if it does not exist
                it.saveDefaults()
                // Load and save to update comments/new fields
                it.load(true)
            }
        )

        // 初始化默认值
        config.initializeDefaults()
        config.save()

        //注册命令
        Registries.COMMANDS.register(HuHoBotCommand(this))

        pluginLogger.info("HuHoBot Loaded. By HuoHuas001")
    }

    override fun onEnable() {
        Server.getInstance().eventBus.registerListener(this)
        enableBot()
    }

    override fun onDisable() {
        ClientManager.setShouldReconnect(false)
        ClientManager.shutdownClient()
    }

    @EventHandler
    private fun onPlayerChat(event: PlayerChatEvent) {
        val message = event.getMessage()
        val playerName = event.getPlayer().displayName

        ClientManager.postChat(playerName,message)
    }

    override fun getQueryAllowList(): BaseEvent {
        return QueryAllowList()
    }

    override fun getQueryOnline(): BaseEvent {
        return QueryOnline(this)
    }

    override fun log_info(msg: String) {
        pluginLogger.info(msg)
    }

    override fun log_warning(msg: String) {
        pluginLogger.warn(msg)
    }

    override fun log_error(msg: String) {
        pluginLogger.error(msg)
    }

    override fun getChatFormat(): ChatFormat {
        return ChatFormat(
            config.chatConfig.fromGame,
            config.chatConfig.fromGroup,
            config.chatConfig.postChat,
            config.chatConfig.postPrefix
        )
    }

    override fun getMotd(): Motd {
        return Motd(
            config.motd.serverIp,
            config.motd.serverPort,
            config.motd.api,
            config.motd.text,
            config.motd.outputOnlineList,
            config.motd.postImg,
            )
    }

    override fun getServerId(): String {
        return config.serverId
    }

    override fun setServerId(serverId: String) {
        config.serverId = serverId
    }

    override fun getHashKey(): String? {
        return config.hashKey
    }

    override fun setHashKey(hashKey: String) {
        config.hashKey = hashKey
        config.save()
        reloadConfig()
    }

    override fun getName(): String {
        return config.serverName
    }

    override fun getPlatform(): String {
        return "allay"
    }

    override fun getPluginVersion(): String {
        return getPluginContainer().descriptor().version
    }

    override fun getCallbackConvertImg(): Int {
        return config.callbackConvertImg
    }

    override fun loadCustomCommand() {
        val commands = config.customCommandMap

        // 初始化 commandMap
        val commandMap = HashMap<String, CustomCommandDetail>()
        for (commandData in commands) {

            commandMap[commandData.key] = commandData.value.convert()
        }

        BotShared.customCommandMap = commandMap
    }

    override fun sendCommand(command: String): CompletableFuture<HExecution> {
        val sender = HuHoBotCommandSender(this)


        class AllayExecution: HExecution {
            override fun getRawString(): String {
                return sender.outputs.toString()
            }

            override fun execute(command: String): CompletableFuture<HExecution> {
                Registries.COMMANDS.execute(sender, command)
                return CompletableFuture.completedFuture(this)
            }
        }

        return AllayExecution().execute(command)
    }

    override fun submit(task: Runnable): Cancelable {
        val taskObj = HuHoBotTask(task)
        Server.getInstance().scheduler.scheduleDelayed(this,taskObj,0)
        return HuHoBotTaskCancelable()
    }

    override fun submitLater(delay: Long, task: Runnable): Cancelable {
        val taskObj = HuHoBotTask(task)
        Server.getInstance().scheduler.scheduleDelayed(this,taskObj, delay.toInt())
        return HuHoBotTaskCancelable()
    }

    override fun submitTimer(
        delay: Long,
        period: Long,
        task: Runnable
    ): Cancelable {
        val taskObj = HuHoBotTask(task)
        Server.getInstance().scheduler.scheduleRepeating(this,taskObj, period.toInt())
        return HuHoBotTaskCancelable()
    }

    override fun broadcastMessage(msg: String) {
        Server.getInstance().messageChannel.broadcastMessage(msg)
    }

    override fun addWhiteList(playerName: String) {
        Server.getInstance().playerManager.addToWhitelist(playerName)
    }

    override fun delWhiteList(playerName: String) {
        Server.getInstance().playerManager.removeFromWhitelist(playerName)
    }

}
package cn.huohuas001.huhobot.mod

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.HuHoBot
import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.bot.events.BindRequest
import cn.huohuas001.bot.provider.ChatFormat
import cn.huohuas001.bot.provider.Motd
import cn.huohuas001.bot.providers.HExecution
import cn.huohuas001.bot.tools.Cancelable
import cn.huohuas001.huhobot.mod.events.QueryAllowList
import cn.huohuas001.huhobot.mod.events.QueryOnline
import cn.huohuas001.huhobot.mod.managers.CommandManager
import cn.huohuas001.huhobot.mod.managers.ConfigManager
import cn.huohuas001.huhobot.mod.tools.HuHoBotScheduler
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.ChatEvent
import dev.architectury.event.events.common.LifecycleEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture

object HuHoBotMod: HuHoBot {
    const val MOD_ID: String = "huhobot"
    val LOGGER: Logger = LogManager.getLogger(MOD_ID)
    lateinit var config: ConfigManager
    lateinit var serverInstance: MinecraftServer
    lateinit var commandManager: CommandManager
    lateinit var scheduler: HuHoBotScheduler


    override var bindRequestObj = BindRequest()
    override var eventList:MutableMap<String, BaseEvent> = HashMap<String, BaseEvent>()

    fun onChatEvent(player:ServerPlayer , component:Component): EventResult{
        val componentString = component.string
        ClientManager.postChat(player.name.string,componentString)
        return EventResult.pass()
    }

    fun reloadBotConfig(){
        config.reloadConfig()
        loadCustomCommand()
    }

    fun init(){
        config = ConfigManager(this)

        //注册服务器聊天事件
        ChatEvent.RECEIVED.register { player, component -> player?.let { onChatEvent(it, component) } }

        LifecycleEvent.SERVER_STARTED.register {
            serverInstance = it

            commandManager = CommandManager(this)
            commandManager.registerCommands(serverInstance.commands.dispatcher)

            //启动调度器
            scheduler = HuHoBotScheduler(this)
            scheduler.startScheduler()

            enableBot()
        }

        LifecycleEvent.SERVER_STOPPING.register{
            disable()
        }
    }

    fun disable(){
        ClientManager.setShouldReconnect(false)
        ClientManager.shutdownClient()
    }

    override fun getQueryAllowList(): BaseEvent {
        return QueryAllowList(this)
    }

    override fun getQueryOnline(): BaseEvent {
        return QueryOnline( this)
    }

    override fun addWhiteList(playerName: String) {
        val cmd = config.getWhiteList().addCommand.replace("{name}",playerName)
        sendCommand(cmd)
    }

    override fun delWhiteList(playerName: String) {
        val cmd = config.getWhiteList().delCommand.replace("{name}",playerName)
        sendCommand(cmd)
    }

    override fun log_info(msg: String) {
        LOGGER.info(msg)
    }

    override fun log_warning(msg: String) {
        LOGGER.warn(msg)
    }

    override fun log_error(msg: String) {
        LOGGER.error(msg)
    }

    override fun getChatFormat(): ChatFormat {
        return config.getChatFormat()
    }

    override fun getMotd(): Motd {
        return config.getMotd()
    }

    override fun getServerId(): String {
        return config.getServerId()
    }

    override fun setServerId(serverId: String) {
        config.setServerId(serverId)
    }

    override fun getHashKey(): String? {
        return config.getHashKey()
    }

    override fun setHashKey(hashKey: String) {
        config.setHashKey(hashKey)
    }

    override fun getName(): String {
        return config.getServerName()
    }

    override fun getPlatform(): String {
        return "architectury"
    }

    override fun getPluginVersion(): String {
        return "0.0.0"
    }

    override fun getCallbackConvertImg(): Int {
        return config.getCallbackConvertImg()
    }

    override fun loadCustomCommand() {
        config.loadCommandsFromConfig()
    }

    override fun sendCommand(command: String): CompletableFuture<HExecution> {
        class ModExecution: HExecution {
            override fun getRawString(): String {
                return "Command Execute."
            }

            override fun execute(command: String): CompletableFuture<HExecution> {
                commandManager.executeCommand(command) {}
                return CompletableFuture.completedFuture(this)
            }
        }
        return ModExecution().execute(command)
    }

    override fun submit(task: Runnable): Cancelable {
        serverInstance.submit { task.run() }
        return HuHoBotCancelable()
    }

    override fun submitLater(delay: Long, task: Runnable): Cancelable {
        return scheduler.runTaskLater(task, delay)
    }

    override fun submitTimer(
        delay: Long,
        period: Long,
        task: Runnable
    ): Cancelable {
        return scheduler.runDelayedLoop(task, delay, period.toInt())
    }

    override fun broadcastMessage(msg: String) {
        serverInstance.playerList.broadcastSystemMessage(Component.literal(msg),false)
    }
}
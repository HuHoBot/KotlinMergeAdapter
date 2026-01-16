package cn.huohuas001.huhobot.spigot

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.HuHoBot
import cn.huohuas001.bot.events.*
import cn.huohuas001.bot.provider.*
import cn.huohuas001.bot.providers.HExecution
import cn.huohuas001.bot.tools.*
import cn.huohuas001.huhobot.spigot.api.BotCustomCommand
import cn.huohuas001.huhobot.spigot.commands.BukkitConsoleSender
import cn.huohuas001.huhobot.spigot.commands.HybridCommandExecutor
import cn.huohuas001.huhobot.spigot.events.GameChat
import cn.huohuas001.huhobot.spigot.events.QueryAllowList
import cn.huohuas001.huhobot.spigot.events.QueryOnline
import cn.huohuas001.huhobot.spigot.manager.CommandManager
import cn.huohuas001.huhobot.spigot.manager.ConfigManager
import com.alibaba.fastjson2.JSONObject
import com.github.Anon8281.universalScheduler.UniversalScheduler
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.logging.Logger

class HuHoBotSpigot: JavaPlugin(),HuHoBot {
    lateinit var pluginLogger: Logger
    lateinit var scheduler: TaskScheduler //计划对象
    lateinit var configManager: ConfigManager
    override var bindRequestObj = BindRequest()
    override var eventList:MutableMap<String, BaseEvent> = HashMap<String, BaseEvent>()
    lateinit var sender: Class<out HExecution>

    fun initConfig(){
        configManager = ConfigManager(this)
        configManager.loadCommandsFromConfig()
        setSender()
    }

    override fun onEnable() {
        pluginLogger = getLogger()
        scheduler = UniversalScheduler.getScheduler(this)
        initConfig()


        //初始化命令
        this.getCommand("huhobot")!!.setExecutor(CommandManager(this))

        server.pluginManager.registerEvents(GameChat(), this)

        enableBot()
    }

    override fun onDisable() {
        ClientManager.setShouldReconnect(false)
        ClientManager.shutdownClient()
    }

    /**
     * 发送消息给所有玩家
     * @param msg 消息
     */
    override fun broadcastMessage(msg: String) {
        server.broadcastMessage(msg)
    }

    /**
     * 运行自定义命令
     * @param command 命令名称
     * @param data 命令数据
     * @param packId 包ID
     * @param runByAdmin 是否由管理员运行
     * @return 是否被取消
     */
    override fun callPluginEvent(command: String, data: JSONObject, packId: String, runByAdmin: Boolean): Boolean {
        val event = BotCustomCommand(command, data, packId, runByAdmin)
        Bukkit.getPluginManager().callEvent(event)
        return event.isCancelled
    }

    override fun sendCommand(command: String): CompletableFuture<HExecution> {
        val senderInstance: HExecution = sender.constructors[0].newInstance(this) as HExecution
        return senderInstance.execute(command)
    }

    fun setSender() {
        val senderType = config.getString("CommandSender")?: "bukkit"
        when (senderType.lowercase(Locale.getDefault())) {
            "hybrid" -> {
                sender = HybridCommandExecutor::class.java
                pluginLogger.info("已启用使用混合控制台执行器")
            }
            else -> {
                sender = BukkitConsoleSender::class.java
                pluginLogger.info("已启用使用模拟控制台执行器")
            }
        }
    }

    override fun submit(task: Runnable): Cancelable {
        return HuHoBotTask(scheduler.runTask(task))
    }

    override fun submitLater(delay: Long, task: Runnable): Cancelable {
        return HuHoBotTask(scheduler.runTaskLater(task,delay))
    }

    override fun submitTimer(delay: Long, period: Long, task: Runnable): Cancelable {
        return HuHoBotTask(scheduler.runTaskTimer(task,delay,period))
    }

    override fun getHashKey(): String {
        return configManager.getHashKey()
    }

    override fun getPlatform(): String {
        return "spigot"
    }

    fun getWhiteList(): WhiteList {
        return WhiteList(
            config.getString("whiteList.add")!!,
            config.getString("whiteList.del")!!,
        )
    }

    override fun getChatFormat(): ChatFormat {
        return ChatFormat(
            config.getString("chatFormat.from_game")!!,
            config.getString("chatFormat.from_group")!!,
            config.getBoolean("chatFormat.post_chat"),
            config.getString("chatFormat.post_prefix")!!,
        )
    }

    override fun getMotd(): Motd {
        //获取motd Config
        val serverIP = config.getString("motd.server_ip")!!
        val serverPort = config.getInt("motd.server_port")
        val api = config.getString("motd.api")!!
        val text = config.getString("motd.text")!!
        val outputOnlineList = config.getBoolean("motd.output_online_list")
        val postImg = config.getBoolean("motd.post_img")
        return Motd(serverIP, serverPort, api, text, outputOnlineList, postImg)
    }

    override fun addWhiteList(playerName: String) {
        val command:String = getWhiteList().addCommand.replace("{name}",playerName)
        sendCommand(command)
    }

    override fun delWhiteList(playerName: String) {
        val command:String = getWhiteList().delCommand.replace("{name}",playerName)
        sendCommand(command)
    }

    override fun getPluginVersion(): String {
        return description.version
    }

    override fun getServerId(): String {
        return configManager.getServerId()
    }

    override fun getCallbackConvertImg(): Int {
        return config.getInt("callbackConvertImg",0)
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
        pluginLogger.info(msg)
    }

    override fun log_error(msg: String) {
        pluginLogger.severe(msg)
    }

    override fun log_warning(msg: String) {
        pluginLogger.warning(msg)
    }

    override fun getQueryOnline(): BaseEvent {
        return QueryOnline(this)
    }

    override fun getQueryAllowList(): BaseEvent {
        return QueryAllowList(this)
    }
}
package cn.huohuas001.huhobot.mod.managers

import cn.huohuas001.bot.provider.BotShared
import cn.huohuas001.bot.provider.ChatFormat
import cn.huohuas001.bot.provider.CustomCommandDetail
import cn.huohuas001.bot.provider.Motd
import cn.huohuas001.bot.provider.WhiteList
import cn.huohuas001.bot.tools.getPackID
import cn.huohuas001.huhobot.mod.ExpectPlatform
import cn.huohuas001.huhobot.mod.HuHoBotMod
import org.apache.logging.log4j.Logger
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import java.io.*
import java.nio.file.Files
import java.util.*

/**
 * 带Getter和Setter的Fabric配置管理器（保留原有配置项操作）
 */
class ConfigManager(private val mod: HuHoBotMod) {
    private val logger: Logger = mod.LOGGER
    private val configDir: File
    private val configFile: File
    private var config: MutableMap<String, Any> = mutableMapOf()

    init {
        // 配置目录：服务器根目录/config/huhobot
        configDir = File("./config/huhobot")

        if (!configDir.exists() && !configDir.mkdirs()) {
            logger.error("创建配置目录失败！路径：${configDir.absolutePath}")
        }

        // 配置文件路径
        configFile = File(configDir, "config.yml")

        // 加载默认配置
        loadDefaultConfig()

        // 读取配置到内存
        reloadConfig()
    }

    /**
     * 加载默认配置（从resources复制）
     */
    private fun loadDefaultConfig() {
        if (configFile.exists()) {
            return
        }

        try {
            val defaultConfigStream = mod.javaClass.getResourceAsStream("/config.yml")
            if (defaultConfigStream == null) {
                logger.error("默认配置文件不存在！请在resources目录下创建config.yml")
                return
            }

            Files.copy(defaultConfigStream, configFile.toPath())
            logger.info("生成默认配置文件：${configFile.absolutePath}")
            defaultConfigStream.close()
        } catch (e: IOException) {
            logger.error("生成默认配置失败$e")
        }
    }

    /**
     * 重新加载配置
     */
    fun reloadConfig() {
        if (!configFile.exists()) {
            logger.warn("配置文件不存在，重新生成...")
            loadDefaultConfig()
        }

        val options = DumperOptions()
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        val yaml = Yaml(options)

        try {
            val input = FileInputStream(configFile)
            config = yaml.load<MutableMap<String, Any>>(input) ?: mutableMapOf()
            input.close()
            logger.info("配置加载完成")
        } catch (e: IOException) {
            logger.error("加载配置失败$e")
            config = mutableMapOf()
        }
    }

    /**
     * 保存配置
     */
    fun saveConfig() {
        if (config.isEmpty()) {
            logger.warn("无配置数据可保存")
            return
        }

        val options = DumperOptions()
        options.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        options.indent = 2
        val yaml = Yaml(options)

        try {
            val writer = FileWriter(configFile)
            yaml.dump(config, writer)
            writer.close()
            logger.info("配置已保存")
        } catch (e: IOException) {
            logger.error("保存配置失败$e")
        }
    }

    // ------------------------------ 通用配置读写工具方法 ------------------------------
    private fun getConfigValueByPath(path: String): Any? {
        if (config.isEmpty() || path.isEmpty()) {
            return null
        }

        val segments = path.split("\\.".toRegex())
        var current: Map<String, Any> = config

        for (i in segments.indices) {
            val segment = segments[i]
            if (!current.containsKey(segment)) {
                return null
            }

            val value = current[segment]
            if (i < segments.size - 1) {
                if (value !is Map<*, *>) {
                    return null
                }
                current = value as Map<String, Any>
            } else {
                return value
            }
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun setConfigValueByPath(path: String, value: Any?) {
        if (config.isEmpty()) {
            config = mutableMapOf()
        }

        val segments = path.split("\\.".toRegex())
        var current: MutableMap<String, Any> = config

        for (i in 0 until segments.size - 1) {
            val segment = segments[i]
            if (!current.containsKey(segment) || current[segment] !is Map<*, *>) {
                current[segment] = mutableMapOf<String, Any>()
            }
            current = current[segment] as MutableMap<String, Any>
        }

        current[segments[segments.size - 1]] = value!!
    }

    // ------------------------------ 原有配置项的Getter和Setter ------------------------------
    // 1. hashKey相关
    fun getHashKey(): String {
        return getString("hashKey", "")
    }

    fun setHashKey(hashKey: String) {
        setConfigValueByPath("hashKey", hashKey)
        saveConfig() // 立即保存
    }

    // 2. serverId相关
    fun getServerId(): String {
        var serverId = getString("serverId", "")
        if(serverId == ""){
            serverId = getPackID()
            setServerId(serverId)
        }
        return serverId
    }

    fun setServerId(serverId: String) {
        setConfigValueByPath("serverId", serverId)
        saveConfig() // 立即保存
    }

    fun getMotd(): Motd{
        val serverIP = getString("motd.server_ip", "play.hypixel.net")
        val serverPort = getInt("motd.server_port", 25565)
        val api = getString("motd.api", "https://motdbe.blackbe.work/status_img/java?host={server_ip}:{server_port}")
        val text = getString("motd.text", "共{online}人在线")
        val outputOnlineList = getBoolean("motd.output_online_list", true)
        val postImg = getBoolean("motd.post_img", true)
        return Motd(serverIP, serverPort, api, text, outputOnlineList, postImg)
    }

    fun getChatFormat(): ChatFormat {
        val fromGame = getString("chatFormat.from_game", "[游戏] {name}: {msg}")
        val fromGroup = getString("chatFormat.from_group", "[群组] {name}: {msg}")
        val postChat = getBoolean("chatFormat.post_chat", true)
        val postPrefix = getString("chatFormat.post_prefix", "#")
        return ChatFormat(fromGame, fromGroup, postChat, postPrefix)
    }

    fun getWhiteList(): WhiteList{
        val addCommand = getString("whiteList.add", "whitelist add {name}")
        val delCommand = getString("whiteList.del", "whitelist remove {name}")
        return WhiteList(addCommand, delCommand)
    }

    // 5. 服务器名字
    fun getServerName(): String {
        return getString("serverName", "Fabric")
    }

    // 6. 自定义命令
    /**
     * 读取 customCommand 配置（List<Map> 结构）
     * @return 自定义命令列表（每个元素是包含 key/command/permission 的 Map）
     */
    private fun getCustomCommands(): List<Map<String, Any>> {
        // 1. 先读取 customCommand 节点（实际是 List 类型）
        val customCommandObj = getConfigValueByPath("customCommand")

        // 2. 类型判断：若不是 List，返回空列表（避免空指针）
        if (customCommandObj !is List<*>) {
            logger.warn("customCommand 配置格式错误，应为列表结构！")
            return mutableListOf()
        }

        // 3. 强制转换为 List，并过滤无效元素（确保每个元素是 Map）
        val rawList = customCommandObj
        val customCommands = mutableListOf<Map<String, Any>>()

        for (item in rawList) {
            if (item is Map<*, *>) {
                // 转换为 Map<String, Object> 并添加到结果中
                @Suppress("UNCHECKED_CAST")
                val commandMap = item as Map<String, Any>
                customCommands.add(commandMap)
            } else {
                logger.warn("customCommand 中存在无效配置项：$item（应为键值对结构）")
            }
        }

        return customCommands
    }

    fun loadCommandsFromConfig(){
        val customCommands = getCustomCommands()
        val commandMap = HashMap<String, CustomCommandDetail>()
        for (command in customCommands) {
            val key = command["key"] as? String ?: continue
            val commandString = command["command"] as? String ?: continue
            val permission = command["permission"] as? Int ?: 0
            commandMap[key] = CustomCommandDetail(key, commandString, permission)
        }
        BotShared.customCommandMap = commandMap
    }

    // ------------------------------ 通用类型Get方法（供扩展） ------------------------------
    fun getString(path: String, defaultValue: String): String {
        val value = getConfigValueByPath(path)
        return if (value is String) value else defaultValue
    }

    fun getInt(path: String, defaultValue: Int): Int {
        val value = getConfigValueByPath(path)
        return if (value is Int) value else defaultValue
    }

    fun getBoolean(path: String, defaultValue: Boolean): Boolean {
        val value = getConfigValueByPath(path)
        return if (value is Boolean) value else defaultValue
    }

    @Suppress("UNCHECKED_CAST")
    fun getMap(path: String): Map<String, Any> {
        val value = getConfigValueByPath(path)
        return if (value is Map<*, *>) value as Map<String, Any> else mutableMapOf()
    }

    // ------------------------------ Getter ------------------------------
    fun getConfigFile(): File {
        return configFile
    }

    fun getRawConfig(): Map<String, Any> {
        return config.toMap()
    }
}

package cn.huohuas001.huhobot.nkmot.managers

import cn.huohuas001.bot.provider.CustomCommandDetail
import cn.huohuas001.bot.tools.getPackID
import cn.huohuas001.huhobot.nkmot.HuHoBotNkMot
import cn.nukkit.utils.Config
import cn.nukkit.utils.ConfigSection
import java.io.File

class ConfigManager(private val plugin: HuHoBotNkMot) {
    private val config: Config
    private val currentVersion = 5

    // 根配置项
    private var serverId: String
    var hashKey: String
        private set
    val chatFormat: ChatFormatConfig
    val motd: MotdConfig
    val postEvent: PostEventConfig
    var customCommands: List<CustomCommand>
        private set
    var version: Int
        private set
    var callbackConvertImg: Int
        private set
    var filterRegex: List<String>
        private set

    init {
        plugin.saveResource("config.yml")
        config = Config(
            File(plugin.dataFolder, "config.yml"),
            Config.YAML,
            ConfigSection().apply {
                // 默认配置
                put("serverId", getPackID())
                put("hashKey", null)
                put("chatFormat", ConfigSection().apply {
                    put("from_game", "<{name}> {msg}")
                    put("from_group", "群:<{nick}> {msg}")
                    put("post_chat", true)
                    put("post_prefix", "")
                })
                put("motd", ConfigSection().apply {
                    put("server_ip", "play.easecation.net")
                    put("server_port", 19132)
                    put("api", "https://motdbe.blackbe.work/status_img?host={server_ip}:{server_port}")
                    put("text", "共{online}人在线")
                    put("output_online_list", true)
                    put("post_img", true)
                    put("markdown", true)
                    put("customMarkdown", false)
                })
                put("postEvent", ConfigSection().apply {
                    put("onJoin", ConfigSection().apply {
                        put("enable", false)
                        put("formatString", "玩家 {playerName} 加入了服务器")
                    })
                    put("onLeft", ConfigSection().apply {
                        put("enable", false)
                        put("formatString", "玩家 {playerName} 离开了服务器")
                    })
                })
                put("customCommand", listOf(
                    ConfigSection().apply {
                        put("key", "加白名")
                        put("command", "whitelist add &1")
                        put("permission", 0)
                    },
                    ConfigSection().apply {
                        put("key", "管理加白名")
                        put("command", "whitelist add &1")
                        put("permission", 1)
                    }
                ))
                put("callbackConvertImg", 0)
                put("filterRegex", listOf("\\u001B\\[[;\\d]*[ -/]*[@-~]"))
                put("version", currentVersion)
            }
        )

        // 自动检查并补全配置文件中缺失的字段
        checkAndFillConfig()

        // 初始化配置项
        serverId = config.getString("serverId")
        hashKey = config.getString("hashKey")
        chatFormat = ChatFormatConfig(config.getSection("chatFormat"))
        motd = MotdConfig(config.getSection("motd"))
        postEvent = PostEventConfig(config.getSection("postEvent"))
        customCommands = config.getMapList("customCommand").map { section ->
            CustomCommand(section as ConfigSection)
        }
        callbackConvertImg = config.getInt("callbackConvertImg", 0)
        filterRegex = config.getStringList("filterRegex")
        version = config.getInt("version", 0)

        if (version < currentVersion) {
            migrateVersion()
        }
    }

    // region 内部配置类
    class ChatFormatConfig(section: ConfigSection) {
        val fromGame: String = section.getString("from_game", "<{name}> {msg}")
        val fromGroup: String = section.getString("from_group", "群:<{nick}> {msg}")
        val postChat: Boolean = section.getBoolean("post_chat", true)
        val postPrefix: String = section.getString("post_prefix", "")
    }

    class MotdConfig(section: ConfigSection) {
        val serverIp: String = section.getString("server_ip", "play.easecation.net")
        val serverPort: Int = section.getInt("server_port", 19132)
        val api: String = section.getString("api", "https://motdbe.blackbe.work/status_img?host={server_ip}:{server_port}")
        val text: String = section.getString("text", "共{online}人在线")
        val outputOnlineList: Boolean = section.getBoolean("output_online_list", true)
        val postImg: Boolean = section.getBoolean("post_img", true)
        val useMarkdown: Boolean = section.getBoolean("markdown", true)
        val customMarkdown: Boolean = section.getBoolean("customMarkdown", false)
    }

    class PostEventConfig(section: ConfigSection) {
        val onJoin: PostEventEntry = PostEventEntry(section.getSection("onJoin"))
        val onLeft: PostEventEntry = PostEventEntry(section.getSection("onLeft"))
    }

    class PostEventEntry(section: ConfigSection) {
        val enable: Boolean = section.getBoolean("enable", false)
        val formatString: String = section.getString("formatString", "")
    }

    class CustomCommand(section: ConfigSection) {
        val key: String = section.getString("key")
        val command: String = section.getString("command")
        val permission: Int = section.getInt("permission", 0)

        fun convert(): CustomCommandDetail {
            return CustomCommandDetail(this.key, this.command, this.permission)
        }
    }
    // endregion

    /**
     * 自动检查配置文件，对比预定义的完整配置项列表，
     * 将缺失的字段自动补全为默认值并保存。
     */
    private fun checkAndFillConfig() {
        var changed = false

        // 定义所有已知配置路径 → 默认值的映射表
        val allDefaults = mapOf<String, Any>(
            "serverId" to getPackID(),
            "version" to currentVersion,
            "callbackConvertImg" to 0,
            "filterRegex" to listOf("\\u001B\\[[;\\d]*[ -/]*[@-~]"),
            "chatFormat.from_game" to "<{name}> {msg}",
            "chatFormat.from_group" to "群:<{nick}> {msg}",
            "chatFormat.post_chat" to true,
            "chatFormat.post_prefix" to "",
            "motd.server_ip" to "play.easecation.net",
            "motd.server_port" to 19132,
            "motd.api" to "https://motdbe.blackbe.work/status_img?host={server_ip}:{server_port}",
            "motd.text" to "共{online}人在线",
            "motd.output_online_list" to true,
            "motd.post_img" to true,
            "motd.markdown" to true,
            "motd.customMarkdown" to false,
            "postEvent.onJoin.enable" to false,
            "postEvent.onJoin.formatString" to "玩家 {playerName} 加入了服务器",
            "postEvent.onLeft.enable" to false,
            "postEvent.onLeft.formatString" to "玩家 {playerName} 离开了服务器",
        )

        for ((path, defaultValue) in allDefaults) {
            if (!config.exists(path)) {
                config.set(path, defaultValue)
                changed = true
                plugin.logger.info("已补全缺失的配置项: $path")
            }
        }

        // 补全 customCommand（列表类型需特殊处理）
        if (!config.exists("customCommand")) {
            config.set("customCommand", listOf(
                ConfigSection().apply {
                    put("key", "加白名")
                    put("command", "whitelist add &1")
                    put("permission", 0)
                },
                ConfigSection().apply {
                    put("key", "管理加白名")
                    put("command", "whitelist add &1")
                    put("permission", 1)
                }
            ))
            changed = true
            plugin.logger.info("已补全缺失的配置项: customCommand")
        }

        if (changed) {
            config.save()
            plugin.logger.info("配置文件自动检查和补全完成，已保存。")
        }
    }

    /**
     * 版本迁移：当配置版本落后于当前版本时，
     * 执行版本特定的升级逻辑。
     */
    private fun migrateVersion() {
        val oldVersion = version
        if (oldVersion < 3) {
            config.set("callbackConvertImg", 0)
            this.callbackConvertImg = 0
        }
        if (oldVersion < 5) {
            // v5 新增 postEvent 配置
            checkAndFillConfig()
        }
        if (!config.exists("motd.markdown")) {
            config.set("motd.markdown", true)
        }
        if (!config.exists("motd.customMarkdown")) {
            config.set("motd.customMarkdown", false)
        }

        config.set("version", currentVersion)
        config.save()
        this.version = currentVersion
        plugin.logger.info("配置文件已从版本 $oldVersion 迁移到 $currentVersion")
    }

    fun getServerId(): String {
        if (serverId.isEmpty() || serverId == "null") {
            serverId = getPackID()
            setServerId(serverId)
        }
        return serverId
    }

    fun setServerId(serverId: String): ConfigManager {
        this.serverId = serverId
        config.set("serverId", serverId)
        return this
    }

    fun setHashKey(hashKey: String): ConfigManager {
        this.hashKey = hashKey
        config.set("hashKey", hashKey)
        return this
    }

    fun save() {
        config.set("serverId", serverId)
        config.set("hashKey", hashKey)
        config.save()
    }
}
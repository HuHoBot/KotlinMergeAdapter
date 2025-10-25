package cn.huohuas001.huhobot.nkmot.managers

import cn.huohuas001.bot.provider.CustomCommandDetail
import cn.huohuas001.bot.tools.getPackID
import cn.huohuas001.huhobot.nkmot.HuHoBotNkMot
import cn.nukkit.utils.Config
import cn.nukkit.utils.ConfigSection
import java.io.File

class ConfigManager(private val plugin: HuHoBotNkMot) {
    private val config: Config
    private val currentVersion = 3

    // 根配置项
    private var serverId: String
    var hashKey: String
        private set
    val chatFormat: ChatFormatConfig
    val motd: MotdConfig
    var customCommands: List<CustomCommand>
        private set
    var version: Int
        private set
    var callbackConvertImg: Int
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
                })
                put("motd", ConfigSection().apply {
                    put("server_ip", "play.easecation.net")
                    put("server_port", 19132)
                    put("api", "https://motdbe.blackbe.work/status_img?host={server_ip}:{server_port}")
                    put("text", "共{online}人在线")
                    put("output_online_list", true)
                    put("post_img", true)
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
                put("callbackConvertImg",0)
                put("version", 1)
            }
        )

        // 初始化配置项
        serverId = config.getString("serverId")
        hashKey = config.getString("hashKey")
        chatFormat = ChatFormatConfig(config.getSection("chatFormat"))
        motd = MotdConfig(config.getSection("motd"))
        customCommands = config.getMapList("customCommand").map { section ->
            CustomCommand(section as ConfigSection)
        }
        callbackConvertImg = config.getInt("callbackConvertImg",0)
        version = config.getInt("version", 0) // 带默认值读取

        if (version < currentVersion) {
            migrateToV3()
        }
    }

    // region 内部配置类
    class ChatFormatConfig(section: ConfigSection) {
        val fromGame: String = section.getString("from_game", "<{name}> {msg}")
        val fromGroup: String = section.getString("from_group", "群:<{nick}> {msg}")
        val postChat: Boolean = section.getBoolean("post_chat", true)  // 新增布尔型配置项，默认true
        val postPrefix: String = section.getString("post_prefix", "") // 新增字符串型配置项，默认空字符串
    }

    class MotdConfig(section: ConfigSection) {
        val serverIp: String = section.getString("server_ip", "play.easecation.net")
        val serverPort: Int = section.getInt("server_port", 19132)
        val api: String = section.getString("api", "https://motdbe.blackbe.work/status_img?host={server_ip}:{server_port}")
        val text: String = section.getString("text", "共{online}人在线")
        val outputOnlineList: Boolean = section.getBoolean("output_online_list", true)
        val postImg: Boolean = section.getBoolean("post_img", true)
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

    fun getServerId(): String {
        if(serverId.isEmpty()){
            serverId = getPackID()
            setServerId(serverId)
        }
        return serverId
    }

    fun setServerId(serverId: String): ConfigManager {
        this.serverId = serverId
        config.set("serverId", serverId) // 同步更新配置
        return this // 支持链式调用
    }

    fun setHashKey(hashKey: String): ConfigManager {
        this.hashKey = hashKey
        config.set("hashKey", hashKey)
        return this
    }

    // 保存配置
    fun save() {
        config.set("serverId", serverId)
        config.set("hashKey", hashKey)
        config.save()
    }

    // 新增迁移方法
    private fun migrateToV3() {
        this.callbackConvertImg = 0

        // 更新版本号
        config.set("version", 3)
        config.save() // 立即保存迁移结果

        version = 3 // 更新内存中的版本号
    }
}

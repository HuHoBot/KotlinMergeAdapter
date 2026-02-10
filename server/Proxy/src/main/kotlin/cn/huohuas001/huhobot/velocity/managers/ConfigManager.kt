package cn.huohuas001.huhobot.velocity.managers

import cn.huohuas001.bot.provider.BotShared
import cn.huohuas001.bot.provider.ChatFormat
import cn.huohuas001.bot.provider.CustomCommandDetail
import cn.huohuas001.bot.provider.Motd
import cn.huohuas001.bot.provider.WhiteList
import cn.huohuas001.bot.tools.getPackID
import cn.huohuas001.huhobot.common.managers.IConfigManager
import cn.huohuas001.huhobot.velocity.HuHoBotVelocity
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.io.IOException
import java.nio.file.Files

class ConfigManager(private val plugin: HuHoBotVelocity) : IConfigManager {

    private val configFile: File = plugin.dataDirectory.resolve("config.yml").toFile()
    private lateinit var config: CommentedConfigurationNode
    private lateinit var loader: YamlConfigurationLoader

    init {
        loadConfig()
    }

    private fun loadConfig() {
        try {
            if (!plugin.dataDirectory.toFile().exists()) {
                Files.createDirectories(plugin.dataDirectory)
            }

            loader = YamlConfigurationLoader.builder()
                .file(configFile)
                .nodeStyle(NodeStyle.BLOCK)
                .build()

            if (!configFile.exists()) {
                configFile.createNewFile()
                config = loader.load()
                setDefaultConfig()
                saveConfig()
            } else {
                config = loader.load()
            }
        } catch (e: IOException) {
            plugin.logger.error("Failed to load config: ${e.message}")
            config = loader.load()
            setDefaultConfig()
        }
    }

    private fun setDefaultConfig() {
        config.node("name").set("HuHoBot")
        config.node("serverId").set("")
        config.node("hashKey").set("")

        config.node("chatFormat", "from_game").set("<{name}> {msg}")
        config.node("chatFormat", "from_group").set("群:<{nick}> {msg}")
        config.node("chatFormat", "post_chat").set(true)
        config.node("chatFormat", "post_prefix").set("")

        config.node("motd", "server_ip").set("play.hypixel.net")
        config.node("motd", "server_port").set(25565)
        config.node("motd", "api").set("https://motdbe.blackbe.work/status_img/java?host={server_ip}:{server_port}")
        config.node("motd", "text").set("共{online}人在线")
        config.node("motd", "output_online_list").set(true)
        config.node("motd", "post_img").set(true)

        config.node("whiteList", "add").set("whitelist add {name}")
        config.node("whiteList", "del").set("whitelist remove {name}")

        config.node("callbackConvertImg").set(0)
        config.node("customCommand").setList(Map::class.java, emptyList())

        // Redis 配置
        config.node("redis", "enabled").set(false)
        config.node("redis", "host").set("localhost")
        config.node("redis", "port").set(6379)
        config.node("redis", "password").set("")
        config.node("redis", "channel").set("HuHoBotChannel")
    }

    private fun saveConfig() {
        try {
            loader.save(config)
        } catch (e: IOException) {
            plugin.logger.error("Failed to save config: ${e.message}")
        }
    }

    override fun loadCommandsFromConfig() {
        val commandsNode = config.node("customCommand")
        val commandMap = HashMap<String, CustomCommandDetail>()

        if (!commandsNode.virtual() && commandsNode.isList) {
            commandsNode.childrenList().forEach { node ->
                val key = node.node("key").getString() ?: return@forEach
                val command = node.node("command").getString() ?: return@forEach
                val permission = node.node("permission").getInt(0)

                commandMap[key] = CustomCommandDetail(key, command, permission)
            }
        }

        BotShared.customCommandMap = commandMap
    }

    override fun reloadConfig(): Boolean {
        loadConfig()
        loadCommandsFromConfig()
        return true
    }

    override fun getName(): String {
        return config.node("name").getString("HuHoBot")!!
    }

    override fun getHashKey(): String {
        return config.node("hashKey").getString("") ?: ""
    }

    override fun setHashKey(hashKey: String) {
        config.node("hashKey").set(hashKey)
        saveConfig()
    }

    override fun getServerId(): String {
        var serverId = config.node("serverId").getString("") ?: ""
        if (serverId.isEmpty()) {
            serverId = getPackID()
            setServerId(serverId)
        }
        return serverId
    }

    override fun setServerId(serverId: String) {
        config.node("serverId").set(serverId)
        saveConfig()
    }

    override fun getChatFormat(): ChatFormat {
        val chatFormat = config.node("chatFormat")
        return ChatFormat(
            chatFormat.node("from_game").getString("<{name}> {msg}")!!,
            chatFormat.node("from_group").getString("群:<{nick}> {msg}")!!,
            chatFormat.node("post_chat").getBoolean(true),
            chatFormat.node("post_prefix").getString("") ?: ""
        )
    }

    override fun getMotd(): Motd {
        val motd = config.node("motd")
        return Motd(
            motd.node("server_ip").getString("play.hypixel.net")!!,
            motd.node("server_port").getInt(25565),
            motd.node("api").getString("")!!,
            motd.node("text").getString("共{online}人在线")!!,
            motd.node("output_online_list").getBoolean(true),
            motd.node("post_img").getBoolean(true)
        )
    }

    override fun getWhiteList(): WhiteList {
        val whiteList = config.node("whiteList")
        return WhiteList(
            whiteList.node("add").getString("whitelist add {name}")!!,
            whiteList.node("del").getString("whitelist remove {name}")!!
        )
    }

    override fun getCallbackConvertImg(): Int {
        return config.node("callbackConvertImg").getInt(0)
    }

    override fun isRedisEnabled(): Boolean {
        return config.node("redis", "enabled").getBoolean(false)
    }

    override fun getRedisHost(): String {
        return config.node("redis", "host").getString("localhost")!!
    }

    override fun getRedisPort(): Int {
        return config.node("redis", "port").getInt(6379)
    }

    override fun getRedisPassword(): String? {
        val password = config.node("redis", "password").getString("")
        return if (password.isNullOrEmpty()) null else password
    }

    override fun getRedisChannel(): String {
        return config.node("redis", "channel").getString("HuHoBotChannel")!!
    }
}

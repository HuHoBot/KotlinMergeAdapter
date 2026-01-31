package cn.huohuas001.huhobot.bungee.managers

import cn.huohuas001.bot.provider.BotShared
import cn.huohuas001.bot.provider.ChatFormat
import cn.huohuas001.bot.provider.CustomCommandDetail
import cn.huohuas001.bot.provider.Motd
import cn.huohuas001.bot.provider.WhiteList
import cn.huohuas001.bot.tools.getPackID
import cn.huohuas001.huhobot.bungee.HuHoBotBungee
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.IOException

class ConfigManager(private val plugin: HuHoBotBungee) {

    private val configFile: File = File(plugin.dataFolder, "config.yml")
    private var config: Configuration = Configuration()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        try {
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }

            if (!configFile.exists()) {
                configFile.createNewFile()
                config = getDefaultConfig()
                saveConfig()
            } else {
                config = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(configFile)
            }
        } catch (e: IOException) {
            plugin.logger.severe("Failed to load config: ${e.message}")
            config = getDefaultConfig()
        }
    }

    private fun getDefaultConfig(): Configuration {
        val defaultConfig = Configuration()
        defaultConfig.set("name", "HuHoBot")
        defaultConfig.set("serverId", "")
        defaultConfig.set("hashKey", "")

        defaultConfig.set("chatFormat.from_game", "<{name}> {msg}")
        defaultConfig.set("chatFormat.from_group", "群:<{nick}> {msg}")
        defaultConfig.set("chatFormat.post_chat", true)
        defaultConfig.set("chatFormat.post_prefix", "")

        defaultConfig.set("motd.server_ip", "play.hypixel.net")
        defaultConfig.set("motd.server_port", 25565)
        defaultConfig.set("motd.api", "https://motdbe.blackbe.work/status_img/java?host={server_ip}:{server_port}")
        defaultConfig.set("motd.text", "共{online}人在线")
        defaultConfig.set("motd.output_online_list", true)
        defaultConfig.set("motd.post_img", true)

        defaultConfig.set("whiteList.add", "whitelist add {name}")
        defaultConfig.set("whiteList.del", "whitelist remove {name}")

        defaultConfig.set("callbackConvertImg", 0)
        defaultConfig.set("customCommand", emptyList<Any>())

        // Redis 配置
        defaultConfig.set("redis.enabled", false)
        defaultConfig.set("redis.host", "localhost")
        defaultConfig.set("redis.port", 6379)
        defaultConfig.set("redis.password", "")
        defaultConfig.set("redis.channel", "HuHoBotChannel")

        return defaultConfig
    }

    private fun saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration::class.java).save(config, configFile)
        } catch (e: IOException) {
            plugin.logger.severe("Failed to save config: ${e.message}")
        }
    }

    fun loadCommandsFromConfig() {
        val commands = config.getList("customCommand") ?: return
        val commandMap = HashMap<String, CustomCommandDetail>()

        for (item in commands) {
            if (item is Map<*, *>) {
                val key = item["key"] as? String ?: continue
                val command = item["command"] as? String ?: continue
                val permission = (item["permission"] as? Number)?.toInt() ?: 0

                commandMap[key] = CustomCommandDetail(key, command, permission)
            }
        }

        BotShared.customCommandMap = commandMap
    }

    fun reloadConfig(): Boolean {
        loadConfig()
        loadCommandsFromConfig()
        return true
    }

    fun getName(): String {
        return config.getString("name", "HuHoBot")
    }

    fun getHashKey(): String {
        return config.getString("hashKey", "")
    }

    fun setHashKey(hashKey: String) {
        config.set("hashKey", hashKey)
        saveConfig()
    }

    fun getServerId(): String {
        var serverId = config.getString("serverId", "")
        if (serverId.isEmpty()) {
            serverId = getPackID()
            setServerId(serverId)
        }
        return serverId
    }

    fun setServerId(serverId: String) {
        config.set("serverId", serverId)
        saveConfig()
    }

    fun getChatFormat(): ChatFormat {
        return ChatFormat(
            config.getString("chatFormat.from_game", "<{name}> {msg}"),
            config.getString("chatFormat.from_group", "群:<{nick}> {msg}"),
            config.getBoolean("chatFormat.post_chat", true),
            config.getString("chatFormat.post_prefix", "")
        )
    }

    fun getMotd(): Motd {
        return Motd(
            config.getString("motd.server_ip", "play.hypixel.net"),
            config.getInt("motd.server_port", 25565),
            config.getString("motd.api", ""),
            config.getString("motd.text", "共{online}人在线"),
            config.getBoolean("motd.output_online_list", true),
            config.getBoolean("motd.post_img", true)
        )
    }

    fun getWhiteList(): WhiteList {
        return WhiteList(
            config.getString("whiteList.add", "whitelist add {name}"),
            config.getString("whiteList.del", "whitelist remove {name}")
        )
    }

    fun getCallbackConvertImg(): Int {
        return config.getInt("callbackConvertImg", 0)
    }

    // Redis 配置
    fun isRedisEnabled(): Boolean {
        return config.getBoolean("redis.enabled", false)
    }

    fun getRedisHost(): String {
        return config.getString("redis.host", "localhost")
    }

    fun getRedisPort(): Int {
        return config.getInt("redis.port", 6379)
    }

    fun getRedisPassword(): String? {
        val password = config.getString("redis.password", "")
        return if (password.isNullOrEmpty()) null else password
    }

    fun getRedisChannel(): String {
        return config.getString("redis.channel", "HuHoBotChannel")
    }
}

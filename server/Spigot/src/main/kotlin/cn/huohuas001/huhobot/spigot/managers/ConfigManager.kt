package cn.huohuas001.huhobot.spigot.manager

import cn.huohuas001.bot.provider.BotShared
import cn.huohuas001.bot.provider.ConfigUpgrader
import cn.huohuas001.bot.provider.CustomCommandDetail
import cn.huohuas001.bot.tools.getPackID
import cn.huohuas001.huhobot.spigot.HuHoBotSpigot
import org.bukkit.configuration.file.FileConfiguration
import java.io.File

class ConfigManager(private val plugin: HuHoBotSpigot) {

    private val configFile: File = File(plugin.dataFolder, "config.yml")
    private val version: Int = 11

    // 配置文件自动升级: 缺失字段 → 默认值
    private val upgradeDefaults: Map<String, Any> = mapOf(
        "postEvent.onJoin.enable" to false,
        "postEvent.onJoin.formatString" to "玩家 {playerName} 加入了服务器",
        "postEvent.onLeft.enable" to false,
        "postEvent.onLeft.formatString" to "玩家 {playerName} 离开了服务器",
        "connectUrl" to "native",
    )

    init {
        if (checkConfig()) {
            migrateConfig()
        }
    }

    fun checkConfig(): Boolean {
        if (configFile.exists()) {
            val config = plugin.config
            val version = if (config.contains("version")) config.getInt("version") else -1
            return this.version > version ||
                    !config.contains("postEvent.onJoin.enable") ||
                    !config.contains("connectUrl")
        }
        return true
    }

    fun migrateConfig() {
        plugin.saveDefaultConfig()
        plugin.reloadConfig()
        val config = plugin.config

        val oldVersion = if (config.contains("version")) config.getInt("version") else -1
        ConfigUpgrader.fillMissing(upgradeDefaults, config::contains, config::set)

        config.set("version", this.version)
        plugin.saveConfig()
        plugin.pluginLogger.info("配置文件已自动升级到版本 $version (旧版本: $oldVersion)")
    }

    fun loadCommandsFromConfig() {
        val config = plugin.getConfig()
        val commands = config.getMapList("customCommand")

        val commandMap = HashMap<String, CustomCommandDetail>()
        for (commandData in commands) {
            val key = commandData["key"] as String
            val command = commandData["command"] as String
            val permission = (commandData["permission"] as Int)
            commandMap[key] = CustomCommandDetail(key, command, permission)
        }

        BotShared.customCommandMap = commandMap
    }

    fun saveConfig(): Boolean {
        plugin.saveConfig()
        return reloadConfig()
    }

    fun reloadConfig(): Boolean {
        plugin.reloadConfig()
        loadCommandsFromConfig()
        plugin.setSender()
        return true
    }

    fun getHashKey(): String {
        return plugin.config.getString("hashKey") ?: ""
    }

    fun setHashKey(hashKey: String) {
        plugin.config.set("hashKey", hashKey)
        plugin.saveConfig()
    }

    fun getServerId(): String {
        val config = plugin.config
        var serverId = config.getString("serverId") ?: ""
        if (serverId.isEmpty()) {
            serverId = getPackID()
            setServerId(serverId)
        }
        return serverId
    }

    fun setServerId(serverId: String) {
        plugin.config.set("serverId", serverId)
        plugin.saveConfig()
    }

    fun getPostEventEnable(eventType: String): Boolean {
        return plugin.config.getBoolean("postEvent.$eventType.enable", false)
    }

    fun getPostEventFormat(eventType: String): String {
        return plugin.config.getString("postEvent.$eventType.formatString", "") ?: ""
    }
}

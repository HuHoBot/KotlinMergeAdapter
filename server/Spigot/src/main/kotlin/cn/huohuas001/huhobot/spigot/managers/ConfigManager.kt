package cn.huohuas001.huhobot.spigot.manager

import cn.huohuas001.bot.provider.BotShared
import cn.huohuas001.bot.provider.CustomCommandDetail
import cn.huohuas001.bot.tools.getPackID
import cn.huohuas001.huhobot.spigot.HuHoBotSpigot
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.collections.get

class ConfigManager(private val plugin: HuHoBotSpigot) {

    private val configFile: File = File(plugin.dataFolder, "config.yml")
    private val oldConfigFile: File = File(plugin.dataFolder, "config_old.yml")
    private val version: Int = 8

    init {
        if (checkConfig()) {
            migrateConfig()
        }
    }

    // 检查是否需要修改配置文件
    fun checkConfig(): Boolean {
        if (configFile.exists()) {
            val version = if (plugin.config.contains("version")) plugin.config.getInt("version") else -1
            return this.version > version || !plugin.config.contains("motd.markdown")
        }
        return true
    }

    private fun mergeOldConfigIntoNewConfig(oldConfig: FileConfiguration, newConfig: FileConfiguration) {
        for (path in newConfig.getKeys(true)) {
            val defaultValue = newConfig.get(path)
            if (defaultValue !is ConfigurationSection && oldConfig.contains(path)) {
                newConfig.set(path, oldConfig.get(path))
            }
        }
    }

    fun migrateConfig() {
        try {
            // 1. 备份旧配置
            if (configFile.exists()) {
                Files.move(configFile.toPath(), oldConfigFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                plugin.pluginLogger.info("已备份旧配置文件至 config_old.yml")
            }

            // 2. 生成新配置文件
            plugin.saveDefaultConfig()
            plugin.reloadConfig()
            val newConfig = plugin.config
            newConfig.set("version", this.version)

            // 3. 迁移旧配置数据
            if (oldConfigFile.exists()) {
                val oldConfig = YamlConfiguration.loadConfiguration(oldConfigFile)
                val oldVersion = if (oldConfig.contains("version")) oldConfig.getInt("version") else -1
                val hadMarkdown = oldConfig.contains("motd.markdown")

                mergeOldConfigIntoNewConfig(oldConfig, newConfig)
                newConfig.set("version", this.version)

                if (!hadMarkdown) {
                    plugin.pluginLogger.info("已添加新的配置项: motd.markdown")
                }

                if (this.version > oldVersion) {
                    plugin.pluginLogger.info("配置文件从版本${oldVersion}升级到${this.version}")
                }
                plugin.pluginLogger.info("配置文件迁移完成")
            } else {
                newConfig.set("version", this.version)
            }

            plugin.saveConfig()
        } catch (e: IOException) {
            plugin.pluginLogger.severe("配置文件迁移失败: ${e.message}")
            plugin.pluginLogger.severe(e.stackTraceToString())
        }
    }

    fun loadCommandsFromConfig() {
        // 从配置文件中读取 commands 列表
        val config = plugin.getConfig()
        val commands = config.getMapList("customCommand")

        // 初始化 commandMap
        val commandMap = HashMap<String, CustomCommandDetail>()
        for (commandData in commands) {
            val key = commandData["key"] as String
            val command = commandData["command"] as String
            val permission = (commandData["permission"] as Int)

            // 创建 CommandObject 并放入 HashMap
            commandMap[key] = CustomCommandDetail(key, command, permission)
        }

        BotShared.customCommandMap = commandMap
    }

    fun saveConfig(): Boolean {
        plugin.saveConfig()
        return reloadConfig()
    }

    fun reloadConfig(): Boolean{
        plugin.reloadConfig()
        loadCommandsFromConfig()
        plugin.setSender()
        return true
    }

    // Getter
    fun getHashKey(): String {
        val config = plugin.config
        return config.getString("hashKey") ?: ""
    }

    // Setter
    fun setHashKey(hashKey: String) {
        val config = plugin.config
        config.set("hashKey", hashKey)
        plugin.saveConfig()
    }

    fun getServerId(): String {
        val config = plugin.config
        var serverId = config.getString("serverId")?:""
        if(serverId.isEmpty()){
            serverId = getPackID()
            setServerId(serverId)
        }
        return serverId
    }

    fun setServerId(serverId: String) {
        val config = plugin.config
        config.set("serverId", serverId)
        plugin.saveConfig()
    }
}

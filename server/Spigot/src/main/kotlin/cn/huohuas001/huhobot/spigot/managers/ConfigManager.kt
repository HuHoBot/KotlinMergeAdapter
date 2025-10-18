package cn.huohuas001.huhobot.spigot.manager

import cn.huohuas001.bot.provider.BotShared
import cn.huohuas001.bot.provider.CustomCommandDetail
import cn.huohuas001.huhobot.spigot.HuHoBotSpigot
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
    private val version: Int = 3

    // 检查是否需要修改配置文件
    fun checkConfig(): Boolean {
        if (configFile.exists()) {
            val version = if (plugin.config.contains("version")) plugin.config.getInt("version") else -1
            return this.version > version
        }
        return true
    }

    // 迁移服务器 URL
    private fun migrateServerUrl(oldConfig: FileConfiguration, newConfig: FileConfiguration) {
        if (oldConfig.contains("motd.server_url") || oldConfig.contains("motdUrl")) {
            val oldUrl = oldConfig.getString("motd.server_url") ?: oldConfig.getString("motdUrl")
            val parts = oldUrl?.split(":")
            val ip = parts?.let { if (it.isNotEmpty()) parts[0] else "localhost" }
            var port = 25565 // 默认端口
            try {
                parts?.let {
                    if (it.size > 1) {
                        port = parts[1].toInt()
                    }
                }
            } catch (e: NumberFormatException) {
                parts?.let { plugin.pluginLogger.warning("无效的端口号: ${it[1]}，已使用默认 25565") }
            }
            newConfig.set("motd.server_ip", ip)
            newConfig.set("motd.server_port", port)
            plugin.pluginLogger.info("已迁移 MOTD 地址: $oldUrl → $ip:$port")
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

                // 使用类型安全的迁移方法
                migrateValue(oldConfig, newConfig, "serverId")
                migrateValue(oldConfig, newConfig, "hashKey")

                // 迁移聊天格式
                migrateNested(oldConfig, newConfig, "chatFormatGame", "chatFormat.from_game")
                migrateNested(oldConfig, newConfig, "chatFormat.from_game", "chatFormat.from_game")
                migrateNested(oldConfig, newConfig, "chatFormatGroup", "chatFormat.from_group")
                migrateNested(oldConfig, newConfig, "chatFormat.from_group", "chatFormat.from_group")
                if (!newConfig.contains("chatFormat.post_chat")) {
                    newConfig.set("chatFormat.post_chat", true) // 设置默认值
                }
                if (!newConfig.contains("chatFormat.post_prefix")) {
                    newConfig.set("chatFormat.post_prefix", "") // 设置默认值
                }

                // 迁移 MOTD 设置
                migrateServerUrl(oldConfig, newConfig)

                // 迁移白名单命令
                migrateNested(oldConfig, newConfig, "addWhiteListCmd", "whiteList.add")
                migrateNested(oldConfig, newConfig, "whiteList.add", "whiteList.add")
                migrateNested(oldConfig, newConfig, "delWhiteListCmd", "whiteList.del")
                migrateNested(oldConfig, newConfig, "whiteList.del", "whiteList.del")

                // 迁移自定义命令（保持结构不变）
                if (oldConfig.isConfigurationSection("customCommand")) {
                    newConfig.set("customCommand", oldConfig.get("customCommand"))
                }

                plugin.pluginLogger.info("配置文件迁移完成")
            }

            plugin.saveConfig()
        } catch (e: IOException) {
            plugin.pluginLogger.severe("配置文件迁移失败: ${e.message}")
            plugin.pluginLogger.severe(e.stackTraceToString())
        }
    }

    private fun migrateValue(oldConfig: FileConfiguration, newConfig: FileConfiguration, path: String) {
        if (oldConfig.contains(path)) {
            val value = oldConfig.get(path)
            if (!newConfig.contains(path)) {
                newConfig.set(path, value)
            }
        }
    }

    private fun migrateNested(oldConfig: FileConfiguration, newConfig: FileConfiguration,
                              oldPath: String, newPath: String) {
        if (oldConfig.contains(oldPath)) {
            val value = oldConfig.get(oldPath)
            if (!newConfig.contains(newPath)) {
                newConfig.set(newPath, value)
            }
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
        plugin.reloadConfig()
        loadCommandsFromConfig()
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
        return config.getString("serverId")?:""
    }

    fun setServerId(serverId: String) {
        val config = plugin.config
        config.set("serverId", serverId)
        plugin.saveConfig()
    }
}

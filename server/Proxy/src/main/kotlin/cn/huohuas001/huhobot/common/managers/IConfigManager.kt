package cn.huohuas001.huhobot.common.managers

import cn.huohuas001.bot.provider.ChatFormat
import cn.huohuas001.bot.provider.Motd
import cn.huohuas001.bot.provider.WhiteList

interface IConfigManager {
    fun getName(): String
    fun getHashKey(): String
    fun setHashKey(hashKey: String)
    fun getServerId(): String
    fun setServerId(serverId: String)
    fun getChatFormat(): ChatFormat
    fun getMotd(): Motd
    fun getWhiteList(): WhiteList
    fun getCallbackConvertImg(): Int
    fun loadCommandsFromConfig()
    fun reloadConfig(): Boolean

    // Redis
    fun isRedisEnabled(): Boolean
    fun getRedisHost(): String
    fun getRedisPort(): Int
    fun getRedisPassword(): String?
    fun getRedisChannel(): String
}
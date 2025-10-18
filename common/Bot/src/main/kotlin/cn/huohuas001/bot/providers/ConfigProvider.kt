package cn.huohuas001.bot.provider

class ChatFormat(
    val fromGame: String,
    val fromGroup: String,
    val postChat: Boolean,
    val postPrefix: String
)

class Motd(
    val serverIP: String,
    val serverPort: Int,
    val api:String,
    val text: String,
    val outputOnlineList: Boolean,
    val postImg: Boolean
)

class WhiteList(
    val addCommand: String,
    val delCommand: String
)

class CustomCommandDetail(
    val key: String,
    val command: String,
    val permission: Int
)

interface ConfigProvider {
    fun getChatFormat(): ChatFormat
    fun getMotd(): Motd

    fun getServerId(): String
    fun setServerId(serverId: String)

    fun getHashKey(): String?
    fun setHashKey(hashKey: String)

    fun getName():String
    fun getPlatform(): String
    fun getPluginVersion(): String

    fun isHashKeyValue(): Boolean{
        val hashKey: String? = getHashKey()
        return hashKey != null && !hashKey.isEmpty()
    }

    fun loadCustomCommand()
}
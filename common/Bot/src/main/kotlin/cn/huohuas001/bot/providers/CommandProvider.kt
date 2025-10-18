package cn.huohuas001.bot.provider

interface CommandProvider {
    fun sendCommand(command: String): String
}
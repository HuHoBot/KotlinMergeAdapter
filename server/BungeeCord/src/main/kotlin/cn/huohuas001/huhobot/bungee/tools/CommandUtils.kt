package cn.huohuas001.huhobot.bungee.tools

/**
 * 命令解析结果
 * @property serverName 目标服务器名称，如果没有指定则为 null
 * @property command 实际要执行的命令
 */
data class ParsedCommand(
    val serverName: String?,
    val command: String
)

object CommandUtils {

    /**
     * 分割命令字符串，提取目标服务器和具体命令
     * 规则：以第一个冒号 ":" 为分隔符
     *
     * 示例:
     * "lobby:say hello" -> ParsedCommand("lobby", "say hello")
     * "survival:give player stone:1" -> ParsedCommand("survival", "give player stone:1")
     * "list" -> ParsedCommand(null, "list")
     *
     * @param rawCommand 原始命令字符串
     * @return 解析后的命令对象
     */
    fun splitCommand(rawCommand: String): ParsedCommand {
        val parts = rawCommand.split(":", limit = 2)
        return if (parts.size == 2) {
            // 确保 serverName 不为空
            val serverName = parts[0].trim()
            val command = parts[1] // 命令部分可以保留前导空格，视需求而定，通常 trim 一下较好

            if (serverName.isEmpty()) {
                ParsedCommand(null, rawCommand)
            } else {
                ParsedCommand(serverName, command)
            }
        } else {
            ParsedCommand(null, rawCommand)
        }
    }
}

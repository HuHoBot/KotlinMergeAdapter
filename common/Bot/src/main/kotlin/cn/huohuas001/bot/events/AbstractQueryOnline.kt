package cn.huohuas001.bot.events

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.bot.HuHoBot

/**
 * 在线玩家查询事件的抽象基类。
 * 子类只需实现 [getPlugin] 和 [getOnlinePlayerNames]。
 */
abstract class AbstractQueryOnline : BaseEvent() {

    /**
     * 获取插件实例。
     */
    protected abstract fun getPlugin(): HuHoBot

    /**
     * 获取在线玩家名称列表（平台相关）。
     */
    protected abstract fun getOnlinePlayerNames(): List<String>

    /**
     * 对文本进行处理（如 PlaceholderAPI 替换），默认不处理。
     */
    protected open fun processText(text: String): String = text

    override fun run(): Boolean {
        val plugin = getPlugin()
        val motd = plugin.getMotd()
        val sb = StringBuilder()

        val playerNames = getOnlinePlayerNames()

        if (motd.outputOnlineList) {
            if (playerNames.isNotEmpty()) {
                sb.append("\n在线玩家列表：\n")
                for (name in playerNames) {
                    sb.append(name).append("\n")
                }
            } else {
                sb.append("\n当前没有在线玩家\n")
            }
        }

        val onlineSize = if (motd.outputOnlineList) playerNames.size else -1
        sb.append(processText(motd.text.replace("{online}", onlineSize.toString())))

        ClientManager.postMotd(sb.toString(), mPackId)
        return true
    }
}

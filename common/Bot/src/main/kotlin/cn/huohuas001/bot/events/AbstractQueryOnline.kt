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
        val playerNames = getOnlinePlayerNames()
        val textTemplate = processText(motd.text)
        ClientManager.postMotd(playerNames, textTemplate, mPackId)
        return true
    }
}

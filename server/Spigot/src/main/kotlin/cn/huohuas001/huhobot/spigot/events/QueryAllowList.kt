package cn.huohuas001.huhobot.spigot.events

import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.bot.tools.CollectionUtils
import cn.huohuas001.huhobot.spigot.HuHoBotSpigot
import cn.huohuas001.huhobot.spigot.api.BotQueryWhiteList
import com.alibaba.fastjson2.JSONObject
import org.bukkit.Bukkit

class QueryAllowList(val plugin: HuHoBotSpigot): BaseEvent() {

    fun callEvent(): Boolean {
        val orWhitelist = plugin.server.whitelistedPlayers
        val whiteList: Set<String> = orWhitelist.mapNotNull { it?.name }.toSet()

        // 根据参数类型创建事件
        val event: BotQueryWhiteList = createEvent()
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return false // 事件被取消时中断处理
        }

        // 保留原有处理流程
        val rBody = JSONObject()
        val content = StringBuilder()

        if (mBody.containsKey("key")) {
            handleKeyword(event, whiteList, content)
        } else if (mBody.containsKey("page")) {
            handlePage(event, whiteList, content)
        } else {
            handleDefault(event, whiteList)
        }

        rBody["list"] = content.toString()
        sendMessage("queryWl", rBody)
        return true
    }

    private fun createEvent(): BotQueryWhiteList {
        if (mBody.containsKey("key")) {
            val key: String? = mBody.getString("key")
            return BotQueryWhiteList.createKeywordEvent(key, mPackId)
        } else if (mBody.containsKey("page")) {
            val page: Int = mBody.getInteger("page")
            return BotQueryWhiteList.createPageEvent(page, mPackId)
        }
        return BotQueryWhiteList.createPageEvent(1, mPackId) // 默认第一页
    }

    private fun handleKeyword(event: BotQueryWhiteList, whitelist: Set<String>, sb: StringBuilder) {
        val key = event.keyWord
        if (key.length < 2) {
            sb.append("请使用两个字母及以上的关键词进行查询!")
            return
        }

        val results: List<String> = CollectionUtils.searchInSet(whitelist, key)
        event.responseList(results, 0) // 通过事件发送响应
    }

    private fun handlePage(event: BotQueryWhiteList, whitelist: Set<String>, sb: StringBuilder) {
        val page = event.pages
        val pages: List<List<String>> = CollectionUtils.chunkSet(whitelist, 10)

        if (page - 1 >= pages.size) {
            sb.append("没有该页码\n")
        }

        val currentPage = pages[page - 1]
        event.responseList(currentPage, pages.size) // 通过事件发送响应
    }

    private fun handleDefault(event: BotQueryWhiteList, whitelist: Set<String>) {
        val pages: List<List<String>> = CollectionUtils.chunkSet(whitelist, 10)
        event.responseList(pages[0], pages.size) // 默认发送第一页
    }

    override fun run(): Boolean {
        plugin.submit { callEvent() }
        return true
    }
}

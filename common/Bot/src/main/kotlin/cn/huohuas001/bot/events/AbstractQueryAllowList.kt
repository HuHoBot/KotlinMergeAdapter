package cn.huohuas001.bot.events

import cn.huohuas001.bot.tools.CollectionUtils
import com.alibaba.fastjson2.JSONObject

/**
 * 白名单查询事件的抽象基类。
 * 子类只需实现 [getWhiteList] 提供白名单数据。
 */
abstract class AbstractQueryAllowList : BaseEvent() {

    /**
     * 获取白名单玩家名称集合（平台相关）。
     */
    protected abstract fun getWhiteList(): Set<String>

    override fun run(): Boolean {
        val whiteList = getWhiteList()
        val sb = StringBuilder()

        if (mBody.containsKey("key")) {
            handleKeyword(whiteList, sb)
        } else if (mBody.containsKey("page")) {
            handlePage(whiteList, sb)
        } else {
            handleDefault(whiteList, sb)
        }

        val rBody = JSONObject()
        rBody["list"] = sb.toString()
        sendMessage("queryWl", rBody)
        return true
    }

    private fun handleKeyword(whiteList: Set<String>, sb: StringBuilder) {
        val key: String = mBody.getString("key")
        sb.append("查询白名单关键词:").append(key).append("结果如下:\n")
        if (key.length < 2) {
            sb.append("请使用两个字母及以上的关键词进行查询!")
            return
        }
        val filterList = CollectionUtils.searchInSet(whiteList, key)
        if (filterList.isEmpty()) {
            sb.append("无结果\n")
        } else {
            for (name in filterList) {
                sb.append(name).append("\n")
            }
            sb.append("共有").append(filterList.size).append("个结果")
        }
    }

    private fun handlePage(whiteList: Set<String>, sb: StringBuilder) {
        val page: Int = mBody.getInteger("page")
        sb.append("服内白名单如下:\n")
        val pages = CollectionUtils.chunkSet(whiteList, 10)
        if (page < 1 || page > pages.size) {
            sb.append("没有该页码\n")
            sb.append("共有").append(pages.size).append("页\n请使用/查白名单 {页码}来翻页")
            return
        }
        val currentPage = pages[page - 1]
        for (name in currentPage) {
            sb.append(name).append("\n")
        }
        sb.append("共有").append(pages.size).append("页，当前为第").append(page)
            .append("页\n请使用/查白名单 {页码}来翻页")
    }

    private fun handleDefault(whiteList: Set<String>, sb: StringBuilder) {
        sb.append("服内白名单如下:\n")
        val pages = CollectionUtils.chunkSet(whiteList, 10)
        if (pages.isEmpty()) {
            sb.append("无结果\n")
        } else {
            for (name in pages[0]) {
                sb.append(name).append("\n")
            }
        }
        sb.append("共有").append(pages.size).append("页，当前为第1页\n请使用/查白名单 {页码}来翻页")
    }
}

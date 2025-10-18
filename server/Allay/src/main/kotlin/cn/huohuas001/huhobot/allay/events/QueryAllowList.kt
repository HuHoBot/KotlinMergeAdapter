package cn.huohuas001.huhobot.allay.events

import cn.huohuas001.bot.events.BaseEvent
import cn.huohuas001.huhobot.allay.utils.CollectionUtils
import com.alibaba.fastjson2.JSONObject
import org.allaymc.api.server.Server

class QueryAllowList : BaseEvent() {
    override fun run(): Boolean {
        val whiteList = Server.getInstance().playerManager.whitelistedPlayers
        val whitelistNameString = StringBuilder()
        val rBody = JSONObject()
        if (mBody.containsKey("key")) {
            val key: String = mBody.getString("key")
            if (key.length < 2) {
                whitelistNameString.append("查询白名单关键词:").append(key).append("结果如下:\n")
                whitelistNameString.append("请使用两个字母及以上的关键词进行查询!")
                rBody["list"] = whitelistNameString
                sendMessage("queryWl", rBody)
                return true
            }
            whitelistNameString.append("查询白名单关键词:").append(key).append("结果如下:\n")
            val filterList: MutableList<String?> = CollectionUtils.searchInSet(whiteList, key)
            if (filterList.isEmpty()) {
                whitelistNameString.append("无结果\n")
            } else {
                for (plName in filterList) {
                    whitelistNameString.append(plName).append("\n")
                }
                whitelistNameString.append("共有").append(filterList.size).append("个结果")
            }
            rBody["list"] = whitelistNameString
            sendMessage("queryWl", rBody)
        } else if (mBody.containsKey("page")) {
            val page: Int = mBody.getInteger("page")
            whitelistNameString.append("服内白名单如下:\n")
            val splitedNameList: MutableList<MutableList<String?>> = CollectionUtils.chunkSet(whiteList, 10)
            val currentNameList = splitedNameList.get(page - 1)
            if (page - 1 > splitedNameList.size) {
                whitelistNameString.append("没有该页码\n")
                whitelistNameString.append("共有").append(splitedNameList.size)
                    .append("页\n请使用/查白名单 {页码}来翻页")
            } else {
                for (plName in currentNameList) {
                    whitelistNameString.append(plName).append("\n")
                }
                whitelistNameString.append("共有").append(splitedNameList.size).append("页，当前为第").append(page)
                    .append("页\n请使用/查白名单 {页码}来翻页")
            }
            rBody["list"] = whitelistNameString
            sendMessage("queryWl", rBody)
        } else {
            whitelistNameString.append("服内白名单如下:\n")
            val splitedNameList: MutableList<MutableList<String?>> = CollectionUtils.chunkSet(whiteList, 10)
            if (splitedNameList.isEmpty()) {
                whitelistNameString.append("无结果\n")
            } else {
                val currentNameList = splitedNameList.get(0)
                for (plName in currentNameList) {
                    whitelistNameString.append(plName).append("\n")
                }
            }
            whitelistNameString.append("共有").append(splitedNameList.size)
                .append("页，当前为第1页\n请使用/查白名单 {页码}来翻页")
            rBody["list"] = whitelistNameString
            sendMessage("queryWl", rBody)
        }
        return true
    }
}
package cn.huohuas001.huhobot.mod.tools

import net.minecraft.server.players.UserWhiteList

object CollectionUtils {
    /**
     * 将一个Set集合转换为List，然后分割成多个子List。
     *
     * @param set  要分割的Set集合。
     * @param size 每个子List的最大容量。
     * @return 分割后的List列表。
     */
    fun <T> chunkSet(set: Set<T>, size: Int): List<List<T>> {
        val list = ArrayList<T>(set)
        return chunkList(list, size)
    }

    fun chuckUserList(list: UserWhiteList, size: Int): List<List<String>>{
        val userList = list.userList
        return chunkList(userList.toList(), size)
    }

    private fun <T> chunkList(list: List<T>, size: Int): List<List<T>> {
        val chunks = ArrayList<List<T>>()
        for (i in 0 until list.size step size) {
            val chunk = list.subList(i, (i + size).coerceAtMost(list.size))
            chunks.add(chunk)
        }
        return chunks
    }

    /**
     * 在Set中搜索包含指定关键字的元素。
     *
     * @param set     要搜索的Set集合。
     * @param keyword 关键字。
     * @return 包含关键字的元素列表。
     */
    fun searchInSet(set: Set<String>, keyword: String): List<String> {
        return set.filter { it.contains(keyword) }.toList()
    }

    fun searchInUserWhiteList(list: UserWhiteList, keyword: String): List<String> {
        val userList = list.userList
        return searchInSet(userList.toSet(), keyword)
    }
}

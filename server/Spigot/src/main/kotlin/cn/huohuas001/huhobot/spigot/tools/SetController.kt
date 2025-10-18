package cn.huohuas001.huhobot.spigot.tools

import org.bukkit.OfflinePlayer

object SetController {
    /**
     * 将一个Set集合转换为List，然后分割成多个子List。
     *
     * @param set  要分割的Set集合。
     * @param size 每个子List的最大容量。
     * @return 分割后的List列表。
     */
    fun chunkSet(set: Set<String>, size: Int): List<List<String>> {
        // 将Set转换为List
        val list = set.toList()
        // 使用chunkList方法进行分片
        return chunkList(list, size)
    }

    private fun chunkList(list: List<String>, size: Int): List<List<String>> {
        val chunks = mutableListOf<List<String>>()
        for (i in 0 until list.size step size) {
            val chunk = list.subList(i, minOf(i + size, list.size))
            chunks.add(chunk)
        }
        return chunks
    }

    /**
     * 将Set<OfflinePlayer>转换为Set<String>，其中包含玩家的名字。
     *
     * @param offlinePlayers 要转换的Set<OfflinePlayer>。
     * @return 包含玩家名字的Set<String>。
     */
    fun convertToPlayerNames(offlinePlayers: MutableSet<OfflinePlayer?>): Set<String> {
        return offlinePlayers.mapNotNull { it?.name }.toSet()
    }

    fun searchInSet(set: Set<String>, keyword: String): List<String> {
        return set.filter { it.contains(keyword) }
    }


}

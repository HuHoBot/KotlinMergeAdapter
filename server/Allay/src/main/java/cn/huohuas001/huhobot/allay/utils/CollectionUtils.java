package cn.huohuas001.huhobot.allay.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class CollectionUtils {
    /**
     * 将一个Set集合转换为List，然后分割成多个子List。
     *
     * @param set  要分割的Set集合。
     * @param size 每个子List的最大容量。
     * @param <T>  Set集合中元素的类型。
     * @return 分割后的List列表。
     */
    public static <T> List<List<T>> chunkSet(Set<T> set, int size) {
        // 将Set转换为List
        var list = new ArrayList<>(set);
        // 使用chunkList方法进行分片
        return chunkList(list, size);
    }

    private static <T> List<List<T>> chunkList(List<T> list, int size) {
        var chunks = new ArrayList<List<T>>();
        for (int i = 0; i < list.size(); i += size) {
            var chunk = list.subList(i, Math.min(i + size, list.size()));
            chunks.add(chunk);
        }
        return chunks;
    }

    public static List<String> searchInSet(Set<String> set, String keyword) {
        return set.stream()
                .filter(s -> s.contains(keyword))
                .collect(Collectors.toList());
    }
}

package cn.huohuas001.huhobot.spigot.api;

import cn.huohuas001.bot.ClientManager;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BotQueryWhiteList extends Event implements Cancellable {
    /**
     * Internal use by Bukkit.
     */
    private static final HandlerList HANDLERS = new HandlerList();
    private final String key;
    private final int pages;
    private final String packId;
    private final QueryMode param;
    /**
     * 取消状态
     */
    private boolean isCancelled = false;
    public BotQueryWhiteList(
            String keyWord,
            int pages,
            QueryMode param,
            String packId) {
        super(false);

        this.key = keyWord;
        this.pages = pages;
        this.param = param;
        this.packId = packId;
    }

    // 新增便捷构造方法
    public static BotQueryWhiteList createKeywordEvent(String keyword, String packId) {
        return new BotQueryWhiteList(keyword, 0, QueryMode.KEYWORD_SEARCH, packId);
    }

    public static BotQueryWhiteList createPageEvent(int page, String packId) {
        return new BotQueryWhiteList(null, page, QueryMode.PAGE_QUERY, packId);
    }

    /**
     * Internal use by Bukkit.
     *
     * @return event's handler list.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public String getKeyWord() {
        return this.key;
    }

    public int getPages() {
        return this.pages;
    }

    public QueryMode getQueryMode() {
        return this.param;
    }

    private String formatResponse(List<String> results, int totalPages) {
        StringBuilder sb = new StringBuilder();

        switch (param) {
            case KEYWORD_SEARCH:
                sb.append("查询白名单关键词:").append(key).append("结果如下:\n");
                if (results.isEmpty()) {
                    sb.append("无结果\n");
                } else {
                    results.forEach(name -> sb.append(name).append("\n"));
                    sb.append("共有").append(results.size()).append("个结果");
                }
                break;

            case PAGE_QUERY:
                sb.append("服内白名单如下:\n");
                if (results.isEmpty()) {
                    sb.append("无结果\n");
                } else {
                    results.forEach(name -> sb.append(name).append("\n"));
                }
                sb.append("共有").append(totalPages).append("页，当前为第")
                        .append(pages).append("页\n请使用/查白名单 {页码}来翻页");
                break;
        }

        return sb.toString();
    }

    // 新增响应方法重载
    public void responseList(List<String> results, int totalPages) {
        String formatted = formatResponse(results, totalPages);
        responeString(formatted);
    }

    public void responeString(String whiteList) {
        ClientManager.INSTANCE.postList(whiteList,packId);
    }

    public enum QueryMode {
        KEYWORD_SEARCH,
        PAGE_QUERY
    }
}

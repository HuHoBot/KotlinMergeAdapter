package cn.huohuas001.huhobot.spigot.api;

import cn.huohuas001.bot.ClientManager;
import com.alibaba.fastjson2.JSONObject;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class BotCustomCommand extends Event implements Cancellable {

    /**
     * Internal use by Bukkit.
     */
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * 取消状态
     */
    private boolean isCancelled = false;

    private final String command;
    private final JSONObject data;
    private final List<String> param;
    private final String packId;
    private final boolean runByAdmin;

    public BotCustomCommand(String command, JSONObject data, String packId, boolean runByAdmin) {
        super(false);

        this.command = command;
        this.data = data;
        this.param = data.getList("runParams", String.class);
        this.packId = packId;
        this.runByAdmin = runByAdmin;
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
    public HandlerList getHandlers()
    {
        return HANDLERS;
    }

    /**
     * Internal use by Bukkit.
     *
     * @return event's handler list.
     */
    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }

    public String getCommand() {
        return this.command;
    }

    public List<String> getParam() {
        return this.param;
    }

    public boolean isRunByAdmin(){
        return this.runByAdmin;
    }

    public JSONObject getData() {
        return this.data;
    }

    public void respone(String msg, String type) {
        ClientManager.INSTANCE.postRespone(msg, type, packId);
    }

    public void respone(JSONObject msg, String type) {
        ClientManager.INSTANCE.postRespone(msg.toJSONString(), type, packId);
    }
}

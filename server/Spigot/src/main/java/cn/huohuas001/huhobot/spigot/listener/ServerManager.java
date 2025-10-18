package cn.huohuas001.huhobot.spigot.listener;

import cn.huohuas001.huhobot.spigot.HuHoBotSpigot;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ServerManager {
    private HuHoBotSpigot plugin;

    public ServerManager(HuHoBotSpigot plugin) {
        this.plugin = plugin;
    }

    public static List<String> msgList = new LinkedList<>();

    public String sendCmd(String cmd, boolean disp) {
        AtomicReference<String> returnStr = new AtomicReference<>("无返回值");

        CommandSender commandSender = new ConsoleSender();

        plugin.scheduler.runTask(() -> {
            msgList.clear();
            Bukkit.dispatchCommand(commandSender, cmd);
        });

        plugin.scheduler.runTaskLaterAsynchronously(() -> {
            synchronized (returnStr) {
                returnStr.notify();
                StringBuilder stringBuilder = new StringBuilder();
                if (msgList.isEmpty()) {
                    msgList.add("无返回值");
                }
                for (String msg : msgList) {
                    if (msgList.get(msgList.size() - 1).equalsIgnoreCase(msg)) {
                        stringBuilder.append(msg);
                    } else {
                        stringBuilder.append(msg).append("\n");
                    }
                }
                if (!disp) {
                    msgList.clear();
                    returnStr.set("无返回值");
                }
                if (stringBuilder.toString().length() <= 5000) {
                    returnStr.set(stringBuilder.toString());
                } else {
                    returnStr.set("返回值过长");
                }
                msgList.clear();
            }
        }, 20L);

        synchronized (returnStr){
            try {
                returnStr.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return returnStr.get();
        }
    }
}

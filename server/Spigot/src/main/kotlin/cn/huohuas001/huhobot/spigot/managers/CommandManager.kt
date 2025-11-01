package cn.huohuas001.huhobot.spigot.manager

import cn.huohuas001.bot.events.BindRequest
import cn.huohuas001.huhobot.spigot.HuHoBotSpigot
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandManager(val plugin: HuHoBotSpigot) : CommandExecutor {

    private fun onReload(sender: CommandSender, args: Array<String>) {
        if (sender is Player && !sender.isOp) {
            sender.sendMessage("${ChatColor.DARK_RED}你没有足够的权限.")
            return
        }
        if (plugin.configManager.reloadConfig()) {
            sender.sendMessage("${ChatColor.AQUA}重载机器人配置文件成功.")
        }
    }

    private fun onReconnect(sender: CommandSender, args: Array<String>) {
        if (sender is Player && !sender.isOp) {
            sender.sendMessage("${ChatColor.DARK_RED}你没有足够的权限.")
            return
        }
        if (plugin.reconnect()) {
            sender.sendMessage("${ChatColor.GOLD}重连机器人成功.")
        } else {
            sender.sendMessage("${ChatColor.DARK_RED}重连机器人失败：已在连接状态.")
        }
    }

    private fun onDisconnect(sender: CommandSender, args: Array<String>) {
        if (sender is Player && !sender.isOp) {
            sender.sendMessage("${ChatColor.DARK_RED}你没有足够的权限.")
            return
        }
        if (plugin.disConnectServer()) {
            sender.sendMessage("${ChatColor.GOLD}已断开机器人连接.")
        }
    }

    private fun onBind(sender: CommandSender, args: Array<String>) {
        val obj: BindRequest = plugin.bindRequestObj
        if (obj.confirmBind(args[1])) {
            sender.sendMessage("${ChatColor.GOLD}已向服务器发送确认绑定请求，请等待服务端下发配置文件.")
        } else {
            sender.sendMessage("${ChatColor.DARK_RED}绑定码错误，请重新输入.")
        }
    }

    private fun onHelp(sender: CommandSender, args: Array<String>) {
        sender.sendMessage("${ChatColor.AQUA}HuHoBot 操作相关命令")
        sender.sendMessage("${ChatColor.GOLD}>${ChatColor.DARK_GRAY}/huhobot reload - 重载配置文件")
        sender.sendMessage("${ChatColor.GOLD}>${ChatColor.DARK_GRAY}/huhobot reconnect - 重新连接服务器")
        sender.sendMessage("${ChatColor.GOLD}>${ChatColor.DARK_GRAY}/huhobot disconnect - 断开服务器连接")
        sender.sendMessage("${ChatColor.GOLD}>${ChatColor.DARK_GRAY}/huhobot bind <bindCode:string> - 确认绑定")
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        val keyWord = if (args.isNotEmpty()) args[0] else ""
        when (keyWord) {
            "reload" -> onReload(sender, args)
            "reconnect" -> onReconnect(sender, args)
            "disconnect" -> onDisconnect(sender, args)
            "bind" -> onBind(sender, args)
            "help" -> onHelp(sender, args)
            else -> sender.sendMessage("${ChatColor.DARK_RED}使用/huhobot help来获取更多详情")
        }
        return true
    }
}
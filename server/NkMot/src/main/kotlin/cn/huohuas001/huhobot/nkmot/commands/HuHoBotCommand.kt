package cn.huohuas001.huhobot.nkmot.commands

import cn.huohuas001.huhobot.nkmot.HuHoBotNkMot
import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.PluginCommand
import cn.nukkit.command.data.CommandEnum
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter
import cn.nukkit.utils.TextFormat

class HuHoBotCommand(private val plugin: HuHoBotNkMot) : PluginCommand<HuHoBotNkMot>("huhobot", plugin) {

    init {
        this.description = "HuHoBot的控制命令"
        this.commandParameters.clear()

        this.commandParameters["pattern1"] = arrayOf(
            CommandParameter.newEnum("enum1", false, CommandEnum("reload", "reconnect", "disconnect"))
        )
        this.commandParameters["pattern2"] = arrayOf(
            CommandParameter.newEnum("enum2", false, arrayOf("bind")),
            CommandParameter.newType("bindCode", true, CommandParamType.MESSAGE)
        )
    }

    private fun onReload(sender: CommandSender, args: Array<String>) {
        if (sender is Player) {
            if (!sender.isOp) {
                sender.sendMessage(TextFormat.DARK_RED.toString() + "你没有足够的权限.")
                return
            }
        }

        if (plugin.reloadBotConfig()) {
            sender.sendMessage(TextFormat.AQUA.toString() + "重载机器人配置文件成功.")
        }
    }

    private fun onReconnect(sender: CommandSender, args: Array<String>) {
        if (sender is Player) {
            if (!sender.isOp) {
                sender.sendMessage(TextFormat.DARK_RED.toString() + "你没有足够的权限.")
                return
            }
        }

        if (plugin.reconnect()) {
            sender.sendMessage(TextFormat.GOLD.toString() + "重连机器人成功.")
        } else {
            sender.sendMessage(TextFormat.DARK_RED.toString() + "重连机器人失败：已在连接状态.")
        }
    }

    private fun onDisconnect(sender: CommandSender, args: Array<String>) {
        if (sender is Player) {
            if (!sender.isOp) {
                sender.sendMessage(TextFormat.DARK_RED.toString() + "你没有足够的权限.")
                return
            }
        }

        if (plugin.disConnectServer()) {
            sender.sendMessage(TextFormat.GOLD.toString() + "已断开机器人连接.")
        }
    }

    private fun onBind(sender: CommandSender, args: Array<String>) {
        val obj = plugin.bindRequestObj
        if (obj.confirmBind(args[1])) {
            sender.sendMessage(TextFormat.GOLD.toString() + "已向服务器发送确认绑定请求，请等待服务端下发配置文件.")
        } else {
            sender.sendMessage(TextFormat.DARK_RED.toString() + "绑定码错误，请重新输入.")
        }
    }

    private fun onHelp(sender: CommandSender, args: Array<String>) {
        sender.sendMessage(TextFormat.AQUA.toString() + "HuHoBot 操作相关命令")
        sender.sendMessage(TextFormat.GOLD.toString() + ">" + TextFormat.DARK_GRAY + "/huhobot reload - 重载配置文件")
        sender.sendMessage(TextFormat.GOLD.toString() + ">" + TextFormat.DARK_GRAY + "/huhobot reconnect - 重新连接服务器")
        sender.sendMessage(TextFormat.GOLD.toString() + ">" + TextFormat.DARK_GRAY + "/huhobot disconnect - 断开服务器连接")
        sender.sendMessage(TextFormat.GOLD.toString() + ">" + TextFormat.DARK_GRAY + "/huhobot bind <bindCode:string> - 确认绑定")
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("使用/huhobot help来获取更多详情")
            return false
        }
        when (args[0]) {
            "reload" -> onReload(sender, args)
            "reconnect" -> onReconnect(sender, args)
            "disconnect" -> onDisconnect(sender, args)
            "bind" -> onBind(sender, args)
            "help" -> onHelp(sender, args)
            else -> sender.sendMessage(TextFormat.DARK_RED.toString() + "使用/huhobot help来获取更多详情")
        }
        return true
    }
}

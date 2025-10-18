package cn.huohuas001.huhobot.allay.commands


import cn.huohuas001.huhobot.allay.HuHoBotAllay
import org.allaymc.api.command.Command
import org.allaymc.api.command.tree.CommandTree
import org.allaymc.api.permission.PermissionGroups
import org.allaymc.api.utils.TextFormat

class HuHoBotCommand(private val plugin: HuHoBotAllay) : Command("huhobot", "HuHoBot's control command") {
    init {
        permissions.forEach { PermissionGroups.OPERATOR.addPermission(it) }
    }

    override fun prepareCommandTree(tree: CommandTree) {
        tree.root
            .key("reconnect")
            .exec { context ->
                if (plugin.reconnect()) {
                    context.addOutput(TextFormat.GOLD.toString() + "重连机器人成功.")
                } else {
                    context.addOutput(TextFormat.DARK_RED.toString() + "重连机器人失败：已在连接状态.")
                }
                context.success()
            }
            .root()
            .key("disconnect")
            .exec { context ->
                if (plugin.disConnectServer()) {
                    context.addOutput(TextFormat.GOLD.toString() + "已断开机器人连接.")
                }
                context.success()
            }
            .root()
            .key("bind")
            .str("code")
            .exec { context ->
                val code = context.getResult<String>(1)
                val obj = plugin.bindRequestObj
                if (obj.confirmBind(code)) {
                    context.addOutput(TextFormat.GOLD.toString() + "已向服务器发送确认绑定请求，请等待服务端下发配置文件.")
                } else {
                    context.addOutput(TextFormat.DARK_RED.toString() + "绑定码错误，请重新输入.")
                }
                context.success()
            }
            .root()
            .key("help")
            .exec { context ->
                context.addOutput(TextFormat.AQUA.toString() + "HuHoBot 操作相关命令")
                context.addOutput(TextFormat.GOLD.toString() + ">" + TextFormat.DARK_GRAY + "/huhobot reconnect - 重新连接服务器")
                context.addOutput(TextFormat.GOLD.toString() + ">" + TextFormat.DARK_GRAY + "/huhobot disconnect - 断开服务器连接")
                context.addOutput(TextFormat.GOLD.toString() + ">" + TextFormat.DARK_GRAY + "/huhobot reload - 重载配置文件")
                context.addOutput(TextFormat.GOLD.toString() + ">" + TextFormat.DARK_GRAY + "/huhobot bind <code:String> - 确认绑定")
                context.success()
            }
            .root()
            .key("reload")
            .exec { context ->
                plugin.reloadConfig()
                context.addOutput(TextFormat.GOLD.toString() + "已重载配置文件.")
                context.success()
            }
    }
}

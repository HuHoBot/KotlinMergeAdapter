package cn.huohuas001.huhobot.bungee.commands

import cn.huohuas001.bot.ClientManager
import cn.huohuas001.huhobot.bungee.HuHoBotBungee
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.TabExecutor

class HuHoBotCommand(private val plugin: HuHoBotBungee) : Command("huhobot", "huhobot.use", "hb"), TabExecutor {

    private fun CommandSender.sendMsg(msg: String) {
        sendMessage(*arrayOf(TextComponent(msg)))
    }

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (args.isEmpty()) {
            showHelp(sender)
            return
        }

        when (args[0].lowercase()) {
            "reload" -> {
                if (!sender.hasPermission("huhobot.reload")) {
                    sender.sendMsg("${ChatColor.RED}你没有权限执行此命令")
                    return
                }
                plugin.configManager.reloadConfig()
                sender.sendMsg("${ChatColor.GREEN}配置文件已重新加载")
            }
            "reconnect" -> {
                if (!sender.hasPermission("huhobot.reconnect")) {
                    sender.sendMsg("${ChatColor.RED}你没有权限执行此命令")
                    return
                }
                if (plugin.reconnect()) {
                    sender.sendMsg("${ChatColor.GREEN}正在尝试重新连接...")
                } else {
                    sender.sendMsg("${ChatColor.YELLOW}连接已存在，无需重连")
                }
            }
            "disconnect" -> {
                if (!sender.hasPermission("huhobot.disconnect")) {
                    sender.sendMsg("${ChatColor.RED}你没有权限执行此命令")
                    return
                }
                if (plugin.disConnectServer()) {
                    sender.sendMsg("${ChatColor.GREEN}已断开连接")
                } else {
                    sender.sendMsg("${ChatColor.RED}断开连接失败")
                }
            }
            "status" -> {
                if (!sender.hasPermission("huhobot.status")) {
                    sender.sendMsg("${ChatColor.RED}你没有权限执行此命令")
                    return
                }
                val status = if (ClientManager.isOpen()) "已连接" else "未连接"
                val redisStatus = if (plugin.redisManager?.isConnected() == true) "已连接" else "未连接"
                sender.sendMsg("${ChatColor.AQUA}HuHoBot 状态: $status")
                sender.sendMsg("${ChatColor.AQUA}Redis 状态: $redisStatus")
                sender.sendMsg("${ChatColor.AQUA}服务器ID: ${plugin.getServerId()}")
                sender.sendMsg("${ChatColor.AQUA}版本: ${plugin.getPluginVersion()}")
            }
            "bind" -> {
                if (!sender.hasPermission("huhobot.bind")) {
                    sender.sendMsg("${ChatColor.RED}你没有权限执行此命令")
                    return
                }
                if (args.size < 2) {
                    sender.sendMsg("${ChatColor.YELLOW}用法: /huhobot bind <code|server_id>")
                    return
                }
                val bindCode = args[1]
                if (plugin.bindRequestObj.confirmBind(bindCode)) {
                    sender.sendMsg("${ChatColor.GREEN}绑定成功！")
                } else {
                    sender.sendMsg("${ChatColor.RED}无效的绑定码或绑定请求已过期")
                }
            }
            "redis" -> {
                if (!sender.hasPermission("huhobot.redis")) {
                    sender.sendMsg("${ChatColor.RED}你没有权限执行此命令")
                    return
                }
                if (args.size < 2) {
                    val redisStatus = if (plugin.redisManager?.isConnected() == true) "已连接" else "未连接"
                    sender.sendMsg("${ChatColor.AQUA}Redis 状态: $redisStatus")
                    sender.sendMsg("${ChatColor.YELLOW}用法: /huhobot redis <reconnect|send|exec>")
                    return
                }
                when (args[1].lowercase()) {
                    "reconnect" -> {
                        sender.sendMsg("${ChatColor.YELLOW}正在重新连接 Redis...")
                        if (plugin.reconnectRedis()) {
                            sender.sendMsg("${ChatColor.GREEN}Redis 重新连接成功")
                        } else {
                            sender.sendMsg("${ChatColor.RED}Redis 重新连接失败或未启用")
                        }
                    }
                    "send" -> {
                        if (args.size < 4) {
                            sender.sendMsg("${ChatColor.YELLOW}用法: /huhobot redis send <服务器名|ALL> <命令>")
                            return
                        }
                        val serverName = args[2]
                        val command = args.drop(3).joinToString(" ")
                        if (plugin.redisManager?.isConnected() == true) {
                            val success = plugin.redisManager!!.sendCommand(serverName, command)
                            if (success) {
                                sender.sendMsg("${ChatColor.GREEN}命令已发送到 $serverName: $command")
                            } else {
                                sender.sendMsg("${ChatColor.RED}发送命令失败")
                            }
                        } else {
                            sender.sendMsg("${ChatColor.RED}Redis 未连接")
                        }
                    }
                    "exec" -> {
                        if (args.size < 4) {
                            sender.sendMsg("${ChatColor.YELLOW}用法: /huhobot redis exec <服务器名> <命令>")
                            sender.sendMsg("${ChatColor.GRAY}此命令会等待子服返回执行结果")
                            return
                        }
                        val serverName = args[2]
                        val command = args.drop(3).joinToString(" ")
                        if (plugin.redisManager?.isConnected() == true) {
                            val callback = plugin.redisManager!!.commandCallback
                            if (callback != null) {
                                sender.sendMsg("${ChatColor.YELLOW}正在执行命令...")
                                callback.executeWithCallback(
                                    serverName = serverName,
                                    command = command,
                                    onOutput = { output ->
                                        // 过滤Minecraft颜色代码
                                        val cleanOutput = output.replace(Regex("§[0-9a-fk-or]"), "")
                                        sender.sendMsg("${ChatColor.WHITE}[$serverName] $cleanOutput")
                                    },
                                    onComplete = {
                                        sender.sendMsg("${ChatColor.GREEN}命令执行完成")
                                    },
                                    onTimeout = {
                                        sender.sendMsg("${ChatColor.RED}命令执行超时，子服可能未响应")
                                    }
                                )
                            } else {
                                sender.sendMsg("${ChatColor.RED}回调管理器未初始化")
                            }
                        } else {
                            sender.sendMsg("${ChatColor.RED}Redis 未连接")
                        }
                    }
                    else -> {
                        sender.sendMsg("${ChatColor.RED}未知的 Redis 子命令")
                    }
                }
            }
            else -> showHelp(sender)
        }
    }

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): Iterable<String> {
        return when {
            args.isEmpty() || args.size == 1 -> {
                listOf("reload", "reconnect", "disconnect", "status", "bind", "redis")
                    .filter { it.startsWith(args.getOrElse(0) { "" }.lowercase()) }
            }
            args.size == 2 && args[0].lowercase() == "redis" -> {
                listOf("reconnect", "send", "exec")
                    .filter { it.startsWith(args[1].lowercase()) }
            }
            args.size == 3 && args[0].lowercase() == "redis" && (args[1].lowercase() == "send" || args[1].lowercase() == "exec") -> {
                val servers = plugin.proxy.servers.keys.toMutableList()
                if (args[1].lowercase() == "send") {
                    servers.add(0, "ALL")
                }
                servers.filter { it.lowercase().startsWith(args[2].lowercase()) }
            }
            else -> emptyList()
        }
    }

    private fun showHelp(sender: CommandSender) {
        sender.sendMsg("${ChatColor.GOLD}=== HuHoBot 帮助 ===")
        sender.sendMsg("${ChatColor.YELLOW}/huhobot reload - 重新加载配置")
        sender.sendMsg("${ChatColor.YELLOW}/huhobot reconnect - 重新连接服务器")
        sender.sendMsg("${ChatColor.YELLOW}/huhobot disconnect - 断开连接")
        sender.sendMsg("${ChatColor.YELLOW}/huhobot status - 查看状态")
        sender.sendMsg("${ChatColor.YELLOW}/huhobot bind <code> - 确认绑定")
        sender.sendMsg("${ChatColor.YELLOW}/huhobot redis - Redis 管理")
        sender.sendMsg("${ChatColor.GRAY}  redis reconnect - 重新连接 Redis")
        sender.sendMsg("${ChatColor.GRAY}  redis send <服务器|ALL> <命令> - 发送命令(无回调)")
        sender.sendMsg("${ChatColor.GRAY}  redis exec <服务器> <命令> - 执行并返回结果")
    }
}

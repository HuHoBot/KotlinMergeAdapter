package cn.huohuas001.huhobot.velocity.commands

import cn.huohuas001.huhobot.velocity.HuHoBotVelocity
import com.velocitypowered.api.command.SimpleCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class HuHoBotCommand(private val plugin: HuHoBotVelocity) : SimpleCommand {

    override fun execute(invocation: SimpleCommand.Invocation) {
        val source = invocation.source()
        val args = invocation.arguments()

        if (args.isEmpty()) {
            showHelp(source)
            return
        }

        when (args[0].lowercase()) {
            "reload" -> {
                if (!source.hasPermission("huhobot.reload")) {
                    source.sendMessage(Component.text("你没有权限执行此命令").color(NamedTextColor.RED))
                    return
                }
                plugin.configManager.reloadConfig()
                source.sendMessage(Component.text("配置文件已重新加载").color(NamedTextColor.GREEN))
            }
            "reconnect" -> {
                if (!source.hasPermission("huhobot.reconnect")) {
                    source.sendMessage(Component.text("你没有权限执行此命令").color(NamedTextColor.RED))
                    return
                }
                if (plugin.reconnect()) {
                    source.sendMessage(Component.text("正在尝试重新连接...").color(NamedTextColor.GREEN))
                } else {
                    source.sendMessage(Component.text("连接已存在，无需重连").color(NamedTextColor.YELLOW))
                }
            }
            "disconnect" -> {
                if (!source.hasPermission("huhobot.disconnect")) {
                    source.sendMessage(Component.text("你没有权限执行此命令").color(NamedTextColor.RED))
                    return
                }
                if (plugin.disConnectServer()) {
                    source.sendMessage(Component.text("已断开连接").color(NamedTextColor.GREEN))
                } else {
                    source.sendMessage(Component.text("断开连接失败").color(NamedTextColor.RED))
                }
            }
            "status" -> {
                if (!source.hasPermission("huhobot.status")) {
                    source.sendMessage(Component.text("你没有权限执行此命令").color(NamedTextColor.RED))
                    return
                }
                val status = if (cn.huohuas001.bot.ClientManager.isOpen()) "已连接" else "未连接"
                val redisStatus = if (plugin.redisManager?.isConnected() == true) "已连接" else "未连接"
                source.sendMessage(Component.text("HuHoBot 状态: $status").color(NamedTextColor.AQUA))
                source.sendMessage(Component.text("Redis 状态: $redisStatus").color(NamedTextColor.AQUA))
                source.sendMessage(Component.text("服务器ID: ${plugin.getServerId()}").color(NamedTextColor.AQUA))
                source.sendMessage(Component.text("版本: ${plugin.getPluginVersion()}").color(NamedTextColor.AQUA))
            }
            "bind" -> {
                if (!source.hasPermission("huhobot.bind")) {
                    source.sendMessage(Component.text("你没有权限执行此命令").color(NamedTextColor.RED))
                    return
                }
                if (args.size < 2) {
                    source.sendMessage(Component.text("用法: /huhobot bind <code|server_id>").color(NamedTextColor.YELLOW))
                    return
                }
                val bindCode = args[1]
                if (plugin.bindRequestObj.confirmBind(bindCode)) {
                    source.sendMessage(Component.text("绑定成功！").color(NamedTextColor.GREEN))
                } else {
                    source.sendMessage(Component.text("无效的绑定码或绑定请求已过期").color(NamedTextColor.RED))
                }
            }
            "redis" -> {
                if (!source.hasPermission("huhobot.redis")) {
                    source.sendMessage(Component.text("你没有权限执行此命令").color(NamedTextColor.RED))
                    return
                }
                if (args.size < 2) {
                    val redisStatus = if (plugin.redisManager?.isConnected() == true) "已连接" else "未连接"
                    source.sendMessage(Component.text("Redis 状态: $redisStatus").color(NamedTextColor.AQUA))
                    source.sendMessage(Component.text("用法: /huhobot redis <reconnect|send>").color(NamedTextColor.YELLOW))
                    return
                }
                when (args[1].lowercase()) {
                    "reconnect" -> {
                        source.sendMessage(Component.text("正在重新连接 Redis...").color(NamedTextColor.YELLOW))
                        if (plugin.reconnectRedis()) {
                            source.sendMessage(Component.text("Redis 重新连接成功").color(NamedTextColor.GREEN))
                        } else {
                            source.sendMessage(Component.text("Redis 重新连接失败或未启用").color(NamedTextColor.RED))
                        }
                    }
                    "send" -> {
                        if (args.size < 4) {
                            source.sendMessage(Component.text("用法: /huhobot redis send <服务器名|ALL> <命令>").color(NamedTextColor.YELLOW))
                            return
                        }
                        val serverName = args[2]
                        val command = args.drop(3).joinToString(" ")
                        if (plugin.redisManager?.isConnected() == true) {
                            val success = plugin.redisManager!!.sendCommand(serverName, command)
                            if (success) {
                                source.sendMessage(Component.text("命令已发送到 $serverName: $command").color(NamedTextColor.GREEN))
                            } else {
                                source.sendMessage(Component.text("发送命令失败").color(NamedTextColor.RED))
                            }
                        } else {
                            source.sendMessage(Component.text("Redis 未连接").color(NamedTextColor.RED))
                        }
                    }
                    "exec" -> {
                        // 带回调的命令执行
                        if (args.size < 4) {
                            source.sendMessage(Component.text("用法: /huhobot redis exec <服务器名> <命令>").color(NamedTextColor.YELLOW))
                            source.sendMessage(Component.text("此命令会等待子服返回执行结果").color(NamedTextColor.GRAY))
                            return
                        }
                        val serverName = args[2]
                        val command = args.drop(3).joinToString(" ")
                        if (plugin.redisManager?.isConnected() == true) {
                            val callback = plugin.redisManager!!.commandCallback
                            if (callback != null) {
                                source.sendMessage(Component.text("正在执行命令...").color(NamedTextColor.YELLOW))
                                callback.executeWithCallback(
                                    serverName = serverName,
                                    command = command,
                                    onOutput = { output ->
                                        // 过滤Minecraft颜色代码
                                        val cleanOutput = output.replace(Regex("§[0-9a-fk-or]"), "")
                                        source.sendMessage(Component.text("[$serverName] $cleanOutput").color(NamedTextColor.WHITE))
                                    },
                                    onComplete = {
                                        source.sendMessage(Component.text("命令执行完成").color(NamedTextColor.GREEN))
                                    },
                                    onTimeout = {
                                        source.sendMessage(Component.text("命令执行超时，子服可能未响应").color(NamedTextColor.RED))
                                    }
                                )
                            } else {
                                source.sendMessage(Component.text("回调管理器未初始化").color(NamedTextColor.RED))
                            }
                        } else {
                            source.sendMessage(Component.text("Redis 未连接").color(NamedTextColor.RED))
                        }
                    }
                    else -> {
                        source.sendMessage(Component.text("未知的 Redis 子命令").color(NamedTextColor.RED))
                    }
                }
            }
            else -> showHelp(source)
        }
    }

    private fun showHelp(source: com.velocitypowered.api.command.CommandSource) {
        source.sendMessage(Component.text("=== HuHoBot 帮助 ===").color(NamedTextColor.GOLD))
        source.sendMessage(Component.text("/huhobot reload - 重新加载配置").color(NamedTextColor.YELLOW))
        source.sendMessage(Component.text("/huhobot reconnect - 重新连接服务器").color(NamedTextColor.YELLOW))
        source.sendMessage(Component.text("/huhobot disconnect - 断开连接").color(NamedTextColor.YELLOW))
        source.sendMessage(Component.text("/huhobot status - 查看状态").color(NamedTextColor.YELLOW))
        source.sendMessage(Component.text("/huhobot bind <code> - 确认绑定").color(NamedTextColor.YELLOW))
        source.sendMessage(Component.text("/huhobot redis - Redis 管理").color(NamedTextColor.YELLOW))
        source.sendMessage(Component.text("  redis reconnect - 重新连接 Redis").color(NamedTextColor.GRAY))
        source.sendMessage(Component.text("  redis send <服务器|ALL> <命令> - 发送命令(无回调)").color(NamedTextColor.GRAY))
        source.sendMessage(Component.text("  redis exec <服务器> <命令> - 执行并返回结果").color(NamedTextColor.GRAY))
    }

    override fun suggest(invocation: SimpleCommand.Invocation): List<String> {
        val args = invocation.arguments()
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
                val servers = plugin.server.allServers.map { it.serverInfo.name }.toMutableList()
                if (args[1].lowercase() == "send") {
                    servers.add(0, "ALL")
                }
                servers.filter { it.lowercase().startsWith(args[2].lowercase()) }
            }
            else -> emptyList()
        }
    }

    override fun hasPermission(invocation: SimpleCommand.Invocation): Boolean {
        return invocation.source().hasPermission("huhobot.use")
    }
}

package cn.huohuas001.huhobot.common.commands

import cn.huohuas001.bot.provider.HExecution
import cn.huohuas001.huhobot.common.HuHoBotProxy
import cn.huohuas001.huhobot.common.tools.CommandUtils
import java.util.concurrent.CompletableFuture

/**
 * 代理端命令执行器基类
 * 子类只需实现 executeLocal 和 platformName
 */
abstract class ProxyConsoleSender(protected val plugin: HuHoBotProxy) : HExecution {

    protected var result: String = ""

    /**
     * 平台名称，用于判断是否本地执行（如 "velocity"、"bungeecord"）
     */
    abstract val platformName: String

    override fun getRawString(): String {
        return result
    }

    override fun execute(command: String): CompletableFuture<HExecution> {
        val parsed = CommandUtils.splitCommand(command)
        val serverName = parsed.serverName
        val realCommand = parsed.command

        return if (serverName == null || serverName.equals(platformName, ignoreCase = true)) {
            executeLocal(realCommand)
        } else {
            executeOnServer(serverName, realCommand)
        }
    }

    /**
     * 在本地代理端执行命令（平台特定实现）
     */
    protected abstract fun executeLocal(command: String): CompletableFuture<HExecution>

    /**
     * 发送命令到指定服务器
     * @param serverName 目标服务器名称
     * @param command 命令
     */
    fun executeOnServer(serverName: String, command: String): CompletableFuture<HExecution> {
        val future = CompletableFuture<HExecution>()
        val self = this

        plugin.submit {
            try {
                if (plugin.redisManager != null && plugin.redisManager!!.isConnected()) {
                    val callbackManager = plugin.redisManager!!.commandCallback

                    // ALL 走 sendCommand，不带 callback
                    if (callbackManager != null && !serverName.equals("ALL", ignoreCase = true)) {
                        // 使用回调执行
                        val outputBuilder = StringBuilder()

                        callbackManager.executeWithCallback(
                            serverName = serverName,
                            command = command,
                            onOutput = { line ->
                                outputBuilder.appendLine(line)
                            },
                            onComplete = {
                                result = outputBuilder.toString()
                                future.complete(self)
                            },
                            onTimeout = {
                                outputBuilder.appendLine("命令执行超时或目标服务器未响应")
                                result = outputBuilder.toString()
                                future.complete(self)
                            }
                        )
                    } else {
                        // 降级处理 或 ALL 广播
                        val success = plugin.redisManager!!.sendCommand(serverName, command)
                        if (success) {
                            result = "命令已发送到 $serverName: $command"
                        } else {
                            result = "发送命令到 $serverName 失败"
                        }
                        future.complete(self)
                    }
                } else {
                    result = "Redis 未连接，无法发送命令到子服务器"
                    future.complete(self)
                }
            } catch (e: Exception) {
                result = "执行命令异常: ${e.message}"
                future.completeExceptionally(e)
            }
        }

        return future
    }
}
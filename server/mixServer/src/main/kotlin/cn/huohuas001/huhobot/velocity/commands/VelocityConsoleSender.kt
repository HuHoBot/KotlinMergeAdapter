package cn.huohuas001.huhobot.velocity.commands

import cn.huohuas001.bot.providers.HExecution
import cn.huohuas001.huhobot.velocity.HuHoBotVelocity
import cn.huohuas001.huhobot.velocity.tools.CommandUtils
import java.util.concurrent.CompletableFuture

/**
 * Velocity命令执行器
 * 通过Redis将命令发送到子服务器执行
 */
class VelocityConsoleSender(val plugin: HuHoBotVelocity) : HExecution {

    private var result: String = ""

    override fun getRawString(): String {
        return result
    }

    override fun execute(command: String): CompletableFuture<HExecution> {
        val parsed = CommandUtils.splitCommand(command)
        val serverName = parsed.serverName
        val realCommand = parsed.command

        return if (serverName == null || serverName.equals("velocity", ignoreCase = true)) {
            executeLocal(realCommand)
        } else {
            executeOnServer(serverName, realCommand)
        }
    }

    /**
     * 在 Velocity 本地执行命令
     */
    private fun executeLocal(command: String): CompletableFuture<HExecution> {
        val future = CompletableFuture<HExecution>()
        val self = this

        plugin.submit {
            try {
                plugin.server.commandManager.executeAsync(
                    plugin.server.consoleCommandSource,
                    command
                ).thenAccept {
                    result = "命令已在 Velocity 执行: $command"
                    future.complete(self)
                }.exceptionally { ex ->
                    result = "执行命令失败: ${ex.message}"
                    future.completeExceptionally(ex)
                    null
                }
            } catch (e: Exception) {
                result = "执行命令异常: ${e.message}"
                future.completeExceptionally(e)
            }
        }
        return future
    }

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

                    // 所以 ALL 走 sendCommand，不带 callback。
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

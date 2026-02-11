package cn.huohuas001.huhobot.velocity.commands

import cn.huohuas001.bot.provider.HExecution
import cn.huohuas001.huhobot.common.commands.ProxyConsoleSender
import cn.huohuas001.huhobot.velocity.HuHoBotVelocity
import java.util.concurrent.CompletableFuture

/**
 * Velocity命令执行器
 */
class VelocityConsoleSender(private val velocityPlugin: HuHoBotVelocity) : ProxyConsoleSender(velocityPlugin) {

    override val platformName = "velocity"

    override fun executeLocal(command: String): CompletableFuture<HExecution> {
        val future = CompletableFuture<HExecution>()
        val self = this

        plugin.submit {
            try {
                velocityPlugin.server.commandManager.executeAsync(
                    velocityPlugin.server.consoleCommandSource,
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
}
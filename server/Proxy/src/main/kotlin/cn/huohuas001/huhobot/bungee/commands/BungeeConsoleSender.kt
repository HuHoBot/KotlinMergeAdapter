package cn.huohuas001.huhobot.bungee.commands

import cn.huohuas001.bot.provider.HExecution
import cn.huohuas001.huhobot.bungee.HuHoBotBungee
import cn.huohuas001.huhobot.common.commands.ProxyConsoleSender
import net.md_5.bungee.api.ProxyServer
import java.util.concurrent.CompletableFuture

/**
 * BungeeCord命令执行器
 */
class BungeeConsoleSender(bungeePlugin: HuHoBotBungee) : ProxyConsoleSender(bungeePlugin) {

    override val platformName = "bungeecord"

    override fun executeLocal(command: String): CompletableFuture<HExecution> {
        val future = CompletableFuture<HExecution>()
        val self = this

        plugin.submit {
            try {
                ProxyServer.getInstance().pluginManager.dispatchCommand(
                    ProxyServer.getInstance().console,
                    command
                )
                result = "命令已在 BungeeCord 执行: $command"
                future.complete(self)
            } catch (e: Exception) {
                result = "执行命令异常: ${e.message}"
                future.completeExceptionally(e)
            }
        }
        return future
    }
}
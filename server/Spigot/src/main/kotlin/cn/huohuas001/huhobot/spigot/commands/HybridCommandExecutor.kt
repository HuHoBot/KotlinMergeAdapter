package cn.huohuas001.huhobot.spigot.commands

import cn.huohuas001.bot.provider.HExecution
import cn.huohuas001.huhobot.spigot.HuHoBotSpigot
import org.bukkit.Bukkit
import java.util.concurrent.CompletableFuture

class HybridCommandExecutor(private val plugin: HuHoBotSpigot) : HExecution {
    private val sender = BukkitConsoleSender(plugin)
    private val appender = CommandOutputAppender.getInstance()

    override fun execute(command: String): CompletableFuture<HExecution> {
        val future = CompletableFuture<HExecution>()

        sender.clearMessages()
        appender.startCapture()

        plugin.submit {
            try {
                Bukkit.dispatchCommand(sender, command)

                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    // 合并两种来源的输出
                    val senderMessages = sender.getAndClearMessages()
                    val logMessages = appender.stopCapture()

                    // 去重并保持顺序
                    val allMessages = (senderMessages + logMessages).distinct()

                    // 更新 sender 的消息列表
                    allMessages.forEach { sender.sendMessage(it) }

                    future.complete(sender)
                }, 40L)

            } catch (e: Exception) {
                appender.stopCapture()
                future.completeExceptionally(e)
            }
        }

        return future
    }

    override fun getRawString(): String {
        return sender.getRawString()
    }

    fun cleanup() {
        // 在插件禁用时调用
        CommandOutputAppender.removeInstance()
    }
}
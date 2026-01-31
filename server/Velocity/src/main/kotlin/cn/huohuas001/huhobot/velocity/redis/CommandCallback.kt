package cn.huohuas001.huhobot.velocity.redis

import cn.huohuas001.huhobot.velocity.HuHoBotVelocity
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 命令回调管理器
 * 用于管理发送到子服务器的命令及其回调
 */
class CommandCallback(private val plugin: HuHoBotVelocity) {

    // 存储等待回调的任务 <taskId, CallbackContext>
    private val pendingCallbacks = ConcurrentHashMap<String, CallbackContext>()

    // 默认超时时间（毫秒）
    private val defaultTimeout = 10000L

    /**
     * 执行远程命令并等待回调
     * @param serverName 目标服务器名称
     * @param command 要执行的命令
     * @param onOutput 收到输出时的回调
     * @param onComplete 命令执行完成时的回调
     * @param onTimeout 超时时的回调
     */
    fun executeWithCallback(
        serverName: String,
        command: String,
        onOutput: (String) -> Unit,
        onComplete: () -> Unit = {},
        onTimeout: () -> Unit = {}
    ): String {
        val taskId = UUID.randomUUID().toString()
        val context = CallbackContext(
            taskId = taskId,
            serverName = serverName,
            command = command,
            onOutput = onOutput,
            onComplete = onComplete,
            onTimeout = onTimeout,
            outputBuffer = StringBuilder(),
            startTime = System.currentTimeMillis()
        )

        pendingCallbacks[taskId] = context

        // 发送带有 taskId 的命令
        plugin.redisManager?.sendCommandWithCallback(serverName, taskId, command)

        // 设置超时清理
        plugin.submitLater(defaultTimeout / 50) {
            val ctx = pendingCallbacks.remove(taskId)
            if (ctx != null) {
                ctx.onTimeout()
            }
        }

        return taskId
    }

    /**
     * 处理从子服务器收到的回调消息
     * @param message 回调消息，格式: taskId|type|content
     */
    fun handleCallback(message: String) {
        val parts = message.split("|", limit = 3)
        if (parts.size < 3) {
            plugin.logger.warn("收到格式错误的回调消息: $message")
            return
        }

        val taskId = parts[0]
        val type = parts[1]
        val content = parts[2]

        val context = pendingCallbacks[taskId] ?: return

        when (type) {
            "[OUTPUT]" -> {
                // 收到输出行
                context.outputBuffer.appendLine(content)
                context.onOutput(content)
            }
            "[COMPLETE]" -> {
                // 命令执行完成
                pendingCallbacks.remove(taskId)
                context.onComplete()
            }
            "[ERROR]" -> {
                // 命令执行错误
                context.onOutput("错误: $content")
                pendingCallbacks.remove(taskId)
                context.onComplete()
            }
        }
    }

    /**
     * 获取任务的完整输出缓冲区
     */
    fun getTaskOutput(taskId: String): String? {
        return pendingCallbacks[taskId]?.outputBuffer?.toString()
    }

    /**
     * 取消一个任务
     */
    fun cancelTask(taskId: String) {
        pendingCallbacks.remove(taskId)
    }

    /**
     * 获取当前等待中的任务数量
     */
    fun getPendingCount(): Int = pendingCallbacks.size

    /**
     * 回调上下文
     */
    data class CallbackContext(
        val taskId: String,
        val serverName: String,
        val command: String,
        val onOutput: (String) -> Unit,
        val onComplete: () -> Unit,
        val onTimeout: () -> Unit,
        val outputBuffer: StringBuilder,
        val startTime: Long
    )
}

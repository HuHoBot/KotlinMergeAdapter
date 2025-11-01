package cn.huohuas001.bot.provider

import cn.huohuas001.bot.providers.HExecution
import java.util.concurrent.CompletableFuture

interface CommandProvider {
    fun sendCommand(command: String): CompletableFuture<HExecution>
}
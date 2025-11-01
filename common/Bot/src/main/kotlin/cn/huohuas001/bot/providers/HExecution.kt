package cn.huohuas001.bot.providers

import java.util.concurrent.CompletableFuture

interface HExecution {
    fun getRawString(): String

    fun execute(command: String): CompletableFuture<HExecution>
}
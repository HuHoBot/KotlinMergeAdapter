package cn.huohuas001.huhobot.spigot.commands

import cn.huohuas001.bot.providers.HExecution
import cn.huohuas001.bot.providers.HReflection
import cn.huohuas001.huhobot.spigot.HuHoBotSpigot
import org.bukkit.Bukkit
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture


class DecidatedServerSender(private val plugin: HuHoBotSpigot): HExecution {

    private lateinit var method: Method
    private lateinit var dedicatedServer: Any
    private lateinit var message: String

    fun check(): Boolean {
        val server = Bukkit.getServer()
        dedicatedServer = HReflection.findFieldByType(server, "DedicatedServer") ?: return false
        try {
            method = dedicatedServer.javaClass.getMethod("runCommand", String::class.java)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun execute(command: String): CompletableFuture<HExecution> {
        message = method.invoke(dedicatedServer, command) as String
        return CompletableFuture.completedFuture(this)
    }

    override fun getRawString(): String {
        return message
    }
}
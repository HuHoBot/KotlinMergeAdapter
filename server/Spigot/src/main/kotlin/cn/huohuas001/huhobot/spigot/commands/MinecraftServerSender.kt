package cn.huohuas001.huhobot.spigot.commands

import cn.huohuas001.bot.providers.HExecution
import cn.huohuas001.bot.providers.HReflection
import cn.huohuas001.huhobot.spigot.HuHoBotSpigot
import org.bukkit.Bukkit

import java.lang.reflect.Method
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture

class MinecraftServerSender(private val plugin: HuHoBotSpigot): HExecution {

    private lateinit var minecraftServer: Any
    private lateinit var method: Method
    private lateinit var rconConsoleSource: Any
    private lateinit var message: String

    fun check(): Boolean {
        val server = Bukkit.getServer()
        minecraftServer = HReflection.findFieldByType(server, "MinecraftServer")?: return false
        try {
            method = minecraftServer.javaClass.getMethod("runCommand", getRconConsoleSourceClassPath(), String::class.java)
            val rconConsoleSourceConstructor = getRconConsoleSourceClassPath().constructors[0]
            rconConsoleSource = rconConsoleSourceConstructor.newInstance(minecraftServer,
                InetSocketAddress.createUnresolved("", 0))
            return true
        } catch (e: Exception) {
            try {
                method = minecraftServer.javaClass.getMethod("executeRemoteCommand", String::class.java)
                return true
            } catch (e1: Exception) {
                return false
            }
        }
    }

    override fun execute(command: String): CompletableFuture<HExecution> {
        // 先检查是否已初始化
        if (!::minecraftServer.isInitialized || !::method.isInitialized) {
            val checked = check()
            if (!checked) {
                val future = CompletableFuture<HExecution>()
                future.completeExceptionally(RuntimeException("MinecraftServer sender not properly initialized"))
                return future
            }
        }

        // 创建 CompletableFuture 用于返回结果
        val future = CompletableFuture<HExecution>()

        // 使用 Bukkit 调度器在主线程中执行命令
        plugin.submit {
            try {
                val result = if (method.name == "runCommand") {
                    method.invoke(minecraftServer, rconConsoleSource, command) as String
                } else {
                    method.invoke(minecraftServer, command) as String
                }
                message = result
            } catch (e: Exception) {
                message = "命令执行失败: ${e.message}"
            }
            // 执行完成后完成 future
            future.complete(this)
        }

        return future
    }

    @Throws(ClassNotFoundException::class)
    private fun getRconConsoleSourceClassPath(): Class<*> {
        return try {
            Class.forName("net.minecraft.server.rcon.RconConsoleSource")
        } catch (classNotFoundException: ClassNotFoundException) {
            try {
                Class.forName("net.minecraft.server.rcon.RemoteControlCommandListener")
            } catch (classNotFoundException2: ClassNotFoundException) {
                //plugin.debugModule?.debugLogger?.log("Failed to use rcon: $classNotFoundException\n$classNotFoundException2")
                throw ClassNotFoundException("Can not find RconConsoleSource class path")
            }
        }
    }

    override fun getRawString(): String {
        return message
    }
}
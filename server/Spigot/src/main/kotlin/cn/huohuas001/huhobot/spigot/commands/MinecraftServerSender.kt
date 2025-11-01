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
        val checked = check()
        CompletableFuture.runAsync {
            message = try {
                if (method.name == "runCommand") {
                    method.invoke(minecraftServer, rconConsoleSource, command) as String
                } else {
                    method.invoke(minecraftServer, command) as String
                }
            } catch (e: Exception) {
                "命令执行失败: ${e.message}"
            }
        }
        return CompletableFuture.completedFuture(this)
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
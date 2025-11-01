package cn.huohuas001.huhobot.spigot.commands

import cn.huohuas001.bot.providers.HExecution
import cn.huohuas001.huhobot.spigot.HuHoBotSpigot
import me.clip.placeholderapi.libs.kyori.adventure.text.Component
import me.clip.placeholderapi.libs.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class NativeServerSender(private val plugin: HuHoBotSpigot) : HExecution {

    private lateinit var commandSender: CommandSender
    private val messageList: MutableList<String> = mutableListOf()

    fun check(): Boolean {
        try {
            val method = Class.forName("org.bukkit.Bukkit").getMethod("createCommandSender", Consumer::class.java)
            commandSender = method.invoke(null, Consumer<Component> { text ->
                val message = LegacyComponentSerializer.legacySection().serialize(text)
                messageList.add(message)
            }) as CommandSender
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun execute(command: String): CompletableFuture<HExecution> {
        Bukkit.dispatchCommand(commandSender, command)
        return CompletableFuture.supplyAsync {
            Thread.sleep(2 * 1000L)
            this
        }
    }


    override fun getRawString(): String {
        return messageList.joinToString("\n")
    }
}
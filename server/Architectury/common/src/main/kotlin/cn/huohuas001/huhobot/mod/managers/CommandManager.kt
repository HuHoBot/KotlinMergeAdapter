package cn.huohuas001.huhobot.mod.managers

import cn.huohuas001.huhobot.mod.HuHoBotMod
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.architectury.event.events.common.CommandRegistrationEvent
import net.minecraft.commands.CommandSource
import net.minecraft.commands.CommandSourceStack
import org.apache.logging.log4j.Logger
import java.util.function.Consumer


class CommandManager(private val plugin: HuHoBotMod) {
    private val commandCallbacks = mutableListOf<Consumer<CommandResult>>()
    private var logger: Logger = plugin.LOGGER

    fun literal(name: String): LiteralArgumentBuilder<CommandSourceStack> {
        return LiteralArgumentBuilder.literal<CommandSourceStack>(name)
    }

    fun <T> argument(name: String?, type: ArgumentType<T?>?): RequiredArgumentBuilder<CommandSourceStack?, T?> {
        return RequiredArgumentBuilder.argument<CommandSourceStack?, T?>(name, type)
    }

    fun registerCommands(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(literal("huhobot")
                .requires { source -> source.entity == null } // 只允许控制台执行
                .then(
                    literal("reload")
                    .executes(this::onReload)
                )
                .then(
                    literal("reconnect")
                    .executes(this::onReconnect)
                )
                .then(
                    literal("disconnect")
                    .executes(this::onDisconnect)
                )
                .then(
                    literal("bind")
                    .then(
                        argument("bindCode", StringArgumentType.string())
                        .executes(this::onBind)
                    )
                )
                .then(
                    literal("help")
                    .executes(this::onHelp)
                )
        )
    }

    private fun onReload(context: CommandContext<CommandSourceStack>): Int {
        plugin.reloadBotConfig()
        logger.info("§b重载机器人配置文件成功.")

        triggerCallbacks(CommandResult("huhobot reload", "", getName(), true))
        return 1
    }

    private fun onReconnect(context: CommandContext<CommandSourceStack>): Int {

        val success = plugin.reconnect()
        if (success) {
            logger.info("§6重连机器人成功.")
        } else {
            logger.warn("§c重连机器人失败：已在连接状态.")
        }

        triggerCallbacks(CommandResult("huhobot reconnect", "", getName(), success))
        return 1
    }

    private fun onDisconnect(context: CommandContext<CommandSourceStack>): Int {

        val success = plugin.disConnectServer()
        if (success) {
            logger.warn("§6已断开机器人连接.")
        }

        triggerCallbacks(CommandResult("huhobot disconnect", "", getName(), success))
        return 1
    }

    private fun onBind(context: CommandContext<CommandSourceStack?>): Int {
        val code = StringArgumentType.getString(context, "bindCode")
        val obj = plugin.bindRequestObj
        val success = obj.confirmBind(code)

        if (success) {
            logger.info("§6已向服务器发送确认绑定请求，请等待服务端下发配置文件.")
        } else {
            logger.error("§c绑定码错误，请重新输入.")
        }

        triggerCallbacks(CommandResult("huhobot bind", code, getName(), success))
        return 1
    }

    private fun onHelp(context: CommandContext<CommandSourceStack>): Int {
        logger.info("§bHuHoBot 操作相关命令")
        logger.info("§6> §7/huhobot reload - 重载配置文件")
        logger.info("§6> §7/huhobot reconnect - 重新连接服务器")
        logger.info("§6> §7/huhobot disconnect - 断开服务器连接")
        logger.info("§6> §7/huhobot bind <bindCode:string> - 确认绑定")
        return 1
    }

    // 获取执行者名字（玩家名或控制台）
    private fun getName(): String {
        return "HuHoBot"
    }

    // 外部执行命令并提供回调
    fun executeCommand(command: String, callback: Consumer<CommandResult>) {
        commandCallbacks.add(callback)
        val server = plugin.serverInstance

        server.execute {
            try {
                val source = server.createCommandSourceStack()
                server.commands.dispatcher.execute(command,source)
                callback.accept(CommandResult(command, "Command executed", "System", true))
            } catch (e: Exception) {
                callback.accept(CommandResult(command, "Error: ${e.message}", "System", false))
            } finally {
                commandCallbacks.remove(callback)
            }
        }
    }

    private fun triggerCallbacks(result: CommandResult) {
        val callbacks = commandCallbacks.toList()
        for (callback in callbacks) {
            callback.accept(result)
        }
    }

    // 命令结果类
    class CommandResult(
        val command: String,
        val output: String,
        val sender: String,
        val success: Boolean
    )
}

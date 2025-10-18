package cn.huohuas001.huhobot.allay.utils


import cn.huohuas001.huhobot.allay.HuHoBotAllay
import org.allaymc.api.command.CommandSender
import org.allaymc.api.math.location.Location3dc
import org.allaymc.api.message.I18n
import org.allaymc.api.message.LangCode
import org.allaymc.api.message.TrContainer
import org.allaymc.api.permission.PermissionGroup
import org.allaymc.api.permission.PermissionGroups
import org.allaymc.api.server.Server

class HuHoBotCommandSender(val plugin: HuHoBotAllay) : CommandSender {
    val outputs = StringBuilder()

    override fun getCommandSenderName(): String {
        return "HuHoBot"
    }

    override fun getCommandExecuteLocation(): Location3dc {
        return Server.getInstance().worldPool.defaultWorld.spawnPoint
    }

    override fun sendMessage(message: String) {
        for (line in message.split("\n")) {
            this.outputs.append(line).append("\n")
            plugin.pluginLogger.info(line)
        }
    }

    override fun sendTranslatable(translatable: String, vararg args: Any) {
        sendMessage(I18n.get().tr(LangCode.zh_CN, translatable, *args))
    }

    override fun sendCommandOutputs(sender: CommandSender, status: Int, vararg outputs: TrContainer) {
        for (output in outputs) {
            sendMessage(I18n.get().tr(LangCode.zh_CN, output.str(), *output.args()))
        }
    }

    override fun getPermissionGroup(): PermissionGroup {
        return PermissionGroups.OPERATOR
    }
}

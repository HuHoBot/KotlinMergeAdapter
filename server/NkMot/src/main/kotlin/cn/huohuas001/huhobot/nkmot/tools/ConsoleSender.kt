package cn.huohuas001.huhobot.nkmot.tools

import cn.nukkit.Server
import cn.nukkit.command.CommandSender
import cn.nukkit.command.ConsoleCommandSender
import cn.nukkit.lang.CommandOutputContainer
import cn.nukkit.lang.TextContainer
import cn.nukkit.lang.TranslationContainer
import cn.nukkit.level.GameRule
import cn.nukkit.network.protocol.types.CommandOutputMessage
import cn.nukkit.permission.Permission
import cn.nukkit.permission.PermissionAttachment
import cn.nukkit.permission.PermissionAttachmentInfo
import cn.nukkit.plugin.Plugin
import cn.nukkit.utils.MainLogger

class ConsoleSender(private val console: ConsoleCommandSender) : CommandSender {
    val output = StringBuilder()

    override fun sendMessage(message: String) {
        var translatedMessage = this.server.language.translateString(message)
        val lines = translatedMessage.trim { it <= ' ' }.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (line in lines) {
            if (line.length > 0) {
                this.output.append(line).append("\n")
            }
            MainLogger.getLogger().info(line)
        }
    }

    override fun sendMessage(message: TextContainer) {
        this.sendMessage(this.server.language.translate(message))
    }

    override fun sendCommandOutput(container: CommandOutputContainer) {
        if (this.location.level.gameRules.getBoolean(GameRule.SEND_COMMAND_FEEDBACK)) {
            for (msg in container.messages) {
                val text = this.server.language.translate(TranslationContainer(msg.messageId, *msg.parameters))
                this.sendMessage(text)
            }
        }
    }

    override fun getServer(): Server {
        return console.server
    }

    override fun getName(): String {
        return "CONSOLE"
    }

    override fun isPlayer(): Boolean {
        return false
    }

    override fun isPermissionSet(s: String): Boolean {
        return console.isPermissionSet(s)
    }

    override fun isPermissionSet(permission: Permission): Boolean {
        return console.isPermissionSet(permission)
    }

    override fun hasPermission(s: String): Boolean {
        return console.hasPermission(s)
    }

    override fun hasPermission(permission: Permission): Boolean {
        return console.hasPermission(permission)
    }

    override fun addAttachment(plugin: Plugin): PermissionAttachment {
        return console.addAttachment(plugin)
    }

    override fun addAttachment(plugin: Plugin, s: String): PermissionAttachment {
        return console.addAttachment(plugin, s)
    }

    override fun addAttachment(plugin: Plugin, s: String, aBoolean: Boolean?): PermissionAttachment {
        return console.addAttachment(plugin, s, aBoolean)
    }

    override fun removeAttachment(permissionAttachment: PermissionAttachment) {
        console.removeAttachment(permissionAttachment)
    }

    override fun recalculatePermissions() {
        console.recalculatePermissions()
    }

    override fun getEffectivePermissions(): Map<String, PermissionAttachmentInfo> {
        return console.effectivePermissions
    }

    override fun isOp(): Boolean {
        return true
    }

    override fun setOp(b: Boolean) {
        // Empty implementation
    }
}

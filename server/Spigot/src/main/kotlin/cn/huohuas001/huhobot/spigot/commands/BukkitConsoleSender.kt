package cn.huohuas001.huhobot.spigot.commands


import cn.huohuas001.bot.providers.HExecution
import cn.huohuas001.huhobot.spigot.HuHoBotSpigot
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.conversations.Conversation
import org.bukkit.conversations.ConversationAbandonedEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.permissions.PermissionAttachmentInfo
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.CompletableFuture

open class Spigot {
    /**
     * Sends this sender a chat component.
     *
     * @param component the components to send
     */
    open fun sendMessage(component: BaseComponent) {
        throw java.lang.UnsupportedOperationException("Not supported yet.")
    }

    /**
     * Sends an array of components as a single message to the sender.
     *
     * @param components the components to send
     */
    open fun sendMessage(vararg components: BaseComponent) {
        throw java.lang.UnsupportedOperationException("Not supported yet.")
    }

    /**
     * Sends this sender a chat component.
     *
     * @param component the components to send
     * @param sender the sender of the message
     */
    fun sendMessage(sender: UUID?, component: BaseComponent) {
        throw java.lang.UnsupportedOperationException("Not supported yet.")
    }

    /**
     * Sends an array of components as a single message to the sender.
     *
     * @param components the components to send
     * @param sender the sender of the message
     */
    fun sendMessage(sender: UUID?, vararg components: BaseComponent) {
        throw java.lang.UnsupportedOperationException("Not supported yet.")
    }
}

class BukkitConsoleSender(val plugin: HuHoBotSpigot) : ConsoleCommandSender, HExecution {
    private val messageList = mutableListOf<String>()

    override fun isOp(): Boolean {
        return true
    }

    override fun setOp(p0: Boolean) {
        throw UnsupportedOperationException()
    }

    override fun isPermissionSet(p0: String): Boolean {
        return false
    }

    override fun isPermissionSet(p0: Permission): Boolean {
        return false
    }

    override fun hasPermission(p0: String): Boolean {
        return true
    }

    override fun hasPermission(p0: Permission): Boolean {
        return true
    }

    override fun addAttachment(p0: Plugin, p1: String, p2: Boolean): PermissionAttachment {
        messageList.add(p1)
        throw UnsupportedOperationException()
    }

    override fun addAttachment(p0: Plugin): PermissionAttachment {
        throw UnsupportedOperationException()
    }

    override fun addAttachment(p0: Plugin, p1: String, p2: Boolean, p3: Int): PermissionAttachment? {
        messageList.add(p1)
        throw UnsupportedOperationException()
    }

    override fun addAttachment(p0: Plugin, p1: Int): PermissionAttachment? {
        throw UnsupportedOperationException()
    }

    override fun removeAttachment(p0: PermissionAttachment) {
        throw UnsupportedOperationException()
    }

    override fun recalculatePermissions() {
        throw UnsupportedOperationException()
    }

    override fun getEffectivePermissions(): MutableSet<PermissionAttachmentInfo> {
        throw UnsupportedOperationException()
    }

    override fun sendMessage(p0: String) {
        messageList.add(p0)
    }

    override fun sendMessage(vararg p0: String?) {
        for (s in p0) {
            sendMessage(s!!)
        }
    }

    override fun sendMessage(p0: UUID?, p1: String) {
        messageList.add(p1)
    }

    override fun sendMessage(p0: UUID?, vararg p1: String?) {
        for (s in p1) {
            sendMessage(p0, s!!)
        }
    }

    override fun getServer(): Server {
        return Bukkit.getServer()
    }

    override fun getName(): String {
        return "CONSOLE"
    }

    override fun spigot(): CommandSender.Spigot {
        throw UnsupportedOperationException()
    }

    override fun isConversing(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun acceptConversationInput(p0: String) {
        messageList.add(p0)
    }

    override fun beginConversation(p0: Conversation): Boolean {
        throw UnsupportedOperationException()
    }

    override fun abandonConversation(p0: Conversation) {
        throw UnsupportedOperationException()
    }

    override fun abandonConversation(p0: Conversation, p1: ConversationAbandonedEvent) {
        throw UnsupportedOperationException()
    }

    override fun sendRawMessage(p0: String) {
        messageList.add(p0)
    }

    override fun sendRawMessage(p0: UUID?, p1: String) {
        messageList.add(p1)
    }

    override fun getRawString(): String {
        return messageList.joinToString("\n")
    }

    override fun execute(command: String): CompletableFuture<HExecution> {
        Bukkit.dispatchCommand(this, command)
        return CompletableFuture.supplyAsync {
            Thread.sleep(2 * 1000L)
            this
        }
    }
}
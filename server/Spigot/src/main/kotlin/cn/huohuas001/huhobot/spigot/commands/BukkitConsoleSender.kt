package cn.huohuas001.huhobot.spigot.commands

import cn.huohuas001.bot.providers.HExecution
import cn.huohuas001.huhobot.spigot.HuHoBotSpigot
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
import java.util.concurrent.CopyOnWriteArrayList

class BukkitConsoleSender(val plugin: HuHoBotSpigot) : ConsoleCommandSender, HExecution {
    private val messageList = CopyOnWriteArrayList<String>()

    // 简化的 Spigot 实现，只覆盖基础方法
    private val customSpigot = object : CommandSender.Spigot() {
        override fun sendMessage(component: net.md_5.bungee.api.chat.BaseComponent) {
            messageList.add(component.toLegacyText())
        }

        override fun sendMessage(vararg components: net.md_5.bungee.api.chat.BaseComponent) {
            components.forEach {
                messageList.add(it.toLegacyText())
            }
        }
    }

    fun clearMessages() {
        messageList.clear()
    }

    fun getAndClearMessages(): List<String> {
        val messages = messageList.toList()
        messageList.clear()
        return messages
    }

    override fun isOp(): Boolean = true
    override fun setOp(p0: Boolean) {}

    override fun isPermissionSet(p0: String): Boolean = false
    override fun isPermissionSet(p0: Permission): Boolean = false
    override fun hasPermission(p0: String): Boolean = true
    override fun hasPermission(p0: Permission): Boolean = true

    override fun addAttachment(p0: Plugin, p1: String, p2: Boolean): PermissionAttachment {
        throw UnsupportedOperationException()
    }

    override fun addAttachment(p0: Plugin): PermissionAttachment {
        throw UnsupportedOperationException()
    }

    override fun addAttachment(p0: Plugin, p1: String, p2: Boolean, p3: Int): PermissionAttachment? {
        throw UnsupportedOperationException()
    }

    override fun addAttachment(p0: Plugin, p1: Int): PermissionAttachment? {
        throw UnsupportedOperationException()
    }

    override fun removeAttachment(p0: PermissionAttachment) {}
    override fun recalculatePermissions() {}

    override fun getEffectivePermissions(): MutableSet<PermissionAttachmentInfo> {
        return mutableSetOf()
    }

    override fun sendMessage(p0: String) {
        messageList.add(p0)
    }

    override fun sendMessage(vararg p0: String?) {
        p0.filterNotNull().forEach { messageList.add(it) }
    }

    override fun sendMessage(p0: UUID?, p1: String) {
        messageList.add(p1)
    }

    override fun sendMessage(p0: UUID?, vararg p1: String?) {
        p1.filterNotNull().forEach { messageList.add(it) }
    }

    override fun getServer(): Server = Bukkit.getServer()
    override fun getName(): String = "CONSOLE"

    override fun spigot(): CommandSender.Spigot {
        return customSpigot
    }

    override fun isConversing(): Boolean = false
    override fun acceptConversationInput(p0: String) {}
    override fun beginConversation(p0: Conversation): Boolean = false
    override fun abandonConversation(p0: Conversation) {}
    override fun abandonConversation(p0: Conversation, p1: ConversationAbandonedEvent) {}

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
        val future = CompletableFuture<HExecution>()

        clearMessages()

        plugin.submit {
            try {
                Bukkit.dispatchCommand(this, command)

                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    future.complete(this)
                }, 40L)

            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }

        return future
    }
}
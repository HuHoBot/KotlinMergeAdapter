package cn.huohuas001.huhobot.spigot.commands

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.Property
import org.apache.logging.log4j.core.layout.PatternLayout
import java.util.concurrent.CopyOnWriteArrayList

class CommandOutputAppender : AbstractAppender(
    "CommandOutputAppender",
    null,
    PatternLayout.createDefaultLayout(),
    true,
    Property.EMPTY_ARRAY
) {
    private val messages = CopyOnWriteArrayList<String>()
    @Volatile
    private var capturing = false

    init {
        start()
    }

    override fun append(event: LogEvent) {
        if (capturing) {
            messages.add(event.message.formattedMessage)
        }
    }

    fun startCapture() {
        capturing = true
        messages.clear()
    }

    fun stopCapture(): List<String> {
        capturing = false
        return messages.toList()
    }

    companion object {
        private var instance: CommandOutputAppender? = null

        fun getInstance(): CommandOutputAppender {
            if (instance == null) {
                instance = CommandOutputAppender()
                val logger = LogManager.getRootLogger() as org.apache.logging.log4j.core.Logger
                logger.addAppender(instance)
            }
            return instance!!
        }

        fun removeInstance() {
            instance?.let {
                val logger = LogManager.getRootLogger() as org.apache.logging.log4j.core.Logger
                logger.removeAppender(it)
                it.stop()
            }
            instance = null
        }
    }
}
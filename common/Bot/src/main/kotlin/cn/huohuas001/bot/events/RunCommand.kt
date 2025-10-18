package cn.huohuas001.bot.events

class RunCommand: BaseEvent() {
    override fun run(): Boolean {
        val command: String = mBody.getString("cmd")
        sendCommand(command)
        return true
    }
}
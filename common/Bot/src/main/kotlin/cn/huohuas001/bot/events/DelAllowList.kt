package cn.huohuas001.bot.events

import cn.huohuas001.bot.provider.BotShared

class DelAllowList: BaseEvent() {
    override fun run(): Boolean{
        val plugin = BotShared.getPlugin()
        val id: String = mBody.getString("xboxid")
        /*val command:String = plugin.getWhiteList().delCommand.replace("{name}",XboxId)
        sendCommand(command)*/
        plugin.delWhiteList( id)
        val name: String = plugin.getName()
        respone(name + "已接受删除名为" + id + "的白名单请求", "success")
        return true
    }
}
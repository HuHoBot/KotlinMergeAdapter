package cn.huohuas001.bot

import cn.huohuas001.bot.events.*
import cn.huohuas001.bot.events.allowlist.AddAllowList
import cn.huohuas001.bot.events.allowlist.DelAllowList
import cn.huohuas001.bot.events.custom_run.CustomRun
import cn.huohuas001.bot.events.custom_run.CustomRunAdmin
import cn.huohuas001.bot.provider.*
import com.alibaba.fastjson2.JSONObject

interface HuHoBot: LoggerProvider, ConfigProvider, CommandProvider, SchedulerProvider, MessageProvider{
    var eventList: MutableMap<String, BaseEvent> //事件列表
    var bindRequestObj: BindRequest

    fun onMessage(data: JSONObject){
        val header = data.getJSONObject("header")
        val body = data.getJSONObject("body")

        val type = header.getString("type")
        val packId = header.getString("id")

        val event: BaseEvent? = eventList[type]
        if (event != null) {
            event.eventCall(packId, body)
        } else {
            log_error("在处理消息是遇到错误: 未知的消息类型")
            log_error("此错误具有不可容错性!请检查插件是否为最新!")
            log_warning("正在断开连接...")
            ClientManager.shutdownClient()
        }
    }

    /**
     * 注册Websocket事件
     *
     * @param eventName 事件名称
     * @param event     事件对象
     */
    private fun registerEvent(eventName: String, event: BaseEvent) {
        eventList[eventName] = event
    }

    /**
     * 统一事件注册
     */
    private fun totalRegEvent() {
        eventList = HashMap()
        registerEvent("sendConfig", SendConfig())
        registerEvent("shaked", Shaked())
        registerEvent("chat", Chat())
        registerEvent("add", AddAllowList())
        registerEvent("delete", DelAllowList())
        registerEvent("cmd", RunCommand())
        registerEvent("queryList", getQueryAllowList())
        registerEvent("queryOnline", getQueryOnline())
        registerEvent("shutdown", ShutDown())
        registerEvent("run", CustomRun())
        registerEvent("runAdmin", CustomRunAdmin())
        registerEvent("heart", Heart())
        bindRequestObj = BindRequest()
        registerEvent("bindRequest", bindRequestObj)
    }

    fun enableBot(){
        BotShared.setInstance(this)

        totalRegEvent() //注册Websocket事件
        ClientManager.connectServer()
        loadCustomCommand()

        log_info("HuHoBot Loaded. By HuoHuas001")
    }

    /**
     * 在控制台输出绑定ID
     */
    fun sendBindMessage() {
        if (!isHashKeyValue()) {
            val serverId: String = getServerId()
            val message = "服务器尚未在机器人进行绑定，请在群内输入\"/绑定 $serverId\""
            log_warning(message)
        }
    }

    /**
     * 重连HuHoBot服务器
     *
     * @return 是否连接成功
     */
    fun reconnect(): Boolean {
        if (ClientManager.isOpen()) {
            return false
        }
        ClientManager.connectServer()
        return true
    }

    /**
     * 断连HuHoBot服务器
     *
     * @return 是否断连成功
     */
    fun disConnectServer(): Boolean {
        ClientManager.setShouldReconnect(false)
        return ClientManager.shutdownClient()
    }

    /**
     * 获取QueryAllowList
     */
    fun getQueryAllowList(): BaseEvent

    /**
     * 获取QueryOnline
     */
    fun getQueryOnline(): BaseEvent

    /**
     * 调用插件事件
     */
    fun callPluginEvent(command: String, data:JSONObject, packId:String , runByAdmin: Boolean): Boolean{
        return false
    }

    fun addWhiteList(playerName: String)
    fun delWhiteList(playerName: String)
    fun getCallbackConvertImg(): Int
}
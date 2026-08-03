package cn.huohuas001.bot.provider

interface LoggerProvider {
    fun log_info(msg: String)
    fun log_warning(msg: String)
    fun log_error(msg: String)
}
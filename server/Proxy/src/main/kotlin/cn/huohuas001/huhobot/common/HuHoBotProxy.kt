package cn.huohuas001.huhobot.common

import cn.huohuas001.bot.HuHoBot
import cn.huohuas001.huhobot.common.managers.IConfigManager
import cn.huohuas001.huhobot.common.redis.RedisManager

interface HuHoBotProxy: HuHoBot {
    var redisManager: RedisManager?
    var configManager: IConfigManager
}

package cn.huohuas001.bot.tools

import java.util.UUID

fun getPackID(): String {
    val guid:UUID = UUID.randomUUID();
    return guid.toString().replace("-", "");
}
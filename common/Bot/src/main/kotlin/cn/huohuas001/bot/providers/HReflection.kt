package cn.huohuas001.bot.providers

object HReflection {
    @Throws(IllegalAccessException::class)
    fun findFieldByType(instance: Any, type: String) : Any? {
        val clazz: Class<*> = instance.javaClass
        for (field in clazz.declaredFields) {
            if (!field.type.name.endsWith(type)) continue
            field.isAccessible = true
            return field[instance]
        }
        return null
    }
}
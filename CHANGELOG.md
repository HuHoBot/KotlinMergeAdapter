# HuHoBot-Adapter v2.0.9

feat(bot): 优化重连机制并更新配置版本
- 重构了 `ClientManager` 中的重连逻辑，改用异步任务调度替代同步等待

feat(spigot): 优化重连机制并更新配置版本
- 更新配置，添加`CommandSender`配置项，移除`CommandExecutionSort`配置项

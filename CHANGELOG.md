# HuHoBot-Adapter v2.0.7

feat(spigot): 实现混合命令执行器并升级版本

- 添加 Log4j 依赖以支持命令输出捕获
- 实现 CommandOutputAppender 用于捕获服务器日志输出
- 创建 HybridCommandExecutor 整合多种命令执行方式
- 重构 BukkitConsoleSender 使用 CopyOnWriteArrayList 线程安全处理
- 移除废弃的 NativeServerSender、DecidatedServerSender 和 MinecraftServerSender
- 更新配置文件将命令发送器设置为 Hybrid 模式
- 项目版本从 2.0.6 升级至 2.0.7
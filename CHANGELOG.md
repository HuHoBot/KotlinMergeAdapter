# HuHoBot-Adapter v2.1.2

feat(motd): 添加 Markdown 渲染支持和优化在线玩家列表显示
- 在配置中添加 useMarkdown 选项以控制 MOTD 渲染方式
- 优化 ClientManager 中的 MOTD 发送接口，分离玩家名称和文本模板参数

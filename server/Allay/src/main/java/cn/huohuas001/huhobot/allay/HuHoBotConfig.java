package cn.huohuas001.huhobot.allay;

import cn.huohuas001.bot.provider.CustomCommandDetail;
import cn.huohuas001.bot.tools.UtilitiesKt;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 主配置类
@SuppressWarnings("ALL")
public class HuHoBotConfig extends OkaeriConfig {
    @Exclude
    private static final int CURRENT_VERSION = 2;

    // region Getters & Setters
    // region 核心配置
    @Getter
    @Setter
    @Comment({
            "服务器唯一ID (启动时自动生成)",
            "! 请勿手动修改，留空即可"
    })
    public String serverId = null;

    @Getter
    @Setter
    @Comment({
            "通信加密密钥 (绑定后自动获取)",
            "! 请勿手动修改，留空即可"
    })
    public String hashKey = null;
    // endregion

    // region 聊天配置
    @Getter
    @Comment({
            "聊天消息配置",
            "from_game: 游戏端消息格式 (可用变量: {name}, {msg})",
            "from_group: 群聊消息格式 (可用变量: {nick}, {msg})",
            "post_chat: 是否转发聊天消息",
            "post_prefix: 消息转发前缀"
    })
    public ChatConfig chatConfig = new ChatConfig();

    // 弃用旧字段并标记为 transient 防止持久化
    @Deprecated
    private transient String chatFormatGroup;

    // region 新的MOTD配置结构
    @Getter
    @Comment({
            "MOTD服务器配置",
            "api: 状态查询API地址，使用{server_ip}和{server_port}作为占位符",
            "text: 显示文本格式，使用{online}作为在线人数占位符"
    })
    public MotdConfig motd = new MotdConfig();
    // endregion

    @Getter
    @Comment("服务器显示名称")
    public String serverName = "AllayMC";

    // region 自定义命令
    @Getter
    @Comment("自定义命令列表")
    public List<CustomCommand> customCommand = Arrays.asList(
            new CustomCommand("加白名", "whitelist add &1", 0),
            new CustomCommand("管理加白名", "whitelist add &1", 1)
    );
    // endregion

    // 在PluginConfig类顶部添加版本字段
    @Comment("配置版本 (检测到版本小于1时会自动迁移旧配置)")
    @CustomKey("version")
    private int configVersion = 1;
    // endregion

    // 废弃原有motdUrl字段
    @Deprecated
    private transient String motdUrl;

    @Deprecated
    public String getMotdUrl() {
        return motd.getServerIp() + ":" + motd.getServerPort();
    }

    // endregion

    public Map<String, CustomCommand> getCustomCommandMap() {
        return customCommand.stream()
                .collect(Collectors.toMap(CustomCommand::getKey, customCommand -> customCommand, (existing, replacement) -> existing));
    }

    // 初始化方法示例
    public void initializeDefaults() {
        if (this.serverId == null) {
            this.serverId = UtilitiesKt.getPackID();
        }

        // 自动迁移逻辑（当版本号不存在时）
        if (this.configVersion < CURRENT_VERSION) {
            performConfigMigration();
        }
    }

    private void performConfigMigration() {
        // 如果存在旧版 motdUrl 配置
        if (this.motdUrl != null && !this.motdUrl.isEmpty()) {
            String[] parts = this.motdUrl.split(":");
            this.motd.serverIp = parts[0];
            this.motd.serverPort = (parts.length > 1) ?
                    Integer.parseInt(parts[1]) : 19132;
        }

        // 标记旧字段为已弃用（不再持久化）
        this.motdUrl = null;

        // 新增聊天配置迁移
        if (this.chatFormatGroup != null && !this.chatFormatGroup.isEmpty()) {
            this.chatConfig.fromGroup = this.chatFormatGroup;
            this.chatConfig.postChat = true;
            this.chatConfig.postPrefix = "";
        }
        this.chatFormatGroup = null; // 清除旧字段


        this.configVersion = 2; // 更新版本号
    }
    // endregion

    @Getter
    public static class ChatConfig extends OkaeriConfig {
        // Getters
        @Comment("游戏端消息格式")
        @CustomKey("from_game")
        public String fromGame = "<{name}> {msg}";

        @Comment("群聊消息格式")
        @CustomKey("from_group")
        public String fromGroup = "群:<{nick}> {msg}";

        @Comment("是否转发聊天消息")
        @CustomKey("post_chat")
        public boolean postChat = true;

        @Comment("消息转发前缀")
        @CustomKey("post_prefix")
        public String postPrefix = "";
    }

    @Getter
    public static class MotdConfig extends OkaeriConfig {
        // Getters
        @Comment("服务器IP地址")
        @CustomKey("server_ip")
        public String serverIp = "play.easecation.net";

        @Comment("服务器端口")
        @CustomKey("server_port")
        public int serverPort = 19132;

        @Comment("状态查询API地址")
        public String api = "https://motdbe.blackbe.work/status_img?host={server_ip}:{server_port}";

        @Comment("显示文本格式")
        public String text = "共{online}人在线";

        @Comment("是否输出在线列表")
        @CustomKey("output_online_list")
        public boolean outputOnlineList = true;

        @Comment("是否发布状态图片")
        @CustomKey("post_img")
        public boolean postImg = true;
    }

    @Getter
    public static class CustomCommand extends OkaeriConfig {
        // Getters 必须存在
        @Comment("触发指令 (支持中文)")
        private String key;

        @Comment("实际执行的命令 (&1=第一个参数)")
        private String command;

        @Comment("权限等级 0=玩家 1=管理")
        private int permission;

        // 需要无参构造器
        public CustomCommand() {
        }

        public CustomCommand(String key, String command, int permission) {
            this.key = key;
            this.command = command;
            this.permission = permission;
        }

        public CustomCommandDetail convert() {
            return new CustomCommandDetail(this.key, this.command, this.permission);
        }
    }
}
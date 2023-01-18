package cn.starlight.disy.qqbot.bot;

import net.mamoe.mirai.utils.BotConfiguration;

@SuppressWarnings("DuplicateBranchesInSwitch")  // 增加可读性，不要给我这个警告！
public final class BotConfigConvert {

    /**
     * 将配置文件中相应字符串转换成心跳包的方法
     * @param conf 配置文件中对应心跳包配置的字符串
     * @return Mirai心跳包类型（HeartbeatStrategy）
     */
    public static BotConfiguration.HeartbeatStrategy convertHeartbeatStrategy(String conf){
        if(conf == null){
            return BotConfiguration.HeartbeatStrategy.STAT_HB;
        }

        return switch (conf.toUpperCase()) {
            case "DEFAULT" -> BotConfiguration.HeartbeatStrategy.STAT_HB;
            case "STAT_HB" -> BotConfiguration.HeartbeatStrategy.STAT_HB;
            case "REGISTER" -> BotConfiguration.HeartbeatStrategy.REGISTER;
            case "NONE" -> BotConfiguration.HeartbeatStrategy.NONE;
            default -> BotConfiguration.HeartbeatStrategy.STAT_HB;
        };
    }



    /**
     * 将配置文件中相应字符串转换成QQ登录协议的方法
     * @param conf 配置文件中对应协议配置的字符串
     * @return Mirai协议类型（MiraiProtocol）
     */
    public static BotConfiguration.MiraiProtocol convertProtocol(String conf){
        if(conf == null){
            return BotConfiguration.MiraiProtocol.IPAD;
        }

        return switch (conf.toUpperCase()) {
            case "DEFAULT" -> BotConfiguration.MiraiProtocol.IPAD;
            case "ANDROID_WATCH" -> BotConfiguration.MiraiProtocol.ANDROID_WATCH;
            case "ANDROID_PHONE" -> BotConfiguration.MiraiProtocol.ANDROID_PHONE;
            case "ANDROID_PAD" -> BotConfiguration.MiraiProtocol.ANDROID_PAD;
            case "IPAD" -> BotConfiguration.MiraiProtocol.IPAD;
            case "MACOS" -> BotConfiguration.MiraiProtocol.MACOS;
            default -> BotConfiguration.MiraiProtocol.ANDROID_WATCH;
        };
    }
}

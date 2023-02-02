package cn.starlight.disy.qqbot.utils;

import com.google.common.base.Charsets;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.UUID;

import static cn.starlight.disy.qqbot.utils.DatabaseOperates.OTHER_DATA;

public class PlayerOperates {

    private static final File PLAYER_DATA_FILE = new File(OTHER_DATA, "Data.yml");

    /**
     * 通过玩家ID计算其UUID的方法
     * @param playerID 玩家ID
     * @return 离线玩家的UUID
     */
    public static UUID getUUID(String playerID){
        return UUID.nameUUIDFromBytes(( "OfflinePlayer:" + playerID ).getBytes(Charsets.UTF_8 ));
    }


    /**
     * 读取配置文件中玩家的IP
     * @param playerName 待读取的玩家名
     * @return 配置文件中玩家的IP值
     */
    public static String getPlayerIP(String playerName){
        YamlConfiguration dataYaml = YamlConfiguration.loadConfiguration(PLAYER_DATA_FILE);
        String ip = dataYaml.getString(playerName + ".IP");

        return ip == null ? "" : ip;
    }


    /**
     * 将玩家的IP保存至配置文件
     * @param playerName 待设置的玩家名
     * @param playerIP 玩家IP值
     */
    public static void setPlayerIP(String playerName, String playerIP){
        YamlConfiguration dataYaml = YamlConfiguration.loadConfiguration(PLAYER_DATA_FILE);

        dataYaml.set(playerName + ".IP", playerIP);
        try{
            dataYaml.save(PLAYER_DATA_FILE);
        }
        catch (Exception e){
            Logger.error("保存" + playerName + "的IP时出错，以下是堆栈信息：");
            e.printStackTrace();
        }
    }


    /**
     * 读取配置文件中玩家免密登录截止的时间戳值
     * @param playerName 待读取的玩家名
     * @return 截止的时间戳值
     */
    public static long getNoPwdStamp(String playerName){
        YamlConfiguration dataYaml = YamlConfiguration.loadConfiguration(PLAYER_DATA_FILE);
        return dataYaml.getLong(playerName + ".No_Password_Stamp");
    }


    /**
     * 将玩家的IP保存至配置文件
     * @param playerName 待设置的玩家名
     * @param stamp 时间戳值
     */
    public static void setNoPwdStamp(String playerName, long stamp){
        YamlConfiguration dataYaml = YamlConfiguration.loadConfiguration(PLAYER_DATA_FILE);

        dataYaml.set(playerName + ".No_Password_Stamp", stamp);
        try{
            dataYaml.save(PLAYER_DATA_FILE);
        }
        catch (Exception e){
            Logger.error("保存" + playerName + "的时间戳时出错，以下是堆栈信息：");
            e.printStackTrace();
        }
    }


    /**
     * 获取玩家是否处于正版认证状态
     * @param playerName 待获取的玩家名
     * @return 一个boolean值，表示是否处于正版认证状态
     */
    public static boolean isOnlineMode(String playerName){
        YamlConfiguration dataYaml = YamlConfiguration.loadConfiguration(PLAYER_DATA_FILE);
        return dataYaml.getBoolean(playerName + ".Is_Online_Mode");
    }


    /**
     * 设置玩家的正版认证状态
     * @param playerName 待设置的玩家名
     * @param onlineMode 待设置的认证状态，为null则清除该玩家的正版认证信息
     */
    public static void setOnlineMode(String playerName, @Nullable Boolean onlineMode){
        YamlConfiguration dataYaml = YamlConfiguration.loadConfiguration(PLAYER_DATA_FILE);

        dataYaml.set(playerName + ".Is_Online_Mode", onlineMode);
        try{
            dataYaml.save(PLAYER_DATA_FILE);
        }
        catch (Exception e){
            Logger.error("保存" + playerName + "的正版模式时出错，以下是堆栈信息：");
            e.printStackTrace();
        }
    }
}

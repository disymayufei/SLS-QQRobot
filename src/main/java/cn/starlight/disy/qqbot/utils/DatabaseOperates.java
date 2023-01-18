package cn.starlight.disy.qqbot.utils;

import net.mamoe.mirai.contact.Group;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static cn.starlight.disy.qqbot.Main.PLUGIN_INSTANCE;
import static cn.starlight.disy.qqbot.bot.RunnableBot.RUNNABLE_BOT_INSTANCE;

// 返回的错误信息是备用的，部分全局变量也可能在日后有用，因此请不要抛出这些警告！
@SuppressWarnings({"UnusedReturnValue", "FieldCanBeLocal"})

public class DatabaseOperates {

    public static final File DATABASE = new File(PLUGIN_INSTANCE.getDataFolder(), "DataBase");;  // 主目录
    public static final File OTHER_DATA = new File(DATABASE, "OtherData"); // 记录其他额外信息的数据库
    public static final File QQ_DATA = new File(DATABASE, "QQData"); // 记录QQ-玩家对应关系的目录

    private static final File WHITELIST = new File(DATABASE, "WhiteList/WhiteList.yml");  // 白名单文件！（注意是yaml文件！）


    /**
     * 将指定ID加入白名单
     * @param playerID 玩家ID
     * @return 错误信息，如果没有错误则返回null
     */
    public synchronized static String addWhiteList(String playerID){

        if(playerID == null){
            return "不允许输入空内容哦！";
        }

        if(playerID.length() < 4 || playerID.length() > 20){
            return "用户名长度仅允许4~20个字符哦！";
        }

        if (!Pattern.compile("^\\w+$").matcher(playerID).matches()){
            return "用户名仅允许英文字母，数字和下划线哦！";
        }

        YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(WHITELIST);

        List<String> whitelistIDList = whitelistYaml.getStringList("WhiteList");
        if(whitelistIDList.contains(playerID)){
            return "这个ID已经被人绑定过了哦！";
        }
        whitelistIDList.add(playerID);
        whitelistYaml.set("WhiteList", whitelistIDList);

        try{
            whitelistYaml.save(WHITELIST);
            Logger.info("成功将" + playerID + "加入白名单");
            return null;
        }
        catch (Exception e){
            Logger.error("添加" + playerID + "的白名单时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return "添加白名单出错，请联系腐竹查看后台错误信息！";
        }

    }


    /**
     * 将指定ID移出白名单
     * @param playerID 玩家ID
     * @return 错误信息，如果没有错误则返回null
     */
    public synchronized static String delWhiteList(String playerID){

        YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(WHITELIST);

        List<String> whitelistIDList = whitelistYaml.getStringList("WhiteList");
        if(!whitelistIDList.contains(playerID)){
            return "这个ID并没有被任何人绑定过哦！";
        }

        whitelistIDList.remove(playerID);
        whitelistYaml.set("WhiteList", whitelistIDList);

        try{
            whitelistYaml.save(WHITELIST);
            Logger.info("成功将" + playerID + "移出白名单");
            return null;
        }
        catch (Exception e){
            Logger.error("移除" + playerID + "的白名单时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return "移出白名单出错，请联系腐竹查看后台错误信息！";
        }
    }


    /**
     * 将指定ID移出白名单
     * @param playerIDList 多个玩家ID的数组
     * @return 错误信息，如果没有错误则返回null
     */
    public synchronized static String delWhiteList(List<String> playerIDList){

        YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(WHITELIST);

        List<String> whitelistIDList = whitelistYaml.getStringList("WhiteList");

        for(String playerID : playerIDList){
            if(!whitelistIDList.contains(playerID)){
                continue;
            }

            whitelistIDList.remove(playerID);
        }

        whitelistYaml.set("WhiteList", whitelistIDList);

        try{
            whitelistYaml.save(WHITELIST);
            Logger.info("成功将" + playerIDList + "移出白名单");
        }
        catch (Exception e){
            Logger.error("移除" + playerIDList + "的白名单时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return "移出白名单出错，请联系腐竹查看后台错误信息！";
        }

        return null;
    }


    /**
     * 判断一个人是否在白名单中
     * @param playerID 该玩家的id
     * @return 一个boolean值，表示玩家是否在白名单中
     */
    public synchronized static boolean isInWhiteList(String playerID){
        YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(WHITELIST);

        List<String> whitelistIDList = whitelistYaml.getStringList("WhiteList");

        return whitelistIDList.contains(playerID);
    }


    /**
     * 判断一个人是否通过了审核（同时可在数据库不存在时自动创建）
     * @param QQNumber 该玩家的QQ号
     * @return 一个boolean值，表示玩家是否通过了审核
     */
    public synchronized static boolean hadPassedTheExam(long QQNumber){
        try {
            File playerDatabase = new File(QQ_DATA, (QQNumber + ".yml"));

            if (!playerDatabase.exists()) {
                playerDatabase.createNewFile();  // 数据库不存在的时候自动创建一个
                return false;
            }

            YamlConfiguration playerDatabaseYaml = YamlConfiguration.loadConfiguration(playerDatabase);
            return playerDatabaseYaml.getBoolean("Had_Allowed");
        }
        catch (Exception e){
            Logger.error("检查" + QQNumber + "的审核情况时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 通过玩家审核的方法
     * @param QQNumber 玩家QQ号
     * @return 错误信息，若未发生错误则返回null
     */
    public synchronized static String passTheExam(long QQNumber){
        try {
            File playerDatabase = new File(QQ_DATA, (QQNumber + ".yml"));

            if (!playerDatabase.exists()) {
                playerDatabase.createNewFile();  // 数据库不存在的时候自动创建一个
            }

            YamlConfiguration playerDatabaseYaml = YamlConfiguration.loadConfiguration(playerDatabase);
            if(playerDatabaseYaml.getBoolean("Had_Allowed")){
                return "玩家" + QQNumber + "似乎已经通过了审核诶！";
            }

            playerDatabaseYaml.set("Had_Allowed", true);

            playerDatabaseYaml.save(playerDatabase);

            return null;
        }
        catch (Exception e){
            Logger.error("通过" + QQNumber + "的审核时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return "通过审核出错啦！请联系腐竹查看后台吧！";
        }
    }


    /**
     * 撤销玩家审核的方法
     * @param QQNumber 玩家QQ号
     * @return 错误信息，若未发生错误则返回null
     */
    public synchronized static String withdrawTheExamResult(long QQNumber){
        try {
            File playerDatabase = new File(QQ_DATA, (QQNumber + ".yml"));

            if (!playerDatabase.exists()) {
                playerDatabase.createNewFile();  // 数据库不存在的时候自动创建一个
            }


            YamlConfiguration playerDatabaseYaml = YamlConfiguration.loadConfiguration(playerDatabase);

            if(!playerDatabaseYaml.getBoolean("Had_Allowed")){
                return "TA似乎还未通过审核诶！";
            }

            playerDatabaseYaml.set("Had_Allowed", false);

            playerDatabaseYaml.save(playerDatabase);

            return null;
        }
        catch (Exception e){
            Logger.error("取消" + QQNumber + "的审核时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return "取消审核出错啦！请联系腐竹查看后台吧！";
        }
    }


    /**
     * 添加管理员的方法
     * @param QQNumber 要添加为管理的QQ号
     * @return 错误信息，若未发生错误则返回null
     */
    public synchronized static String addAdmin(long QQNumber){
        try {
            List<Long> adminList = PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ");
            if(adminList.contains(QQNumber)){
                return "TA已经是管理了诶！";
            }
            adminList.add(QQNumber);

            PLUGIN_INSTANCE.getConfig().set("Admins_QQ", adminList);
            PLUGIN_INSTANCE.saveConfig();
            PLUGIN_INSTANCE.reloadConfig();

            return null;
        }
        catch (Exception e){
            Logger.error("添加" + QQNumber + "为管理员时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return "腐竹腐竹！添加管理员出错了，快去查看后台吧！";
        }
    }


    /**
     * 移除管理员的方法
     * @param QQNumber 要移除管理的QQ号
     * @return 错误信息，若未发生错误则返回null
     */
    public synchronized static String delAdmin(long QQNumber){
        try {
            List<Long> adminList = PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ");
            if(!adminList.contains(QQNumber)){
                return "TA都不是管理，让我怎么取消嘛！";
            }
            adminList.remove(QQNumber);

            PLUGIN_INSTANCE.getConfig().set("Admins_QQ", adminList);
            PLUGIN_INSTANCE.saveConfig();
            PLUGIN_INSTANCE.reloadConfig();

            return null;
        }
        catch (Exception e){
            Logger.error("移除" + QQNumber + "为管理员时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return "腐竹腐竹！移除管理员出错了，快去查看后台吧！";
        }
    }


    /**
     * 查询一个人绑定过的ID
     * @param QQNumber 该玩家的QQ号
     * @return 包含其绑定过的所有ID的列表
     */
    public synchronized static List<String> checkBindID(long QQNumber){
        File IDFile = new File(QQ_DATA, (QQNumber + ".yml"));

        if(!IDFile.exists()){
            return Collections.emptyList();
        }

        YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(IDFile);
        return whitelistYaml.getStringList("Bind_ID");
    }


    /**
     * 为指定QQ添加ID绑定（同时将该ID纳入白名单中）
     * @param QQNumber 该ID对应的QQ号
     * @param playerID 待绑定的玩家ID
     * @return 绑定的错误信息，如果未发生错误则返回null
     */
    public synchronized static String addBindID(long QQNumber, String playerID){

        if(isInWhiteList(playerID)){
            return "ID: \"" + playerID + "\"已经被人绑定过了哦！";
        }

        File IDFile = new File(QQ_DATA, (QQNumber + ".yml"));

        try{
            if(!IDFile.exists()){
                if(!IDFile.createNewFile()){
                    return "绑定ID失败，请联系腐竹检查文件创建状态!";
                }
            }

            YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(IDFile);

            List<String> bindIDList = whitelistYaml.getStringList("Bind_ID");

            String bindStatus = addWhiteList(playerID);
            if(bindStatus == null) {  // 判断添加白名部分是否成功
                bindIDList.add(playerID);
                whitelistYaml.set("Bind_ID", bindIDList);
                whitelistYaml.save(IDFile);
            }

            return bindStatus;
        }
        catch (Exception e){
            Logger.error("绑定" + QQNumber + "的ID：" + playerID + "时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return "绑定ID出错，请联系腐竹查看后台错误信息！";
        }
    }


    /**
     * 为指定QQ解除ID绑定（同时将该ID移出白名单）
     * @param QQNumber 该ID对应的QQ号
     * @param playerID 待绑定的玩家ID
     * @return 绑定的错误信息，如果未发生错误则返回null
     */
    public synchronized static String delBindID(long QQNumber, String playerID){

        if(!isInWhiteList(playerID)){
            return "ID: \"" + playerID + "\"还没有被任何人绑定过哦！";
        }

        File IDFile = new File(QQ_DATA, (QQNumber + ".yml"));

        try{
            if(!IDFile.exists()){
                return "你还没绑定过任何ID诶，这让我怎么删除呢？";
            }

            YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(IDFile);

            List<String> bindIDList = whitelistYaml.getStringList("Bind_ID");
            if(!bindIDList.contains(playerID)){
                return "这个ID又不是你绑定的！不给你删！";
            }

            String delStatus = delWhiteList(playerID);
            if(delStatus == null) {
                bindIDList.remove(playerID);

                whitelistYaml.set("Bind_ID", bindIDList);
                whitelistYaml.save(IDFile);
            }

            return delStatus;
        }
        catch (Exception e){
            Logger.error("删除" + QQNumber + "的ID：" + playerID + "时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return "删除ID出错，请联系腐竹查看后台错误信息！";
        }
    }


    /**
     * 强制删除一个ID（建议仅腐竹可用）
     * @param playerID 要强制删除的ID
     * @return 错误信息，若未发生错误则返回null
     */
    public synchronized static String forceDelBindID(String playerID){
        File[] IDFilesArray = QQ_DATA.listFiles();

        if(IDFilesArray == null){
            return "ID: \"" + playerID + "\"还没有被任何人绑定过哦！";
        }

        for(File IDFile : IDFilesArray){
            if(IDFile.isFile()){
                try {
                    YamlConfiguration IDYaml = YamlConfiguration.loadConfiguration(IDFile);
                    List<String> IDList = IDYaml.getStringList("Bind_ID");
                    if (IDList.contains(playerID)) {
                        IDList.remove(playerID);
                        IDYaml.set("Bind_ID", IDList);
                        IDYaml.save(IDFile);
                        delWhiteList(playerID);

                        return null;
                    }
                }
                catch (Exception e){
                    Logger.error("强制删除ID：" + playerID + "时出错！以下是错误的堆栈信息：");
                    e.printStackTrace();
                    return "删除ID出错，快去查看后台错误信息吧！";
                }
            }
        }

        return "ID: \"" + playerID + "\"还没有被任何人绑定过哦！";
    }


    /**
     * 反查一个ID的QQ拥有者（建议仅管理可用）
     * @param playerID 要强制删除的ID
     * @return 该玩家的QQ号，若没有任何人绑定该ID，则返回null
     */
    public synchronized static String findQQByID(String playerID){
        File[] IDFilesArray = QQ_DATA.listFiles();

        if(IDFilesArray == null){
            return null;
        }

        for(File IDFile : IDFilesArray){
            if(IDFile.isFile()){
                try {
                    YamlConfiguration IDYaml = YamlConfiguration.loadConfiguration(IDFile);
                    List<String> IDList = IDYaml.getStringList("Bind_ID");
                    if (IDList.contains(playerID)) {
                        return IDFile.getName().replace(".yml", "");
                    }
                }
                catch (Exception e){
                    Logger.error("强制删除ID：" + playerID + "时出错！以下是错误的堆栈信息：");
                    e.printStackTrace();
                    return "删除ID出错，快去查看后台错误信息吧！";
                }
            }
        }

        return null;
    }


    /**
     * 删除一个人全部的信息（包括QQ号对应关系与白名单）
     * @param QQNumber 要删除用户的QQ号
     * @return 错误信息，如果没有错误则返回null
     */
    public synchronized static String clearPersonalData(long QQNumber){
        try{
            File personalDataFile = new File(QQ_DATA, (QQNumber + ".yml"));

            if(personalDataFile.exists()){
                YamlConfiguration personDataYaml = YamlConfiguration.loadConfiguration(personalDataFile);

                List<String> personalIDList = personDataYaml.getStringList("Bind_ID");
                delWhiteList(personalIDList);  // 清除掉TA所有的白名单数据

                personalDataFile.delete();  // 然后就删掉这个方法
            }
        }
        catch (Exception e){
            Logger.error("移除" + QQNumber + "的个人信息时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return "清除个人信息出错，请联系腐竹查看后台错误信息！";
        }

        return null;
    }


    /**
     * 同步白名单与绑定的ID，恢复数据
     */
    public synchronized static void recoverAllData(){
        try {
            File[] personalDataFiles = QQ_DATA.listFiles();

            YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(WHITELIST);
            List<String> rawWhitelistIDList = whitelistYaml.getStringList("WhiteList");
            List<String> finalWhitelistIDList = new ArrayList<>();

            if(personalDataFiles != null){
                for(File f : personalDataFiles){
                    if(f.isFile()){
                        if(f.getName().contains(".yml")){
                            YamlConfiguration personDataYaml = YamlConfiguration.loadConfiguration(f);
                            List<String> personalIDList = personDataYaml.getStringList("Bind_ID");

                            rawWhitelistIDList.addAll(personalIDList);
                        }
                    }
                }
            }

            for(String playerID : rawWhitelistIDList){
                if(!finalWhitelistIDList.contains(playerID)){
                    finalWhitelistIDList.add(playerID);
                }
            }

            whitelistYaml.set("WhiteList", finalWhitelistIDList);
            whitelistYaml.save(WHITELIST);

        }
        catch (Exception e){
            Logger.error("同步ID时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
        }
    }

    /**
     * 将全部不在服务器群中的人的白名单撤销
     */
    public static void kickAllDeadMember(){
        Logger.info("准备清理退群人员白名");

        File[] QQList = QQ_DATA.listFiles();
        long mainServerGroup = PLUGIN_INSTANCE.getConfig().getLong("Main_Server_Group");

        if(QQList != null){
            for(File playerData : QQList){
                if(!playerData.isDirectory()){
                    if(playerData.getName().contains(".yml")){
                        Long playerQQNum = convertToLong(playerData.getName().replace(".yml", ""));
                        // 通过数据库文件名转换对应到玩家QQ号
                        if(playerQQNum != null){
                            Group serverGroup = RUNNABLE_BOT_INSTANCE.getCore().getGroup(mainServerGroup);
                            if(serverGroup != null){
                                if(serverGroup.get(playerQQNum) == null){
                                    String afterRemoveMsg = DatabaseOperates.clearPersonalData(playerQQNum);
                                    if(afterRemoveMsg == null){
                                        Logger.warn(playerQQNum + "已不在群中，其个人信息已被移除");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Logger.info("清理退群人员白名工作完成！");
    }

    /**
     * 将字符串转为long型变量的方法
     * @param num 待转换的字符串
     * @return 转换后的Long（包装类），如果不能转换则返回null
     */
    private static Long convertToLong(String num){
        Long result = null;
        try {
            result = Long.parseLong(num);
        }
        catch (NumberFormatException ignored){}

        return result;
    }
}

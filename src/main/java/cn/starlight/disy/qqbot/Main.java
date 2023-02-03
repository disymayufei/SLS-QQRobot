package cn.starlight.disy.qqbot;

import cn.starlight.disy.qqbot.bot.RunnableBot;
import cn.starlight.disy.qqbot.listeners.PlayerOnJoin;
import cn.starlight.disy.qqbot.network.WSServer;
import cn.starlight.disy.qqbot.utils.DatabaseOperates;
import cn.starlight.disy.qqbot.utils.Logger;
import cn.starlight.disy.qqbot.utils.PasswordGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.server.WebSocketServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import static cn.starlight.disy.qqbot.bot.RunnableBot.RUNNABLE_BOT_INSTANCE;

public class Main extends JavaPlugin {

    public static Main PLUGIN_INSTANCE = null;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Thread WSS_THREAD = null;

    @Override
    public void onEnable(){
        PLUGIN_INSTANCE = this;

        Logger.info("插件已加载，版本V3.0");
        Logger.info("Bot准备加载！");

        initDatabase();

        RUNNABLE_BOT_INSTANCE = new RunnableBot();
        RunnableBot.BOT_THREAD = new Thread(RUNNABLE_BOT_INSTANCE);

        RunnableBot.BOT_THREAD.start();

        int listenedPort = this.getConfig().getInt("WS_Port");
        if(listenedPort <= 0 || listenedPort >= 65536){
            Logger.warn("检测到不合法的WebSocket端口，已自动替换为默认端口！");
            listenedPort = 16123;
        }

        WebSocketServer wsServer = new WSServer(new InetSocketAddress("0.0.0.0", listenedPort));

        WSS_THREAD = new Thread(wsServer);
        WSS_THREAD.start();

        Bukkit.getPluginManager().registerEvents(new PlayerOnJoin(), this);
    }

    @Override
    public void onDisable(){
        if(RunnableBot.BOT_THREAD != null){
            RunnableBot.BOT_THREAD.interrupt();
        }

        if(WSS_THREAD != null){
            WSS_THREAD.interrupt();
        }

        PLUGIN_INSTANCE = null;

        Logger.info("Bot关闭了哦，有缘再见！");
    }

    /**
     * 初始化数据库
     */
    private void initDatabase(){
        this.saveDefaultConfig();  // 创建默认的Config文件

        /* 适配旧的config文件 */
        if(this.getConfig().getString("Announce_Token") == null){
            try{
                this.getConfig().set("Announce_Token", PasswordGenerator.gen());
            }
            catch (Exception e){
                this.getConfig().set("Announce_Token", "CHANGEME!!");
            }
            saveConfig();
        }

        if(this.getConfig().getString("Send_Pic_While_Invite") == null){
            this.getConfig().set("Send_Pic_While_Invite", true);

            saveConfig();
        }

        File database = DatabaseOperates.DATABASE;

        if(!database.exists() && !database.isDirectory()){  // 如果没有DataBase文件夹
            if(!database.mkdirs()){  // 那就创建它
                Logger.error("主数据库创建失败，插件即将自动关闭！请进行检查！");
                this.getPluginLoader().disablePlugin(this);
            }
        }


        File whitelist = new File(database, "WhiteList");  // 保存白名单的文件夹

        if(!whitelist.exists() && !whitelist.isDirectory()){
            if(!whitelist.mkdirs()){
                Logger.error("白名单数据库创建失败，插件即将自动关闭！请进行检查！");
                this.getPluginLoader().disablePlugin(this);
            }
        }

        File whitelistYml = new File(whitelist, "WhiteList.yml");

        if(!whitelistYml.exists()){
            try {
                whitelistYml.createNewFile();

                YamlConfiguration whitelistConf = YamlConfiguration.loadConfiguration(whitelistYml);

                whitelistConf.set("WhiteList", new ArrayList<String>());  // 创建默认白名（空白名）
                whitelistConf.save(whitelistYml);
            }
            catch (IOException e){
                Logger.error("白名单数据库(YAML)创建错误，插件即将自动关闭！以下是错误的堆栈信息：");
                e.printStackTrace();
                this.getPluginLoader().disablePlugin(this);
            }
        }


        File qqData = new File(database, "QQData");  // 保存QQ号与ID对应关系的文件夹

        if(!qqData.exists() && !qqData.isDirectory()){
            if(!qqData.mkdirs()){
                Logger.error("QQ信息数据库创建失败，插件即将自动关闭！请进行检查！");
                this.getPluginLoader().disablePlugin(this);
            }
        }


        File otherData = new File(database, "OtherData");

        if(!otherData.exists() && !otherData.isDirectory()){
            if(!otherData.mkdirs()){
                Logger.error("补充数据库创建失败，插件即将自动关闭！请进行检查！");
                this.getPluginLoader().disablePlugin(this);
            }
        }

        File otherDataYml = new File(otherData, "Data.yml");

        if(!otherDataYml.exists()){
            try {
                otherDataYml.createNewFile();
            }
            catch (IOException e){
                Logger.error("补充数据库(YAML)创建错误，插件即将自动关闭！以下是错误的堆栈信息：");
                e.printStackTrace();
                this.getPluginLoader().disablePlugin(this);
            }
        }

        File denyListYml = new File(otherData, "DenyList.yml");
        if(!denyListYml.exists()){
            try {
                denyListYml.createNewFile();
            }
            catch (IOException e){
                Logger.error("审核未通过列表(YAML)创建错误，插件即将自动关闭！以下是错误的堆栈信息：");
                e.printStackTrace();
                this.getPluginLoader().disablePlugin(this);
            }
        }


        File helpData = new File(database, "HelpFile");
        if(!helpData.exists() && !helpData.isDirectory()){
            if(!helpData.mkdirs()){
                Logger.error("帮助文档库创建失败，插件即将自动关闭！请进行检查！");
                this.getPluginLoader().disablePlugin(this);
            }
        }


        File helpDataFileNormal = new File(helpData, "player_help_msg.txt");

        if(!helpDataFileNormal.exists()){
            try {
                helpDataFileNormal.createNewFile();
            }
            catch (IOException e){
                Logger.error("玩家帮助文档创建错误，插件即将自动关闭！以下是错误的堆栈信息：");
                e.printStackTrace();
                this.getPluginLoader().disablePlugin(this);
            }
        }

        File qAndADataFileNormal = new File(helpData, "player_q_and_a_msg.txt");

        if(!qAndADataFileNormal.exists()){
            try {
                qAndADataFileNormal.createNewFile();
            }
            catch (IOException e){
                Logger.error("玩家Q&A文档创建错误，插件即将自动关闭！以下是错误的堆栈信息：");
                e.printStackTrace();
                this.getPluginLoader().disablePlugin(this);
            }
        }

        File helpDataFileAdmin = new File(helpData, "admin_help_msg.txt");

        if(!helpDataFileAdmin.exists()){
            try {
                helpDataFileAdmin.createNewFile();
            }
            catch (IOException e){
                Logger.error("玩家帮助文档创建错误，插件即将自动关闭！以下是错误的堆栈信息：");
                e.printStackTrace();
                this.getPluginLoader().disablePlugin(this);
            }
        }

        File welcomeFile = new File(helpData, "welcome_msg.txt");

        if(!welcomeFile.exists()){
            try {
                welcomeFile.createNewFile();
            }
            catch (IOException e){
                Logger.error("欢迎新成员文档创建错误，插件即将自动关闭！以下是错误的堆栈信息：");
                e.printStackTrace();
                this.getPluginLoader().disablePlugin(this);
            }
        }

        changePermission(this.getDataFolder());
    }

    /**
     * 增加权限，使路径及子路径都有权限
     */
    public void changePermission(File root){
        try {
            if (!System.getProperty("os.name").startsWith("Win")) {
                String cmdGrant = "chmod -R 777 " + root.getAbsolutePath();
                Runtime.getRuntime().exec(cmdGrant);
            }
        }
        catch (Exception e){
            Logger.warn("文件权限修改失败，这可能会导致您在修改和删除前，需要手动设置权限！");
            if(this.getConfig().getBoolean("Debug_Mode")){
                Logger.warn("以下是错误的堆栈信息:");
                e.printStackTrace();
            }
        }
    }
}

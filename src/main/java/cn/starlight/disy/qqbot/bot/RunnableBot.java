package cn.starlight.disy.qqbot.bot;

import cn.starlight.disy.qqbot.utils.DatabaseOperates;
import cn.starlight.disy.qqbot.utils.Logger;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.BotConfiguration;

import java.io.File;

import static cn.starlight.disy.qqbot.Main.PLUGIN_INSTANCE;
import static cn.starlight.disy.qqbot.bot.AdminOperates.ADMIN_OPERATES_INSTANCE;
import static cn.starlight.disy.qqbot.bot.NormalMemberOperates.NORMAL_MEMBER_OPERATES_INSTANCE;

public class RunnableBot implements Runnable{
    public static RunnableBot RUNNABLE_BOT_INSTANCE = null;
    public static Bot BOT_INSTANCE;

    public static Thread BOT_THREAD = null;


    public RunnableBot(){
        Thread currentThread = Thread.currentThread();
        currentThread.setContextClassLoader(Mirai.class.getClassLoader());

        long QQNumber = PLUGIN_INSTANCE.getConfig().getLong("Account");  // QQ号
        String password = PLUGIN_INSTANCE.getConfig().getString("Password");  // 密码

        String botProtocol = PLUGIN_INSTANCE.getConfig().getString("Bot_Protocol");
        String botHeartbeatStrategy = PLUGIN_INSTANCE.getConfig().getString("Bot_HeartbeatStrategy");

        File workDir = new File(PLUGIN_INSTANCE.getDataFolder(), "Mirai/WorkData");

        if(!workDir.exists()){
            workDir.mkdirs();
        }

        BOT_INSTANCE = BotFactory.INSTANCE.newBot(
                QQNumber,  // QQ号
                password,  // 密码
                new BotConfiguration(){
                    {
                        if(!PLUGIN_INSTANCE.getConfig().getBoolean("Show_Mirai_Bot_Log")){
                            noBotLog();
                        }
                        if(!PLUGIN_INSTANCE.getConfig().getBoolean("Show_Network_Log")){
                            noNetworkLog();
                        }

                        fileBasedDeviceInfo();
                        setProtocol(BotConfigConvert.convertProtocol(botProtocol));
                        setWorkingDir(workDir);
                        setHeartbeatStrategy(BotConfigConvert.convertHeartbeatStrategy(botHeartbeatStrategy));
                    }
                });
    }

    private void launchBot(){
        Logger.info("机器人正在启动...");

        try{
            BOT_INSTANCE.login();  // 登录机器人
        }
        catch (Exception e){
            Logger.error("机器人启动失败！服务器即将自动关闭，以下是错误的堆栈信息：");
            e.printStackTrace();
            PLUGIN_INSTANCE.getServer().shutdown();  // 启动失败即关服
        }

        DatabaseOperates.kickAllDeadMember();  // 移除所有退群玩家的ID
        DatabaseOperates.recoverAllData();  // 初始化时自动同步数据，防止白名与绑定ID的不一致

        /* 清除旧群残留玩家 */
        new Thread(NORMAL_MEMBER_OPERATES_INSTANCE::kickPassedMember).start();

        /* 注册所有Bot事件 */
        BOT_INSTANCE.getEventChannel().subscribeAlways(GroupMessageEvent.class, (event) -> {
            String mes = event.getMessage().contentToString();
            if(mes.startsWith("#测试 ")){
                String sendingMes = mes.substring(4).replace("<br>", "\n");
                ForwardMessageBuilder helpMesBuilder = new ForwardMessageBuilder(event.getGroup());
                helpMesBuilder.add(event.getSender(), new PlainText(sendingMes));
                event.getGroup().sendMessage(helpMesBuilder.build());
            }
            else if(mes.startsWith("#测试help ")){
                String numStr = mes.substring(8);
                int num;
                try{
                    num = Integer.parseInt(numStr);
                }
                catch (Exception e){
                    return;
                }

                String help = NormalMemberOperates.getPlayerHelpText().substring(0, num);
                ForwardMessageBuilder helpMesBuilder = new ForwardMessageBuilder(event.getGroup());
                helpMesBuilder.add(event.getSender(), new PlainText(help));
                event.getGroup().sendMessage(helpMesBuilder.build());
            }
        });
        BOT_INSTANCE.getEventChannel().subscribeAlways(GroupMessageEvent.class, NORMAL_MEMBER_OPERATES_INSTANCE::operatesListener);  // 玩家指令相关
        BOT_INSTANCE.getEventChannel().subscribeAlways(GroupMessageEvent.class, ADMIN_OPERATES_INSTANCE::adminMes);  // 管理指令相关

        BOT_INSTANCE.getEventChannel().subscribeAlways(MemberJoinEvent.class, NORMAL_MEMBER_OPERATES_INSTANCE::welcomeNewMember);  // 欢迎新成员相关
        BOT_INSTANCE.getEventChannel().subscribeAlways(MemberJoinRequestEvent.class, NORMAL_MEMBER_OPERATES_INSTANCE::checkLegitimacyWhileJoin);  // 加群时自动验证是否通过审核
        /* 注册完毕 */

        Logger.info("机器人启动完成！");
    }

    public Bot getCore(){
        return BOT_INSTANCE;
    }

    @Override
    public void run() {
        BOT_THREAD = new Thread(this::launchBot);
        BOT_THREAD.start();  // 启动机器人

        while (!Thread.currentThread().isInterrupted()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                try{
                    BOT_INSTANCE.close();
                }
                catch (NoClassDefFoundError ignored){}

                Logger.warn("机器人已成功关闭！");
                return;
            }
        }
    }
}

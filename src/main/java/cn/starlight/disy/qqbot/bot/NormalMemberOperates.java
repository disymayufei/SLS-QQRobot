package cn.starlight.disy.qqbot.bot;

import cn.starlight.disy.qqbot.Main;
import cn.starlight.disy.qqbot.network.WSServer;
import cn.starlight.disy.qqbot.utils.*;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.file.AbsoluteFolder;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
import org.java_websocket.WebSocket;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static cn.starlight.disy.qqbot.Main.GSON;
import static cn.starlight.disy.qqbot.Main.PLUGIN_INSTANCE;
import static cn.starlight.disy.qqbot.bot.RunnableBot.BOT_INSTANCE;
import static cn.starlight.disy.qqbot.bot.RunnableBot.RUNNABLE_BOT_INSTANCE;
import static cn.starlight.disy.qqbot.utils.DatabaseOperates.DATABASE;

public class NormalMemberOperates {

    public static final NormalMemberOperates NORMAL_MEMBER_OPERATES_INSTANCE = new NormalMemberOperates();

    private final List<String> ALIVE_MSG = Arrays.asList(
            "我还活着诶！",
            "内个...有什么事嘛？",
            "Hi!",
            "Hello",
            "你好吖！",
            "老叫我我是会烦你的哦",
            "让我看看是哪个屑又在叫我了",
            "你别看我是一个bot，但偶尔我也是会有小情绪的",
            "110010010111101",
            "pong",
            "你知道吗，我是用Java写出来的哦！不是Jvav是Java！",
            "最近有没有什么八卦，和我聊聊！（星星眼）",
            "我觉得这个功能已经被滥用了...",
            "不要想着靠你一个人试出来所有确认我存活后我会说的话，因为这样你会被当做刷屏被管理t掉的！",
            "哼哼哼，啊啊啊啊啊啊啊啊啊啊啊",
            "你说说你，天天不干点正事，老确认我存活干什么！",
            "宝你觉得我今天是不是又瘦一些了",
            "刷屏警告！虽然你也许没想刷屏就是了...",
            "我猜你一定是很无聊吧，要不为啥老确认一个机器人的存活状况...",
            "不要用存活确认刷屏哦，否则管理员会t掉你的！",
            "如果你缺人陪你聊天，你可以等我恢复智能，而不是一直确认存活",
            "真的，有时候我会说胡话，但这并不代表我喜欢你！",
            "Alive！",
            "你好呀，是不是又叫我了",
            "老叫我我会生气的[○･｀Д´･ ○]！",
            "不用确认了，说了我在！",
            "你猜我现在是不是智能bot",
            "我在我在！",
            "叫我干嘛？",
            "天天叫我还不进服玩的屑",
            "一切OK哦！",
            "总确认我存活干嘛？难道说...你喜欢我！",
            "我活着活着活着！！",
            "下次你不如发个ping包给我？",
            "快进服玩！",
            "今天又是个好天气诶...等下，你是不是叫了我！",
            "我已出仓，感觉良好！",
            "SL小Bot在此向你问好！"
    );


    /* 预编译的正则，防止接收消息时临时编译，拖慢机器人处理速度 */
    private final Pattern ID_CHECK_REGEX = Pattern.compile("^[A-Za-z0-9_-]{3,20}$");
    private final Pattern BIND_ID_REGEX = Pattern.compile("^[#＃]绑定ID[:：]", Pattern.CASE_INSENSITIVE);
    private final Pattern DEL_ID_REGEX = Pattern.compile("^[#＃]删除ID[:：]", Pattern.CASE_INSENSITIVE);
    private final Pattern SEARCH_ID_REGEX = Pattern.compile("^[#＃]查询绑定ID", Pattern.CASE_INSENSITIVE);
    private final Pattern PWD_FREE_REGEX = Pattern.compile("^[#＃]免密(登录|登陆)[:：]");
    private final Pattern HELP_REGEX = Pattern.compile("^[#＃](help|帮助|获取帮助)$", Pattern.CASE_INSENSITIVE);
    private final Pattern PLAYER_HELP_REGEX = Pattern.compile("^[#＃]玩家帮助列表$");
    private final Pattern CHECK_SERVER_REGEX = Pattern.compile("^[#＃]查服$");
    private final Pattern CHECK_INTERNAL_SERVER_REGEX = Pattern.compile("^[#＃]查内服$");
    private final Pattern ONLINE_MODE_HELP_REGEX = Pattern.compile("^[#＃]正版(认证|验证)");
    private final Pattern ONLINE_MODE_REGEX = Pattern.compile("^[#＃](确认|取消)正版(认证|验证)[:：]");
    private final Pattern EXECUTE_SINGLE_CMD_REGEX = Pattern.compile("^[#＃]执行命令[:：]");
    private final Pattern Q_AND_A_REGEX = Pattern.compile("^[#＃]Q", Pattern.CASE_INSENSITIVE);
    private final Pattern SHOUT_TO_INTERNAL = Pattern.compile("^[#＃]内服喊话[:：]");

    private final Pattern IMAGE_AND_VIDEO_FILE_REGEX = Pattern.compile("(.jpg|.jpeg|.jiff|.png|.gif|.bmp|.webp|.mp4|.mov|.mkv)$");
    /* 正则部分常量结束 */


    /* 接下来将是很长的一段if */
    public void operatesListener(GroupMessageEvent event){
        String groupMes = event.getMessage().contentToString();
        long groupID = event.getGroup().getId();
        long senderID = event.getSender().getId();

        // 存活确认
        if("#存活确认".equals(groupMes)){
            if (PLUGIN_INSTANCE.getConfig().getLongList("Bind_Only_Group").contains(groupID)) {
                event.getGroup().sendMessage(new MessageChainBuilder()
                        .append(new At(senderID))
                        .append(" ")
                        .append(this.ALIVE_MSG.get(new Random().nextInt(this.ALIVE_MSG.size())))
                        .build());
            }
        }
        
        // 绑定/删除ID
        else if(BIND_ID_REGEX.matcher(groupMes).find() || DEL_ID_REGEX.matcher(groupMes).find()){
            if(PLUGIN_INSTANCE.getConfig().getLongList("Bind_Only_Group").contains(groupID)) {

                /* 以下是绑定ID的部分 */
                if (BIND_ID_REGEX.matcher(groupMes).find()) {
                    if (!DatabaseOperates.hadPassedTheExam(senderID)) {
                        String sendingMsg = PLUGIN_INSTANCE.getConfig().getString("Require_Exam_Message");

                        event.getGroup().sendMessage(new MessageChainBuilder()  // 借助消息链构造携带at的消息
                                .append(new At(senderID))
                                .append(" ")
                                .append(sendingMsg == null ? "请先去做审核题哦！" : sendingMsg)
                                .build()
                        );
                        return;
                    }

                    List<String> bindIDList = DatabaseOperates.checkBindID(senderID);
                    if (bindIDList.size() < 2) {
                        String playerID = groupMes.substring(6);
                        if (playerID.length() < 3 || playerID.length() > 20) {
                            event.getGroup().sendMessage(new MessageChainBuilder()
                                    .append(new At(senderID))
                                    .append(" ")
                                    .append("ID的长度仅允许在3~20（含上下限）之间哦！")
                                    .build()
                            );
                            return;
                        } else if (!ID_CHECK_REGEX.matcher(playerID).matches()) {
                            if (playerID.contains("<") || playerID.contains(">")) {
                                event.getGroup().sendMessage(new MessageChainBuilder()
                                        .append(new At(senderID))
                                        .append(" ")
                                        .append("绑定ID时，不需要加上<>的！这个只是告诉你，这里面的参数可变且必填的哦！")
                                        .build()
                                );
                                return;
                            }
                            event.getGroup().sendMessage(new MessageChainBuilder()
                                    .append(new At(senderID))
                                    .append(" ")
                                    .append("ID仅允许英文字母，数字和下划线出现哦！")
                                    .build()
                            );
                            return;
                        }

                        String bindStatus = DatabaseOperates.addBindID(senderID, playerID);
                        if (bindStatus == null) {

                            event.getGroup().sendMessage(new MessageChainBuilder()
                                    .append(new At(senderID))
                                    .append(" ")
                                    .append("绑定ID：")
                                    .append(playerID)
                                    .append("成功啦！快进服玩玩叭，不知道IP和端口可以看群公告哦！")
                                    .build()
                            );
                        } else {

                            event.getGroup().sendMessage(new MessageChainBuilder()
                                    .append(new At(senderID))
                                    .append(" ")
                                    .append(bindStatus)
                                    .build()
                            );
                        }
                    } else {
                        if (PLUGIN_INSTANCE.getConfig().getLongList("Owner_QQ").contains(senderID)) {
                            String playerID = groupMes.substring(6);
                            String bindStatus = DatabaseOperates.addBindID(senderID, playerID);
                            if (bindStatus == null) {
                                event.getGroup().sendMessage("腐竹腐竹！绑定ID：" + playerID + "成功啦！");
                            } else {
                                event.getGroup().sendMessage("腐竹腐竹，" + bindStatus.replace("请联系腐竹", "快去"));
                            }
                        } else {
                            event.getGroup().sendMessage(new MessageChainBuilder()
                                    .append(new At(senderID))
                                    .append(" ")
                                    .append("一个人最多绑定2个ID哦！如果需要删除绑定错误的ID，请回复”#删除ID：你的ID“即可！")
                                    .build());
                        }
                    }
                }

                else if(DEL_ID_REGEX.matcher(groupMes).find()){
                    String playerID = groupMes.substring(6);

                    if(playerID.contains("<") || playerID.contains(">")){
                        event.getGroup().sendMessage(new MessageChainBuilder()
                                .append(new At(senderID))
                                .append(" ")
                                .append("删除ID时，不需要加上<>的！这个只是告诉你，这里面的参数可变且必填的哦！")
                                .build()
                        );
                        return;
                    }

                    if(!PLUGIN_INSTANCE.getConfig().getLongList("Owner_QQ").contains(senderID)){
                        if(DatabaseOperates.checkBindID(senderID).contains(playerID)){
                            String cancelBindStatus = DatabaseOperates.delBindID(senderID, playerID);

                            if(cancelBindStatus == null){

                                event.getGroup().sendMessage(new MessageChainBuilder()
                                        .append(new At(senderID))
                                        .append(" ")
                                        .append("删除ID：")
                                        .append(playerID)
                                        .append("成功啦！如有需要不要忘记重新绑定哦！")
                                        .build()
                                );
                            }
                            else {
                                event.getGroup().sendMessage(new MessageChainBuilder()
                                        .append(new At(senderID))
                                        .append(" ")
                                        .append(cancelBindStatus)
                                        .build()
                                );
                            }
                        }
                    }

                    else {
                        String cancelBindStatus = DatabaseOperates.forceDelBindID(playerID);

                        if(cancelBindStatus == null){

                            event.getGroup().sendMessage(new MessageChainBuilder()
                                    .append(new At(senderID))
                                    .append(" ")
                                    .append("删除ID：")
                                    .append(playerID)
                                    .append("成功啦，腐竹大大！")
                                    .build()
                            );
                        }
                        else {
                            event.getGroup().sendMessage(new MessageChainBuilder()
                                    .append(new At(senderID))
                                    .append(" 腐竹腐竹！")
                                    .append(cancelBindStatus)
                                    .build()
                            );
                        }
                    }
                }
            }
            else {
                event.getGroup().sendMessage(new MessageChainBuilder()
                        .append(new At(senderID))
                        .append(" 绑定或删除ID只能在各交流群进行哦！")
                        .build()
                );
            }
        }

        // 查询绑定ID
        else if(SEARCH_ID_REGEX.matcher(groupMes).matches()){
            if(PLUGIN_INSTANCE.getConfig().getLongList("Bind_Only_Group").contains(groupID)) {

                List<String> playerIDList = DatabaseOperates.checkBindID(senderID);

                if(playerIDList.size() == 0){
                    event.getGroup().sendMessage(new MessageChainBuilder().append(new At(senderID)).append("嘛？你没绑定过任何ID哦！").build());
                    return;
                }

                MessageChainBuilder messageChainBuilder = new MessageChainBuilder();
                messageChainBuilder.append(new At(senderID)).append("嘛？你绑定了以下ID哦：");

                for(String playerID : playerIDList){
                    messageChainBuilder.append("\n").append(playerID);
                }

                event.getGroup().sendMessage(messageChainBuilder.build());
            }
        }

        // 免密登录
        else if(PWD_FREE_REGEX.matcher(groupMes).find()){
            if(PLUGIN_INSTANCE.getConfig().getLong("Main_Server_Group") == groupID) {

                String[] pwdFreeData = groupMes.substring(6).split(" ");
                if(pwdFreeData.length != 2){
                    event.getGroup().sendMessage("免密登录格式错误哦！正确的格式是：\"#免密登录:<你的游戏ID> <免密登录可持续的时间>\"(注意ID和时间之间的空格！)");
                    return;
                }

                String playerID = pwdFreeData[0];
                String deltaTimeStr = pwdFreeData[1];

                if(!DatabaseOperates.checkBindID(senderID).contains(playerID)){
                    event.getGroup().sendMessage(new MessageChainBuilder().append(new At(senderID)).append(" 这个ID似乎不是你绑定的...").build());
                    return;
                }

                long deltaTime = DateTime.parseTime(deltaTimeStr);

                PlayerOperates.setNoPwdStamp(playerID, new Date().getTime() + deltaTime * 1000);

                event.getGroup().sendMessage(new MessageChainBuilder()
                        .append(new At(senderID))
                        .append(" 免密登录设置成功！截止到")
                        .append(DateTime.convertToDate(deltaTime))
                        .append("前，您相同IP下的设备均可免密登录哦！")
                        .build()
                );
            }
        }

        // 获取帮助菜单
        else if(HELP_REGEX.matcher(groupMes).matches() || PLAYER_HELP_REGEX.matcher(groupMes).matches()){
            List<Long> adminList = PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ");

            if(HELP_REGEX.matcher(groupMes).matches()){
                if(adminList.contains(senderID)){
                    try{
                        event.getGroup().sendMessage(ExternalResource.uploadAsImage(PixelFont.gen(this.getAdminHelpText(), 25), event.getGroup()));
                    }
                    catch (Exception e){
                        event.getGroup().sendMessage("图片帮助信息发送失败，我将送上文字版哦！");
                        event.getGroup().sendMessage(this.getAdminHelpText());
                        e.printStackTrace();
                    }

                }
                else {
                    try{
                        event.getGroup().sendMessage(ExternalResource.uploadAsImage(PixelFont.gen(this.getPlayerHelpText(), 25), event.getGroup()));
                    }
                    catch (Exception e){
                        event.getGroup().sendMessage("图片帮助信息发送失败，我将送上文字版哦！");
                        event.getGroup().sendMessage(this.getPlayerHelpText());
                        e.printStackTrace();
                    }

                    /*
                    ForwardMessageBuilder helpMesBuilder = new ForwardMessageBuilder(event.getGroup());
                    helpMesBuilder.add(BOT_INSTANCE.getBot(), new PlainText(this.getPlayerHelpText()));
                    event.getGroup().resetLock(helpMesBuilder.build());
                     */
                }
            }

            if(PLAYER_HELP_REGEX.matcher(groupMes).matches()){
                try{
                    event.getGroup().sendMessage(ExternalResource.uploadAsImage(PixelFont.gen(this.getPlayerHelpText(), 25), event.getGroup()));
                }
                catch (Exception e){
                    event.getGroup().sendMessage("图片帮助信息发送失败，我将送上文字版哦！");
                    event.getGroup().sendMessage(this.getPlayerHelpText());
                    e.printStackTrace();
                }

                /*
                ForwardMessageBuilder helpMesBuilder = new ForwardMessageBuilder(event.getGroup());
                helpMesBuilder.add(BOT_INSTANCE.getBot(), new PlainText(this.getPlayerHelpText()));
                event.getGroup().resetLock(helpMesBuilder.build());
                 */
            }
        }

        // 获取正版认证帮助
        else if(ONLINE_MODE_HELP_REGEX.matcher(groupMes).find()){
            event.getGroup().sendMessage(
                    new MessageChainBuilder()
                            .append(new At(senderID))
                            .append(" 警告：如果您的账号不是正版账号，您将面临无法进入服务器的问题！如果您确认自己是正版账号，请再次回复：\"#确认正版认证:<你的ID>\"来完成验证！")
                            .build()
            );
        }

        // 修改正版认证状态
        else if(ONLINE_MODE_REGEX.matcher(groupMes).find()){
            String playerID = groupMes.substring(8);

            if(!DatabaseOperates.checkBindID(senderID).contains(playerID)){
                event.getGroup().sendMessage(
                        new MessageChainBuilder()
                                .append(new At(senderID))
                                .append(" 你又没绑定过这个ID！这让我怎么帮你正版认证嘛...")
                                .build()
                );

                return;
            }

            if(WSServer.connectionMap.containsKey("Bungeecord")){
                WebSocket conn = WSServer.connectionMap.get("Bungeecord");
                if(conn != null && conn.isOpen()){
                    Map<String, Object> packet = new HashMap<>();
                    packet.put("type", "onlineModeStatus");

                    Map<String, Object> packetArgs = new HashMap<>();
                    if(groupMes.contains("确认")) {
                        packetArgs.put("enable", true);
                    }
                    else {
                        packetArgs.put("enable", false);
                    }
                    packetArgs.put("playerName", playerID);

                    packet.put("args", packetArgs);
                    packet.put("reqGroup", new long[]{ groupID });

                    conn.send(GSON.toJson(packet));

                    return;
                }
            }

            event.getGroup().sendMessage(new MessageChainBuilder().append(new At(senderID)).append(" Bungeecord端看起来当前不在线呢！可以稍后再试一次！").build());
        }

        // 查服
        else if(CHECK_SERVER_REGEX.matcher(groupMes).matches()){
            if(WSServer.connectionMap.containsKey("Bungeecord")){
                WebSocket conn = WSServer.connectionMap.get("Bungeecord");
                if(conn != null && conn.isOpen()){
                    Map<String, Object> packet = new HashMap<>();
                    packet.put("type", "getOnlinePlayers");
                    packet.put("args", null);
                    packet.put("reqGroup", new long[]{ groupID });

                    conn.send(GSON.toJson(packet));

                    return;
                }
            }

            event.getGroup().sendMessage(new MessageChainBuilder().append(new At(senderID)).append(" Bungeecord端看起来当前不在线呢！可以稍后再试一次！").build());
        }

        // 查内服
        else if(CHECK_INTERNAL_SERVER_REGEX.matcher(groupMes).matches()){
            if(Main.INTERNAL_SERVER_TIMER_INSTANCE.isLocking()){
                event.getGroup().sendMessage("查询太快了诶，先休息一下吧！");
                return;
            }

            Map<String, Object> packet = new HashMap<>();
            packet.put("type", "getOnlinePlayers");
            packet.put("args", null);
            packet.put("reqGroup", new long[]{ groupID });

            for(Map.Entry<String, WebSocket> pair : WSServer.connectionMap.entrySet()){
                if(pair.getKey().contains("Internal")){
                    WebSocket conn = pair.getValue();
                    if(conn != null && conn.isOpen()){
                        conn.send(GSON.toJson(packet));
                    }
                    else {
                        event.getGroup().sendMessage(new MessageChainBuilder().append(new At(senderID)).append(" 内服节点[").append(pair.getKey()).append("]看起来当前不在线呢！这个服的玩家可能不会正常显示哦！").build());
                    }
                }
            }
        }

        // 向内服执行命令
        else if(EXECUTE_SINGLE_CMD_REGEX.matcher(groupMes).find()){
            if(PLUGIN_INSTANCE.getConfig().getLongList("Internal_Server_Group").contains(groupID)) {
                boolean isFakePlayerSpawnCmd = false;
                String fakePlayerName = null;

                String cmd = groupMes.substring(6);
                if (cmd.startsWith("/")) {
                    cmd = cmd.substring(1);
                }

                if (!cmd.startsWith("player")) {
                    if(cmd.startsWith("ban") || cmd.startsWith("clone") || cmd.startsWith("fill") || cmd.startsWith("op") || cmd.startsWith("deop") || cmd.startsWith("give") || cmd.startsWith("kill") || cmd.startsWith("kick") || cmd.startsWith("pardon") || cmd.startsWith("setblock") || cmd.startsWith("whitelist") || cmd.startsWith("unban") || cmd.startsWith("stop")){
                        event.getGroup().sendMessage(new MessageChainBuilder()
                                .append(new At(senderID))
                                .append(" 这么敏感的指令还是请OP们进服亲自执行吧！")
                                .build()
                        );

                        return;
                    }

                    if(!PLUGIN_INSTANCE.getConfig().getLongList("Internal_Admins_QQ").contains(event.getSender().getId())){
                        event.getGroup().sendMessage(new MessageChainBuilder()
                                .append(new At(senderID))
                                .append(" 非假人生成命令只能由管理员执行哦！")
                                .build()
                        );

                        return;
                    }
                }
                else {
                    if(cmd.contains("shadow")){
                        event.getGroup().sendMessage(new MessageChainBuilder()
                                .append(new At(senderID))
                                .append(" 禁止在群内使用Shadow指令哦！")
                                .build()
                        );

                        return;
                    }

                    if(cmd.contains("spawn")) {
                        isFakePlayerSpawnCmd = true;  // 群内玩家召唤的假人仅可以是生存模式的
                        fakePlayerName = cmd.split(" ")[1];
                    }
                }

                Map<String, Object> cmdPacket = new HashMap<>();


                if(WSServer.connectionMap.containsKey("Internal-Survival")){
                    WebSocket conn = WSServer.connectionMap.get("Internal-Survival");
                    if(conn != null && conn.isOpen()){
                        Map<String, Object> packet = new HashMap<>();
                        cmdPacket.put("cmd", cmd);

                        packet.put("type", "exeCmd");
                        packet.put("args", cmdPacket);
                        packet.put("reqGroup", new long[]{ groupID });

                        conn.send(GSON.toJson(packet));

                        if(isFakePlayerSpawnCmd && fakePlayerName != null){
                            Map<String, Object> extraPacket = new HashMap<>();
                            cmdPacket.clear();
                            cmdPacket.put("cmd", String.format("gamemode survival %s", fakePlayerName));

                            extraPacket.put("type", "exeCmd");
                            extraPacket.put("args", cmdPacket);
                            extraPacket.put("reqGroup", null);

                            conn.send(GSON.toJson(extraPacket));
                        }

                        return;
                    }
                }

                event.getGroup().sendMessage(new MessageChainBuilder().append(new At(senderID)).append(" 内服服务端看起来当前不在线呢！可以稍后再试一次！").build());
            }
        }

        // Q & A
        else if(Q_AND_A_REGEX.matcher(groupMes).find()){
            int page;

            if(groupMes.length() < 4){
                event.getGroup().sendMessage(this.getPlayerQAndAText());
                return;
            }
            else {
                try {
                    page = Integer.parseInt(groupMes.substring(3));
                }
                catch (Exception e){
                    event.getGroup().sendMessage(this.getPlayerQAndAText());
                    return;
                }
            }

            MessageChainBuilder general_mcb = new MessageChainBuilder().append(new At(senderID)).append(" ");

            switch (page) {
                case 1 ->  // 不知道IP
                        event.getGroup().sendMessage(general_mcb.append("不知道IP嘛？看这里：https://sls.wiki/index.php?title=StarLight-Server#%E6%9C%8D%E5%8A%A1%E5%99%A8IP%E4%B8%8E%E7%AB%AF%E5%8F%A3").build());
                case 2 ->  // 不知道加速IP的作用
                        event.getGroup().sendMessage(general_mcb.append("不知道加速IP的作用嘛？看这里：https://sls.wiki/index.php?title=%E7%9B%B4%E8%BF%9EIP%E4%B8%8E%E5%8A%A0%E9%80%9FIP").build());
                case 3 ->  // 不知道IPv4与IPv6
                        event.getGroup().sendMessage(general_mcb.append("IPv4和IPv6完全搞不懂？看这里：https://sls.wiki/index.php?title=IPv4%E4%B8%8EIPv6").build());
                case 4 ->  // 不会用领地插件
                        event.getGroup().sendMessage(general_mcb.append("不会用领地插件嘛？看这里：https://sls.wiki/index.php?title=Residence").build());
                case 5 ->  // 不知道服规
                        event.getGroup().sendMessage(general_mcb.append("想回顾下服规嘛？看这里：https://www.kdocs.cn/l/cmT6lVdryHJu").build());
                case 6 ->  // 服务器运营模式
                        event.getGroup().sendMessage(general_mcb.append("服务器为纯公益服，日常收益完全依靠玩家的自愿捐助！想了解更多？来看看：https://www.starlight.cool/donation.html").build());
                case 7 ->  // 想和服务器玩家开语音
                        event.getGroup().sendMessage(general_mcb.append("要和玩家一起开语音游玩嘛？加入我们的kook频道：https://www.kookapp.cn/app/channels/7756332831338277").build());
                default -> {
                    event.getGroup().sendMessage(this.getPlayerQAndAText());
                    return;
                }
            }

            event.getGroup().sendMessage("注意哦！由于腐竹没有额外资金，也不愿意向某tx交保护费，所以这些链接也许无法从QQ直接点开哦！可以先把链接复制到浏览器，然后就能打开啦！");
        }

        // 内服喊话
        else if(SHOUT_TO_INTERNAL.matcher(groupMes).find()){
            if(PLUGIN_INSTANCE.getConfig().getLongList("Internal_Server_Group").contains(groupID)){
                String chat = groupMes.substring(6);

                Map<String, Object> packet = new HashMap<>();
                packet.put("type", "chat");

                String playerNameCard = event.getSender().getNameCard();
                if(playerNameCard.equals("")){
                    playerNameCard = event.getSender().getNick();
                }

                packet.put("args", String.format("[SLS内群-%s] %s", playerNameCard, chat));
                packet.put("reqGroup", new long[]{ groupID });

                for(Map.Entry<String, WebSocket> pair : WSServer.connectionMap.entrySet()){
                    if(pair.getKey().contains("Internal")){
                        WebSocket conn = pair.getValue();

                        if(conn != null && conn.isOpen()){
                            pair.getValue().send(GSON.toJson(packet));
                        }
                        else {
                            event.getGroup().sendMessage("子服" + pair.getKey() + "的节点当前好像不在线诶，你发送的消息可能无法到达这个子服哦！");
                        }
                    }
                }
            }
            else {
                event.getGroup().sendMessage("仅限内服群可以使用这项喊话哦！");
            }
        }
    }

    /**
     * 检查进入主群的时候是否通过审核
     * @param event 申请入群事件
     */
    public void checkLegitimacyWhileJoin(MemberJoinRequestEvent event){
        if(event.getGroupId() == PLUGIN_INSTANCE.getConfig().getLong("Main_Server_Group")){
            if(DatabaseOperates.hadPassedTheExam(event.getFromId())){
                event.accept();
            }

            else {
                event.reject(false, "你似乎还没有通过审核诶！");

                Group review_group = RUNNABLE_BOT_INSTANCE.getCore().getGroup(PLUGIN_INSTANCE.getConfig().getLong("Review_Group"));
                if(review_group != null){
                    review_group.sendMessage(new MessageChainBuilder()
                            .append("滴滴，玩家：")
                            .append(event.getFromNick())
                            .append("（QQ号：")
                            .append(String.valueOf(event.getFromId()))
                            .append(event.getInvitorId() == null ? "" : ("(邀请者QQ号：" + event.getInvitorId() + ")"))
                            .append("）未通过审核，但尝试加主交流群，已被我自动拒绝了哦！")
                            .build()
                    );
                }
            }
        }
    }


    /* 群操作方法 */

    /**
     * 处理发送迎接新玩家消息的方法
     * @param event 新成员入群事件
     */
    public void welcomeNewMember(MemberJoinEvent event){
        long groupID = event.getGroupId();

        if(PLUGIN_INSTANCE.getConfig().getLong("Old_Server_Group") == groupID){
            String rawMsg = this.getWelcomeMsg();

            int atPosition = rawMsg.indexOf("{@Member}");  // 标记需要at成员的位置

            if(atPosition != -1){  // 如果有at需求
                rawMsg = rawMsg.replace("{@Member}", "");
                MessageChainBuilder messageChainBuilder = new MessageChainBuilder();

                for(int i = 0; i < rawMsg.length(); i++){
                    if(i == atPosition){
                        messageChainBuilder.append(new At(event.getMember().getId()));
                    }
                    messageChainBuilder.append(rawMsg.charAt(i));
                }

                event.getGroup().sendMessage(messageChainBuilder.build());
            }
            else {
                event.getGroup().sendMessage(rawMsg);  // 否则直接发送原始消息
            }

        }
        else if(PLUGIN_INSTANCE.getConfig().getLong("Main_Server_Group")  == groupID){
            event.getGroup().sendMessage(new MessageChainBuilder()
                    .append("让我们一起欢迎新玩家")
                    .append(new At(event.getMember().getId()))
                    .append("的到来！")
                    .build()
            );
        }
    }

    /**
     * 踢掉已经通过审核的玩家
     * 方法为阻塞的，因此请不要与主线程同步运行
     */
    public void kickPassedMember(){
        this.classifyGroupFiles();

        Group old_group = RUNNABLE_BOT_INSTANCE.getCore().getGroup(PLUGIN_INSTANCE.getConfig().getLong("Old_Server_Group"));
        Group new_group = RUNNABLE_BOT_INSTANCE.getCore().getGroup(PLUGIN_INSTANCE.getConfig().getLong("Main_Server_Group"));

        if(old_group != null && new_group != null){
            Logger.info("准备清理旧群人员");

            List<Long> admin_list = PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ");
            admin_list.add(2739302193L);  // pop猫
            admin_list.add(203701725L);  // Feya
            admin_list.add(1853359516L);  // Parzival

            int counter = 0;

            for(NormalMember member : old_group.getMembers()){
                if(new_group.get(member.getId()) != null){  // 已经在新群了
                    if(member.getPermission().equals(MemberPermission.MEMBER)) {
                        if(!admin_list.contains(member.getId())){
                            Logger.info("应踢出：" + member.getId());
                            while (true){
                                try{
                                    if(member.getPermission().equals(MemberPermission.MEMBER)) {
                                        member.kick("你已加入新交流群！");
                                    }
                                    Logger.info("已踢出：" + member.getId());
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException ex) {
                                        break;
                                    }

                                    break;
                                }
                                catch (IllegalStateException e){
                                    Logger.error(e.getMessage());
                                    counter++;
                                    if(counter > 5){
                                        counter = 0;
                                        break;
                                    }
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException ex) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    if(member.getPermission().equals(MemberPermission.MEMBER)) {
                        if(!admin_list.contains(member.getId())){
                            String memberNameCard = member.getNameCard();
                            if(!memberNameCard.contains("[未审核成员]")){
                                String newNameCard = "[未审核成员] ";
                                if(memberNameCard.equals("")){
                                    newNameCard += member.getNick();
                                }
                                else {
                                    newNameCard += member.getNameCard();
                                }

                                member.setNameCard(newNameCard);
                            }
                        }
                    }
                }
            }

            Logger.info("旧群人员清理完成");
        }
    }


    /**
     * 为群文件自动分类
     */
    public void classifyGroupFiles(){
        List<Long> need_classify_groups = new ArrayList<>();

        need_classify_groups.add(PLUGIN_INSTANCE.getConfig().getLong("Main_Server_Group"));
        need_classify_groups.addAll(PLUGIN_INSTANCE.getConfig().getLongList("Internal_Server_Group"));

        for(long group_num : need_classify_groups){
            Group group = RUNNABLE_BOT_INSTANCE.getCore().getGroup(group_num);

            if(group != null){
                AbsoluteFolder root_folder = group.getFiles().getRoot();

                AbsoluteFolder litematic_folder = root_folder.resolveFolder("投影文件");
                if(litematic_folder == null) litematic_folder = root_folder.createFolder("投影文件");

                AbsoluteFolder image_folder = root_folder.resolveFolder("杂物");
                if(image_folder == null) image_folder = root_folder.createFolder("杂物");


                final AbsoluteFolder final_litematic_folder = litematic_folder;
                final AbsoluteFolder final_image_folder = image_folder;

                group.getFiles().getRoot().files().collect((file, continuation) -> {
                    String file_name = file.getName();

                    if(file_name.contains(".litematic")){
                        file.moveTo(final_litematic_folder);
                    }
                    else if(IMAGE_AND_VIDEO_FILE_REGEX.matcher(file_name).find()){
                        file.moveTo(final_image_folder);
                    }

                    return null;
                }, new Continuation<Unit>() {
                    @NotNull
                    @Override
                    public CoroutineContext getContext() {
                        return EmptyCoroutineContext.INSTANCE;
                    }

                    @Override
                    public void resumeWith(@NotNull Object o) {

                    }
                });
            }
        }

        Logger.info("全部群文件自动分类完成！");
    }


    /* 私有方法 */

    /**
     * 获取管理员帮助文档的方法
     * @return 管理员帮助文档中的内容，如果获取失败则返回一个不包含任何字符的字符串
     */
    private String getAdminHelpText(){
        File help_data_file_admin = new File(DATABASE, "HelpFile/admin_help_msg.txt");

        if(help_data_file_admin.exists()){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(help_data_file_admin), StandardCharsets.UTF_8));
                StringBuilder result_builder = new StringBuilder(bufferedReader.readLine());

                String temp_str;

                while ((temp_str=bufferedReader.readLine()) != null){
                    result_builder.append("\n");
                    result_builder.append(temp_str);
                }

                return result_builder.toString();
            } catch (IOException e) {
                Logger.error("读取管理员帮助文件时失败，以下是错误的堆栈信息：");
                e.printStackTrace();
                return "";
            }
        }

        return "";
    }


    /**
     * 获取玩家帮助文档的方法
     * @return 玩家帮助文档中的内容，如果获取失败则返回一个不包含任何字符的字符串
     */
    public static String getPlayerHelpText(){
        File help_data_file_player = new File(DATABASE, "HelpFile/player_help_msg.txt");

        if(help_data_file_player.exists()){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(help_data_file_player), StandardCharsets.UTF_8));
                StringBuilder result_builder = new StringBuilder(bufferedReader.readLine());

                String temp_str;

                while ((temp_str=bufferedReader.readLine()) != null){
                    result_builder.append("\n");
                    result_builder.append(temp_str);
                }

                return result_builder.toString();
            } catch (IOException e) {
                Logger.error("读取玩家帮助文件时失败，以下是错误的堆栈信息：");
                e.printStackTrace();
                return "";
            }
        }

        return "";
    }


    /**
     * 获取玩家Q&A文档的方法
     * @return 玩家Q&A文档中的内容，如果获取失败则返回一个不包含任何字符的字符串
     */
    private String getPlayerQAndAText(){
        File help_data_file_player = new File(DATABASE, "HelpFile/player_q_and_a_msg.txt");

        if(help_data_file_player.exists()){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(help_data_file_player), StandardCharsets.UTF_8));
                StringBuilder result_builder = new StringBuilder(bufferedReader.readLine());

                String temp_str;

                while ((temp_str=bufferedReader.readLine()) != null){
                    result_builder.append("\n");
                    result_builder.append(temp_str);
                }

                return result_builder.toString();
            } catch (IOException e) {
                Logger.error("读取玩家Q&A文件时失败，以下是错误的堆栈信息：");
                e.printStackTrace();
                return "";
            }
        }

        return "";
    }


    /**
     * 获取欢迎新成员信息的方法
     * @return 欢迎新成员的信息
     */
    private String getWelcomeMsg(){
        File welcome_file = new File(DATABASE, "HelpFile/welcome_msg.txt");

        if(welcome_file.exists()){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(welcome_file), StandardCharsets.UTF_8));
                StringBuilder result_builder = new StringBuilder(bufferedReader.readLine());

                String temp_str;

                while ((temp_str=bufferedReader.readLine()) != null){
                    result_builder.append("\n");
                    result_builder.append(temp_str);
                }

                return result_builder.toString();
            } catch (IOException e) {
                Logger.error("读取欢迎文件时失败，以下是错误的堆栈信息：");
                e.printStackTrace();
                return "";
            }
        }

        return "";
    }

}

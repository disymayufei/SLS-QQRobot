package cn.starlight.disy.qqbot.bot;

import cn.starlight.disy.qqbot.utils.DatabaseOperates;
import cn.starlight.disy.qqbot.utils.PlayerOperates;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.util.List;
import java.util.regex.Pattern;

import static cn.starlight.disy.qqbot.Main.PLUGIN_INSTANCE;
import static cn.starlight.disy.qqbot.bot.RunnableBot.RUNNABLE_BOT_INSTANCE;

public class AdminOperates {

    public static final AdminOperates ADMIN_OPERATES_INSTANCE = new AdminOperates();

    private final Pattern EXAM_REGEX = Pattern.compile("^[#＃]审核通过[:：]");
    private final Pattern SILENT_EXAM_REGEX = Pattern.compile("^[#＃]静默过审[:：]");
    private final Pattern WITHDRAW_EXAM_REGEX = Pattern.compile("^[#＃]撤销审核[:：]");
    private final Pattern ADD_ADMIN_REGEX = Pattern.compile("^[#＃]添加管理[:：]");
    private final Pattern DEL_ADMIN_REGEX = Pattern.compile("^[#＃]删除管理[:：]");
    private final Pattern CALC_UUID_REGEX = Pattern.compile("^[#＃]计算离线UUID[:：]", Pattern.CASE_INSENSITIVE);
    private final Pattern FIND_QQ_BY_ID_REGEX = Pattern.compile("^[#＃]反查ID[:：]", Pattern.CASE_INSENSITIVE);
    private final Pattern FIND_IDS_BY_QQ_REGEX = Pattern.compile("^[#＃]查询ID[:：]", Pattern.CASE_INSENSITIVE);


    public void adminMes(GroupMessageEvent event){
        long group_id = event.getGroup().getId();
        long senderID = event.getSender().getId();

        if(PLUGIN_INSTANCE.getConfig().getLongList("Bind_Only_Group").contains(group_id)){

            String msg = event.getMessage().contentToString();

            /* 腐竹部分 */

            /* 添加管理部分 */
            if(ADD_ADMIN_REGEX.matcher(msg).find()) {
                if (PLUGIN_INSTANCE.getConfig().getLongList("Owner_QQ").contains(senderID)) {
                    String player_qq_num = msg.substring(6);

                    try {
                        long qq_num = Long.parseLong(player_qq_num);
                        String status = DatabaseOperates.addAdmin(qq_num);

                        if (event.getGroup().contains(qq_num)) {
                            if (status == null) {
                                event.getGroup().sendMessage(new MessageChainBuilder()
                                        .append("腐竹大大，已经添加")
                                        .append(new At(qq_num))
                                        .append("进管理行列啦！")
                                        .build()
                                );
                            } else {
                                event.getGroup().sendMessage(status);
                            }

                        } else {
                            event.getGroup().sendMessage(status == null ? "腐竹大大，已经添加" + qq_num + "进管理行列啦！" : status);
                        }
                    } catch (Exception e) {
                        event.getGroup().sendMessage("腐竹腐竹，你添加的真的是个QQ号嘛？");
                    }


                } else {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append("不是腐竹却还想要乱改管理员的")
                            .append(new At(senderID))
                            .append("是屑！")
                            .build()
                    );
                }
            }

            /* 删除管理部分 */
            else if(DEL_ADMIN_REGEX.matcher(msg).find()){
                if(PLUGIN_INSTANCE.getConfig().getLongList("Owner_QQ").contains(senderID)) {
                    String player_qq_num = msg.substring(6);

                    try {
                        long qq_num = Long.parseLong(player_qq_num);
                        String status = DatabaseOperates.delAdmin(qq_num);

                        if (event.getGroup().contains(qq_num)) {
                            if (status == null) {
                                event.getGroup().sendMessage(new MessageChainBuilder()
                                        .append("腐竹大大，已经将")
                                        .append(new At(qq_num))
                                        .append("清出管理行列啦！")
                                        .build()
                                );
                            } else {
                                event.getGroup().sendMessage(status);
                            }

                        } else {
                            event.getGroup().sendMessage(status == null ? "腐竹大大，已经将" + qq_num + "清出管理行列啦！" : status);
                        }
                    } catch (Exception e) {
                        event.getGroup().sendMessage("腐竹腐竹，你取消的真的是个QQ号嘛？");
                    }
                }
                else {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append("不是腐竹却还想要乱改管理员的")
                            .append(new At(senderID))
                            .append("是屑！")
                            .build()
                    );
                }
            }

            /* 管理部分 */

            /* 审核通过部分 */
            else if(EXAM_REGEX.matcher(msg).find()){
                if(PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ").contains(senderID)) {
                    String player_qq_num = msg.substring(6);

                    try {
                        long qq_num = Long.parseLong(player_qq_num);
                        if(qq_num < 99999){
                            event.getGroup().sendMessage("管理大大！你通过的真的是个QQ号嘛？");
                            return;
                        }
                        String status = DatabaseOperates.passTheExam(qq_num);

                        if (event.getGroup().contains(qq_num)) {
                            if (status == null) {
                                event.getGroup().sendMessage(new MessageChainBuilder()
                                        .append("管理大大，已经同意")
                                        .append(new At(qq_num))
                                        .append("绑定白名单啦！")
                                        .build()
                                );
                            } else {
                                event.getGroup().sendMessage(status);
                                return;
                            }

                        } else {
                            event.getGroup().sendMessage(status == null ? "管理大大，已经同意" + qq_num + "绑定白名单啦！" : status);
                        }

                        Group for_review_group = RUNNABLE_BOT_INSTANCE.getCore().getGroup(PLUGIN_INSTANCE.getConfig().getLong("Old_Server_Group"));
                        if(for_review_group != null){
                            NormalMember new_player = for_review_group.get(qq_num);
                            if(new_player != null){
                                new_player.sendMessage("[StarLight - Bot] 你已经通过审核了，快点击这个链接加入我们的服务器交流群吧：https://jq.qq.com/?_wv=1027&k=BwfeiIBU");
                            }
                            else {
                                Group review_group = RUNNABLE_BOT_INSTANCE.getCore().getGroup(PLUGIN_INSTANCE.getConfig().getLong("Review_Group"));
                                if(review_group != null){
                                    review_group.sendMessage(new MessageChainBuilder()
                                            .append("滴滴，我尝试帮你们邀请玩家：")
                                            .append(String.valueOf(qq_num))
                                            .append("，但发送邀请消息失败了诶！")
                                            .build());
                                }
                            }
                        }
                    } catch (Exception e) {
                        event.getGroup().sendMessage("管理大大！你通过的真的是个QQ号嘛？");
                    }
                }
                else {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append("不是管理却还想要执行管理指令的")
                            .append(new At(senderID))
                            .append("是屑！")
                            .build()
                    );
                }
            }


            else if(SILENT_EXAM_REGEX.matcher(msg).find()){
                if(PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ").contains(senderID)) {
                    String player_qq_num = msg.substring(6);

                    try {
                        long qq_num = Long.parseLong(player_qq_num);
                        if(qq_num < 99999){
                            event.getGroup().sendMessage("管理大大！你通过的真的是个QQ号嘛？");
                            return;
                        }
                        String status = DatabaseOperates.passTheExam(qq_num);

                        if (event.getGroup().contains(qq_num)) {
                            if (status == null) {
                                event.getGroup().sendMessage(new MessageChainBuilder()
                                        .append("管理大大，已经同意")
                                        .append(new At(qq_num))
                                        .append("绑定白名单啦！")
                                        .build()
                                );
                            } else {
                                event.getGroup().sendMessage(status);
                            }

                        } else {
                            event.getGroup().sendMessage(status == null ? "管理大大，已经同意" + qq_num + "绑定白名单啦！" : status);
                        }
                    } catch (Exception e) {
                        event.getGroup().sendMessage("管理大大！你通过的真的是个QQ号嘛？");
                    }
                }
                else {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append("不是管理却还想要执行管理指令的")
                            .append(new At(senderID))
                            .append("是屑！")
                            .build()
                    );
                }
            }


            else if(WITHDRAW_EXAM_REGEX.matcher(msg).find()){
                if(PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ").contains(senderID)) {
                    String playerQQNum = msg.substring(6);

                    try {
                        long qqNum = Long.parseLong(playerQQNum);
                        String status = DatabaseOperates.withdrawTheExamResult(qqNum);

                        if (event.getGroup().contains(qqNum)) {
                            if (status == null) {
                                event.getGroup().sendMessage(new MessageChainBuilder()
                                        .append("管理大大，已经撤销")
                                        .append(new At(qqNum))
                                        .append("绑定白名单的权利啦！")
                                        .build()
                                );
                            } else {
                                event.getGroup().sendMessage(status);
                            }

                        } else {
                            event.getGroup().sendMessage(status == null ? "管理大大，已经撤销" + qqNum + "绑定白名单的权利啦！" : status);
                        }
                    } catch (Exception e) {
                        event.getGroup().sendMessage("管理管理，你撤销的真的是个QQ号嘛？");
                    }
                }

                else {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append("不是管理却还想要执行管理指令的")
                            .append(new At(senderID))
                            .append("是屑！")
                            .build()
                    );
                }
            }

            /* 计算UUID部分 */
            else if(CALC_UUID_REGEX.matcher(msg).find()){
                if(PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ").contains(senderID)) {
                    String playerID = msg.substring(10);
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append(new At(senderID))
                            .append(" 玩家")
                            .append(playerID)
                            .append("的离线UUID为：\n")
                            .append(PlayerOperates.getUUID(playerID).toString())
                            .build()
                    );
                }
                else {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append("不是管理却还想要执行管理指令的")
                            .append(new At(senderID))
                            .append("是屑！")
                            .build()
                    );
                }
            }

            /* 反查ID部分 */
            else if(FIND_QQ_BY_ID_REGEX.matcher(msg).find()){
                if(PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ").contains(senderID)) {
                    String playerID = msg.substring(6);
                    String QQID = DatabaseOperates.findQQByID(playerID);
                    long qq_number;



                    if(QQID != null){
                        try {
                            qq_number = Long.parseLong(QQID);
                            if(event.getGroup().contains(qq_number)){
                                event.getGroup().sendMessage(new MessageChainBuilder()
                                        .append(new At(senderID))
                                        .append(" ID：")
                                        .append(playerID)
                                        .append("绑定的QQ号为：")
                                        .append(QQID)
                                        .append("，对应群员为：")
                                        .append(new At(qq_number))
                                        .build()
                                );

                                return;
                            }
                        }
                        catch (Exception ignored){}
                        event.getGroup().sendMessage(new MessageChainBuilder()
                                .append(new At(senderID))
                                .append(" ID：")
                                .append(playerID)
                                .append("绑定的QQ号为：")
                                .append(QQID)
                                .build()
                        );
                    }
                    else {
                        event.getGroup().sendMessage(new MessageChainBuilder()
                                .append(new At(senderID))
                                .append(" ID：")
                                .append(playerID)
                                .append("还没有人绑定过诶！")
                                .build()
                        );
                    }

                }
                else {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append("不是管理却还想要执行管理指令的")
                            .append(new At(senderID))
                            .append("是屑！")
                            .build()
                    );
                }
            }

            else if(FIND_IDS_BY_QQ_REGEX.matcher(msg).find()){
                if(PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ").contains(senderID)) {
                    long playerQQ = -1;

                    for(Message m : event.getMessage()){
                        if(m instanceof At){
                            playerQQ = ((At) m).getTarget();
                            break;
                        }
                    }

                    if(playerQQ == -1){
                        try {
                            playerQQ = Long.parseLong(msg.substring(6));
                        }
                        catch (Exception e){
                            event.getGroup().sendMessage(new MessageChainBuilder()
                                    .append(new At(senderID))
                                    .append(" 管理管理，你查询的真的是一个QQ号嘛？")
                                    .build()
                            );

                            return;
                        }
                    }

                    List<String> player_bind_id = DatabaseOperates.checkBindID(playerQQ);

                    if(player_bind_id != null){
                        if(player_bind_id.size() > 0){
                            MessageChainBuilder mcb = new MessageChainBuilder();
                            mcb.append("玩家：").append(new At(playerQQ)).append("绑定的ID有：");
                            for(String id : player_bind_id){
                                mcb.append("\n").append("- ").append(id);
                            }

                            event.getGroup().sendMessage(mcb.build());

                            return;
                        }
                    }

                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append("玩家：")
                            .append(new At(playerQQ))
                            .append("没有绑定过任何ID哦！")
                            .build()
                    );
                }
                else {
                    event.getGroup().sendMessage(new MessageChainBuilder()
                            .append("不是管理却还想要执行管理指令的")
                            .append(new At(senderID))
                            .append("是屑！")
                            .build()
                    );
                }
            }
        }
    }
}

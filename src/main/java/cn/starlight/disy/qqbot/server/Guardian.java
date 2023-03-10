package cn.starlight.disy.qqbot.server;

import cn.starlight.disy.qqbot.utils.DatabaseOperates;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static cn.starlight.disy.qqbot.Main.PLUGIN_INSTANCE;

@SuppressWarnings("FieldCanBeLocal")
public class Guardian {

    private static final String DEFAULT_KICK_MSG = "§d§l[StarLight服务器]\n§6§l你好像还没有绑定过你的ID\n请加入我们的QQ群:§b535392227\n§6§l并在审核通过后，在交流群中群里发送§e“#绑定ID:你的ID”\n§l§6即可§a正常§6进入服务器了哦！";

    /* t出不在白名单的玩家的方法，被t出则返回true */
    public static boolean kickPlayerIfNotInWhitelist(Player player){
        String kickMsg = PLUGIN_INSTANCE.getConfig().getString("Kick_Message");
        if("".equals(kickMsg)){
            kickMsg = DEFAULT_KICK_MSG;  // 如果配置文件缺失，则替换为默认踢出信息
        }

        if(!DatabaseOperates.isInWhiteList(player.getName())){
            player.kickPlayer(kickMsg);
            return true;
        }

        return false;
    }

    public static void noPasswordLogin(String playerName){
        new BukkitRunnable(){
            @Override
            public void run() {
                PLUGIN_INSTANCE.getServer().dispatchCommand(PLUGIN_INSTANCE.getServer().getConsoleSender(), "authme forcelogin " + playerName);
            }
        }.runTaskLater(PLUGIN_INSTANCE, 20);
    }
}

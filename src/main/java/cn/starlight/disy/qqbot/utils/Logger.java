package cn.starlight.disy.qqbot.utils;

public final class Logger {
    public static void info(Object msg){
        System.out.println("[StarLight_Bot - INFO] " + msg);
    }

    public static void warn(Object msg){
        System.out.println("[StarLight_Bot - WARN] " + msg);
    }

    public static void error(Object msg){
        System.out.println("[StarLight_Bot - ERR] " + msg);
    }
}

package cn.seu.herald_android;

import android.content.Context;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Utils {

    public static String saveCrashInfoToSDCard(Context context, Throwable throwable) {
        StringBuilder crashInfoStringBuilder = new StringBuilder();

        //获取Crash时间

        //获取导致Crash的时间
        String uncaughtException = getUncaughtException(throwable);
        crashInfoStringBuilder.append(uncaughtException + "\n");

        return crashInfoStringBuilder.toString();

    }

    /**
     * 获取造成Crash的异常的具体信息
     */
    public static String getUncaughtException(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        String uncaughtException = stringWriter.toString();
        return uncaughtException;

    }

    /**
     * 获取Crash的时间
     */
    public static String getCrashTime() {
        String currentTime = "";
        long currentTimeMillis = System.currentTimeMillis();
        System.setProperty("user.timezone", "Asia/Shanghai");
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(timeZone);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date currentDate = new Date(currentTimeMillis);
        currentTime = simpleDateFormat.format(currentDate);
        return currentTime;
    }
}
package cn.seu.herald_android;

import android.app.Application;
import android.content.Intent;
import android.test.ApplicationTestCase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testDate(){
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        try{
            String time = "9:30-16:00";
            Date dateStart = new Date();
            Date dateEnd = new Date();
            dateStart.setHours(Integer.parseInt(time.split(":")[0]));
            dateStart.setMinutes(Integer.parseInt(time.split(":")[1].split("-")[0]));
            dateEnd.setHours(Integer.parseInt(time.split(":")[1].split("-")[1]));
            dateEnd.setMinutes(Integer.parseInt(time.split(":")[2]));
            Date now = new Date();
            if(now.after(dateStart)&&now.before(dateEnd))
                Log.d("timetest",now.getHours()+"");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
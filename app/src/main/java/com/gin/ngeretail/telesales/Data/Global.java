package com.gin.ngeretail.telesales.Data;

import android.app.Application;
import android.widget.Toast;

import com.gin.ngeretail.telesales.BuildConfig;

public class Global extends Application {
    public static String BASE_URL,BASE_URL_2,INFO;

    public static void checkForDebugOrRelease(){
        if(BuildConfig.BUILD_TYPE.equals("debug")){
             BASE_URL = ApiData.readFile()+"api/gin/telesales/";
             BASE_URL_2 = ApiData.readFile()+"api/ng/sm/me/";
             INFO=ApiData.readFile();
        }else{
             BASE_URL = "https://api.ngeretail.com/"+"api/gin/telesales/";
             BASE_URL_2 = "https://api.ngeretail.com/"+"api/ng/sm/me/";
            INFO="https://api.ngeretail.com/";
        }
    }

}

package com.gin.ngeretail.telesales.Data;

import android.os.Environment;
import android.preference.PreferenceActivity;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.gin.ngeretail.telesales.BuildConfig;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cz.msebera.android.httpclient.Header;

public class ApiData {
    //wifi office
    public static final String BASE_URL = readFile()+"api/gin/telesales/";
    public static final String BASE_URL_2 = readFile()+"api/ng/sm/me/";

    //mobile data
    //public static final String BASE_URL = "http://192.168.43.52:9999/api/gin/telesales/";

    public static final String APP_ID = "&appId=telesales";
    public static InputStream instream;

    public static void Authorize(AsyncHttpClient httpClient, String tokenId){
        httpClient.addHeader("Content-Type", "multipart/form-data");
        httpClient.addHeader("responseType","json");
        httpClient.addHeader("token", tokenId);
        httpClient.addHeader("appId","telesales");

        RequestParams requestParams = new RequestParams();
        requestParams.put("token", tokenId);
        requestParams.put("appId", "telesales");
    }

    public static String readFile(){
        String myData="";
        File myExternalFile = new File(Environment.getExternalStorageDirectory(), "host.bos");
        try{
            FileInputStream fis = new FileInputStream(myExternalFile);
            if(fis != null){
                DataInputStream in = new DataInputStream(fis);
                //BufferedReader br = new BufferedReader(new InputStreamReader(in));
                BufferedReader br = new BufferedReader(new FileReader(myExternalFile));


                String strLine;
                while ((strLine = br.readLine()) != null) {
                    myData = myData + strLine;
                }
                br.close();
                in.close();
                fis.close();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return myData;
        //return "http://65.52.164.164:9997/";

    }

}
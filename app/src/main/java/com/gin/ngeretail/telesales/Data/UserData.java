package com.gin.ngeretail.telesales.Data;

import android.net.Uri;

import java.io.Serializable;

public class UserData implements Serializable{
    public static final String FILE_NAME = "loginData";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_EMAIL = "email";
    public static final String PROFILE_PIC = "profile";
    public static final String CHOSEE_SIM ="sim";
    public static final String ERROR ="error";
    public static final String KEY_LOGIN_STATUS = "loginStatus";
    public static final String RETRY_UPLOAD_MP3 = "upload";


    public String id;
    public String emailUser;
    public String tokenId;
    public boolean loginStatus = false;
    public String sim = "";
    public String profileImage;
    public String error="";
    public String upload="";
    public String publicKey;
}

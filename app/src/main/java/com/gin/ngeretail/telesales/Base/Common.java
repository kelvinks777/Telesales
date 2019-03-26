package com.gin.ngeretail.telesales.Base;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

public class Common {

    public static ApplicationVersion GetVersionInfo(Context context) {
        ApplicationVersion appVersion = new ApplicationVersion();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            appVersion.versionName = packageInfo.versionName;
            appVersion.versionCode = packageInfo.versionCode;
            appVersion.isDebugAble = (0 != (context.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return appVersion;
    }

    public static class ApplicationVersion {
        public String versionName;
        public int versionCode;
        public boolean isDebugAble;

        ApplicationVersion() {
            this.versionName = "";
            this.versionCode = 0;
            this.isDebugAble = false;
        }
    }

}

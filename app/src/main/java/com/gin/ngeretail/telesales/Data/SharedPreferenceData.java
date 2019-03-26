package com.gin.ngeretail.telesales.Data;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceData {
    private String fileName;
    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public static final int STRING = 0;
    public static final int INTEGER = 1;
    public static final int BOOLEAN = 2;

    public SharedPreferenceData(String fileName, Context context) {
        this.fileName = fileName;
        this.context = context;

        preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void write(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void write(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void write(String key, Boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public Object read(String key, int DataType) {
        Object object = new Object();
        if (DataType == INTEGER) {
            object = preferences.getInt(key, 0);
        } else if (DataType == STRING) {
            object = preferences.getString(key, "");
        } else if (DataType == BOOLEAN)
            object = preferences.getBoolean(key, false);
        return object;
    }
}

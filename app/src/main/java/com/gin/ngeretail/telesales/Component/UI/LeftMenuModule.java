package com.gin.ngeretail.telesales.Component.UI;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gin.ngeretail.telesales.Activity.CallCustomerActivity;
import com.gin.ngeretail.telesales.Base.Common;
import com.gin.ngeretail.telesales.Data.UserData;
import com.gin.ngeretail.telesales.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class LeftMenuModule {
    private final AppCompatActivity activity;
    private final Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ImageView ivProfilePic;
    private TextView tvUsername,tvAppVersion;

    private ILeftMenu listener;
    public LeftMenuModule(AppCompatActivity activity, Toolbar toolbar) {
        this.activity = activity;
        this.toolbar = toolbar;
        initComponent();
    }

    private void initComponent() {
        drawerLayout = activity.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawerLayout, toolbar, R.string.open_nav_drawer, R.string.close_nav_drawer);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = activity.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            try {
                if (listener == null)
                    return false;

                boolean result = listener.onMenuItemSelected(item.getItemId());
                drawerLayout.closeDrawer(GravityCompat.START);

                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });

        View view = navigationView.getHeaderView(0);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvAppVersion =view.findViewById(R.id.tvAppVersion);
    }

    public void setMenuResourse(int resourse) {
        navigationView.getMenu().clear();
        navigationView.inflateMenu(resourse);
    }

    public void setProfileInfo(UserData userData) {
        if (userData != null) {
            //Bitmap bitmap = BitmapFactory.decodeFile(userData.profileImage);
            tvUsername.setText(userData.emailUser);
            Picasso.get()
                    .load(userData.profileImage)
                    .transform(new CropCircleTransformation())
                    .into(ivProfilePic);
//           byte[] bytes  = Base64.decode(userData.profileImage,Base64.DEFAULT);
//           InputStream inputStream = new ByteArrayInputStream(bytes);
//           Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
           //ivProfilePic.setImageBitmap(CallCustomerActivity.getCircleBitmap(ConvertStringToBitmap("https://lh6.googleusercontent.com/-0NceADWyNBU/AAAAAAAAAAI/AAAAAAAAAWk/MoXhL6Zs20w/s96-c/photo.jpg")));
//           ivProfilePic.setImageBitmap(bitmap);
        }
    }

    public static Bitmap ConvertStringToBitmap(String image64) {
        Bitmap bitmap;
        try {
            byte[] decodedByte = ConvertString64ToByte(image64);
            bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);

        }catch (Exception e) {
            e.printStackTrace();
            bitmap = null;
        }

        return bitmap;
    }

    public void setAppVersion(Common.ApplicationVersion appVersion) {
        String strAppVersion;
        if (appVersion.isDebugAble)
            strAppVersion = "DEBUG: av:" + appVersion.versionName + " cv:" + Integer.toString(appVersion.versionCode);
        else
            strAppVersion = "av:" + appVersion.versionName + " cv:" + Integer.toString(appVersion.versionCode);
        tvAppVersion.setText(strAppVersion);
    }

    public static byte[] ConvertString64ToByte(String base64String) {
        return Base64.decode(base64String.getBytes(), Base64.DEFAULT);
    }

    public void closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (listener != null)
                listener.onHandleBackPressed();
        }
    }

    public void setLeftMenuListener(ILeftMenu listener) {
        this.listener = listener;
    }

    public interface ILeftMenu {
        boolean onMenuItemSelected(int itemId) throws Exception;
        void onHandleBackPressed();
    }
}

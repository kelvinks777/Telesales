package com.gin.ngeretail.telesales.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.gin.ngeretail.telesales.Component.UI.CustomAlert;
import com.gin.ngeretail.telesales.Data.ApiData;
import com.gin.ngeretail.telesales.Data.UserData;
import com.gin.ngeretail.telesales.Data.SharedPreferenceData;
import com.gin.ngeretail.telesales.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText edtemail;
    private SharedPreferenceData sharedPreferenceData;
    private AsyncHttpClient httpClient;
    private ProgressDialog progressDialog;
    private UserData userData;
    private CustomAlert alert;

    //for login
    private static final String TAG ="GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
//
//    // [START declare_auth]
//    private FirebaseAuth mAuth;
//    // [END declare_auth]
//
//    private GoogleSignInClient mGoogleSignInClient;
//

    //region override function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            init();
            checkingPermission();
            checkLoginStatusToken(sharedPreferenceData);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLogin:
                if(validateLogin() && isValidEmailId(edtemail.getText().toString().trim())){
                    doLogin(edtemail.getText().toString());
                }
                else{
                    alert.showToastMessage("Invalid Email Address.");
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true);
            }
        };
        alert.showAskDialog("Keluar dari aplikasi?", okListener);
    }
    //endregion

    //region other function

    private void checkLoginStatus(SharedPreferenceData sharedPreferenceData) {
        boolean statusLogin = (boolean) sharedPreferenceData.read(UserData.KEY_LOGIN_STATUS, SharedPreferenceData.BOOLEAN);
        String email = (String) sharedPreferenceData.read(UserData.KEY_EMAIL, SharedPreferenceData.STRING);
        String tokenId = (String) sharedPreferenceData.read(UserData.KEY_TOKEN, SharedPreferenceData.STRING);

        if(statusLogin){
            userData = setUserData(tokenId, email, true);
            moveToCallCustomerActivity(userData);
        }
    }

    private void moveToCallCustomerActivity(UserData userData){
        Intent intent = new Intent(getApplicationContext(), CallCustomerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CallCustomerActivity.USER_DATA_CODE, userData);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private boolean validateLogin(){
        String warningText = "Harus diisi !";
        boolean isValid= true;
        if (edtemail.getText().toString().equals("")) {
            edtemail.setError(warningText);
            isValid=false;
        }

        return isValid;

    }

    private void startProgressDialog(int type) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Mohon tunggu");
        progressDialog.setMessage("Sedang memproses login");
        progressDialog.setProgressStyle(type);
        progressDialog.setIndeterminate(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    private void dismissProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

    private UserData setUserData(String tokenId, String email, boolean loginStatus) {
        UserData userData = new UserData();
        userData.tokenId = tokenId;
        userData.emailUser = email;
        userData.loginStatus = loginStatus;

        return userData;
    }

    private boolean isValidEmailId(String email){
        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }

    private void checkingPermission() {
        if(Build.VERSION.SDK_INT >= CallCustomerActivity.MINIMUM_SDK_VERSION){
            final String[] permission = {Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_CALL_LOG};
            ActivityCompat.requestPermissions(this, permission, CallCustomerActivity.ALL_PERMISSION_CODE);
        }
    }

    //endregion

    //region async function

    private void doLogin(final String email){
        String url = ApiData.BASE_URL + "Login/SignIn?email=" + email + ApiData.APP_ID;
        httpClient.get(this, url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                startProgressDialog(ProgressDialog.STYLE_SPINNER);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject object = new JSONObject(result);
                    String tokenId = object.getString(UserData.KEY_TOKEN);

                    sharedPreferenceData.write(UserData.KEY_TOKEN, tokenId);
                    sharedPreferenceData.write(UserData.KEY_EMAIL, email);
                    sharedPreferenceData.write(UserData.KEY_LOGIN_STATUS, true);

                    userData = setUserData(tokenId, email, true);

                    dismissProgressDialog();
                    moveToCallCustomerActivity(userData);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dismissProgressDialog();
                alert.showToastMessage("Login gagal");
            }
        });
    }

    //endregion

    //region init function

    private void init(){
        initObject();
        edtemail = findViewById(R.id.edtEmail);
        Button btnlogin = findViewById(R.id.btnLogin);
        btnlogin.setOnClickListener(this);
    }

    private void initObject() {
        httpClient = new AsyncHttpClient();
        sharedPreferenceData = new SharedPreferenceData(UserData.FILE_NAME, this);
        alert = new CustomAlert(this);
    }

    private void checkLoginStatusToken(SharedPreferenceData sharedPreferenceData) {
        String tokenId = (String) sharedPreferenceData.read(UserData.KEY_TOKEN, SharedPreferenceData.STRING);

        if(tokenId.equals("")){
//            Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
//            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
//            this.startActivity(intent);
        }
        else{
            checkTokenValid(tokenId);
        }
    }

    private void checkTokenValid(String token){
        ApiData.Authorize(httpClient, token);
        String url = ApiData.BASE_URL + "Login/TokenTest?tokenData=" + token;
        httpClient.get(this, url, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                   // boolean statusLogin = (boolean) sharedPreferenceData.read(UserData.KEY_LOGIN_STATUS, SharedPreferenceData.BOOLEAN);
                    String email = (String) sharedPreferenceData.read(UserData.KEY_EMAIL, SharedPreferenceData.STRING);
                    String tokenId = (String) sharedPreferenceData.read(UserData.KEY_TOKEN, SharedPreferenceData.STRING);
                    userData = setUserData(tokenId, email, true);

                    //dismissProgressDialog();
                    moveToCallCustomerActivity(userData);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //dismissProgressDialog();
//                alert.showToastMessage("Login gagal");
                Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    //endregion
}

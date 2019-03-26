package com.gin.ngeretail.telesales.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.gin.ngeretail.telesales.BuildConfig;
import com.gin.ngeretail.telesales.Component.UI.CustomAlert;
import com.gin.ngeretail.telesales.Data.ApiData;
import com.gin.ngeretail.telesales.Data.FirebaseUserData;
import com.gin.ngeretail.telesales.Data.Global;
import com.gin.ngeretail.telesales.Data.SharedPreferenceData;
import com.gin.ngeretail.telesales.Data.User;
import com.gin.ngeretail.telesales.Data.UserData;
import com.gin.ngeretail.telesales.R;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LoginTelesalesActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth auth;
    private static final int RC_SIGN_IN = 9001;
    private Button btnGoogleSignIn;
    private GoogleSignInClient mGoogleSignInClient;
    private AsyncHttpClient httpClient;
    private CustomAlert alert;
    private static CustomAlert alertInfoError;
    private ProgressDialog progressDialog;
    private DatabaseReference mDatabase;
    private SharedPreferenceData sharedPreferenceData;
    private String firebaseToken;
    private String email;
    private String foto;
    private Global global;
    public static final int MINIMUM_SDK_VERSION = 23;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login_telesales);
            checkingPermission();
            global.checkForDebugOrRelease();
            info();
            init();
            checkLoginStatusToken(sharedPreferenceData);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account){
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //email = task.getResult().getUser().getEmail();
                            email = account.getEmail().toString();
                            foto = account.getPhotoUrl().toString();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = auth.getCurrentUser();
                            User users = new User(account.getEmail(), user.getDisplayName(), user.getDisplayName(), user.getPhotoUrl().toString());
                            mDatabase.child("users").child(user.getUid()).setValue(users);
                            getFirebaseToken(user);
                        } else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage() ,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getFirebaseToken(FirebaseUser user) {
        user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(Task<GetTokenResult> task) {
                if(task.isSuccessful()) {
                    //get token
                     firebaseToken = task.getResult().getToken();
                    //send to NGES
                    signIns(firebaseToken);
                } else {
                    //handle error
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase

                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

                //onSuccessResult(requestCode, resultCode, data);
            } catch (Exception e) {
                // Google Sign In failed, update UI appropriately
                Log.e("telesales_login", e.getMessage());
                // ...
            }
        }
    }

    private void checkingPermission() {
        if(Build.VERSION.SDK_INT >= MINIMUM_SDK_VERSION){
            final String[] permission = {Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_CALL_LOG};
            ActivityCompat.requestPermissions(this, permission, CallCustomerActivity.ALL_PERMISSION_CODE);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGoogleSignIn:
                signIn();
                break;
        }
    }

    public void startProgressDialog(int type) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Mohon tunggu");
        progressDialog.setMessage("Sedang memproses login");
        progressDialog.setProgressStyle(type);
        progressDialog.setIndeterminate(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    private void init(){
        initObject();
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnGoogleSignIn.setOnClickListener(this);
    }

    private void initObject(){
        httpClient = new AsyncHttpClient();
        alert = new CustomAlert(this);
        alertInfoError = new CustomAlert(this);
        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sharedPreferenceData = new SharedPreferenceData(UserData.FILE_NAME, this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.telesales_id_new))
                .requestEmail()
                .build();

        mGoogleSignInClient =GoogleSignIn.getClient(this,gso);
    }

    private void  saveUserToDb(final String uid, final FirebaseUserData firebaseUserData, final DatabaseReference.CompletionListener completionListener) {
        mDatabase
                .child(uid)
                .setValue(firebaseUserData, completionListener);
    }

    private FirebaseUserData getFirebaseUserDataFromGoogleSignInAccount(GoogleSignInAccount account) {
        FirebaseUserData result = new FirebaseUserData();
        result.email = account.getEmail();
        result.nickName = account.getGivenName() == null ? "" : account.getGivenName();
        result.fullName = account.getDisplayName();
        result.photoUrl = account.getPhotoUrl() == null ? "" : account.getPhotoUrl().toString();
        return result;
    }

    private void writeNewUser(String userId, String email, String nickName, String fullName, String photoUrl) {
        User user = new User(email, nickName, fullName, photoUrl);

        mDatabase.child("users").child(userId).setValue(user);
    }

    private void signIns(String token){
        String url = global.BASE_URL_2+ "signIn?signInToken=" + token + ApiData.APP_ID;
        httpClient.setTimeout(5000);
        httpClient.get(this, url, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                startProgressDialog(ProgressDialog.STYLE_SPINNER);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (statusCode == 200){
                    String result = new String(responseBody);
                    try {
                        JSONObject object = new JSONObject(result);
                        String tokenId = object.getString(UserData.KEY_TOKEN);
                        getUser(tokenId);
//                    sharedPreferenceData.write(UserData.KEY_TOKEN,tokenId);
//                    sharedPreferenceData.write(UserData.KEY_EMAIL,email);
//                    sharedPreferenceData.write(UserData.PROFILE_PIC, foto);
//                    UserData userData = setUserData(tokenId, email, true,foto,"");
//                    dismissProgressDialog();
//                    moveToCallCustomerActivity(userData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dismissProgressDialog();
                outGoogle();
                alertInfoError.showError(error.getMessage(), (Exception) error, null);
            }

//            @Override
//            public void onRetry(int retryNo) {
//               if(retryNo == 408){
//                   signIns(token);
//                   return;
//               }
//            }

        });
    }

    private void getUser(String token){
        ApiData.Authorize(httpClient, token);
        String url = global.BASE_URL_2+ "getUser?token=" + token + ApiData.APP_ID;
        httpClient.setTimeout(5000);

        httpClient.get(this, url, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject object = new JSONObject(result);
                    //String tokenId = object.getString(UserData.KEY_TOKEN);
                    sharedPreferenceData.write(UserData.KEY_TOKEN,token);
                    sharedPreferenceData.write(UserData.KEY_EMAIL,email);
                    sharedPreferenceData.write(UserData.PROFILE_PIC, foto);
                    UserData userData = setUserData(token, email, true,foto,"");
                    dismissProgressDialog();
                    moveToCallCustomerActivity(userData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dismissProgressDialog();
                outGoogle();
                alertInfoError.showError(error.getMessage(), (Exception) error, null);
            }
        });

    }

    private UserData setUserData(String tokenId, String email, boolean loginStatus, String profile, String sim) {
        UserData userData = new UserData();
        userData.tokenId = tokenId;
        userData.emailUser = email;
        userData.loginStatus = loginStatus;
        userData.profileImage = profile;
        userData.sim = sim;
        return userData;
    }

//    private void getData(String token){
//        String url = ApiData.BASE_URL_2 + "getUser?token=" + token + ApiData.APP_ID;
//        httpClient.get(this, url, new AsyncHttpResponseHandler() {
//
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                String dataJson = new String(responseBody);
//                try {
//                    JSONObject object = new JSONObject(dataJson);
//                    String email = object.getString(UserData.KEY_EMAIL);
//                    String token = object.getString(UserData.KEY_TOKEN);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                dismissProgressDialog();
//                alert.showToastMessage("Login gagal");
//            }
//        });
//    }


//    private void checkLoginStatus(SharedPreferenceData sharedPreferenceData){
//        FirebaseAuth.AuthStateListener mAuthlistener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                if (auth.getCurrentUser() == null) {
//                    startActivity(new Intent(LoginTelesalesActivity.this, LoginTelesalesActivity.class));
//                } else {
//                    String email = (String) sharedPreferenceData.read(UserData.KEY_EMAIL, SharedPreferenceData.STRING);
//                    String tokenId = (String) sharedPreferenceData.read(UserData.KEY_TOKEN, SharedPreferenceData.STRING);
//                    UserData userData = setUserData(tokenId, email, true);
//                    Intent intent = new Intent(getApplicationContext(), CallCustomerActivity.class);
//                    Bundle bundle = new Bundle();
//                    bundle.putSerializable(CallCustomerActivity.USER_DATA_CODE, userData);
//                    intent.putExtras(bundle);
//                    startActivity(intent);
//                }
//            }
//        };
//
//        auth.addAuthStateListener(mAuthlistener);
//    }

    private void moveToCallCustomerActivity(UserData userData){
        Intent intent = new Intent(getApplicationContext(), CallCustomerActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CallCustomerActivity.USER_DATA_CODE, userData);
        intent.putExtras(bundle);
        startActivity(intent);
    }

//    private void checkTokenValid(String token){
//        ApiData.Authorize(httpClient,token);
//        String url = ApiData.BASE_URL + "Login/TokenTest?tokenData=" + token;
//        httpClient.get(this, url, new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                String tokenIdss=(String)sharedPreferenceData.read(UserData.KEY_TOKEN,SharedPreferenceData.STRING);
//                String email=(String)sharedPreferenceData.read(UserData.KEY_EMAIL,SharedPreferenceData.STRING);
//                String foto = (String)sharedPreferenceData.read(UserData.PROFILE_PIC, SharedPreferenceData.STRING);
//                UserData userdata =setUserData(tokenIdss,email,true, foto);
//                moveToCallCustomerActivity(userdata);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
////                Intent intent = new Intent(LoginTelesalesActivity.this, LoginTelesalesActivity.class);
////                startActivity(intent);
//            }
//        });
//    }

    private void checkLoginStatusToken(SharedPreferenceData sharedPreferenceData) {
        String tokenIds = (String) sharedPreferenceData.read(UserData.KEY_TOKEN, SharedPreferenceData.STRING);

        if(tokenIds.equals("")){

        }
        else{
            String tokenIdss=(String)sharedPreferenceData.read(UserData.KEY_TOKEN,SharedPreferenceData.STRING);
            String email=(String)sharedPreferenceData.read(UserData.KEY_EMAIL,SharedPreferenceData.STRING);
            String foto = (String)sharedPreferenceData.read(UserData.PROFILE_PIC, SharedPreferenceData.STRING);
            String sim =(String)sharedPreferenceData.read(UserData.CHOSEE_SIM, SharedPreferenceData.STRING);
            UserData userdata =setUserData(tokenIdss,email,true, foto, sim);
            moveToCallCustomerActivity(userdata);
        }
    }

    private void info(){
        Toast.makeText(getApplicationContext(),global.INFO,Toast.LENGTH_SHORT).show();
    }

    private void outGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.telesales_id_new))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(),gso);
        mGoogleSignInClient.signOut();
        auth.signOut();
    }

}

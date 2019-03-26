package com.gin.ngeretail.telesales.Data;

public class User {
    public String email;
    public String nickName;
    public String fullName;
    public String photoUrl;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String nickName, String fullName, String photoUrl) {
        this.email = email;
        this.nickName = nickName;
        this.fullName = fullName;
        this.photoUrl = photoUrl;
    }
}

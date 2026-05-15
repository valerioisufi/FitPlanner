package com.example.fitplannercommon;

public class LoginBean {
    private String email;
    private String password;

    public LoginBean() {}

    public LoginBean(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}

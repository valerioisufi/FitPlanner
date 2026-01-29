package com.example.fitplannercommon;

public class RegisterBean {
    private String username;
    private String password;

    // Regex: almeno 1 numero, 1 minuscola, 1 maiuscola, min 8 caratteri
    private static final String REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
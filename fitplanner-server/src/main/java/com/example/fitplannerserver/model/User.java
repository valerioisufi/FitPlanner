package com.example.fitplannerserver.model;

import java.time.LocalDate;
import java.time.Period;

public abstract class User {
    private String name;
    private String surname;
    private String email;
    private LocalDate birthdate;
    private String gender;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }
    public void setBirthdate(int birthdate) {
        this.birthdate = LocalDate.ofEpochDay(birthdate);
    }
    public int getAge (){
        if (this.birthdate == null) throw new IllegalArgumentException("birthdate null");
        LocalDate now = LocalDate.now();
        if (birthdate.isAfter(now)) throw new IllegalArgumentException("birthdate nel futuro");
        return Period.between(birthdate, now).getYears();
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
}

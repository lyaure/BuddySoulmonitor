package com.buddynsoul.monitor;

public class User {
    private String name;
    private String email;
    private String registrationDate;
    private boolean admin;

    public User(String name, String email, String registrationDate, boolean admin) {
        this.name = name;
        this.email = email;
        this.registrationDate = registrationDate;
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}

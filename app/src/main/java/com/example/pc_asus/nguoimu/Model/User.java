package com.example.pc_asus.nguoimu.Model;

public class User {
    public String name;
    public  String email;
    public String phoneNumber;
    public String photoURL;
    public String idDevice;

    public User() {
    }

    public User(String name, String email, String phoneNumber, String photoURL,String idDevice) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.photoURL = photoURL;
        this.idDevice = idDevice;
    }
}

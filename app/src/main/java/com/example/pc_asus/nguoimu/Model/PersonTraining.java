package com.example.pc_asus.nguoimu.Model;

public class PersonTraining {

    public  String name;
    public String id;
    public String photoURL;
    public String key;

    public PersonTraining() {
    }

    public PersonTraining(String name, String id, String photoURL) {
        this.name = name;
        this.id = id;
        this.photoURL = photoURL;
    }

    public PersonTraining(String name, String id, String photoURL, String key) {
        this.name = name;
        this.id = id;
        this.photoURL = photoURL;
        this.key = key;
    }
}

package com.ccsidd.rtone.objects;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by dung on 8/19/15.
 */
public class User extends RealmObject {

    @Required
    private String username;
    private String password;

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

package com.seblogapps.stognacci.restserviceexample;

import android.app.Application;

import com.seblogapps.stognacci.restserviceexample.data.User;

/**
 * Created by stognacci on 29/04/2016.
 */
public class RESTServiceApplication extends Application {
    private static RESTServiceApplication instance;
    private User user;
    private String accessToken;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        user = new User();
    }

    public static RESTServiceApplication getInstance() {
        return instance;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

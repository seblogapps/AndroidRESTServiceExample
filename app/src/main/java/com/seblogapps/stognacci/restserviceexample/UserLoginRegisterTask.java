package com.seblogapps.stognacci.restserviceexample;

import android.content.Context;

import com.seblogapps.stognacci.restserviceexample.webservices.WebServiceTask;

/**
 * Created by stognacci on 29/04/2016.
 */
public class UserLoginRegisterTask extends WebServiceTask {

    String mEmail;
    String mPassword;
    Boolean mShowButton;

    public UserLoginRegisterTask(Context context, String email, String password, Boolean showButton) {
        super(context);
        this.mEmail = email;
        this.mPassword = password;
        this.mShowButton = showButton;
    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void showProgress() {

    }

    @Override
    public boolean performRequest() {
        return false;
    }

    @Override
    public void performSuccessfulOperation() {

    }
}

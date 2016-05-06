package com.seblogapps.stognacci.restserviceexample;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import com.seblogapps.stognacci.restserviceexample.data.User;
import com.seblogapps.stognacci.restserviceexample.webservices.WebServiceTask;
import com.seblogapps.stognacci.restserviceexample.webservices.WebServiceUtils;

import org.json.JSONObject;

public class LoginRegisterActivity extends AppCompatActivity {

    private static final String LOG_TAG = LoginRegisterActivity.class.getSimpleName();

    private UserLoginRegisterTask mUserLoginRegisterTask = null;
    private EditText mEmailView;
    private EditText mPasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        Log.d(LOG_TAG, "hasInternetConnection: " + WebServiceUtils.hasInternetConnection(LoginRegisterActivity.this));
    }

    private void initViews() {
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
    }

    public void attempLoginRegister(View view) {
        boolean cancel = false;
        View focusView = null;

        if (mUserLoginRegisterTask != null) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_password_length));
            focusView = mPasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            mUserLoginRegisterTask = new UserLoginRegisterTask(email, password, view.getId() == R.id.email_sign_in_button);
            mUserLoginRegisterTask.execute((Void) null);
        }

    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showProgress(final boolean isShown) {
        findViewById(R.id.login_progress).setVisibility(isShown ? View.VISIBLE : View.GONE);
        findViewById(R.id.login_form).setVisibility(isShown ? View.VISIBLE : View.GONE);
    }

    private class UserLoginRegisterTask extends WebServiceTask {

        private final ContentValues mContentValues = new ContentValues();
        private boolean mIsLogin;


        public UserLoginRegisterTask(String email, String password, Boolean isLogin) {
            super(LoginRegisterActivity.this);
            mContentValues.put(Constants.EMAIL, email);
            mContentValues.put(Constants.PASSWORD, password);
            mContentValues.put(Constants.GRANT_TYPE, Constants.CLIENT_CREDENTIALS);
            this.mIsLogin = isLogin;
        }

        @Override
        public void showProgress() {
            LoginRegisterActivity.this.showProgress(true);
        }

        @Override
        public void hideProgress() {
            LoginRegisterActivity.this.showProgress(false);
        }

        @Override
        public boolean performRequest() {
            JSONObject jsonObject = WebServiceUtils.requestJSONObject(mIsLogin ? Constants.LOGIN_URL : Constants.SIGNUP_URL,
                    WebServiceUtils.METHOD.POST, mContentValues, true);
            mUserLoginRegisterTask = null;
            if (!hasError(jsonObject)) {
                if (mIsLogin) {
                    User user = new User();
                    user.setId(jsonObject.optLong(Constants.ID));
                    user.setEmail(mContentValues.getAsString(Constants.EMAIL));
                    user.setPassword(mContentValues.getAsString(Constants.PASSWORD));
                    RESTServiceApplication.getInstance().setUser(user);
                    RESTServiceApplication.getInstance().setAccessToken(
                            jsonObject.optJSONObject(Constants.ACCESS).optString(Constants.ACCESS_TOKEN));
                    return true;
                } else {
                    mIsLogin = true;
                    performRequest();
                    return true;
                }
            }
            return false;
        }


        @Override
        public void performSuccessfulOperation() {
            Intent intent = new Intent(LoginRegisterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}

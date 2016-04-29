package com.seblogapps.stognacci.restserviceexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import com.seblogapps.stognacci.restserviceexample.webservices.WebServiceUtils;

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
            mUserLoginRegisterTask = new UserLoginRegisterTask(LoginRegisterActivity.this, email, password, view.getId() == R.id.email_sign_in_button);
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
}

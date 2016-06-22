package com.seblogapps.stognacci.restserviceexample;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.seblogapps.stognacci.restserviceexample.data.User;
import com.seblogapps.stognacci.restserviceexample.webservices.WebServiceTask;
import com.seblogapps.stognacci.restserviceexample.webservices.WebServiceUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private UserInfoTask mUserInfoTask = null;
    private UserEditTask mUserEditTask = null;
    private UserResetTask mUserResetTask = null;
    private UserDeleteTask mUserDeleteTask = null;

    //private EditText mEmailText;
    private TextInputEditText mEmailText;
    private TextInputEditText mPasswordText;
    private TextInputEditText mNameText;
    private TextInputEditText mPhoneNumberText;
    private TextInputEditText mNoteText;

    private interface ConfirmationListener {

        void onConfirmation(boolean isConfirmed);
    }
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        showProgress(true);
        mUserInfoTask = new UserInfoTask();
        mUserInfoTask.execute();
    }

    private void showProgress(final boolean isShow) {
        findViewById(R.id.progress).setVisibility(isShow ? View.VISIBLE : View.GONE);
        findViewById(R.id.email_TIL).setVisibility(isShow ? View.GONE : View.VISIBLE);
        findViewById(R.id.password_TIL).setVisibility(isShow ? View.GONE : View.VISIBLE);
        findViewById(R.id.name_TIL).setVisibility(isShow ? View.GONE : View.VISIBLE);
        findViewById(R.id.phoneNumber_TIL).setVisibility(isShow ? View.GONE : View.VISIBLE);
        findViewById(R.id.note_TIL).setVisibility(isShow ? View.GONE : View.VISIBLE);
        findViewById(R.id.update_button).setVisibility(isShow ? View.GONE : View.VISIBLE);
        findViewById(R.id.delete_button).setVisibility(isShow ? View.GONE : View.VISIBLE);
        findViewById(R.id.reset_button).setVisibility(isShow ? View.GONE : View.VISIBLE);
        findViewById(R.id.sign_out_button).setVisibility(isShow ? View.GONE : View.VISIBLE);
    }

    private void populateText() {
        User user = RESTServiceApplication.getInstance().getUser();
        mEmailText.setText(user.getEmail());
        mPasswordText.setText(user.getPassword());
        mNameText.setText(user.getName() == null ? "" : user.getName());
        mPhoneNumberText.setText(user.getPhoneNumber() == null ? "" : user.getPhoneNumber());
        mNoteText.setText(user.getNote() == null ? "" : user.getNote());
    }

    private void initViews() {
        mEmailText = (TextInputEditText) findViewById(R.id.email);
        mPasswordText = (TextInputEditText) findViewById(R.id.password);
        mNameText = (TextInputEditText) findViewById(R.id.name);
        mPhoneNumberText = (TextInputEditText) findViewById(R.id.phoneNumber);
        mNoteText = (TextInputEditText) findViewById(R.id.note);
    }

    public void clickUpdateButton(View view) {
        if (mPasswordText.getText().toString().trim().length() >= 5) {
            showProgress(true);
            mUserEditTask = new UserEditTask();
            mUserEditTask.execute();
        } else {
            Toast.makeText(this, R.string.error_password_length, Toast.LENGTH_LONG).show();
        }
    }

    public void clickDeleteButton(View view) {
        showConfigurationDialog(new ConfirmationListener() {
            @Override
            public void onConfirmation(boolean isConfirmed) {
                if (isConfirmed) {
                    showProgress(true);
                    mUserDeleteTask = new UserDeleteTask();
                    mUserDeleteTask.execute();
                }
            }
        });
    }

    public void clickResetButton(View view) {
        showConfigurationDialog(new ConfirmationListener() {
            @Override
            public void onConfirmation(boolean isConfirmed) {
                if (isConfirmed) {
                    showProgress(true);
                    mUserResetTask = new UserResetTask();
                    mUserResetTask.execute();
                }
            }
        });
    }

    private void showConfigurationDialog(final ConfirmationListener confirmationListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure? This operation cannot be undone");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmationListener.onConfirmation(true);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                confirmationListener.onConfirmation(false);
                dialog.dismiss();
            }
        });
        builder.create();
        builder.show();
    }

    public void clickSignOutButton(View view) {
        showLoginScreen();
    }

    private void showLoginScreen() {
        Intent loginIntent = new Intent(MainActivity.this, LoginRegisterActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }

    private abstract class ActivityWebServiceTask extends WebServiceTask {
        public ActivityWebServiceTask(WebServiceTask webServiceTask) {
            super(MainActivity.this);
        }

        @Override
        public void showProgress() {
            MainActivity.this.showProgress(true);
        }

        @Override
        public void hideProgress() {
            MainActivity.this.showProgress(false);
        }

        @Override
        public void performSuccessfulOperation() {
            populateText();
        }
    }

    private class UserInfoTask extends ActivityWebServiceTask {
        public UserInfoTask() {
            super(mUserInfoTask);
        }

        public boolean performRequest() {
            ContentValues contentValues = new ContentValues();
            User user = RESTServiceApplication.getInstance().getUser();
            contentValues.put(Constants.ID, user.getId());
            contentValues.put(Constants.ACCESS_TOKEN,
                    RESTServiceApplication.getInstance().getAccessToken());

            JSONObject obj = WebServiceUtils.requestJSONObject(Constants.INFO_URL,
                    WebServiceUtils.METHOD.GET, contentValues, null);
            if (!hasError(obj)) {
                JSONArray jsonArray = obj.optJSONArray(Constants.INFO);
                JSONObject jsonObject = jsonArray.optJSONObject(0);

                user.setName(jsonObject.optString(Constants.NAME));
                if (user.getName().equalsIgnoreCase("null")) {
                    user.setName(null);
                }

                user.setPhoneNumber(jsonObject.optString(Constants.PHONE_NUMBER));
                if (user.getPhoneNumber().equalsIgnoreCase("null")) {
                    user.setPhoneNumber(null);
                }

                user.setNote(jsonObject.optString(Constants.NOTE));
                if (user.getNote().equalsIgnoreCase("null")) {
                    user.setNote(null);
                }

                user.setId(jsonObject.optLong(Constants.ID_INFO));
                return true;
            }
            return false;
        }
    }

    private class UserEditTask extends ActivityWebServiceTask {
        public UserEditTask() {
            super(mUserEditTask);
        }

        public boolean performRequest() {
            ContentValues contentValues = new ContentValues();
            User user = RESTServiceApplication.getInstance().getUser();
            contentValues.put(Constants.ID, user.getId());
            contentValues.put(Constants.NAME, mNameText.getText().toString());
            contentValues.put(Constants.PASSWORD, mPasswordText.getText().toString());
            contentValues.put(Constants.PHONE_NUMBER, mPhoneNumberText.getText().toString());
            contentValues.put(Constants.NOTE, mNoteText.getText().toString());

            ContentValues urlValues = new ContentValues();
            urlValues.put(Constants.ACCESS_TOKEN,
                    RESTServiceApplication.getInstance().getAccessToken());

            JSONObject obj = WebServiceUtils.requestJSONObject(Constants.UPDATE_URL,
                    WebServiceUtils.METHOD.POST, urlValues, contentValues);
            if (!hasError(obj)) {
                JSONArray jsonArray = obj.optJSONArray(Constants.INFO);
                JSONObject jsonObject = jsonArray.optJSONObject(0);
                user.setName(jsonObject.optString(Constants.NAME));
                user.setPassword(jsonObject.optString(Constants.PASSWORD));
                user.setPhoneNumber(jsonObject.optString(Constants.PHONE_NUMBER));
                user.setNote(jsonObject.optString(Constants.NOTE));
                return true;
            }
            return false;
        }
    }

    private class UserResetTask extends ActivityWebServiceTask {
        public UserResetTask() {
            super(mUserResetTask);
        }

        public boolean performRequest() {
            ContentValues contentValues = new ContentValues();
            User user = RESTServiceApplication.getInstance().getUser();
            contentValues.put(Constants.ID, user.getId());
            contentValues.put(Constants.ACCESS_TOKEN,
                    RESTServiceApplication.getInstance().getAccessToken());

            JSONObject obj = WebServiceUtils.requestJSONObject(Constants.RESET_URL,
                    WebServiceUtils.METHOD.POST, contentValues, null);
            if (!hasError(obj)) {
                user.setName("");
                user.setPhoneNumber("");
                user.setNote("");
                return true;
            }
            return false;
        }
    }

    private class UserDeleteTask extends ActivityWebServiceTask {
        public UserDeleteTask() {
            super(mUserDeleteTask);
        }

        @Override
        public void performSuccessfulOperation() {
            showLoginScreen();
        }

        public boolean performRequest() {
            ContentValues contentValues = new ContentValues();
            User user = RESTServiceApplication.getInstance().getUser();
            contentValues.put(Constants.ID, user.getId());
            contentValues.put(Constants.ACCESS_TOKEN,
                    RESTServiceApplication.getInstance().getAccessToken());

            JSONObject obj = WebServiceUtils.requestJSONObject(Constants.DELETE_URL,
                    WebServiceUtils.METHOD.DELETE,
                    contentValues, null);

            if (!hasError(obj)) {
                RESTServiceApplication.getInstance().setUser(null);
                return true;
            }
            return false;
        }
    }
}

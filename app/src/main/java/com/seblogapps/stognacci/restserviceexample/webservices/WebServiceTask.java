package com.seblogapps.stognacci.restserviceexample.webservices;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.seblogapps.stognacci.restserviceexample.Constants;
import com.seblogapps.stognacci.restserviceexample.R;

import org.json.JSONObject;

/**
 * Created by stognacci on 29/04/2016.
 */
public abstract class WebServiceTask extends AsyncTask<Void, Void, Boolean> {

    private static final String LOG_TAG = WebServiceUtils.class.getSimpleName();

    public abstract void showProgress();

    public abstract boolean performRequest();

    public abstract void performSuccessfulOperation();

    public abstract void hideProgress();

    private String mMessage;
    private Context mContext;

    public WebServiceTask(Context context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        showProgress();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (!WebServiceUtils.hasInternetConnection(mContext)) {
            mMessage = Constants.CONNECTION_MESSAGE;
            return false;
        }
        return performRequest();
    }

    @Override
    protected void onPostExecute(Boolean success) {
        hideProgress();
        if (success) {
            performSuccessfulOperation();
        }
        if (mMessage != null && !mMessage.isEmpty()) {
            Toast.makeText(mContext, mMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled() {
        hideProgress();
    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        hideProgress();
    }

    public boolean hasError(JSONObject obj) {
        if (obj != null) {
            int status = obj.optInt(Constants.STATUS);
            Log.d(LOG_TAG, "Response: " + obj.toString());
            mMessage = obj.optString(Constants.MESSAGE);
            if (status == Constants.STATUS_ERROR || status == Constants.STATUS_UNAUTHORIZED) {
                return true;
            } else {
                return false;
            }
        }
        mMessage = mContext.getString(R.string.error_url_not_found);
        return true;
    }
}

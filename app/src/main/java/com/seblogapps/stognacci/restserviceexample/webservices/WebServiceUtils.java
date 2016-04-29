package com.seblogapps.stognacci.restserviceexample.webservices;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.seblogapps.stognacci.restserviceexample.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by stognacci on 27/04/2016.
 */
public class WebServiceUtils {

    public static final String LOG_TAG = WebServiceUtils.class.getSimpleName();

    public enum METHOD {
        POST, GET, DELETE
    }

    public static JSONObject requestJSONObject(String serviceUrl, METHOD method,
                                               ContentValues headerValues, boolean hasAuthorization) {
        return requestJSONObject(serviceUrl, method, headerValues, null, null, hasAuthorization);
    }

    public static JSONObject requestJSONObject(String serviceUrl, METHOD method,
                                               ContentValues urlValues, ContentValues bodyValues) {
        return requestJSONObject(serviceUrl, method, null, urlValues, bodyValues, false);
    }

    private static JSONObject requestJSONObject(String serviceURL, METHOD method,
                                                ContentValues headerValues,
                                                ContentValues urlValues, ContentValues bodyValues, boolean hasAuthorization) {
        HttpURLConnection urlConnection = null;
        try {
            if (urlValues != null) {
                serviceURL = addParametersToUrl(serviceURL, urlValues);
            }
            URL urlToRequest;
            urlToRequest = new URL(serviceURL);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            urlConnection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(Constants.READ_TIMEOUT);
            urlConnection.setRequestMethod(method.toString());
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            if (hasAuthorization) {
                addBasicAuthentication(urlConnection);
            }

            if (headerValues != null) {
                Uri.Builder builder = new Uri.Builder();
                for (String key : headerValues.keySet()) {
                    builder.appendQueryParameter(key, headerValues.getAsString(key));
                }
                String query = builder.build().getEncodedQuery();
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter((outputStream), "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                outputStream.close();
            }

            if (bodyValues != null) {
                JSONObject jsonObject = new JSONObject();
                for (String key : bodyValues.keySet()) {
                    jsonObject.put(key, bodyValues.getAsString(key));
                }
                String jsonToStr = jsonObject.toString();
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                OutputStream outputStream = urlConnection.getOutputStream();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
                outputStreamWriter.write(jsonToStr);
                outputStreamWriter.flush();
                outputStreamWriter.close();
            }

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                Log.d(LOG_TAG, "Unauthorized Access!");
            } else if (statusCode != HttpURLConnection.HTTP_OK) {
                Log.d(LOG_TAG, "URL Response Error");
            }

            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            return new JSONObject(convertInputStreamToString(inputStream));

        } catch (MalformedURLException e) {
            Log.d(LOG_TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } catch (ProtocolException e) {
            Log.d(LOG_TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(LOG_TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            Log.d(LOG_TAG, e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }


        return null;
    }

    private static String convertInputStreamToString(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String responseText;
        try {
            while ((responseText = bufferedReader.readLine()) != null) {
                stringBuilder.append(responseText);
            }
        } catch (IOException e) {
            Log.d(LOG_TAG, "IOException in convertInputStreamToString");
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static String addParametersToUrl(String serviceURL, ContentValues urlValues) {
        StringBuffer stringBuffer = new StringBuffer(serviceURL);
        stringBuffer.append("?");
        for (String key : urlValues.keySet()) {
            stringBuffer.append(key);
            stringBuffer.append("=");
            stringBuffer.append(urlValues.getAsString(key));
            stringBuffer.append("&");
        }
        // Remove last unwanted "&" 3 possible ways to do it
        //stringBuffer.replace(stringBuffer.length()-1, stringBuffer.length() -1, "");
        //stringBuffer.substring(0, stringBuffer.length()-1);
        stringBuffer.setLength(stringBuffer.length() - 1);
        return stringBuffer.toString();
    }

    private static void addBasicAuthentication(HttpURLConnection urlConnection) {
        final String basicAuth = "Basic " + Base64.encodeToString((Constants.APP_KEY +
        ":" + Constants.APP_SECRET).getBytes(), Base64.NO_WRAP);
        urlConnection.setRequestProperty(Constants.AUTHORIZATION, basicAuth);
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}

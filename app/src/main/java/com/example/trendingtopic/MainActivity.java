package com.example.trendingtopic;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.trendingtopic.activity.ShowTweetActivity;
import com.example.trendingtopic.activity.TwitterLoginActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {


    TwitterLoginButton loginButton;

    public static SessionManager<TwitterSession> s ;

    String url ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.activity_main);


        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET
        }, 10);



        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
        s = TwitterCore.getInstance().getSessionManager();
        System.err.println("khaled session is "+ session.getUserName());
        if (session == null) {
            // no session => login
            startActivity(new Intent(this, TwitterLoginActivity.class));
        } else {
            // user logged-in => show tweet
            startActivity(new Intent(this, ShowTweetActivity.class));
        }
        finish();
    }
}


/*
package com.example.trendingtopic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {


    TwitterLoginButton loginButton;


    String url ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.activity_main);

        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if (session == null) {
            // no session => login
            startActivity(new Intent(this, TwitterLoginActivity.class));
        } else {
            // user logged-in => show tweet
            startActivity(new Intent(this, ShowTweetActivity.class));
        }
        finish();





        loginButton = (TwitterLoginButton) findViewById(R.id.login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                TwitterAuthToken authToken = session.getAuthToken();
                String token = authToken.token;
                String secret = authToken.secret;

                //login(session);
                url = "http://where.yahooapis.com/v1/places.q('48.856613,2.352222')?appid="+session.getUserId()+"&format=json";
                getTimelineForSearchTerm(url,MainActivity.this);

            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
                Toast.makeText(MainActivity.this, "Somthing gose wrong", Toast.LENGTH_SHORT);
            }
        });




    }

    public void login(TwitterSession session) {
        String username = session.getUserName();
        Intent intent = new Intent(MainActivity.this, Main2Activity.class);
        intent.putExtra("username", username);
        startActivity(intent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the login button.
        loginButton.onActivityResult(requestCode, resultCode, data);
    }


    public static final String TAG = "TwitterUtils";

    public static String appAuthentication() {

        HttpURLConnection httpConnection = null;
        OutputStream outputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder response = null;

        try {
            URL url = new URL(ConstantsUtils.URL_AUTHENTICATION);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);

            String accessCredential = ConstantsUtils.CONSUMER_KEY + ":"
                    + ConstantsUtils.CONSUMER_SECRET;
            String authorization = "Basic "
                    + Base64.encodeToString(accessCredential.getBytes(),
                    Base64.NO_WRAP);
            String param = "grant_type=client_credentials";

            httpConnection.addRequestProperty("Authorization", authorization);
            httpConnection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            httpConnection.connect();

            outputStream = httpConnection.getOutputStream();
            outputStream.write(param.getBytes());
            outputStream.flush();
            outputStream.close();
            // int statusCode = httpConnection.getResponseCode();
            // String reason =httpConnection.getResponseMessage();

            bufferedReader = new BufferedReader(new InputStreamReader(
                    httpConnection.getInputStream()));
            String line;
            response = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }

            Log.d(TAG,
                    "POST response code: "
                            + String.valueOf(httpConnection.getResponseCode()));
            Log.d(TAG, "JSON response: " + response.toString());

        } catch (Exception e) {
            Log.e(TAG, "POST error: " + Log.getStackTraceString(e));

        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
        return response.toString();
    }


    public static String getTimelineForSearchTerm(String twitt_url, Context context) {
        HttpURLConnection httpConnection = null;
        BufferedReader bufferedReader = null;
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(ConstantsUtils.URL_INDIA_TRENDING);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");

            String jsonString = appAuthentication();
            JSONObject jsonObjectDocument = new JSONObject(jsonString);
            String token = jsonObjectDocument.getString("token_type") + " "
                    + jsonObjectDocument.getString("access_token");
            httpConnection.setRequestProperty("Authorization", token);

            httpConnection.setRequestProperty("Authorization", token);
            httpConnection.setRequestProperty("Content-Type",
                    "application/json");
            httpConnection.connect();

            bufferedReader = new BufferedReader(new InputStreamReader(
                    httpConnection.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }

            Log.d(TAG,
                    "GET response code: "
                            + String.valueOf(httpConnection
                            .getResponseCode()));
            Log.d(TAG, "JSON response: " + response.toString());

        } catch (Exception e) {
            Log.e(TAG, "GET error: " + Log.getStackTraceString(e));

        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();

            }
        }
        System.out.println(response.toString());
        return response.toString();
    }


}

* */
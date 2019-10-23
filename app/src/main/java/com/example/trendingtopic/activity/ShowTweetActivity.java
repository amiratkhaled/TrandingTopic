package com.example.trendingtopic.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.trendingtopic.App;
import com.example.trendingtopic.Logger;
import com.example.trendingtopic.R;
import com.example.trendingtopic.service.NotificationService;
import com.example.trendingtopic.service.PopularTrendingTweetService;
import com.example.trendingtopic.storage.StorageHelper;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ShowTweetActivity extends AppCompatActivity {

    private ListView mTweetsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tweet);


        requestLocationPermission();
        setUpTweetServiceAlarm();
        setUpNotificationAlarm();
        setUpViews();
        setUpTweetList();
    }

    private void setUpTweetServiceAlarm() {
        // TODO : Only create the alarm if it does not already exist
        Intent getTweetIntent = new Intent(this, PopularTrendingTweetService.class);
        PendingIntent alarmIntent = PendingIntent.getService(this, App.Constants.COLLECT_TWEET_CODE, getTweetIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal = Calendar.getInstance();
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, alarmIntent);
    }

    private void setUpNotificationAlarm() {
        // TODO : Only create the alarm if it does not already exist
        Intent notificationIntent = new Intent(this, NotificationService.class);
        PendingIntent alarmIntent = PendingIntent.getService(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 20);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    private void setUpViews() {

        mTweetsList = (ListView) findViewById(R.id.tweets_list_view);

    }

    private void setUpTweetList() {

        try {

            Date today = Calendar.getInstance().getTime();
            List<Long> tweets = new StorageHelper(ShowTweetActivity.this).getTweetsOfDay(today);

            if (tweets.isEmpty()) {
                Toast.makeText(ShowTweetActivity.this, "No tweets to display at this time", Toast.LENGTH_LONG).show();
                return;
            }
            for(int i = 0 ; i < tweets.size();i++){
                System.out.println(tweets.get(i));
            }

            /*TweetViewFetchAdapter<CompactTweetView> adapter =
                    new TweetViewFetchAdapter<CompactTweetView>(ShowTweetActivity.this);

            adapter.setTweetIds(tweets, new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> result) {
                }

                @Override
                public void failure(TwitterException e) {
                    Toast.makeText(ShowTweetActivity.this, "Could not load tweets", Toast.LENGTH_LONG).show();
                }
            });
            mTweetsList.setAdapter(adapter);*/

        } catch (JSONException | IOException e) {
            Logger.e("TWEET", e.getMessage(), e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_show_tweet, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            //Twitter.logOut();
            Intent i = new Intent(this, TwitterLoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestLocationPermission() {

        int permissionCheck = ContextCompat.checkSelfPermission(ShowTweetActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if ((permissionCheck == PackageManager.PERMISSION_GRANTED)) {

            Toast.makeText(this, "Location activated", Toast.LENGTH_SHORT).show();

        } else {

            ActivityCompat.requestPermissions(ShowTweetActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, App.Constants.LOCATION_PERMISSION_CODE);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case App.Constants.LOCATION_PERMISSION_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location activated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Cannot load tweets, need the location of the device.", Toast.LENGTH_LONG).show();
                    finish();
                }

                break;

            default:
                break;
        }
    }
}

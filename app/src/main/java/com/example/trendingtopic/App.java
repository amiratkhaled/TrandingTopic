package com.example.trendingtopic;


import android.app.Application;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Twitter.initialize(this);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(Constants.TWITTER_KEY, Constants.TWITTER_SECRET);
        Fabric.with(this,  new Crashlytics());
    }

    public static class Constants {
        // Twitter
        private static final String TWITTER_KEY = "RiDn30K8LdWJJZBSZ8SIRRXce";
        private static final String TWITTER_SECRET = "fhw35dfuHqGujOSdqE2o7OiXIhjLeYlscZrycYfnJ1vfrrKfyq";
        // Collect Tweet Service
        public static final int COLLECT_TWEET_CODE = 10;
        // Storage Service
        public static final String STORAGE_FILE = "digest.json";
        // Location Service
        public static final int LOCATION_PERMISSION_CODE = 20;
    }
}
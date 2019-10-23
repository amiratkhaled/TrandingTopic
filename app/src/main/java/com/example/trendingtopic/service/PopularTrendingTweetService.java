package com.example.trendingtopic.service;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.trendingtopic.BuildConfig;
import com.example.trendingtopic.ExtendedTwitterApiClient;
import com.example.trendingtopic.Logger;
import com.example.trendingtopic.MainActivity;
import com.example.trendingtopic.model.Trend;
import com.example.trendingtopic.model.TrendsResult;
import com.example.trendingtopic.storage.StorageHelper;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * helper methods.
 */
public class PopularTrendingTweetService extends IntentService {

    private final String LOG_TAG = "TWEET_SERVICE";

    public PopularTrendingTweetService() {
        super("PopularTrendingTweetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            /*
                STEP 1 : Get the user's location coordinates
            */
            Location currentLocation = getLocation();
            /*
                STEP 2 : Get the Where On Earth ID of the current location
            */
            long woeid;
            if (BuildConfig.DEBUG) {
                Logger.d(LOG_TAG, "*** Faking San Francisco location");
                //woeid = getLocationWOEID(37.781157, -122.400612831116);
                woeid = getLocationWOEID(currentLocation.getLatitude(), currentLocation.getLongitude());
            } else
                woeid = getLocationWOEID(currentLocation.getLatitude(), currentLocation.getLongitude());
            /*
                STEP 3 : Get the most popular trend for this location
             */
            Trend trend = getPopularTrend(woeid);
            /*
                STEP 4 : Get the most popular tweet for this trend
             */
            long tweetId = getTweetId(trend.query);
            /*
                STEP 5 : Save the tweet id in storage for today
             */
            saveTweetId(tweetId);
            /*
                DONE
             */
        } catch (TweetServiceException e) {
            endWithFailure(e);
        }
    }

    /**
     * STEP 1 : Get the user's location coordinates
     *
     * @return
     * @throws TweetServiceException
     */

    public LocationListener locationListener;
    public LocationManager locationManager;
    public Location location;


    private void grentLocationPermission(){
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                System.err.println("\n " + location.getLatitude() + "\n " + location.getLongitude());

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        };
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.


            return;
        }

        String latitude,longitude;
        if (locationManager != null) {
            locationManager.requestLocationUpdates("gps", 0, 0, locationListener);
            location = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                System.err.println("location find"+location.getLongitude()+"   "+location.getLatitude());

            } else {
                Toast.makeText(
                        this,
                        "Location Null", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Location getLocation() throws TweetServiceException {



        //locationManager.requestLocationUpdates("gps", 1000, 0, locationListener);



        grentLocationPermission();









        Location targetLocation = new Location("");//provider name is unnecessary

        Logger.d(LOG_TAG, "*** STARTED Get Location...");

        //final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        try {

            Location loc = location;
            System.err.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh no location");
            Logger.d(LOG_TAG, "*** DONE Get Location: " + loc.toString());

            return loc;
        } catch (SecurityException e) {
            System.err.println("no location");
            throw new TweetServiceException("Could not get the current location coordinates", e);
        }
    }

    /**
     * Get the Where On Earth ID of the current location
     *
     * @param latitude
     * @param longitude
     * @return
     * @throws TweetServiceException
     */
    private long getLocationWOEID(double latitude, double longitude) throws TweetServiceException {

        Logger.d(LOG_TAG, "*** STARTED Get WOEID...");

        ExtendedTwitterApiClient trendsClient = new ExtendedTwitterApiClient(MainActivity.s.getActiveSession());
        System.err.println("i am here " + latitude);
        System.err.println("name is"+trendsClient.getTrendsService().closest((long) latitude, (long) longitude).size());
        List<com.example.trendingtopic.model.Location> locations = trendsClient.getTrendsService().closest((long) latitude, (long) longitude);
        if (locations != null && !locations.isEmpty()) {
            com.example.trendingtopic.model.Location loc = locations.get(0);
            Logger.d(LOG_TAG, "*** DONE WOEID: " + loc.woeid + " ("+loc.name+", "+loc.country+")");
            return loc.woeid;
        } else {
            throw new TweetServiceException("Could not get the WOEID from Twitter's Trends service");
        }
    }

    /**
     * Get the most popular trend for this location
     *
     * @param woeid
     * @return
     * @throws TweetServiceException
     */
    private Trend getPopularTrend(long woeid) throws TweetServiceException {

        Logger.d(LOG_TAG, "*** STARTED Get Trends...");

        ExtendedTwitterApiClient trendsClient = new ExtendedTwitterApiClient(MainActivity.s.getActiveSession());
        List<TrendsResult> results = trendsClient.getTrendsService().place(woeid);
        if (results != null && !results.isEmpty()) {
            List<Trend> trends = new ArrayList<>(results.get(0).trends);
            if (!trends.isEmpty()) {
                // Order results by tweet volume desc
                Collections.sort(trends, Collections.reverseOrder());
                Trend popularTrend = trends.get(0);

                Logger.d(LOG_TAG, "*** DONE Get most popular trend: " + popularTrend.name);

                return popularTrend;
            }
        }
        throw new TweetServiceException("Could not get the trends from Twitter's Trends service");
    }

    /**
     * STEP 4 : Get the most popular tweet for this trend
     *
     * @param query
     * @return
     * @throws TweetServiceException
     */
    private long getTweetId(String query) throws TweetServiceException {

        Logger.d(LOG_TAG, "*** STARTED Get Tweet...");

        ExtendedTwitterApiClient trendsClient = new ExtendedTwitterApiClient(MainActivity.s.getActiveSession());
        ExtendedTwitterApiClient.SearchService searchService = trendsClient.getSyncSearchService();
        Search search = searchService.tweets(query, null, null, null, "popular", 1, null, null, null, null);
        if (search != null && search.tweets != null && !search.tweets.isEmpty()) {
            Tweet t = search.tweets.get(0);

            Logger.d(LOG_TAG, "*** DONE Get popular trending tweet: " + t.text);

            return t.getId();
        }
        throw new TweetServiceException("Could not get the tweets from Twitter's Search service");

    }


    /**
     * STEP 5 : Save the tweet id in storage for today
     *
     * @param tweetId
     * @throws TweetServiceException
     */
    private boolean saveTweetId(long tweetId) throws TweetServiceException {
        Logger.d(LOG_TAG, "*** STARTED Save to storage...");

        StorageHelper helper = new StorageHelper(getApplicationContext());
        try {
            Date today = Calendar.getInstance().getTime();
            boolean saved = helper.saveTweetIdForDay(today, tweetId);
            Logger.d(LOG_TAG, "*** DONE Save to storage:");
            return saved;
        } catch (IOException | JSONException e) {
            throw new TweetServiceException("Could not save the tweet id to storage", e);
        }
    }

    /*
        MISC
     */

    private void endWithFailure(Exception e) {
        Logger.e(LOG_TAG, e.getMessage(), e);
    }


    private class TweetServiceException extends Exception {
        public TweetServiceException(String message) {
            super(message);
        }

        public TweetServiceException(String message, Exception e) {
            super(message, e);
        }
    }

}

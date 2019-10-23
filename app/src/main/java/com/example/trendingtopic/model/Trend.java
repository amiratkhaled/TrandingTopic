package com.example.trendingtopic.model;

/**
 * Created by paristote on 1/12/16.
 */
public class Trend implements Comparable<Trend> {

    public long tweet_volume;
    //    "events": null,
    public String name;
    //    "promoted_content": null,
    public String query;

    public String url;

    @Override
    public int compareTo(Trend another) {
        if (tweet_volume > another.tweet_volume)
            return 1;

        if (tweet_volume < another.tweet_volume)
            return -1;

        return 0;
    }
}

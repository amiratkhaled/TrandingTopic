package com.example.trendingtopic.storage;

import android.content.Context;

import com.example.trendingtopic.App;
import com.example.trendingtopic.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StorageHelper {

    private final String LOG_TAG = "STORAGE_HELPER";

    private final String ROOT_OBJECT = "storage";

    private final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MMM-dd", Locale.US);

    private Context mContext;

    public StorageHelper(Context ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        mContext = ctx.getApplicationContext();
        initStorageFile();

    }


    private void initStorageFile() {
        File storage = getStorageFile();
        if (!storage.exists()) {
            try {
                /*
                Init storage with content:
                    {
                        "storage" : { }
                    }
                */
                JSONObject emptyContent = new JSONObject();
                emptyContent.put(ROOT_OBJECT, new JSONObject());
                writeContentToStorage(emptyContent);
            } catch (JSONException | IOException e) {
                Logger.d(LOG_TAG, e.getMessage());
            }
        }
    }

    public File getStorageFile() {
        File filesDir = mContext.getFilesDir();
        File digestFile = new File(filesDir, App.Constants.STORAGE_FILE);
        return digestFile;
    }

    public JSONObject getStorageContent() throws IOException, JSONException {
        BufferedReader br = new BufferedReader(new FileReader(getStorageFile()));
        StringBuffer buf = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            buf.append(line);
            buf.append('\n');
        }
        br.close();
        JSONObject json = new JSONObject(buf.toString());
        return json;
    }

    public boolean writeContentToStorage(JSONObject json) throws IOException, JSONException {
        String content = json.toString();
        FileOutputStream outputStream;
        outputStream = mContext.openFileOutput(App.Constants.STORAGE_FILE, Context.MODE_PRIVATE);
        outputStream.write(content.getBytes());
        outputStream.close();
        return true;
    }

    public boolean saveTweetIdForDay(Date date, long tweetId) throws IOException, JSONException {
        String today = DATE_FORMATTER.format(date);
        JSONObject db = getStorageContent();
        JSONObject storage = db.getJSONObject(ROOT_OBJECT);
        JSONArray todaysTweets = null;
        if (storage.has(today))
            todaysTweets = storage.getJSONArray(today);
        else
            todaysTweets = new JSONArray();
        todaysTweets.put(tweetId);
        storage.put(today, todaysTweets);
        db.put(ROOT_OBJECT, storage);
        boolean saved = writeContentToStorage(db);
        Logger.d(LOG_TAG, "\nSaved content to db:\n" + db.toString(2));
        return saved;
    }

    public List<Long> getTweetsOfDay(Date date) throws IOException, JSONException {
        JSONObject storage = getStorageContent().getJSONObject(ROOT_OBJECT);
        String today = DATE_FORMATTER.format(date);
        List<Long> tweets = new ArrayList<>();
        if (storage.has(today)) {
            JSONArray tweetsJson = storage.getJSONArray(today);
            Set<Long> tweetsSet = new HashSet<>();
            for (int i = 0; i < tweetsJson.length(); i++) {
                // Add ID to a Set before adding to the List
                // to ensure we don't return duplicates
                if (tweetsSet.add(tweetsJson.getLong(i)))
                    tweets.add(tweetsJson.getLong(i));
            }
        }
        return tweets;
    }

}

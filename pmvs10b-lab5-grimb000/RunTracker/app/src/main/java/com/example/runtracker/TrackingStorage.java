package com.example.runtracker;

import android.content.Context;
import android.content.SharedPreferences;

public final class TrackingStorage {

    private static final String PREFS = "run_tracker_prefs";
    private static final String KEY_STATUS = "status";
    private static final String KEY_PROVIDER = "provider";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_UPDATES = "updates";

    private TrackingStorage() {
    }

    public static void reset(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        preferences.edit()
            .putString(KEY_STATUS, context.getString(R.string.status_tracking))
            .putString(KEY_PROVIDER, context.getString(R.string.waiting_for_fix))
            .putString(KEY_LATITUDE, "-")
            .putString(KEY_LONGITUDE, "-")
            .putString(KEY_UPDATES, "0")
            .apply();
    }

    public static void saveUpdate(
        Context context,
        String provider,
        String latitude,
        String longitude
    ) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int currentUpdates = Integer.parseInt(preferences.getString(KEY_UPDATES, "0"));
        preferences.edit()
            .putString(KEY_STATUS, context.getString(R.string.status_tracking))
            .putString(KEY_PROVIDER, provider)
            .putString(KEY_LATITUDE, latitude)
            .putString(KEY_LONGITUDE, longitude)
            .putString(KEY_UPDATES, String.valueOf(currentUpdates + 1))
            .apply();
    }

    public static State read(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return new State(
            preferences.getString(KEY_STATUS, context.getString(R.string.status_ready)),
            preferences.getString(KEY_PROVIDER, context.getString(R.string.waiting_for_fix)),
            preferences.getString(KEY_LATITUDE, "-"),
            preferences.getString(KEY_LONGITUDE, "-"),
            preferences.getString(KEY_UPDATES, "0")
        );
    }

    public static final class State {
        public final String status;
        public final String provider;
        public final String latitude;
        public final String longitude;
        public final String updates;

        State(
            String status,
            String provider,
            String latitude,
            String longitude,
            String updates
        ) {
            this.status = status;
            this.provider = provider;
            this.latitude = latitude;
            this.longitude = longitude;
            this.updates = updates;
        }
    }
}

package com.example.runtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

public class TrackingUpdateReceiver extends BroadcastReceiver {

    public static final String ACTION_LOCATION_UPDATE =
        "com.example.runtracker.ACTION_LOCATION_UPDATE";
    public static final String ACTION_UI_UPDATE =
        "com.example.runtracker.ACTION_UI_UPDATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_LOCATION_UPDATE.equals(intent.getAction())) {
            return;
        }

        Location location = intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
        if (location != null) {
            TrackingStorage.saveUpdate(
                context,
                location.getProvider(),
                String.format("%.6f", location.getLatitude()),
                String.format("%.6f", location.getLongitude())
            );
        }

        Intent uiIntent = new Intent(ACTION_UI_UPDATE);
        uiIntent.setPackage(context.getPackageName());
        context.sendBroadcast(uiIntent);
    }
}

package com.example.runtracker;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 101;
    private static final int MODE_LISTENER = 1;
    private static final int MODE_PENDING = 2;

    private LocationManager locationManager;
    private PendingIntent pendingIntent;
    private int requestedMode = MODE_LISTENER;

    private TextView listenerStatusValue;
    private TextView listenerProviderValue;
    private TextView listenerLatitudeValue;
    private TextView listenerLongitudeValue;
    private TextView listenerUpdatesValue;
    private int listenerUpdatesCount = 0;

    private TextView pendingStatusValue;
    private TextView pendingProviderValue;
    private TextView pendingLatitudeValue;
    private TextView pendingLongitudeValue;
    private TextView pendingUpdatesValue;

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            listenerUpdatesCount++;
            listenerStatusValue.setText(getString(R.string.status_tracking));
            listenerProviderValue.setText(location.getProvider());
            listenerLatitudeValue.setText(String.format("%.6f", location.getLatitude()));
            listenerLongitudeValue.setText(String.format("%.6f", location.getLongitude()));
            listenerUpdatesValue.setText(String.valueOf(listenerUpdatesCount));
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            listenerStatusValue.setText(getString(R.string.provider_enabled, provider));
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            listenerStatusValue.setText(getString(R.string.provider_disabled, provider));
        }
    };

    private final BroadcastReceiver uiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            renderPendingState();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listenerStatusValue = findViewById(R.id.listenerStatusValue);
        listenerProviderValue = findViewById(R.id.listenerProviderValue);
        listenerLatitudeValue = findViewById(R.id.listenerLatitudeValue);
        listenerLongitudeValue = findViewById(R.id.listenerLongitudeValue);
        listenerUpdatesValue = findViewById(R.id.listenerUpdatesValue);

        pendingStatusValue = findViewById(R.id.pendingStatusValue);
        pendingProviderValue = findViewById(R.id.pendingProviderValue);
        pendingLatitudeValue = findViewById(R.id.pendingLatitudeValue);
        pendingLongitudeValue = findViewById(R.id.pendingLongitudeValue);
        pendingUpdatesValue = findViewById(R.id.pendingUpdatesValue);

        Button listenerStartButton = findViewById(R.id.listenerStartButton);
        Button listenerStopButton = findViewById(R.id.listenerStopButton);
        Button pendingStartButton = findViewById(R.id.pendingStartButton);
        Button pendingStopButton = findViewById(R.id.pendingStopButton);

        Intent pendingIntentBase = new Intent(this, TrackingUpdateReceiver.class);
        pendingIntentBase.setAction(TrackingUpdateReceiver.ACTION_LOCATION_UPDATE);
        pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            pendingIntentBase,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        listenerStartButton.setOnClickListener(v -> {
            requestedMode = MODE_LISTENER;
            startListenerTracking();
        });
        listenerStopButton.setOnClickListener(v -> stopListenerTracking());
        pendingStartButton.setOnClickListener(v -> {
            requestedMode = MODE_PENDING;
            startPendingTracking();
        });
        pendingStopButton.setOnClickListener(v -> stopPendingTracking());

        updateProviderSummary();
        renderPendingState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ContextCompat.registerReceiver(
            this,
            uiReceiver,
            new IntentFilter(TrackingUpdateReceiver.ACTION_UI_UPDATE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        );
        renderPendingState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(uiReceiver);
    }

    private void startListenerTracking() {
        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }

        listenerUpdatesCount = 0;
        listenerUpdatesValue.setText(String.valueOf(listenerUpdatesCount));
        listenerStatusValue.setText(getString(R.string.status_tracking));

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                locationListener
            );
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000L,
                1f,
                locationListener
            );
        }

        updateProviderSummary();
    }

    private void stopListenerTracking() {
        if (hasLocationPermission()) {
            locationManager.removeUpdates(locationListener);
        }
        listenerStatusValue.setText(getString(R.string.status_stopped));
    }

    private void startPendingTracking() {
        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }

        TrackingStorage.reset(this);
        pendingStatusValue.setText(getString(R.string.status_tracking));

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                pendingIntent
            );
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000L,
                1f,
                pendingIntent
            );
        }

        updateProviderSummary();
        renderPendingState();
    }

    private void stopPendingTracking() {
        if (hasLocationPermission()) {
            locationManager.removeUpdates(pendingIntent);
        }
        pendingStatusValue.setText(getString(R.string.status_stopped));
    }

    private void updateProviderSummary() {
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        String providerSummary = getString(
            R.string.provider_summary,
            gpsEnabled ? "ON" : "OFF",
            networkEnabled ? "ON" : "OFF"
        );
        listenerProviderValue.setText(providerSummary);
        pendingProviderValue.setText(providerSummary);

        if (!hasLocationPermission()) {
            listenerStatusValue.setText(getString(R.string.permission_required));
            pendingStatusValue.setText(getString(R.string.permission_required));
        } else if (!gpsEnabled && !networkEnabled) {
            listenerStatusValue.setText(getString(R.string.enable_location));
            pendingStatusValue.setText(getString(R.string.enable_location));
        } else {
            if (listenerUpdatesCount == 0) {
                listenerStatusValue.setText(getString(R.string.status_ready));
            }
            if ("0".contentEquals(pendingUpdatesValue.getText())) {
                pendingStatusValue.setText(getString(R.string.status_ready));
            }
        }
    }

    private void renderPendingState() {
        TrackingStorage.State state = TrackingStorage.read(this);
        pendingStatusValue.setText(state.status);
        pendingProviderValue.setText(state.provider);
        pendingLatitudeValue.setText(state.latitude);
        pendingLongitudeValue.setText(state.longitude);
        pendingUpdatesValue.setText(state.updates);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            },
            LOCATION_PERMISSION_REQUEST
        );
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        @NonNull String[] permissions,
        @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestedMode == MODE_PENDING) {
                startPendingTracking();
            } else {
                startListenerTracking();
            }
        } else {
            listenerStatusValue.setText(getString(R.string.permission_required));
            pendingStatusValue.setText(getString(R.string.permission_required));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hasLocationPermission()) {
            locationManager.removeUpdates(locationListener);
            locationManager.removeUpdates(pendingIntent);
        }
    }
}

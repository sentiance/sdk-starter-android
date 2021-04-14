package com.sentiance.sdkstarter;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sentiance.sdk.OnInitCallback;
import com.sentiance.sdk.OnStartFinishedHandler;
import com.sentiance.sdk.SdkConfig;
import com.sentiance.sdk.SdkStatus;
import com.sentiance.sdk.Sentiance;
import com.sentiance.sdk.Token;
import com.sentiance.sdk.TokenResultCallback;
import com.sentiance.sdk.ondevicefull.crashdetection.VehicleCrashEvent;
import com.sentiance.sdk.ondevicefull.crashdetection.VehicleCrashListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyApplication extends Application implements OnInitCallback, OnStartFinishedHandler {

    private static final String TAG = "SDKStarter";
    private static final String SENTIANCE_APP_ID = "YOUR_APP_ID";
    private static final String SENTIANCE_SECRET = "YOUR_APP_SECRET";

    private SimpleDateFormat dateFormatter;

    @Override
    public void onCreate () {
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);

        super.onCreate();
        initializeSentianceSdk();
    }

    private void initializeSentianceSdk () {
        // Create the config.
        SdkConfig config = new SdkConfig.Builder(SENTIANCE_APP_ID, SENTIANCE_SECRET, createNotification())
                .setOnSdkStatusUpdateHandler(new SdkStatusUpdateHandler(getApplicationContext()))
                .build();

        // Initialize the Sentiance SDK.
        Sentiance.getInstance(this).init(config, this);
    }

    @Override
    public void onInitSuccess () {
        printInitSuccessLogStatements();

        listenForVehicleCrashes();

        // Sentiance SDK was successfully initialized, we can now start it.
        Sentiance.getInstance(this).start(this);
    }

    @Override
    public void onInitFailure (InitIssue initIssue, @Nullable Throwable throwable) {
        Log.e(TAG, "Could not initialize SDK: " + initIssue);

        switch (initIssue) {
            case INVALID_CREDENTIALS:
                Log.e(TAG, "Make sure SENTIANCE_APP_ID and SENTIANCE_SECRET are set correctly.");
                break;
            case CHANGED_CREDENTIALS:
                Log.e(TAG, "The app ID and secret have changed; this is not supported. If you meant to change the app credentials, please uninstall the app and try again.");
                break;
            case SERVICE_UNREACHABLE:
                Log.e(TAG, "The Sentiance API could not be reached. Double-check your internet connection and try again.");
                break;
            case LINK_FAILED:
                Log.e(TAG, "An issue was encountered trying to link the installation ID to the metauser.");
                break;
            case INITIALIZATION_ERROR:
                Log.e(TAG, "An unexpected exception or an error occurred during initialization.", throwable);
                break;
            case SDK_RESET_IN_PROGRESS:
                Log.e(TAG, "SDK reset operation is in progress. Wait until it's complete.", throwable);
                break;
        }
    }

    @Override
    public void onStartFinished (SdkStatus sdkStatus) {
        Log.i(TAG, "SDK start finished with status: " + sdkStatus.startStatus);
    }

    private Notification createNotification () {
        // PendingIntent that will start your application's MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // On Oreo and above, you must create a notification channel
        String channelId = "trips";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Trips", NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle(getString(R.string.app_name) + " is running")
                .setContentText("Touch to open.")
                .setContentIntent(pendingIntent)
                .setShowWhen(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
    }

    private void printInitSuccessLogStatements () {
        Log.i(TAG, "Sentiance SDK initialized, version: " + Sentiance.getInstance(this).getVersion());
        Log.i(TAG, "Sentiance platform user id for this install: " + Sentiance.getInstance(this).getUserId());
        Sentiance.getInstance(this).getUserAccessToken(new TokenResultCallback() {
            @Override
            public void onSuccess (Token token) {
                Log.i(TAG, "Access token to query the HTTP API: Bearer " + token.getTokenId());
                // Using this token, you can query the Sentiance API.
            }

            @Override
            public void onFailure () {
                Log.e(TAG, "Couldn't get access token");
            }
        });
    }

    private void listenForVehicleCrashes() {
        Sentiance.getInstance(this).setVehicleCrashListener(new VehicleCrashListener() {
            @Override
            public void onVehicleCrash(VehicleCrashEvent event) {
                Log.i(TAG, "A vehicle crash was detected on " + getFormattedDate(event.getTime()) + " at location " +
                    getFormattedLocation(event.getLocation()));
            }
        });
    }

    private String getFormattedDate(long epochTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(epochTime);
        return dateFormatter.format(calendar.getTime());
    }

    private String getFormattedLocation(@Nullable Location location) {
        if (location == null) {
            return "[unknown]";
        } else {
            return String.format(Locale.ENGLISH, "[%.4f, %.4f]", location.getLatitude(), location.getLongitude());
        }
    }
}

package com.sentiance.sdkstarter;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

public class MyApplication extends Application implements OnInitCallback, OnStartFinishedHandler {

    private static final String SENTIANCE_APP_ID = "5d7676a02d44e60600000011";
    private static final String SENTIANCE_SECRET = "1e3919f4775c236800b61621fab4d2e5ba789fbadc5a2496482f1adbab09e67b15eb89df8a34812d0169a88b1158a660d4c5bd58026cdb1860c39bf49c54394e";

    private static final String TAG = "SDKStarter";

    @Override
    public void onCreate () {
        super.onCreate();
        initializeSentianceSdk();
    }

    private void initializeSentianceSdk () {
        // Create the config.
        SdkConfig config = new SdkConfig.Builder(SENTIANCE_APP_ID, SENTIANCE_SECRET, createNotification())
                .setOnSdkStatusUpdateHandler(new SdkStatusUpdateHandler(getApplicationContext()))
                .baseURL("https://preprod-api.sentiance.com/")
                .build();

        // Initialize the Sentiance SDK.
        Sentiance.getInstance(this).init(config, this);
    }

    @Override
    public void onInitSuccess () {
        printInitSuccessLogStatements();

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
}

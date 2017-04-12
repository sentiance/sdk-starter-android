package com.sentiance.sdkstarter;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.sentiance.sdk.OnInitCallback;
import com.sentiance.sdk.OnSdkStatusUpdateHandler;
import com.sentiance.sdk.OnStartFinishedHandler;
import com.sentiance.sdk.SdkConfig;
import com.sentiance.sdk.SdkStatus;
import com.sentiance.sdk.Sentiance;
import com.sentiance.sdk.Token;
import com.sentiance.sdk.TokenResultCallback;

public class MyApplication extends Application implements OnInitCallback, OnSdkStatusUpdateHandler, OnStartFinishedHandler {

    public static final String ACTION_SENTIANCE_STATUS_UPDATE = "ACTION_SENTIANCE_STATUS_UPDATE";

    private static final String SENTIANCE_APP_ID = "YOUR_APP_ID";
    private static final String SENTIANCE_SECRET = "YOUR_SECRET";

    private static final String TAG = "SDKStarter";

    @Override
    public void onCreate() {
        super.onCreate();
        initializeSentianceSdk();
    }

    private void initializeSentianceSdk() {
        // Create a notification that will be used by the Sentiance SDK to start the service foregrounded.
        // This discourages Android from killing the process.
        Intent intent = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name) + " is running")
                .setContentText("Touch to open.")
                .setShowWhen(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();

        // Create the config.
        SdkConfig config = new SdkConfig.Builder(SENTIANCE_APP_ID, SENTIANCE_SECRET)
                .enableForeground(notification)
                .setOnSdkStatusUpdateHandler(this)
                .build();

        // Initialize the Sentiance SDK.
        Sentiance.getInstance(this).init(config, this);
    }

    @Override
    public void onInitSuccess() {
        printInitSuccessLogStatements();

        // Sentiance SDK was successfully initialized, we can now start it.
        Sentiance.getInstance(this).start(this);
    }

    @Override
    public void onInitFailure(InitIssue initIssue) {
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
        }
    }

    @Override
    public void onStartFinished(SdkStatus sdkStatus) {
        Log.i(TAG, "SDK start finished with status: " + sdkStatus.startStatus);
    }

    @Override
    public void onSdkStatusUpdate(SdkStatus sdkStatus) {
        Log.i(TAG, "SDK status updated: " + sdkStatus.toString());

        // The status update is broadcast internally; this is so the other components of the app
        // (specifically MainActivity) can react on this.
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_SENTIANCE_STATUS_UPDATE));
    }

    private void printInitSuccessLogStatements() {
        Log.i(TAG, "Sentiance SDK initialized, version: " + Sentiance.getInstance(this).getVersion());
        Log.i(TAG, "Sentiance platform user id for this install: " + Sentiance.getInstance(this).getUserId());
        Sentiance.getInstance(this).getUserAccessToken(new TokenResultCallback() {
            @Override
            public void onSuccess(Token token) {
                Log.i(TAG, "Access token to query the HTTP API: Bearer " + token.getTokenId());
                // Using this token, you can query the Sentiance API.
            }

            @Override
            public void onFailure() {
                Log.e(TAG, "Couldn't get access token");
            }
        });
    }
}

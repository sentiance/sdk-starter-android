package com.sentiance.sdkstarter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;


import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sentiance.sdk.OnSdkStatusUpdateHandler;
import com.sentiance.sdk.SdkStatus;

public class SdkStatusUpdateHandler implements OnSdkStatusUpdateHandler {
    static final String ACTION_SENTIANCE_STATUS_UPDATE = "ACTION_SENTIANCE_STATUS_UPDATE";

    private static final String TAG = "SdkStatusUpdate";
    private final Context mContext;

    SdkStatusUpdateHandler (Context context) {
        mContext = context;
    }

    @Override
    public void onSdkStatusUpdate (SdkStatus status) {
        Log.i(TAG, "SDK status updated: " + status.toString());

        // The status update is broadcast internally; this is so the other components of the app
        // (specifically MainActivity) can react on this.
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_SENTIANCE_STATUS_UPDATE));
        checkStatus(status);
    }

    private void checkStatus(SdkStatus status) {
        if (status.startStatus == SdkStatus.StartStatus.PENDING) {
            // Something is deterring the SDK detections.
            checkDetectionIssues(status);
        }

        if (status.locationSetting != SdkStatus.LocationSetting.OK) {
            Log.i(TAG, "The device location mode is not set to high accuracy.");

            // This impacts the accuracy of locations the SDK receives.

            // Ask the user to set the location mode to high accuracy (enabling
            // both GPS and network location providers).
            // See: https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        }

        if (status.isBatterySavingEnabled) {
            Log.i(TAG, "Battery saving is enabled");

            // Depending on the device, this may limit background and location tracking.

            // If possible, ask the user to disable battery saving.
        }

        if (status.isBatteryOptimizationEnabled) {
            Log.i(TAG, "OS battery optimization is enabled");

            // This may cause detection issues on some devices.
            // See: https://docs.sentiance.com/sdk/appendix/android/android-battery-optimization
        }

        if (!status.isAccelPresent) {
            Log.i(TAG, "The device reports a lack of accelerometer.");
        }

        if (!status.isGpsPresent) {
            Log.i(TAG, "The device reports a lack of gyroscope.");
        }

        // Network and disk quota status
        checkQuotas(status);
    }

    private void checkDetectionIssues (SdkStatus status) {
        if (!status.isLocationPermGranted) {
            Log.i(TAG, "Location permission has not been granted.");

            // Ask the user to grant the location permission.
        }

        if (status.locationSetting == SdkStatus.LocationSetting.DISABLED) {
            Log.i(TAG, "Device location tracking has been disable.");

            // Ask the user to set the location mode to high accuracy (enabling
            // both GPS and network location providers).
            // See: https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        }

        if (!status.isActivityRecognitionPermGranted) {
            Log.i(TAG, "Activity recognition permission has not been granted.");

            // Ask the user to grant the activity recognition permission.
        }

        if (!status.isLocationAvailable) {
            Log.i(TAG, "Location is not available.");

            // The device's location has been unavailable for some time. The
            // SDK will automatically recover once locations become available.
        }

        if (status.isAirplaneModeEnabled) {
            Log.i(TAG, "Airplane mode is enabled.");

            // Ask the user to disable airplane mode if possible.
        }

        if (status.isBackgroundProcessingRestricted) {
            Log.i(TAG, "Background processing is restricted");

            // On Android 9 and above, this restriction prevents an app from running
            // in the background, disabling SDK detections.

            // Ask the user to remove this restriction.
            // See: https://docs.sentiance.com/sdk/api-reference/android/sdkstatus
        }
        
        if (status.diskQuotaStatus == SdkStatus.QuotaStatus.EXCEEDED) {
            Log.i(TAG, "Disk quota exceeded");

            // The disk quota has been completely consumed.
            // Detections will stop until the pending SDK data is submitted
            // to clear up some quota.
            //
            // You may call Sentiance.submitDetections() to force the submission
            // and clear up some disk space. Note that calling this method will
            // bypass SDK mobile data and wifi quota limits.
        }


        // The following issues are only logged as they cannot be resolved by
        // the app or the user.

        if (!status.isRemoteEnabled) {
            Log.i(TAG, "The user is disabled by the platform.");
        }

        if (!status.isGpsPresent) {
            Log.i(TAG, "The device does not have a GPS.");
        }

        if (status.isGooglePlayServicesMissing) {
            Log.i(TAG, "Google Play Services is missing");

            // The device likely does not have Google Play Services. A play
            // services free version of the Sentiance SDK may be used instead.
            //
            // This may also be caused by a Google Play Services update,
            // in which case the issue will be automatically resolved.
        }
    }

    private void checkQuotas(SdkStatus status) {
        if (status.wifiQuotaStatus == SdkStatus.QuotaStatus.WARNING) {
            Log.i(TAG, "Wifi quota warning");

            // The Wifi quota is almost consumed.
        }
        else if (status.wifiQuotaStatus == SdkStatus.QuotaStatus.EXCEEDED) {
            Log.i(TAG, "Wifi quota exceeded");

            // The Wifi quota has been completely consumed.
            // Data submission over wifi will stop until the quota clears up
            // again the next day.
        }

        if (status.mobileQuotaStatus == SdkStatus.QuotaStatus.WARNING) {
            Log.i(TAG, "Mobile data quota warning");

            // The mobile data quota is almost consumed.
        }
        else if (status.mobileQuotaStatus == SdkStatus.QuotaStatus.EXCEEDED) {
            Log.i(TAG, "Mobile data quota exceeded");

            // The mobile data quota has been completely consumed.
            // Data submission over mobile data will stop until the quota
            // clears up again the next day.
        }

        if (status.diskQuotaStatus == SdkStatus.QuotaStatus.WARNING) {
            Log.i(TAG, "Disk quota warning");

            // The disk quota is almost consumed.
            //
            // You may call Sentiance.submitDetections() to force the submission
            // and clear up some disk space. Note that calling this method will
            // bypass SDK mobile data and wifi quota limits.
        }
    }
}
package com.sentiance.sdkstarter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sentiance.sdk.InitState;
import com.sentiance.sdk.SdkStatus;
import com.sentiance.sdk.Sentiance;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final BroadcastReceiver statusUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            refreshStatus();
        }
    };

    private ListView statusList;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (new PermissionManager(this).getNotGrantedPermissions().size() > 0) {
            startActivity(new Intent(this, PermissionCheckActivity.class));
        }

        setContentView(R.layout.activity_main);

        statusList = findViewById(R.id.statusList);
    }

    @Override
    protected void onResume () {
        super.onResume();

        // Register a receiver so we are notified by MyApplication when the Sentiance SDK status was updated.
        LocalBroadcastManager.getInstance(this).registerReceiver(statusUpdateReceiver, new IntentFilter(SdkStatusUpdateHandler.ACTION_SENTIANCE_STATUS_UPDATE));

        refreshStatus();
    }

    @Override
    protected void onPause () {
        super.onPause();

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(statusUpdateReceiver);
    }

    private void refreshStatus () {
        List<String> statusItems = new ArrayList<>();

        if (Sentiance.getInstance(this).getInitState() == InitState.INITIALIZED) {
            statusItems.add("SDK version: " + Sentiance.getInstance(this).getVersion());
            statusItems.add("User ID: " + Sentiance.getInstance(this).getUserId());

            SdkStatus sdkStatus = Sentiance.getInstance(this).getSdkStatus();

            statusItems.add("Start status: " + sdkStatus.startStatus.name());
            statusItems.add("Can detect: " + sdkStatus.canDetect);
            statusItems.add("Remote enabled: " + sdkStatus.isRemoteEnabled);
            statusItems.add("Activity perm granted: " + sdkStatus.isActivityRecognitionPermGranted);
            statusItems.add("Location perm granted: " + sdkStatus.isLocationPermGranted);
            statusItems.add("Location setting: " + sdkStatus.locationSetting.name());

            statusItems.add(formatQuota("Wi-Fi", sdkStatus.wifiQuotaStatus, Sentiance.getInstance(this).getWiFiQuotaUsage(), Sentiance.getInstance(this).getWiFiQuotaLimit()));
            statusItems.add(formatQuota("Mobile data", sdkStatus.mobileQuotaStatus, Sentiance.getInstance(this).getMobileQuotaUsage(), Sentiance.getInstance(this).getMobileQuotaLimit()));
            statusItems.add(formatQuota("Disk", sdkStatus.diskQuotaStatus, Sentiance.getInstance(this).getDiskQuotaUsage(), Sentiance.getInstance(this).getDiskQuotaLimit()));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                statusItems.add("Battery optimization enabled: " + sdkStatus.isBatteryOptimizationEnabled);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                statusItems.add("Battery saving enabled: " + sdkStatus.isBatterySavingEnabled);
            }
            if (Build.VERSION.SDK_INT >= 28) {
                statusItems.add("Background processing restricted: " + sdkStatus.isBackgroundProcessingRestricted);
            }
        }
        else {
            statusItems.add("SDK not initialized");
        }

        statusList.setAdapter(new ArrayAdapter<>(this, R.layout.list_item_status, R.id.textView, statusItems));
    }

    private String formatQuota (String name, SdkStatus.QuotaStatus status, long bytesUsed, long bytesLimit) {
        return String.format(Locale.US, "%s quota: %s / %s (%s)",
                name,
                Formatter.formatShortFileSize(this, bytesUsed),
                Formatter.formatShortFileSize(this, bytesLimit),
                status.name());
    }

}

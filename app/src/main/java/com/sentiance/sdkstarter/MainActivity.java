package com.sentiance.sdkstarter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sentiance.sdk.InitState;
import com.sentiance.sdk.ResetCallback;
import com.sentiance.sdk.SdkStatus;
import com.sentiance.sdk.Sentiance;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "SDKStarter";

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

        if (!new PermissionManager(this).getNotGrantedPermissions().isEmpty()) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_reset) {
            showSdkResetConfirmationDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
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

    private void showSdkResetConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("SDK Reset")
            .setMessage("Are you sure you want to reset the SDK?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick (DialogInterface dialog, int which) {
                    doSdkReset();
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick (DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .show();
    }

    private void doSdkReset() {
        Sentiance.getInstance(this).reset(new ResetCallback() {
            @Override
            public void onResetSuccess() {
                Log.i(TAG, "Sentiance SDK was successfully reset");
                refreshStatus();
            }

            @Override
            public void onResetFailure(ResetFailureReason reason) {
                Log.i(TAG, "Sentiance SDK reset failed with reason " + reason.name());
            }
        });
    }
}

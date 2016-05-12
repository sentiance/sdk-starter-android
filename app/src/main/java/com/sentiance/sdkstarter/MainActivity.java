package com.sentiance.sdkstarter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sentiance.sdk.Sdk;
import com.sentiance.sdk.SdkIssue;
import com.sentiance.sdk.StatusMessage;
import com.sentiance.sdk.util.Optional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final long STATUS_REFRESH_INTERVAL_MILLIS = 5000;

    private final Handler handler = new Handler();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    private final BroadcastReceiver authenticationBroadcastReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshStatus();
        }
    };

    private final Runnable refreshStatusRunnable = new Runnable() {
        @Override
        public void run() {
            refreshStatus();
            handler.postDelayed(refreshStatusRunnable, STATUS_REFRESH_INTERVAL_MILLIS);
        }
    };

    private ListView statusList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusList = (ListView) findViewById(R.id.statusList);
    }

    private void refreshStatus() {
        List<String> statusItems = new ArrayList<>();
        statusItems.add("SDK flavor: " + Sdk.getInstance(getApplicationContext()).getFlavor());
        statusItems.add("SDK version: " + Sdk.getInstance(getApplicationContext()).getVersion());

        // On Android, the user id is a resource url, using format https://api.sentiance.com/users/USER_ID, you can replace the part to obtain the short URL code:
        Optional<String> userId = Sdk.getInstance(getApplicationContext()).user().getId();
        if (userId.isPresent()) {
            statusItems.add("User ID: " + userId.get().replace("https://api.sentiance.com/users/", ""));
        } else {
            statusItems.add("User ID: N/A");
        }

        // You can use the status message to obtain more information
        StatusMessage statusMessage = Sdk.getInstance(getApplicationContext()).getStatusMessage();
        statusItems.add("Mode: " + statusMessage.mode.name());

        for (SdkIssue issue : statusMessage.issues) {
            statusItems.add("Issue: " + issue.type.name());
        }

        statusItems.add("Wi-Fi data: " + statusMessage.wifiQuotaUsed + " / " + statusMessage.wifiQuotaLimit);
        statusItems.add("Mobile data: " + statusMessage.mobileQuotaUsed + " / " + statusMessage.mobileQuotaLimit);
        statusItems.add("Disk: " + statusMessage.diskQuotaUsed + " / " + statusMessage.diskQuotaLimit);
        statusItems.add("Wi-Fi last seen: " + dateFormat.format(new Date(statusMessage.wifiLastSeenTimestamp)));

        statusList.setAdapter(new ArrayAdapter<>(this, R.layout.list_item_status, R.id.textView, statusItems));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Our MyApplication broadcasts when the SDK authentication was successful
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(authenticationBroadcastReciever, new IntentFilter(MyApplication.ACTION_SDK_AUTHENTICATION_SUCCESS));

        // Periodically refresh the status UI
        handler.post(refreshStatusRunnable);

        refreshStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(authenticationBroadcastReciever);
        handler.removeCallbacks(refreshStatusRunnable);
    }

}

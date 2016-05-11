package com.sentiance.sdkstarter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.sentiance.sdk.Sdk;
import com.sentiance.sdk.StatusMessage;

public class MainActivity extends AppCompatActivity {

    private TextView userIdLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userIdLabel = (TextView) findViewById(R.id.userIdLabel);
    }

    private void refreshLabels() {
        // On Android, the user id is a resource url, using format https://api.sentiance.com/users/USER_ID, you can replace the part to obtain the short URL code:
        userIdLabel.setText(Sdk.getInstance(getApplicationContext()).user().getId().get().replace("https://api.sentiance.com/users/", ""));

        // You can use the status message to obtain more information
        StatusMessage statusMessage = Sdk.getInstance(getApplicationContext()).getStatusMessage();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Our MyApplication broadcasts when the SDK authentication was successful
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshLabels();
            }
        }, new IntentFilter(MyApplication.ACTION_SDK_AUTHENTICATION_SUCCESS));

        refreshLabels();
    }
}

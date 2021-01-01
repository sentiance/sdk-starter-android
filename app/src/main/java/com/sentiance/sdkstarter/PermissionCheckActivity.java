package com.sentiance.sdkstarter;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;

public class PermissionCheckActivity extends AppCompatActivity {

    PermissionManager mPermissionManager;
    Button grantPermissions;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permissions);
        grantPermissions = findViewById(R.id.grant_permissions);
        grantPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPermissionsSettingActivity();
            }
        });

        mPermissionManager = new PermissionManager(this);

        List<Permission> permissions = mPermissionManager.getNotGrantedPermissions();
        if (!permissions.isEmpty() && !isHandlingPermissionResult(savedInstanceState)) {
            requestPermissions(permissions);
        }
        else {
            startMainActivity();
        }
    }

    private boolean isHandlingPermissionResult(@Nullable Bundle savedInstanceState) {
        return savedInstanceState != null &&
            savedInstanceState.getBoolean("android:hasCurrentPermissionsRequest", false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPermissionManager.getNotGrantedPermissions().isEmpty()) {
            startMainActivity();
        }
    }

    private void startPermissionsSettingActivity() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void requestPermissions (List<Permission> permissions) {
        for (Permission p : permissions) {
            if (requestPermission(p, false)) {
                return; // one at a time
            }
        }
    }

    private boolean requestPermission(Permission p, boolean bypassRationale) {
        for (Permission dep: p.getDependencies()) {
            if (!dep.isGranted(this)) {
                // This permission depends on another one that's not yet been granted.
                return false;
            }
        }

        if (!p.isGranted(this) && !p.getCanShowAgain(this)) {
            // This permission was denied more than once.
            return false;
        }

        if (!bypassRationale && p.shouldShowRationale(this)) {
            showExplanation(p);
        }
        else if (!p.isGranted(this)) {
            ActivityCompat.requestPermissions(this, p.getManifestPermissions(), p.getAskCode());
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (alertDialog != null && alertDialog.isShowing()) {
            // Avoid leaking the activity
            alertDialog.dismiss();
        }

        if (mPermissionManager.getNotGrantedPermissions().isEmpty()) {
            startMainActivity();
        }
        else {
            // Requesting a new permission doesn't work in the same activity that handles the result of a previous
            // request. We therefore restart this activity to automatically triggers additional permission requests.
            startPermissionCheckActivity();
        }
    }

    private void showExplanation (final Permission p) {
        alertDialog = new AlertDialog.Builder(this)
            .setTitle(p.getDialogTitle())
            .setMessage(p.getDialogMessage())
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    requestPermission(p, true);
                }
            })
            .show();
    }

    private void startPermissionCheckActivity() {
        startActivity(new Intent(this, PermissionCheckActivity.class));
        finish();
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
}

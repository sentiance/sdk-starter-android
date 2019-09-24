package com.sentiance.sdkstarter;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.List;

public class PermissionCheckActivity extends AppCompatActivity {

    PermissionManager mPermissionManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPermissionManager = new PermissionManager(this);

        List<Permission> permissions = mPermissionManager.getNotGrantedPermissions();
        if (permissions.size() > 0) {
            requestPermission(permissions.get(0), false);
        }
        else {
            startMain();
        }
    }

    private void requestPermission(Permission p, boolean bypassRationale) {
        if (!bypassRationale && p.shouldShowRationale(this)) {
            showExplanation(p);
        }
        else if (!p.isGranted(this)) {
            ActivityCompat.requestPermissions(this, p.getManifestPermissions(), p.getAskCode());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mPermissionManager.getNotGrantedPermissions().size() == 0) {
            startMain();
        }
        else {
            // Requesting new permissions from the same activity instance fails for some reason.
            finish();
            startActivity(new Intent(this, PermissionCheckActivity.class));
        }
    }

    private void showExplanation (final Permission p) {
        new AlertDialog.Builder(this)
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

    private void startMain() {
        finish();
        startActivity(new Intent(this, MainActivity.class));
    }
}

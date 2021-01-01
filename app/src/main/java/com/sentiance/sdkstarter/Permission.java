package com.sentiance.sdkstarter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Permission {
    private static final String KEY_CAN_SHOW_AGAIN = "can_show_again";
    private static final String KEY_SHOW_RATIONALE = "show_rationale";

    private final String name;
    private final String[] manifestPermissions;
    private final int askCode;
    private final String dialogTitle;
    private final String dialogMessage;
    private final List<Permission> dependantPermissions;

    Permission (String name, String[] manifestPermissions, int askCode, String dialogTitle, String dialogMessage) {
        this(name, manifestPermissions, askCode, dialogTitle, dialogMessage, Collections.<Permission>emptyList());
    }

    Permission (String name, String[] manifestPermissions, int askCode,
                String dialogTitle, String dialogMessage, List<Permission> dependantPermissions) {
        this.name = name;
        this.manifestPermissions = manifestPermissions;
        this.askCode = askCode;
        this.dialogTitle = dialogTitle;
        this.dialogMessage = dialogMessage;
        this.dependantPermissions = new ArrayList<>(dependantPermissions);
    }

    boolean isGranted (Activity activity) {
        for (String permission : manifestPermissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    boolean shouldShowRationale (Activity activity) {
        for (String permission : manifestPermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    int getAskCode () {
        return askCode;
    }

    String[] getManifestPermissions () {
        return manifestPermissions;
    }

    String getDialogTitle () {
        return dialogTitle;
    }

    String getDialogMessage () {
        return dialogMessage;
    }

    public List<Permission> getDependencies() {
        return dependantPermissions;
    }

    @Override
    public String toString () {
        return "Permission{" +
                "name='" + name + '\'' +
                '}';
    }

    boolean getCanShowAgain (Activity activity) {
        String key = KEY_CAN_SHOW_AGAIN + "_" + askCode;
        return getPrefs(activity).getBoolean(key, true);
    }

    void setCanShowAgain (Activity activity, boolean value) {
        String key = KEY_CAN_SHOW_AGAIN + "_" + askCode;
        getPrefs(activity).edit().putBoolean(key, value).apply();
    }

    boolean isShowRationaleSet (Activity activity) {
        String key = KEY_SHOW_RATIONALE + "_" + askCode;
        return getPrefs(activity).contains(key);
    }

    void setShowRationale (Activity activity, boolean value) {
        String key = KEY_SHOW_RATIONALE + "_" + askCode;
        getPrefs(activity).edit().putBoolean(key, value).apply();
    }

    void clearShowRationale (Activity activity) {
        String key = KEY_SHOW_RATIONALE + "_" + askCode;
        getPrefs(activity).edit().remove(key).apply();
    }

    private SharedPreferences getPrefs (Activity activity) {
        return activity.getSharedPreferences("permission", Context.MODE_PRIVATE);
    }
}
package com.sentiance.sdkstarter;

import android.Manifest;
import android.app.Activity;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

class PermissionManager {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 15442;
    private static final int ACTIVITY_PERMISSION_REQUEST_CODE = 15443;
    private static final String TITLE_LOCATION = "Location permission";
    private static final String MESSAGE_LOCATION = "The Sentiance SDK needs access to your location all the time in order to build your profile.";
    private static final String TITLE_ACTIVITY = "Motion activity permission";
    private static final String MESSAGE_ACTIVITY = "The Sentiance SDK needs access to your activity data in order to build your profile.";

    private Activity mActivity;

    PermissionManager (Activity activity) {
        mActivity = activity;

        updateCanShowAgainPermissions();
    }

    List<Permission> getNotGrantedPermissions() {
        List<Permission> notGrantedPermissions = new ArrayList<>();

        for (Permission p : getAllPermissions()) {
            if (!p.isGranted(mActivity) && p.getCanShowAgain(mActivity)) {
                notGrantedPermissions.add(p);
            }
        }

        return notGrantedPermissions;
    }

    private void updateCanShowAgainPermissions () {
        for (Permission p : getAllPermissions()) {
            if (p.isGranted(mActivity)) {
                // Permission is granted. Reset the show rationale and can show again prefs.
                p.setCanShowAgain(mActivity, true);
                p.clearShowRationale(mActivity);
                continue;
            }

            if (!p.shouldShowRationale(mActivity)) {
                if (p.isShowRationaleSet(mActivity)) {
                    // We were allowed to show a rationale before, but not anymore.
                    // This is a result of the "don't ask again" option selected by the user.
                    p.setShowRationale(mActivity, false);
                    p.setCanShowAgain(mActivity, false);
                }
            }
            else {
                // We can show a rational. This is when our permission request
                // was shot down by the user the first time we asked.
                p.setShowRationale(mActivity, true);
            }
        }
    }

    private boolean isQPlus() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    private List<Permission> getAllPermissions () {
        List<Permission> list = new ArrayList<>();

        list.add(new Permission("Location",
                isQPlus() ? new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION} :
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE,
                TITLE_LOCATION, MESSAGE_LOCATION));

        if (isQPlus()) {
            list.add(new Permission("Activity", new String[] {Manifest.permission.ACTIVITY_RECOGNITION},
                    ACTIVITY_PERMISSION_REQUEST_CODE,
                    TITLE_ACTIVITY, MESSAGE_ACTIVITY));
        }


        return list;
    }
}

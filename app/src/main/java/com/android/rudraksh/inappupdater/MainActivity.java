package com.android.rudraksh.inappupdater;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.rudraksh.inappupdate.InAppUpdateManager;
import com.android.rudraksh.inappupdate.UpdateType;
import com.android.rudraksh.inappupdate.UserResponse;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    /**
     * The logger tag name.
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The InAppUpdateManager.
     */
    private InAppUpdateManager mInAppUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView txtCurrentVersion = findViewById(R.id.txt_current_version);
        TextView txtAvailableVersion = findViewById(R.id.txt_available_version);

        txtCurrentVersion.setText(String.valueOf(BuildConfig.VERSION_CODE));

        mInAppUpdateManager = InAppUpdateManager.initialize(this);

        // Starts checking for new update if found.
        mInAppUpdateManager.checkAvailableUpdateIfFound(versionCode -> {
            Log.d(TAG, "Found new version: " + versionCode);
            txtAvailableVersion.setText(String.valueOf(versionCode));
            mInAppUpdateManager.popupDialogForUpdateAvailability(userResponse -> {
                if (userResponse == UserResponse.ACCEPTED) {
                    mInAppUpdateManager.setDefaultUpdateMode(UpdateType.APP_UPDATE_TYPE_FLEXIBLE)
                            .startCheckingForUpdates();
                } else {
                    mInAppUpdateManager.popupSnackbarForUpdateRejection();
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start watching the update status if the application has been put in background.
        mInAppUpdateManager.continueUpdate();
    }

    /**
     * Starts the flexible update.
     * @param view  Requires the view to perform action.
     */
    public void doFlexibleUpdate(View view) {
        mInAppUpdateManager.setDefaultUpdateMode(UpdateType.APP_UPDATE_TYPE_FLEXIBLE)
                .startCheckingForUpdates();
    }

    /**
     * Starts the immediate update.
     * @param view  Requires the view to perform action.
     */
    public void doImmediateUpdate(View view) {
        mInAppUpdateManager.setDefaultUpdateMode(UpdateType.APP_UPDATE_TYPE_IMMEDIATE)
                .startCheckingForUpdates();
    }
}
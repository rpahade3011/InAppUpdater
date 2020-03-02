package com.android.rudraksh.inappupdate;

import android.app.Activity;
import android.content.IntentSender;
import android.util.Log;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import java.lang.ref.WeakReference;

import androidx.appcompat.app.AlertDialog;

public class InAppUpdateManager {

    /**
     * The logger tag name.
     */
    private static final String LOG_TAG = InAppUpdateManager.class.getSimpleName();

    /**
     * The update request intent request code.
     */
    private static final int UPDATE_REQUEST_CODE = 8294;
    /**
     * The InAppUpdateManager singleton instance.
     */
    private static InAppUpdateManager sInstance;
    /**
     * The activity Weak reference.
     */
    private WeakReference<Activity> mActivityWeakReference;
    /**
     * The default update type mode.
     */
    private int mDefaultUpdateMode = UpdateType.APP_UPDATE_TYPE_FLEXIBLE;

    /**
     * Creates instance of the manager.
     */
    private AppUpdateManager mAppUpdateManager;

    /**
     * Returns an intent object that you use to check for an update.
     */
    private Task<AppUpdateInfo> mAppUpdateInfoTask;

    /**
     * Returns the activity reference.
     * @return Activity
     */
    private Activity getWeakActivity() {
        return mActivityWeakReference.get();
    }

    /**
     * Builds the update manager and creates a new single instance of {@link InAppUpdateManager}.
     * @param   activity requires an activity reference
     * @return  returns a single instance created.
     */
    public static InAppUpdateManager initialize(Activity activity) {
        if (sInstance == null) {
            sInstance = new InAppUpdateManager(activity);
        }
        Log.d(LOG_TAG, "Instance created");
        return sInstance;
    }

    /**
     * Constructs the new instance of {@link InAppUpdateManager}.
     * @param activity  requires an activity reference
     */
    private InAppUpdateManager(Activity activity) {
        mActivityWeakReference = new WeakReference<>(activity);
        this.mAppUpdateManager = AppUpdateManagerFactory.create(getWeakActivity());
        this.mAppUpdateInfoTask = mAppUpdateManager.getAppUpdateInfo();
    }

    /**
     * Sets the default update mode whether flexible or immediate.
     * @param   defaultMode   requires an integer of default update mode
     * @return  returns an single instance of {@link InAppUpdateManager}
     */
    public InAppUpdateManager setDefaultUpdateMode(int defaultMode) {
        String mode = defaultMode ==
                UpdateType.APP_UPDATE_TYPE_FLEXIBLE
                ? mActivityWeakReference.get().getResources()
                .getString(R.string.app_update_type_flexible)
                : mActivityWeakReference.get().getResources()
                .getString(R.string.app_update_type_immediate);
        this.mDefaultUpdateMode = defaultMode;
        Log.d(LOG_TAG, "Default selected mode: " + mode);
        return this;
    }

    /**
     * Starts checking of any available updates for the application.
     * If the {@code mDefaultUpdateMode} is set to {@link UpdateType#APP_UPDATE_TYPE_FLEXIBLE}
     * then assign a listener to observe on install state of update.
     */
    public void startCheckingForUpdates() {
        if (mDefaultUpdateMode == UpdateType.APP_UPDATE_TYPE_FLEXIBLE) {
            setupInstallStateListener();
        }
        checkForUpdates();
    }

    /**
     * Performs checking the availability of the update. {@link AppUpdateInfo#updateAvailability()}
     * and {@code AppUpdateInfo#isUpdateTypeAllowed} informs that the update is available to download
     * and install.
     */
    private void checkForUpdates() {
        // Checks that the platform will allow the specified type of update.
        Log.d(LOG_TAG, "Checking for updates");
        mAppUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // For a immediate update, use AppUpdateType.IMMEDIATE
                    && appUpdateInfo.isUpdateTypeAllowed(mDefaultUpdateMode)) {
                // Request the update.
                Log.d(LOG_TAG, "Update available");
                startUpdate(appUpdateInfo);
            } else {
                Log.d(LOG_TAG, "No Update available");
            }
        });
    }

    /**
     * Gets the information on new update is available.
     * @param onAvailableVersionListener    Requires OnAvailableVersionListener to notify about the
     *                                      updated version code of application
     */
    public void checkAvailableUpdateIfFound(OnAvailableVersionListener onAvailableVersionListener) {
        mAppUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                // Request the update.
                Log.d(LOG_TAG, "Update available");
                onAvailableVersionListener
                        .onNewVersionAvailable(appUpdateInfo.availableVersionCode());
            } else {
                Log.d(LOG_TAG, "No Update available");
            }
        });
    }

    /**
     * Starts downloading the new update by providing the result to the callee.
     *
     * @param appUpdateInfo Requires an AppUpdateInfo to be executed
     */
    private void startUpdate(AppUpdateInfo appUpdateInfo) {
        try {
            Log.d(LOG_TAG, "Starting update");
            mAppUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    mDefaultUpdateMode,
                    getWeakActivity(),
                    UPDATE_REQUEST_CODE);
        } catch (IntentSender.SendIntentException sie) {
            Log.e(LOG_TAG, "Caught an exception while requesting update: " + sie.getMessage());
        }
    }

    /**
     * Sets a notifier on the {@code mAppUpdateManager} about the install state and if the install
     * state notifies the {@code InstallStatus.DOWNLOADED} popup the snackbar to notify user about
     * the update status.
     */
    private void setupInstallStateListener() {
        InstallStateUpdatedListener listener = installState -> {
            if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                // After the update is downloaded, show a notification
                // and request user confirmation to restart the app.
                Log.d(LOG_TAG, "An update has been downloaded");
                popupSnackbarForCompleteUpdate();
            }
        };
        mAppUpdateManager.registerListener(listener);
    }

    /**
     * Notifies the user about the user rejection of updated application.
     */
    public void popupSnackbarForUpdateRejection() {
        Snackbar snackbar =
                Snackbar.make(
                        getWeakActivity().getWindow().getDecorView().findViewById(android.R.id.content),
                        getWeakActivity().getResources()
                                .getString(R.string.app_update_available_dialog_rejection_message),
                        Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    /**
     * Notifies the user about the install state of application.
     */
    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        getWeakActivity().getWindow().getDecorView().findViewById(android.R.id.content),
                        getWeakActivity().getResources().getString(R.string.app_update_download_complete),
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(mActivityWeakReference.get().getResources().getString(R.string.app_update_download_complete_restart_action), v -> mAppUpdateManager.completeUpdate());
        snackbar.show();
    }

    /**
     * Starts continuing the flexible update if the user is not on the app or doing something while
     * using the application or the application has been put in background.
     */
    private void continueUpdateForFlexible() {
        sInstance.mAppUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {
                    // If the update is downloaded but not installed,
                    // notify the user to complete the update.
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        Log.d(LOG_TAG, "An update has been downloaded");
                        sInstance.popupSnackbarForCompleteUpdate();
                    } else {
                        Log.d(LOG_TAG, "Update availability: " + appUpdateInfo.installStatus() );
                    }
                });
    }

    /**
     * Need to be declared in the {@code Activity#onResume} lifecycle callbacks so that the update
     * status should be notified to user.
     */
    public void continueUpdate() {
        if (sInstance.mDefaultUpdateMode == UpdateType.APP_UPDATE_TYPE_FLEXIBLE) {
            continueUpdateForFlexible();
        } else {
            continueUpdateForImmediate();
        }
    }

    /**
     * Starts continuing the immediate update if the user is not on the app or doing something while
     * using the application or the application has been put in background.
     */
    private void continueUpdateForImmediate() {
        sInstance.mAppUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {
                    if (appUpdateInfo.updateAvailability()
                            == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                        // If an in-app update is already running, resume the update.
                        try {
                            sInstance.mAppUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    sInstance.mDefaultUpdateMode,
                                    getWeakActivity(),
                                    UPDATE_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            Log.d(LOG_TAG, "Exception occurred: " + e.getMessage());
                        }
                    } else {
                        Log.d(LOG_TAG, "Update availability: " + appUpdateInfo.updateAvailability());
                    }
                });
    }

    /**
     * Notifies user about the response noted after the update is available.
     * @param userResponseListener  Required {@link UserResponse#ACCEPTED}
     *                              or {@link UserResponse#DENIED}
     */
    public void popupDialogForUpdateAvailability(IUserResponseListener userResponseListener) {
        AlertDialog.Builder popupDialogBuilder = new AlertDialog.Builder(getWeakActivity());
        popupDialogBuilder.setTitle(getWeakActivity().getResources()
                .getString(R.string.app_update_available_dialog_title));
        popupDialogBuilder.setMessage(getWeakActivity().getResources()
                .getString(R.string.app_update_available_dialog_message));
        popupDialogBuilder.setPositiveButton(getWeakActivity()
                        .getResources().getString(R.string.app_update_dialog_positive_button_text),
                ((dialog, which) -> {
                    if (userResponseListener != null) {
                        userResponseListener.onUserResponseNoted(UserResponse.ACCEPTED);
                    } else {
                        throw new RuntimeException("IUserResponseListener cannot be null");
                    }
                }));
        popupDialogBuilder.setNegativeButton(getWeakActivity()
                        .getResources().getString(R.string.app_update_dialog_negative_button_text),
                ((dialog, which) -> {
                    if (userResponseListener != null) {
                        userResponseListener.onUserResponseNoted(UserResponse.DENIED);
                    } else {
                        throw new RuntimeException("IUserResponseListener cannot be null");
                    }
                }));
        popupDialogBuilder.create();
        popupDialogBuilder.show();
    }
}
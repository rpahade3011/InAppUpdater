package com.android.rudraksh.inappupdate;

public interface OnAvailableVersionListener {

    /**
     * Handles the response of available version.
     * @param versionCode   int - Required a new version to be displayed
     */
    void onNewVersionAvailable(int versionCode);
}
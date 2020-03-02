package com.android.rudraksh.inappupdate;

public interface IUserResponseListener {

    /**
     * Notifies user about the response noted after the update is available.
     * @param userResponse  Required {@link UserResponse#ACCEPTED} or {@link UserResponse#DENIED}
     */
    void onUserResponseNoted(UserResponse userResponse);
}
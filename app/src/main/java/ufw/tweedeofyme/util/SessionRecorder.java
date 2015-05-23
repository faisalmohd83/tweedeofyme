package ufw.tweedeofyme.util;

/**
 * Created by Faisal on 21-05-2015.
 */

import android.util.Log;

import com.twitter.sdk.android.core.Session;

public class SessionRecorder {

    private static Long userId;

    public static Session recordInitialSessionState(Session twitterSession) {
        if (twitterSession != null) {
            recordSessionActive("Splash: user with active Twitter session", twitterSession);
            return twitterSession;
        } else {
            recordSessionInactive("Splash: anonymous user");
            return null;
        }
    }

    public static void recordSessionActive(String message, Session session) {
        userId = session.getId();
        recordSessionActive(message, String.valueOf(session.getId()));
    }

    public static void recordSessionInactive(String message) {
        recordSessionState(message, null, false);
    }

    private static void recordSessionActive(String message, String userIdentifier) {
        recordSessionState(message, userIdentifier, true);
    }

    private static void recordSessionState(String message,
                                           String userIdentifier,
                                           boolean active) {
    }

    public static Long getUserId() {
        return userId;
    }
}

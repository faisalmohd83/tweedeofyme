package ufw.tweedeofyme.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Session;

import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;
import ufw.tweedeofyme.util.Const;
import ufw.tweedeofyme.util.SessionRecorder;

public class InitialActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(Const.TWITTER_KEY, Const.TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        final Session activeSession = SessionRecorder.recordInitialSessionState(
                Twitter.getSessionManager().getActiveSession()
        );

        if (activeSession != null) {
            showTimeLine();
        } else {
            loginActivity();
        }
    }

    private void showTimeLine() {
        startActivity(new Intent(this, TimelineActivity.class));
        finish();
    }

    private void loginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}

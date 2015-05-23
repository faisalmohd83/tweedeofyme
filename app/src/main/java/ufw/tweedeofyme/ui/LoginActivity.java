package ufw.tweedeofyme.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import ufw.tweedeofyme.R;
import ufw.tweedeofyme.util.SessionRecorder;

public class LoginActivity extends Activity {

    private TwitterLoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setUpTwitterButton();
    }

    private void setUpTwitterButton() {
        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                SessionRecorder.recordSessionActive("Login: twitter account active", result.data);

                //show Timeline Activity
                startActivity(new Intent(LoginActivity.this, TimelineActivity.class));
                finish();
            }

            @Override
            public void failure(TwitterException exception) {
                //Answers.getInstance().logEvent("login:twitter:failure");
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.toast_twitter_signin_fail),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

}

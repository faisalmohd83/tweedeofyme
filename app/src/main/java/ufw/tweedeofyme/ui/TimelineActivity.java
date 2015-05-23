package ufw.tweedeofyme.ui;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;

import ufw.tweedeofyme.R;
import ufw.tweedeofyme.util.SessionRecorder;

public class TimelineActivity extends ListActivity {

    private static final String TAG = "TimelineActivity";
    EditText tweetBox;
    TweetTimelineListAdapter timelineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // UI setup
        loadUserTimeline();
        prepareTweetbox();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_timeline, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_logout:
                // TODO Logout code goes here.
                Toast.makeText(getApplicationContext(), "Logout to be implemented",
                        Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadUserTimeline() {
        Long userId = SessionRecorder.getUserId();
        if (userId != null) {
            UserTimeline userTimeline = new UserTimeline.Builder().userId(userId).build();

            timelineAdapter = new TweetTimelineListAdapter(this, userTimeline);
            setListAdapter(timelineAdapter);
        } else
            getListView().setEmptyView(findViewById(R.id.loading));
    }

    private void prepareTweetbox() {
        tweetBox = (EditText) findViewById(R.id.tw__tweet_text);
        tweetBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                String tweetMsg = tweetBox.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    if (tweetMsg.length() > 0) {
                        handled = shareTweet(tweetMsg);
                    }
                }
                return handled;
            }
        });
    }

    private boolean shareTweet(String tweetMsg) {
        final Session activeSession = SessionRecorder.recordInitialSessionState(
                Twitter.getSessionManager().getActiveSession()
        );

        if (activeSession == null) {
            return false;
        }

        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(activeSession);
        StatusesService statusesService = twitterApiClient.getStatusesService();
        statusesService.update(tweetMsg, null, false, null, null,
                null, null, false, new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> result) {
                        //Do something with result, which provides a Tweet inside of result.data
                        Toast.makeText(getApplicationContext(), "Tweeted successfully",
                                Toast.LENGTH_SHORT).show();
                        timelineAdapter.notifyDataSetChanged();
                        tweetBox.setText(null);
                    }

                    public void failure(TwitterException exception) {
                        //Do something on failure
                        Toast.makeText(getApplicationContext(), "Oops! something went wrong. Try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        return true;
    }


}

package ufw.tweedeofyme.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
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
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.UserTimeline;

import ufw.tweedeofyme.R;
import ufw.tweedeofyme.util.SessionRecorder;

public class TimelineActivity extends ListActivity implements AbsListView.OnScrollListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "TimelineActivity";

    boolean enableRefresh = false;

    EditText tweetBox;
    ListView listView;
    SwipeRefreshLayout swipeLayout;
    TweetTimelineListAdapter timelineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        listView = (ListView) findViewById(android.R.id.list);

        // UI setup
        loadUserTimeline();
        prepareTweetbox();

        // set custom scroll listener to enable swipe refresh layout only when at list top
        listView.setOnScrollListener(this);

        // specify action to take on swipe refresh
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(android.R.color.holo_red_light,
                android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light);
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
                // Clear active Twitter Session and Finish the Activity
                Twitter.getSessionManager().clearActiveSession();
                SessionRecorder.recordSessionInactive("Logout: accounts deactivated");
                startActivity(new Intent(this, LoginActivity.class));
                finish();
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
            listView.setEmptyView(findViewById(R.id.loading));
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
                        timelineAdapter.refresh(new TimelineUpdateCallback());
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

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (listView != null && listView.getChildCount() > 0) {
            // check that the first item is visible and that its top matches the parent
            enableRefresh = listView.getFirstVisiblePosition() == 0 &&
                    listView.getChildAt(0).getTop() >= 0;
        } else {
            enableRefresh = false;
        }
        swipeLayout.setEnabled(enableRefresh);
    }

    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(true);
        timelineAdapter.refresh(new TimelineUpdateCallback());
    }

    private class TimelineUpdateCallback extends Callback<TimelineResult<Tweet>> {
        @Override
        public void success(Result<TimelineResult<Tweet>> result) {
            Log.d(TAG, "Result - Success");
            swipeLayout.setRefreshing(false);
        }

        @Override
        public void failure(TwitterException e) {
            Log.d(TAG, "Result - Failure");
            swipeLayout.setRefreshing(false);
        }
    }
}

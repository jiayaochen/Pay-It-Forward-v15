package com.jclg.payitforward;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by graceg on 2016-02-18.
 */
public class HistoryActivity extends AppCompatActivity {

    private static final int HISTORY_COMPLETE = 0;
    private static final int HISTORY_INCOMPLETE = 1;
    private static final int HISTORY_EXPIRED = 2;

    public static class ListUtils extends HistoryActivity {
        public static void setDynamicHeight(ListView mListView) {
            ListAdapter mListAdapter = mListView.getAdapter();

            if (mListAdapter == null) {
                // when adapter is null
                return;
            }

            int height = 0;
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);

            for (int i = 0; i < mListAdapter.getCount(); i++) {
                View listItem = mListAdapter.getView(i, null, mListView);
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));//

            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }

    private Toolbar myToolbar;

    private ListView listView_complete;
    private ListView listView_incomplete;
    private ListView listView_expired;

    private ArrayList<Posts> completeData = new ArrayList<Posts>();
    private ArrayList<Posts> incompleteData = new ArrayList<Posts>();
    private ArrayList<Posts> expiredData = new ArrayList<Posts>();

    // OnResume Flag
    private boolean isCreateDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);

        myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        /* Setup views from layout */

        // Get listview from layout
        listView_complete = (ListView)findViewById(R.id.history_listView_complete);
        listView_incomplete = (ListView)findViewById(R.id.history_listView_incomplete);
        listView_expired = (ListView)findViewById(R.id.history_listView_expired);

        // Setup listview adapters
        listView_complete.setAdapter(new ArrayAdapter<Posts>(this, R.layout.list_item_layout, completeData));
        listView_incomplete.setAdapter(new ArrayAdapter<Posts>(this, R.layout.list_item_layout, incompleteData));
        listView_expired.setAdapter(new ArrayAdapter<Posts>(this, R.layout.list_item_layout, expiredData));

        /* Setup OnClick Listeners */

        // Setup ListView onItemClick functionality
        listView_complete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Find post object corresponding to item clicked
                Posts listPost = completeData.get(position);

                // Display post
                loadViewPostActivity(listPost);
            }
        });

        // Setup ListView onItemClick functionality
        listView_incomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Find post object corresponding to item clicked
                Posts listPost = incompleteData.get(position);

                // Display post
                loadViewPostActivity(listPost);
            }
        });

        // Setup ListView onItemClick functionality
        listView_expired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Find post object corresponding to item clicked
                Posts listPost = expiredData.get(position);

                // Display post
                loadViewPostActivity(listPost);
            }
        });

        // Load data onto listview adapter
        refreshLists();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isCreateDone )
            refreshLists();

        isCreateDone = true;
    }

    /*
        Parse Functions
     */

    private void loadDataInParse(final int History_flag) {
        String profileId = ParseUser.getCurrentUser().getParseObject("profileId").getObjectId();

        // Setup main query for search in Parse databse
        ParseQuery<ParseObject> mainQuery = ParseQuery.getQuery("Posts");

        // Setup or condition for requester history
        ParseQuery<ParseObject> requesterQuery = ParseQuery.getQuery("Posts");
        requesterQuery.whereEqualTo("Requester", profileId);

        // Setup or condition for tasker history
        ParseQuery<ParseObject> taskerQuery = ParseQuery.getQuery("Posts");
        taskerQuery.whereEqualTo("Tasker", profileId);

        // Combine or conditions into main query
        List<ParseQuery<ParseObject>> orQueries = new ArrayList<>();
        orQueries.add(requesterQuery);
        orQueries.add(taskerQuery);

        mainQuery = ParseQuery.or(orQueries);

        switch(History_flag){
            case HISTORY_COMPLETE:
                // Restrict to cases where the author is the current user.
                mainQuery.whereEqualTo("PostStatus", PostStatus.POST_COMPLETE);
                break;

            case HISTORY_INCOMPLETE:
                // Restrict to cases where the author is the current user.
                mainQuery.whereEqualTo("PostStatus", PostStatus.POST_INCOMPLETE);
                break;

            case HISTORY_EXPIRED:
                mainQuery.whereEqualTo("PostStatus", PostStatus.POST_EXPIRED);
                break;

            default:
                System.out.println("invalid manage post flag");
                return;
        }

        // Run the query
        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            // Query finished
            public void done(List<ParseObject> results, ParseException e) {
                System.out.println("done query");

                // Query Successful
                if (e == null) {

                    // Temporary variables
                    Posts postsTemp;

                    // If there are results, clear the list of posts for new posts
                    clearData(History_flag);

                    // Extract data from Parse
                    for (ParseObject post : results) {

                        // Create the new post to place onto the map
                        postsTemp = new Posts(
                                post.getString("Requester"),
                                post.getString("Tasker"),
                                post.getObjectId(),
                                post.getString("Title"),
                                post.getString("Description"),
                                post.getDouble("Latitude"), post.getDouble("Longitude"),
                                post.getInt("PostStatus"),
                                post.getInt("PostCategory"),
                                post.getBoolean("RequesterReview"), post.getBoolean("TaskerReview"),
                                null, post.getInt("OffersNum"),
                                post.getDate("DueDate"));

                        // populate local posts data
                        addData(History_flag, postsTemp);

                        System.out.println("hit: " + postsTemp.getTitle() + " " + postsTemp.getId());
                    }

                    // Update adapter
                    updateList(History_flag);

                } else {
                    Log.d(getClass().getSimpleName(), "Error: " + e.getMessage());
                }
            }

        });
    }

    /*
        Helper Functions
     */

    private void clearData (int History_flag) {
        switch(History_flag){
            case HISTORY_COMPLETE:
                completeData.clear();
                return;

            case HISTORY_INCOMPLETE:
                incompleteData.clear();
                return;

            case HISTORY_EXPIRED:
                expiredData.clear();
                return;

            default:
                System.out.println("invalid manage post flag");
                return;
        }
    }

    private void addData (int History_flag, Posts postsTemp) {
        switch(History_flag){
            case HISTORY_COMPLETE:
                completeData.add(postsTemp);
                return;

            case HISTORY_INCOMPLETE:
                incompleteData.add(postsTemp);
                return;

            case HISTORY_EXPIRED:
                expiredData.add(postsTemp);
                return;

            default:
                System.out.println("invalid manage post flag");
                return;
        }
    }

    private void updateList (int History_flag) {
        switch(History_flag){
            case HISTORY_COMPLETE:
                ((ArrayAdapter<Posts>) listView_complete.getAdapter()).notifyDataSetChanged();
                ListUtils.setDynamicHeight(listView_complete);
                return;

            case HISTORY_INCOMPLETE:
                ((ArrayAdapter<Posts>) listView_incomplete.getAdapter()).notifyDataSetChanged();
                ListUtils.setDynamicHeight(listView_incomplete);
                return;

            case HISTORY_EXPIRED:
                ((ArrayAdapter<Posts>) listView_expired.getAdapter()).notifyDataSetChanged();
                ListUtils.setDynamicHeight(listView_expired);
                return;

            default:
                System.out.println("invalid manage post flag");
                return;
        }
    }

    private void refreshLists(){
        loadDataInParse(HISTORY_COMPLETE);
        loadDataInParse(HISTORY_INCOMPLETE);
        loadDataInParse(HISTORY_EXPIRED);
    }

    /*
        Activity Loaders
     */

    private void loadViewPostActivity(Posts listPost) {
        Intent intent = new Intent(this, ViewPostActivity.class);
        intent.putExtra(PifApplication.INTENT_EXTRA_MY_POSTOBJECT, listPost);

        startActivity(intent);
    }

    private void loadLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void loadProfileView() {
        Intent intent = new Intent(this, ViewProfileActivity.class);
        startActivity(intent);
    }

    private void loadEditProfileView() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_posts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh: {
                refreshLists();
                break;
            }

            case R.id.action_logout:
                ParseUser.logOut();
                loadLoginActivity();
                break;

            case R.id.action_viewProfile: {
                // Do something when user selects Settings from Action Bar overlay
                //loadProfileView();
                loadEditProfileView();
                break;
            }

//            case R.id.action_settings: {
//                // Do something when user selects Settings from Action Bar overlay
//                break;
//            }
        }

        return super.onOptionsItemSelected(item);
    }
}

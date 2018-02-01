package com.jclg.payitforward;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xinlan on 1/24/2016.
 */
public class ManagePostsActivity extends AppCompatActivity {

    private static final int MANAGEPOSTS_REQUESTER = 0;
    private static final int MANAGEPOSTS_TASKER = 1;
    private static final int MANAGEPOSTS_ASSIGNED = 2;
    private static final int MANAGEPOSTS_INPROGRESS = 3;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ManagePosts Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.jclg.payitforward/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ManagePosts Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.jclg.payitforward/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public static class ListUtils extends ManagePostsActivity {
        public static void setDynamicHeight(ListView mListView) {
            ListAdapter mListAdapter = mListView.getAdapter();

            if (mListAdapter == null) {
                // when adapter is null
                return;
            }

            int height = 0;
            int desiredWidth = MeasureSpec.makeMeasureSpec(mListView.getWidth(), MeasureSpec.UNSPECIFIED);

            for (int i = 0; i < mListAdapter.getCount(); i++) {
                View listItem = mListAdapter.getView(i, null, mListView);
                listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));//

            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }

    private Toolbar myToolbar;

    private ListView listView_requester;
    private ListView listView_tasker;
    private ListView listView_assigned;
    private ListView listView_inprogress;

    private ArrayList<Posts> requesterData = new ArrayList<Posts>();
    private ArrayList<Posts> taskerData = new ArrayList<Posts>();
    private ArrayList<Posts> assignedData = new ArrayList<Posts>();
    private ArrayList<Posts> inprogressData = new ArrayList<Posts>();

    private Button button_history;

    // OnResume Flag
    private boolean isCreateDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_posts);

        myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        /* Setup views from layout */

        // Get listview from layout
        listView_requester = (ListView) findViewById(R.id.managePosts_listView_requester);
        listView_tasker = (ListView) findViewById(R.id.managePosts_listView_tasker);
        listView_assigned = (ListView) findViewById(R.id.managePosts_listView_assigned);
        listView_inprogress = (ListView) findViewById(R.id.managePosts_listView_inprogress);

        // Setup listview adapters
        listView_requester.setAdapter(new ArrayAdapter<Posts>(this, R.layout.list_item_layout, requesterData));
        listView_tasker.setAdapter(new ArrayAdapter<Posts>(this, R.layout.list_item_layout, taskerData));
        listView_assigned.setAdapter(new ArrayAdapter<Posts>(this, R.layout.list_item_layout, assignedData));
        listView_inprogress.setAdapter(new ArrayAdapter<Posts>(this, R.layout.list_item_layout, inprogressData));

        // Setup history button
        button_history = (Button) findViewById(R.id.managePosts_button_history);

        /* Setup OnClick Listeners */

        // Setup ListView onItemClick functionality
        listView_requester.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Find post object corresponding to item clicked
                Posts listPost = requesterData.get(position);

                // Display post
                loadViewPostActivity(listPost);
            }
        });

        // Setup ListView onItemClick functionality
        listView_tasker.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Find post object corresponding to item clicked
                Posts listPost = taskerData.get(position);

                // Display post
                loadViewPostActivity(listPost);
            }
        });

        // Setup ListView onItemClick functionality
        listView_assigned.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Find post object corresponding to item clicked
                Posts listPost = assignedData.get(position);

                // Display post
                loadViewPostActivity(listPost);
            }
        });

        // Setup ListView onItemClick functionality
        listView_inprogress.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Find post object corresponding to item clicked
                Posts listPost = inprogressData.get(position);

                // Display post
                loadViewPostActivity(listPost);
            }
        });

        // Setup history button listener
        button_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load history activity
                loadHistoryActivity();
            }
        });

        // Load data onto listview adapter
        refreshLists();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isCreateDone)
            refreshLists();

        isCreateDone = true;
    }

    private void loadDataInParse(final int ManagePosts_flag) {
        // Setup query for search in Parse databse
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");

        // Profile ID string
        String profileId = ParseUser.getCurrentUser().getParseObject("profileId").getObjectId();

        switch (ManagePosts_flag) {
            case MANAGEPOSTS_REQUESTER:
                // Restrict to cases where the author is the current user.
                query.whereEqualTo("PostStatus", PostStatus.POST_OPEN);
                query.whereEqualTo("Requester", profileId);
                break;

            case MANAGEPOSTS_TASKER:
                // Restrict to cases where the author is the current user.
                query.whereEqualTo("PostStatus", PostStatus.POST_OPEN);
                query.whereEqualTo("Offers", profileId);
                break;

            case MANAGEPOSTS_INPROGRESS:
                query.whereEqualTo("PostStatus", PostStatus.POST_INPROGRESS);
                query.whereEqualTo("Requester", profileId);
                break;

            case MANAGEPOSTS_ASSIGNED:
                query.whereEqualTo("PostStatus", PostStatus.POST_INPROGRESS);
                query.whereEqualTo("Tasker", profileId);
                break;

            default:
                System.out.println("invalid manage post flag");
                return;
        }

        // Run the query
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            // Query finished
            public void done(List<ParseObject> results, ParseException e) {
                System.out.println("done query");

                // Query Successful
                if (e == null) {

                    // Temporary variables
                    Posts postsTemp;
                    String[] offersTemp = new String[PifApplication.OFFERS_MAX];

                    // If there are results, clear the list of posts for new posts
                    clearData(ManagePosts_flag);

                    // Extract data from Parse
                    for (ParseObject post : results) {

                        // Offers must come in JSONArray form and is converted to a string array
                        try {
                            offersTemp = convertJSONArrayToStringArray(post.getJSONArray("Offers"));
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

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
                                offersTemp, post.getInt("OffersNum"),
                                post.getDate("DueDate"));

                        System.out.println("ManagePosts::loadDataInParse - " + postsTemp.getOffersNums());

                        // populate local posts data
                        addData(ManagePosts_flag, postsTemp);

                        System.out.println("hit: " + postsTemp.getTitle() + " " + postsTemp.getDescription() + " " + postsTemp.getId());
                    }

                    // Update adapter
                    updateList(ManagePosts_flag);

                } else {
                    Log.d(getClass().getSimpleName(), "Error: " + e.getMessage());
                }
            }

        });
    }

    private void clearData(int ManagePosts_flag) {
        switch (ManagePosts_flag) {
            case MANAGEPOSTS_REQUESTER:
                requesterData.clear();
                return;

            case MANAGEPOSTS_TASKER:
                taskerData.clear();
                return;

            case MANAGEPOSTS_ASSIGNED:
                assignedData.clear();
                return;

            case MANAGEPOSTS_INPROGRESS:
                inprogressData.clear();
                return;

            default:
                System.out.println("invalid manage post flag");
                return;
        }
    }

    private void addData(int ManagePosts_flag, Posts postsTemp) {
        switch (ManagePosts_flag) {
            case MANAGEPOSTS_REQUESTER:
                requesterData.add(postsTemp);
                return;

            case MANAGEPOSTS_TASKER:
                taskerData.add(postsTemp);
                return;

            case MANAGEPOSTS_ASSIGNED:
                assignedData.add(postsTemp);
                return;

            case MANAGEPOSTS_INPROGRESS:
                inprogressData.add(postsTemp);
                return;

            default:
                System.out.println("invalid manage post flag");
                return;
        }
    }

    private void updateList(int ManagePosts_flag) {
        switch (ManagePosts_flag) {
            case MANAGEPOSTS_REQUESTER:
                ((ArrayAdapter<Posts>) listView_requester.getAdapter()).notifyDataSetChanged();
                ListUtils.setDynamicHeight(listView_requester);
                return;

            case MANAGEPOSTS_TASKER:
                ((ArrayAdapter<Posts>) listView_tasker.getAdapter()).notifyDataSetChanged();
                ListUtils.setDynamicHeight(listView_tasker);
                return;

            case MANAGEPOSTS_ASSIGNED:
                ((ArrayAdapter<Posts>) listView_assigned.getAdapter()).notifyDataSetChanged();
                ListUtils.setDynamicHeight(listView_assigned);
                return;

            case MANAGEPOSTS_INPROGRESS:
                ((ArrayAdapter<Posts>) listView_inprogress.getAdapter()).notifyDataSetChanged();
                ListUtils.setDynamicHeight(listView_inprogress);
                return;

            default:
                System.out.println("invalid manage post flag");
                return;
        }
    }

    public String[] convertJSONArrayToStringArray(JSONArray src) throws JSONException {
        if (src == null)
            return null;

        String[] ret = new String[PifApplication.OFFERS_MAX];

        for (int i = 0; i < src.length() && i < PifApplication.OFFERS_MAX; i++)
            ret[i] = src.getString(i);

        return ret;
    }

    private void refreshLists() {
        loadDataInParse(MANAGEPOSTS_REQUESTER);
        loadDataInParse(MANAGEPOSTS_TASKER);
        loadDataInParse(MANAGEPOSTS_ASSIGNED);
        loadDataInParse(MANAGEPOSTS_INPROGRESS);
    }

    private void loadViewPostActivity(Posts listPost) {
        Intent intent = new Intent(this, ViewPostActivity.class);
        intent.putExtra(PifApplication.INTENT_EXTRA_MY_POSTOBJECT, listPost);

        startActivity(intent);
    }

    private void loadHistoryActivity() {
        Intent intent = new Intent(this, HistoryActivity.class);
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
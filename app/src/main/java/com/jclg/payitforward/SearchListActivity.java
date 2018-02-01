package com.jclg.payitforward;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeff on 3/24/2016.
 */
public class SearchListActivity extends AppCompatActivity {
    /* Search list resources */
    private ArrayList<Posts> posts;

    private String postsearchList;
    private String postCategoryList;

    /* Search layout views */
    private EditText searchList_Item;
    private Button searchList_Button;
    private Button searchList_MapMode_Button;
    private Spinner searchList_Category;

    private ListView pinsListView;

    private boolean isCreateDone = false;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_list);

        posts = new ArrayList<Posts>();

        /* The Intent Extras */

        // Setup intent and obtain extras
        Intent intent = this.getIntent();
        Parcelable[] posts_parcel = intent.getParcelableArrayExtra(PifApplication.INTENT_EXTRA_SEARCH_POSTLIST);

        // Convert parcel items into post items
        if (posts_parcel != null) {
            for (Parcelable parcel : posts_parcel){
                posts.add((Posts)parcel);
            }

            // Update adapter
            ((ArrayAdapter<Posts>) pinsListView.getAdapter()).notifyDataSetChanged();
        }

        /* Toolbar */
        // Setup tool bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        /* searchList List */
        // Setup ListView to hold search contents in list form
        pinsListView = (ListView) findViewById(R.id.searchList_listView_searchResults);
        ArrayAdapter<Posts> adapter = new ArrayAdapter<Posts>(this, R.layout.list_item_layout, posts);
        pinsListView.setAdapter(adapter);

        // Setup ListView onItemClick functionality
        pinsListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Posts listPost = posts.get(position);

                // Display post
                loadViewPostActivity(listPost);
            }
        });

        /* searchList Bar */
        // Setup Search bar to get user search query
        searchList_Item = (EditText) findViewById(R.id.searchList_editText_searchPrompt);
        searchList_Button = (Button) findViewById(R.id.searchList_button_sendSearch);

        /* searchList Category Spinner */
        // Setup Spinner to get user category
        searchList_Category = (Spinner) findViewById(R.id.searchList_spinner_searchCategory);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter_spinner = ArrayAdapter.createFromResource(
                this,
                R.array.search_category_array,
                android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter_spinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        searchList_Category.setAdapter(adapter_spinner);

        /* List Mode Button */
        // Setup button to take user to list of results instead of map
        searchList_MapMode_Button = (Button) findViewById(R.id.searchList_button_mapmode);

        //Function to update post status
        setExpiredPostStatus();
        initializePostsInParse();

        // Setup searchList button listener
        searchList_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postsearchList = searchList_Item.getText().toString();
                postsearchList = postsearchList.trim();


                if (!postsearchList.isEmpty()) {
                    searchPostsInParse();
                } else
                    System.out.println("onCreate: nothing reenter...");
            }
        });

        // Setup category spinner listener
        searchList_Category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                postCategoryList = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Setup list mode button listener
        searchList_MapMode_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isCreateDone) {
            posts.clear();

            // Update adapter
            ((ArrayAdapter<Posts>) pinsListView.getAdapter()).notifyDataSetChanged();

            setExpiredPostStatus();
            initializePostsInParse();
        }

        isCreateDone = true;
    }

    /*
        Parse Functions
     */

    private void setExpiredPostStatus() {
        // Setup query for search in Parse databse
        final ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");
        query.whereEqualTo("PostStatus", PostStatus.POST_OPEN);
        // Run the query
        query.findInBackground(new FindCallback<ParseObject>() {
            @SuppressWarnings("unchecked")
            @Override
            // Query finished
            public void done(List<ParseObject> result, ParseException e) {
                System.out.println("done query set sts");
                // Query Successful
                if (e == null) {
                    // Set post status to Expired if current time passes due date.
                    for (ParseObject post : result) {
                        if (e == null) {
                            System.out.println("search in if" );
                            long dueDateMillis = post.getDate("DueDate").getTime();
                            long curDateMillis = System.currentTimeMillis();
                            long timeDiff = dueDateMillis - curDateMillis;
                            if (timeDiff <= 0) {
                                System.out.println("Title: " + post.getString("Title"));
                                post.put("PostStatus", PostStatus.POST_EXPIRED);
                                post.saveInBackground(new SaveCallback() {
                                    public void done(ParseException e) {
                                        setProgressBarIndeterminateVisibility(false);
                                        if (e == null) {
                                            System.out.println("post status updated");
                                        } else {
                                            System.out.println("post status update failed");
                                        }
                                    }
                                });
                            }
                        };
                    }
                } else { // Query Unsuccessful
                    Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void searchPostsInParse() {
        // Setup main query for search in Parse databse
        ParseQuery<ParseObject> mainQuery;

        // Setup or condition for post title
        ParseQuery<ParseObject> titleCaseSensitiveQuery = ParseQuery.getQuery("Posts");
        titleCaseSensitiveQuery.whereContains("Title", postsearchList);

        ParseQuery<ParseObject> titleCaseInsenseQuery = ParseQuery.getQuery("Posts");
        titleCaseInsenseQuery.whereContains("Title", postsearchList.toLowerCase());

        // Setup or condition for post description
        ParseQuery<ParseObject> DescripCaseSensitiveQuery = ParseQuery.getQuery("Posts");
        DescripCaseSensitiveQuery.whereContains("Description", postsearchList);

        ParseQuery<ParseObject> DescripCaseInsenseQuery = ParseQuery.getQuery("Posts");
        DescripCaseInsenseQuery.whereContains("Description", postsearchList.toLowerCase());

        // Combine or conditions into main query
        List<ParseQuery<ParseObject>> orQueries = new ArrayList<>();
        orQueries.add(titleCaseSensitiveQuery);
        orQueries.add(DescripCaseSensitiveQuery);
        orQueries.add(titleCaseInsenseQuery);
        orQueries.add(DescripCaseInsenseQuery);

        mainQuery = ParseQuery.or(orQueries);
        mainQuery.whereEqualTo("PostStatus", PostStatus.POST_OPEN);
        // Run the query
        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            @SuppressWarnings("unchecked")
            @Override
            // Query finished
            public void done(List<ParseObject> result, ParseException e) {
                System.out.println("done query");

                // Query Successful
                if (e == null) {

                    // Clear posts and markers local data
                    posts.clear();

                    // Temporary variables
                    Posts postsTemp;
                    String[] offersTemp = new String[PifApplication.OFFERS_MAX];

                    //If result list is empty, print a Toast Message to user.
                    if (result.size() == 0) {
                        Toast.makeText(getApplicationContext(), "No Posts Found With Given Keyword.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Extract data from Parse
                        for (ParseObject post : result) {

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

                            // populate local posts data
                            posts.add(postsTemp);

                            System.out.println("hit: " + postsTemp.getTitle() + " " + postsTemp.getDescription() + " " + postsTemp.getId());
                        }
                    }

                    // Update adapter
                    ((ArrayAdapter<Posts>) pinsListView.getAdapter()).notifyDataSetChanged();
                }

                // Query Unsuccessful
                else {
                    Toast.makeText(getApplicationContext(), "Query Failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void initializePostsInParse() {
        // Setup main query for search in Parse databse
        ParseQuery<ParseObject> mainQuery = ParseQuery.getQuery("Posts");

        mainQuery.whereEqualTo("PostStatus", PostStatus.POST_OPEN);

        // Run the query
        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            @SuppressWarnings("unchecked")
            @Override
            // Query finished
            public void done(List<ParseObject> result, ParseException e) {
                System.out.println("done query");

                // Query Successful
                if (e == null) {

                    // Clear posts and markers local data
                    posts.clear();

                    // Temporary variables
                    Posts postsTemp;
                    String[] offersTemp = new String[PifApplication.OFFERS_MAX];

                    //If result list is empty, print a Toast Message to user.
                    if (result.size() == 0) {
                        Toast.makeText(getApplicationContext(), "No Posts Found With Given Keyword.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        // Extract data from Parse
                        for (ParseObject post : result) {

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

                            // populate local posts data
                            posts.add(postsTemp);

                            System.out.println("hit: " + postsTemp.getTitle() + " " + postsTemp.getDescription() + " " + postsTemp.getId());
                        }
                    }

                    // Update adapter
                    ((ArrayAdapter<Posts>) pinsListView.getAdapter()).notifyDataSetChanged();
                }

                // Query Unsuccessful
                else {
                    Toast.makeText(getApplicationContext(), "Query Failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /*
        Helper Functions
     */

    public String[] convertJSONArrayToStringArray(JSONArray src) throws JSONException {
        // Check that there are offers.
        if (src == null)
            return null;

        String[] dest = new String[PifApplication.OFFERS_MAX];

        for (int i = 0; i < src.length() && i < PifApplication.OFFERS_MAX; i++)
            dest[i] = src.getString(i);

        return dest;
    }

    /*
        Activity Loaders
     */

    private void loadLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void loadViewPostActivity(Posts mapPost) {
        Intent intent = new Intent(this, ViewPostActivity.class);
        intent.putExtra(PifApplication.INTENT_EXTRA_MY_POSTOBJECT, mapPost);

        startActivity(intent);
    }

    private void loadSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra(PifApplication.INTENT_EXTRA_SEARCH_POSTLIST, posts);

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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
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

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Search Page", // TODO: Define a title for the content shown.
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
                "Search Page", // TODO: Define a title for the content shown.
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

}

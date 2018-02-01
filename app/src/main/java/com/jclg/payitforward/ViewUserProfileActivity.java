package com.jclg.payitforward;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

/**
 * Created by xinlan on 1/30/2016.
 */
public class ViewUserProfileActivity extends AppCompatActivity {

    public static class ListUtils extends ManagePostsActivity {
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

    // Toolbar
    private Toolbar myToolbar;

    // Parse User object
    private ImageView imageView;

    private String profile_username;

    /* Rating Bar */
    private RatingBar ratingbar_Rating;

    /* User About */
    private TextView textView_username;
    private TextView textview_userAbout;

    /* View all reviews button */
    private ListView listView_viewReviews;
    private Button button_viewReviews;

    private List<String> titleList = new ArrayList<>();
    private List<String> nameList = new ArrayList<>();
    private List<String> descriptionList = new ArrayList<>();
    private List<Float> ratingList = new ArrayList<>();

    private ParseFile file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_user_profile);

        myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        Intent intent = this.getIntent();

        profile_username = intent.getStringExtra("Uid");

        imageView = (ImageView) findViewById(R.id.userProfile_imageView_userpic);

        textView_username = (TextView) findViewById(R.id.userProfile_textView_username);

        /* Rating Bar */
        ratingbar_Rating = (RatingBar)findViewById(R.id.userProfile_ratingBar_starRating);

        /* Rating Bar */
        textview_userAbout = (TextView) findViewById(R.id.userProfile_textView_userAbout);

        /* View all reviews */
        listView_viewReviews = (ListView)findViewById(R.id.userProfile_listView_userReview);
        ReviewAdapter reviewAdapter = new ReviewAdapter(this, titleList, nameList, descriptionList, ratingList);
        listView_viewReviews.setAdapter(reviewAdapter);

        // Setup view user profile
        loadProfileData();

        // View all reviews on click
        button_viewReviews = (Button)findViewById(R.id.userProfile_button_viewReviews);
        button_viewReviews.setVisibility(GONE);

        button_viewReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(userProfileActivity.this, EditProfileActivity.class);
//                intent.putExtra("myName", currentUser.getUsername());
//                startActivity(intent);
            }
        });
    }

        /*
        Parse Functions
     */

    private void getUserReviewsInParse(){
        // Setup query for search in Parse databse
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Review");
        query.whereEqualTo("reviewee", profile_username);

        // Run the query
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            // Query finished
            public void done(List<ParseObject> result, ParseException e) {
                // Query Successful
                if (e == null) {
                    System.out.println("ViewUserProfile::getUserReviewsInParse - query success");

                    float sum = 0f;
                    int numReviews = 0;
                    Review reviewTemp;

                    // Extract data from Parse
                    for (ParseObject review : result) {

                        // Create the new post to place onto the map
                        reviewTemp = new Review(
                                review.getObjectId(),
                                review.getString("author"),
                                review.getString("title"),
                                review.getString("description"),
                                review.getBoolean("complete"),
                                ((float) review.getInt("rating")),
                                review.getString("reviewer"),
                                review.getString("reviewee")
                        );

                        titleList.add(reviewTemp.getTitle());
                        nameList.add(reviewTemp.getReviewer());
                        descriptionList.add(reviewTemp.getDescription());
                        ratingList.add(reviewTemp.getRating()/2);

                        sum += reviewTemp.getRating()/2;
                        numReviews++;

                        ((ReviewAdapter) listView_viewReviews.getAdapter()).notifyDataSetChanged();
                        ListUtils.setDynamicHeight(listView_viewReviews);

                        System.out.println("hit: " + reviewTemp.getTitle() + " " + reviewTemp.getDescription() + " " + reviewTemp.getId());
                    }

                    ratingbar_Rating.setRating(sum / numReviews);

//                    ((ReviewAdapter) listView_viewReviews.getAdapter()).notifyDataSetChanged();
//                    ListUtils.setDynamicHeight(listView_viewReviews);
                }
                // Query Unsuccessful
                else {
                    System.out.println("ViewUserProfile::getUserReviewsInParse - query fail");

                    Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void searchUserProfileInParse(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Profile");
        query.whereContains("objectId", profile_username);

        query.findInBackground(new FindCallback<ParseObject>() {
            @SuppressWarnings("unchecked")
            @Override
            public void done(List<ParseObject> result, ParseException e) {
                if (e == null) {
                    System.out.println("ViewUserProfile::searchUserProfileInParse - query success");

                    for (ParseObject profile : result) {
                        System.out.println("name: " + profile.getString("username"));
                        textView_username.setText(profile.getString("username"));

                        System.out.println("description: " + profile.getString("bibliography"));
                        textview_userAbout.setText(profile.getString("bibliography"));

                        file = (ParseFile) profile.get("profilePicture");
                        System.out.println("file: " + file);

                        file.getDataInBackground(new GetDataCallback() {
                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    //Log.d("test", "We've got data in data.");
                                    // Decode the Byte[] into Bitmap
                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    System.out.println("bitmap: " + bmp);
                                    int x = 10000;
                                    while (x-- > 0) ;
                                    imageView.setImageBitmap(bmp);
                                } else {
                                    Log.d("test", "There was a problem downloading the data.");
                                }
                            }
                        });
                    }
                } else {
                    System.out.println("ViewUserProfile::searchUserProfileInParse - query fail");
                    Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
        Helper Functions
     */

    private void loadProfileData(){
        searchUserProfileInParse();
        getUserReviewsInParse();
        ratingbar_Rating.setStepSize(0.1f);
    }

    private void clearProfileData(){
        textView_username.setText("");
        textview_userAbout.setText("");

        titleList.clear();
        nameList.clear();
        descriptionList.clear();
        ratingList.clear();

        ((ReviewAdapter) listView_viewReviews.getAdapter()).notifyDataSetChanged();
    }

    /*
        Activity Loaders
     */

    private void loadLoginView() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void loadEditProfileView() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
    }

    /*
        Toolbar Overflow Menu
     */
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
                clearProfileData();
                loadProfileData();
                break;
            }

            case R.id.action_logout:
                ParseUser.logOut();
                loadLoginView();
                break;

            case R.id.action_viewProfile: {
                loadEditProfileView();
                break;
            }

//            case R.id.action_settings: {
//                // Do something when profile_username selects Settings from Action Bar overlay
//                break;
//            }
        }

        return super.onOptionsItemSelected(item);
    }
}

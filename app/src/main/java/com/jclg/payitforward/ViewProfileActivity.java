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
public class ViewProfileActivity extends AppCompatActivity {

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
    private ParseUser currentUser;

    private Button button_edit;

//    private ListView listView_profile;
    private TextView textView_username;

    private ImageView imageView;

    /* Rating Bar */
    private RatingBar ratingbar_Rating;

    /* User About */
    private TextView textview_userAbout;
    private String description;

    /* View all reviews */
    private ListView listView_viewReviews;
    private Button button_viewReviews;

    private List<String> titleList = new ArrayList<>();
    private List<String> nameList = new ArrayList<>();
    private List<String> reviewList = new ArrayList<>();
    private List<Float> ratingList = new ArrayList<>();

    private ParseFile file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_profile);

        myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        currentUser = ParseUser.getCurrentUser();

        button_edit = (Button)findViewById(R.id.viewProfile_button_edit);

        imageView = (ImageView) findViewById(R.id.viewProfile_imageView_userpic);

        textView_username = (TextView) findViewById(R.id.viewProfile_textView_username);


        /* Rating Bar */
        ratingbar_Rating = (RatingBar)findViewById(R.id.viewProfile_ratingBar_starRating);

        /* Rating Bar */
        textview_userAbout = (TextView) findViewById(R.id.viewProfile_textView_userAbout);

        /* View all reviews */
        listView_viewReviews = (ListView)findViewById(R.id.viewProfile_listView_userReview);

        ReviewAdapter reviewAdapter = new ReviewAdapter(this, titleList, nameList, reviewList, ratingList);
        listView_viewReviews.setAdapter(reviewAdapter);

        button_viewReviews = (Button)findViewById(R.id.viewProfile_button_viewReviews);
        button_viewReviews.setVisibility(GONE);

        // Load profile data
        loadProfileData();

        button_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("myName", currentUser.getUsername());
                intent.putExtra("myEmail", currentUser.getEmail());
                startActivity(intent);
            }
        });

        // View all reviews on click
        button_viewReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(ViewProfileActivity.this, EditProfileActivity.class);
//                intent.putExtra("myName", currentUser.getUsername());
//                startActivity(intent);
            }
        });

    }


    private void loadProfileData(){
        textView_username.setText(currentUser.getUsername());
        getUserProfileInParse();
        getUserReviewsInParse();
        ratingbar_Rating.setStepSize(0.1f);
    }

    private void clearProfileData(){
        // Clear username and description
        textView_username.setText("");
        textview_userAbout.setText("");

        // Clear review list information
        titleList.clear();
        nameList.clear();
        reviewList.clear();
        ratingList.clear();

        ((ReviewAdapter) listView_viewReviews.getAdapter()).notifyDataSetChanged();
    }

    /*
        Parse Functions
     */

    private void getUserProfileInParse(){
        // Profile ID string
        String profileId = currentUser.getParseObject("profileId").getObjectId();

        // Setup query for search in Profile database
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Profile");
        query.whereEqualTo("objectId", profileId);

        query.findInBackground(new FindCallback<ParseObject>() {
            @SuppressWarnings("unchecked")
            @Override
            public void done(List<ParseObject> result, ParseException e) {
                if (e == null) {
                    System.out.println("ViewProfile - query successful");

                    for (ParseObject profile : result) {
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
                    System.out.println("ViewProfile - query fail");

                    Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getUserReviewsInParse(){
        // ProfileId string
        String profileId = currentUser.getParseObject("profileId").getObjectId();

        // Setup query for search in Parse databse
        ParseQuery<ParseObject> query_profile = ParseQuery.getQuery("Review");
        query_profile.whereEqualTo("reviewee", profileId);

        // Run the query
        query_profile.findInBackground(new FindCallback<ParseObject>() {
            @Override
            // Query finished
            public void done(List<ParseObject> result, ParseException e) {
                System.out.println("View Profile - find reviews: done query");

                // Query Successful
                if (e == null) {
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
                        reviewList.add(reviewTemp.getDescription());
                        ratingList.add(reviewTemp.getRating() / 2);

                        sum += reviewTemp.getRating() / 2;
                        numReviews++;

                        System.out.println("hit: " + reviewTemp.getTitle() + " " + reviewTemp.getDescription() + " " + reviewTemp.getId());
                    }

                    ((ReviewAdapter) listView_viewReviews.getAdapter()).notifyDataSetChanged();
                    ListUtils.setDynamicHeight(listView_viewReviews);

                    ratingbar_Rating.setRating(sum / numReviews);
                }
                // Query Unsuccessful
                else {
                    System.out.println("View Profile: fail");
                }
            }
        });
    }

    private void loadLoginView() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_profile, menu);
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
                //loadProfileData();
                break;
            }

            case R.id.action_logout:
                ParseUser.logOut();
                loadLoginView();
                break;

//            case R.id.action_settings: {
//                // Do something when user selects Settings from Action Bar overlay
//                break;
//            }
        }

        return super.onOptionsItemSelected(item);
    }
}

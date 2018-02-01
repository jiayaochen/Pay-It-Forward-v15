package com.jclg.payitforward;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by Jeff on 2/20/2016.
 */
public class ReviewActivity extends AppCompatActivity {

    /* Toolbar */
    private Toolbar myToolbar;

    /* The Post being viewed */
    private Posts viewPostObject;

    /* Rating Bar */
    private RatingBar ratingbar_Rate;

    private float float_Rating;

    /* Review Content */
    private EditText editText_Content;

    private String string_Content;

    /* Submit Button */
    private Button button_Review;

    /* Other Review Fields */
    private String string_Author;
    private String string_Reviewer;
    private String string_Reviewee;
    private String string_Title;
    private boolean boolean_Status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_review);

         /* Toolbar */
        myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        /* The Intent Extras */
        // Setup intent and obtain extras
        Intent intent = this.getIntent();
        viewPostObject = intent.getParcelableExtra(PifApplication.INTENT_EXTRA_MY_POSTOBJECT);

        /* Rating Bar */
        ratingbar_Rate = (RatingBar)findViewById(R.id.review_ratingBar_starRating);

        /* Submit Button */
        button_Review = (Button)findViewById(R.id.review_button_submitRating);

        /* Review Content */
        editText_Content = (EditText)findViewById(R.id.review_editText_content);

        /* Setup OnClickListeners */

        // Setup rate button listener
        button_Review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
            setupReview();
            submitReviewInParse();

            finish();
            }
        });
    }

    /*
        Setup Functions
     */

    private void setupReview() {
        float_Rating = ratingbar_Rate.getRating();
        string_Content = editText_Content.getText().toString();

        string_Author = viewPostObject.getRequester();
        string_Title = viewPostObject.getTitle();
        boolean_Status = (viewPostObject.getPostStatus() == PostStatus.POST_COMPLETE);

        // If current reviewer is the requester
        String profileId = ParseUser.getCurrentUser().getParseObject("profileId").getObjectId();

        if (viewPostObject.getRequester().equals(profileId)) {
            string_Reviewer = viewPostObject.getRequester();
            string_Reviewee = viewPostObject.getTasker();

            viewPostObject.setRequester_Flag(true);
            updatePostInParse(true);
        }

        // Else it is the tasker
        else {
            string_Reviewer = viewPostObject.getTasker();
            string_Reviewee = viewPostObject.getRequester();

            viewPostObject.setTasker_Flag(true);
            updatePostInParse(false);
        }
    }

    /*
        Parse Functions
     */

    private void submitReviewInParse() {
        // create new review
        final ParseObject post = new ParseObject("Review");

        // About the post
        post.put("author", string_Author);

        post.put("title", string_Title);
        post.put("complete", boolean_Status);

        // About the review
        post.put("rating", (float_Rating*2));
        post.put("description", string_Content);

        post.put("reviewer", string_Reviewer);
        post.put("reviewee", string_Reviewee);

        post.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(), "Rated and Reviewed", Toast.LENGTH_SHORT).show();
                } else {
                    // The post failed.
                    Toast.makeText(getApplicationContext(), "Failed to Review", Toast.LENGTH_SHORT).show();
                    Log.d(getClass().getSimpleName(), "User update error: " + e);
                }
            }
        });

    }

    private void updatePostInParse(final boolean isRequester) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");

        // Retrieve the object by id
        query.getInBackground(viewPostObject.getId(), new GetCallback<ParseObject>() {
            public void done(ParseObject updatePost, ParseException e) {
                if (e == null) {
                    // Now let's update it with some new data. In this case we add a new user to the offers array.
                    if (isRequester)
                        updatePost.put("RequesterReview", true);
                    else
                        updatePost.put("TaskerReview", true);

                    updatePost.saveInBackground();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Task is No Longer Available!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
        Helper Functions
     */

    /*
        Activity Loaders
     */
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

}

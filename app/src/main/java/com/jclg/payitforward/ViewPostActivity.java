package com.jclg.payitforward;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.view.View.*;

/**
 * Created by graceg on 2016-01-11.
 */
public class ViewPostActivity extends AppCompatActivity {

    public static class ListUtils extends ViewPostActivity {
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

    /* Toolbar */
    private Toolbar myToolbar;

    /* The Post being viewed */
    private Posts viewPostObject;
    private String days_to_dueDate="0";
    private String hours_to_dueDate="0";

    /* Offers */
    private Button Offers_Button;
    private TextView Offers_TextView;
    private ListView Offers_ListView;

    // Offer parameters
    private ArrayList<String> Offer_Usernames = new ArrayList<String>();
    private boolean hasOffered = false;

    /* Assignee */
    private TextView Assignee_TextView;
    private ListView Assignee_ListView;

    // Assignee parameters
    private ArrayList<String> Assignee_Usernames = new ArrayList<String>();

    /* Comments */
    private Button Comments_Button;
    private TextView Comments_TextView;
    private ListView Comments_ListView;

    // Comment parameters
    private List<String> nameList = new ArrayList<>();
    private List<String> commentList = new ArrayList<>();

    /* Chat */
    private Button Chat_Button;

    /* Complete */
    private Button Complete_Button;

    /* InComplete */
    private Button InComplete_Button;

    /* Review */
    private Button Review_Button;

    // OnResume Flag
    private boolean isCreateDone = false;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_post);

        /* Toolbar */
        Toolbar myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        /* The Intent Extras */
        // Setup intent and obtain extras
        Intent intent = this.getIntent();
        viewPostObject = intent.getParcelableExtra(PifApplication.INTENT_EXTRA_MY_POSTOBJECT);

        /* Post Details */
        // Set post myTitle, myContent, myLocation, myDueDate
        ((TextView) findViewById(R.id.viewPost_textView_title)).setText(viewPostObject.getTitle());

        TextView viewpost_username = ((TextView) findViewById(R.id.viewPost_textView_user));
        findUsernameInParse(viewpost_username, viewPostObject.getRequester());

        ((TextView) findViewById(R.id.viewPost_textView_content)).setText(viewPostObject.getDescription());

        double postLatitude = viewPostObject.getLatitude();
        double postLongtitude = viewPostObject.getLongitude();
        String StreetAddress = getCompleteAddressString(postLatitude,postLongtitude);
        System.out.println("Post Object has latitude " + postLatitude + "and longtitude" + postLongtitude);
        ((TextView) findViewById(R.id.viewPost_textView_address)).setText(StreetAddress);

        /* Offers */
        Offers_Button = ((Button) findViewById(R.id.viewPost_button_offer));
        Offers_TextView = (TextView) findViewById(R.id.viewPost_textView_offers);
        Offers_ListView = (ListView) findViewById(R.id.viewPost_listView_offers);

        Offers_Button.setVisibility(GONE);
        Offers_TextView.setVisibility(GONE);
        Offers_ListView.setVisibility(GONE);

        /* Assignee */
        Assignee_TextView = (TextView) findViewById(R.id.viewPost_textView_assignee);
        Assignee_ListView = (ListView) findViewById(R.id.viewPost_listView_assignee);

        Assignee_TextView.setVisibility(GONE);
        Assignee_ListView.setVisibility(GONE);

        /* Comments */
        Comments_Button = (Button) findViewById(R.id.viewPost_button_comment);
        Comments_TextView = (TextView) findViewById(R.id.viewPost_textView_comments);
        Comments_ListView = (ListView)findViewById(R.id.viewPost_listView_comments);

        Comments_Button.setVisibility(GONE);
        Comments_TextView.setVisibility(GONE);
        Comments_ListView.setVisibility(GONE);

        /* Chat */
        Chat_Button = (Button) findViewById(R.id.viewPost_button_chat);

        Chat_Button.setVisibility(GONE);

        /* Complete */
        Complete_Button = (Button) findViewById(R.id.viewPost_button_complete);

        Complete_Button.setVisibility(GONE);

        /* InComplete */
        InComplete_Button = (Button) findViewById(R.id.viewPost_button_incomplete);

        InComplete_Button.setVisibility(GONE);

        /* Review */
        Review_Button = (Button) findViewById(R.id.viewPost_button_review);

        Review_Button.setVisibility(GONE);

        /* Setup Custom Adapters */

        // setup custom offers adapter
        OffersAdapter offersAdapter = new OffersAdapter(this, Offer_Usernames, viewPostObject);
        Offers_ListView.setAdapter(offersAdapter);

        // setup custom assignee adapter
        OffersAdapter assigneeAdapter = new OffersAdapter(this, Assignee_Usernames, viewPostObject);
        Assignee_ListView.setAdapter(assigneeAdapter);

        // custom comments adapter
        CommentsAdapter commentsAdapter = new CommentsAdapter(this, nameList, commentList);
        Comments_ListView.setAdapter(commentsAdapter);

        /*
            Activity Building Functions
         */

        setupViewPost();

        /* Setup OnClickListeners */

        // Setup author profile on click listener
        viewpost_username.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUserProfileActivity();
            }
        });

        // Setup comments reply on click listeners
        Comments_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                TextView Comment_Name = (TextView) view.findViewById(R.id.list_Comment_Name);
                final String usernameText = Comment_Name.getText().toString();

                System.out.println("username in viewpost: " + usernameText);

                TextView comment_reply = (TextView) view.findViewById(R.id.list_Comment_reply);
                final String commenter_string = Comment_Name.getText().toString();

                System.out.println("username in viewpost: " + commenter_string);

                comment_reply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ViewPostActivity.this, CommentActivity.class);
                        intent.putExtra("Pid", viewPostObject.getId());
                        intent.putExtra("Uid", usernameText);
                        intent.putExtra(PifApplication.INTENT_EXTRA_MY_POSTOBJECT, viewPostObject);
                        startActivity(intent);
                    }
                });
            }
        });

        /* Buttons */

        // Setup Offer button listener
        Offers_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addOfferInParse();
                finish();
            }
        });

        // Setup Comment button listener
        Comments_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCommentActivity();
            }
        });

        // Setup Chat button listener
        Chat_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "We Be Chatting Now!", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup Complete button listener
        Complete_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close task as complete
                closeTaskInParse(PostStatus.POST_COMPLETE);
                finish();
            }
        });

        // Setup InComplete button listener
        InComplete_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close task as incomplete
                closeTaskInParse(PostStatus.POST_INCOMPLETE);
                finish();
            }
        });

        // Setup Review button listener
        Review_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close task as incomplete
                loadReviewActivity();
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onResume(){
        super.onResume();

        if (isCreateDone)
            setupViewPost();

        isCreateDone = true;
    }

    /*
        Activity Setup Functions
     */

    private void setupViewPost() {
        setupDueDate();

        switch (viewPostObject.getPostStatus()) {
            case PostStatus.POST_OPEN:
                // Show offers
                Offers_Button.setVisibility(VISIBLE);
                Offers_TextView.setVisibility(VISIBLE);
                Offers_ListView.setVisibility(VISIBLE);
                // Show comments
                Comments_Button.setVisibility(VISIBLE);
                Comments_ListView.setVisibility(VISIBLE);
                Comments_TextView.setVisibility(VISIBLE);

                setupOffers();
                setupComments();

                break;

            case PostStatus.POST_INPROGRESS:
                // TODO implement peer2peer chatting if possible
                Chat_Button.setVisibility(GONE);
                // Show complete button
                Complete_Button.setVisibility(VISIBLE);
                // Show incomplete button
                InComplete_Button.setVisibility(VISIBLE);
                // Show assignee
                Assignee_ListView.setVisibility(VISIBLE);
                Assignee_TextView.setVisibility(VISIBLE);

                setupChat();
                setupClose();
                setupAssignee();

                break;

            case PostStatus.POST_COMPLETE:
            case PostStatus.POST_INCOMPLETE:
                // Show review
                Review_Button.setVisibility(VISIBLE);

                setupReview();

                break;

            case PostStatus.POST_EXPIRED:
                // Show review as a dumby button to show post has expired
                Review_Button.setVisibility(VISIBLE);

                Review_Button.setEnabled(false);
                Review_Button.setBackgroundColor(0xffcccccc); // grey out offer option
                Review_Button.setText("Task Expired!");

                break;

            default:
        }
    }

    private void setupDueDate() {
        // Retrieve the object by id
        Date time_remain = viewPostObject.getDueDate();
        System.out.println("the Post's Due Date is: " + viewPostObject.getDueDate());

        long timeDiff;
        long dueDateMillis = time_remain.getTime();
        long curDateMillis = System.currentTimeMillis();
        timeDiff = dueDateMillis - curDateMillis;

        if(timeDiff<=0 && viewPostObject.getPostStatus() == PostStatus.POST_OPEN){
            viewPostObject.setPostStatus(PostStatus.POST_EXPIRED);
            System.out.println("The time Difference is <= 0");
        }

        int iDays = (int)(Math.abs(timeDiff) / (1000*60*60*24));
        System.out.println("INT: " + iDays);
        double dDay = (double) Math.abs(timeDiff) / (1000*60*60*24);
        System.out.println("DOUBLE: " + dDay);
        double hours = dDay - iDays;
        System.out.println("DIFFERENCE: " + hours);

        days_to_dueDate = String.valueOf(Math.round(Math.abs(timeDiff) /(1000*60*60*24)));
        hours_to_dueDate = String.valueOf(Math.round(hours * 24));

        System.out.println("days to due date: " + days_to_dueDate);
        System.out.println("hours to due date: " + hours_to_dueDate);

        if(Integer.valueOf(days_to_dueDate) > 0  && Integer.valueOf(hours_to_dueDate) >0){
            if(timeDiff < 0) {
                ((TextView) findViewById(R.id.viewPost_textView_time_left)).setText("Due: " + days_to_dueDate + " day(s) and " + hours_to_dueDate + " hour(s) ago");
            }
            else {
                ((TextView) findViewById(R.id.viewPost_textView_time_left)).setText("Due: " + days_to_dueDate + " day(s) and " + hours_to_dueDate + " hour(s) from now");
            }

        }
        else if(Integer.valueOf(days_to_dueDate) == 0  && Integer.valueOf(hours_to_dueDate) > 0){
            if(timeDiff < 0) {
                ((TextView) findViewById(R.id.viewPost_textView_time_left)).setText("Due: " + hours_to_dueDate + " hour(s) ago");
            }
            else {
                ((TextView) findViewById(R.id.viewPost_textView_time_left)).setText("Due: " + hours_to_dueDate + " hour(s) from now");
            }

        }
        else if(Integer.valueOf(days_to_dueDate) > 0  && Integer.valueOf(hours_to_dueDate) ==  0){
            if(timeDiff < 0) {
                ((TextView) findViewById(R.id.viewPost_textView_time_left)).setText("Due: " + days_to_dueDate + " day(s) ago");
            }
            else {
                ((TextView) findViewById(R.id.viewPost_textView_time_left)).setText("Due: " + days_to_dueDate + " day(s) from now");
            }
        }
        else {
            ((TextView) findViewById(R.id.viewPost_textView_time_left)).setText("Due: now");
        }
    }

    private void setupOffers() {
        hasOffered = false;

        String profileId = ParseUser.getCurrentUser().getParseObject("profileId").getObjectId();

        // Current user is the Requester
        if (viewPostObject.getRequester().equals(profileId)) {
            // Get a list of all offer usernames
            populateOffersList();

            // Immediately disable offer button
            Offers_Button.setVisibility(GONE);
            Offers_Button.setEnabled(false);
            Offers_Button.setBackgroundColor(0xffcccccc); // grey out offer option
            Offers_Button.setText("");
        }

        // Current user is a Tasker
        else {
            // Get a list of all offer usernames
            populateOffersList();

            // If user has already offered to help, disable offer button
            if (hasOffered) {
                Offers_Button.setEnabled(false);
                Offers_Button.setBackgroundColor(0xffcccccc); // grey out offer option
                Offers_Button.setText("Offer Request Sent!");
            }
        }

        Offers_TextView.setText("OFFERS (" + viewPostObject.getOffersNums() + ")");

        ((OffersAdapter) Offers_ListView.getAdapter()).notifyDataSetChanged();
        ListUtils.setDynamicHeight(Offers_ListView);
    }

    private void setupAssignee() {
        Assignee_Usernames.add(viewPostObject.getTasker());

        ((OffersAdapter) Assignee_ListView.getAdapter()).notifyDataSetChanged();
        ListUtils.setDynamicHeight(Assignee_ListView);
    }

    private void setupComments() {
        searchCommentsInParse();
    }

    private void setupChat() {
        return;
    }

    private void setupClose() {
        String profileId = ParseUser.getCurrentUser().getParseObject("profileId").getObjectId();

        // Current user is not the Author
        if (!viewPostObject.getRequester().equals(profileId)) {
            // Do not Show complete
            Complete_Button.setVisibility(GONE);
            // Do not Show incomplete
            InComplete_Button.setVisibility(GONE);
        }
    }

    private void setupReview() {
        String profileId = ParseUser.getCurrentUser().getParseObject("profileId").getObjectId();

        // If current user is the requester
        if (viewPostObject.getRequester().equals(profileId)) {
            if (viewPostObject.getRequester_Flag()) {
                Review_Button.setEnabled(false);
                Review_Button.setBackgroundColor(0xffcccccc); // grey out offer option
                Review_Button.setText("Thank You for Rating and Reviewing!");
            }
        }
        // Else it is the tasker
        else {
            if (viewPostObject.getTasker_Flag()) {
                Review_Button.setEnabled(false);
                Review_Button.setBackgroundColor(0xffcccccc); // grey out offer option
                Review_Button.setText("Thank You for Rating and Reviewing!");
            }
        }
    }

    /*
        Parse Functions
     */

    private void addOfferInParse() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");

        final String profileId = ParseUser.getCurrentUser().getParseObject("profileId").getObjectId();

        // Retrieve the object by id
        query.getInBackground(viewPostObject.getId(), new GetCallback<ParseObject>() {
            public void done(ParseObject updatePost, ParseException e) {
                if (e == null) {
                    int update_OffersNum = updatePost.getInt("OffersNum") + 1;

                    // Now let's update it with some new data. In this case we add a new user to the
                    // offers array.
                    updatePost.put("OffersNum", update_OffersNum);
                    updatePost.add("Offers", profileId);
                    updatePost.saveInBackground();

                    Toast.makeText(getApplicationContext(), "Offer Made!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Task Is No Longer Available!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void searchCommentsInParse(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Comment");

        query.whereContains("postId", viewPostObject.getId());

        query.findInBackground(new FindCallback<ParseObject>() {
            @SuppressWarnings("unchecked")
            @Override
            public void done(List<ParseObject> result, ParseException e) {
                System.out.println("done query");
                if (e == null) {

                    nameList.clear();
                    commentList.clear();
                    //imgList.clear();

                    int count = 0;

                    for (ParseObject commentRt : result) {

                        //ParseUser user = commentRt.getParseUser("userId");

                        System.out.println("New comment: " + count + " " + commentRt.getString("username"));

                        nameList.add(commentRt.getString("profileId"));

                        commentList.add(commentRt.getString("description"));

                        //getUserPictureInParse(user);

                        System.out.println("New Comment: " + commentRt.getString("description"));
                    }

                    ((CommentsAdapter) Comments_ListView.getAdapter()).notifyDataSetChanged();
                    ListUtils.setDynamicHeight(Comments_ListView);

                    Comments_TextView.setText("COMMENTS (" + Comments_ListView.getAdapter().getCount() + ")");
                } else {
                    Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void closeTaskInParse(final int postStatus) {
        // In this context postStatus will either be COMPLETE or INCOMPLETE
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Posts");

        // Retrieve the object by id
        query.getInBackground(viewPostObject.getId(), new GetCallback<ParseObject>() {
            public void done(ParseObject updatePost, ParseException e) {
                if (e == null) {
                    // Now let's update it with some new data. In this case we add a new user to the
                    // offers array.
                    updatePost.put("PostStatus", postStatus);
                    updatePost.saveInBackground();

                    if (postStatus == PostStatus.POST_COMPLETE)
                        Toast.makeText(getApplicationContext(), "Task Completed", Toast.LENGTH_SHORT).show();
                    else if (postStatus == PostStatus.POST_INCOMPLETE)
                        Toast.makeText(getApplicationContext(), "Task Incomplete", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), "Invalid Input", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void findUsernameInParse(final TextView TextView_username, String profileId_username){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Profile");

        // Retrieve the object by id
        query.getInBackground(profileId_username, new GetCallback<ParseObject>() {
            public void done(ParseObject getPost, ParseException e) {
                if (e == null) {
                    TextView_username.setText("Posted by " + getPost.getString("username"));

                    System.out.println("ViewPostActivity: Username acquired");
                }
                else
                    System.out.println("ViewPostActivity: Failed to get username");
            }
        });
    }

    /*
        Helper Functions
     */

    private void populateOffersList () {

        // Clear old list
        Offer_Usernames.clear();

        // Check if there are offers for the post
        if (viewPostObject.getOffers() != null) {

            // Fill list of offers
            for (int i=0 ; i<PifApplication.OFFERS_MAX ; i++) {
                if ((viewPostObject.getOffers())[i] == null)
                    break;

                // This list of strings will be used to construct the offers Listview
                Offer_Usernames.add((viewPostObject.getOffers())[i]);

                // Check that current user has already offered
                String profileId = ParseUser.getCurrentUser().getParseObject("profileId").getObjectId();

                if ((viewPostObject.getOffers())[i].equals(profileId))
                    hasOffered = true;
            }
        }
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                System.out.println("My Current location address: " + strReturnedAddress.toString());
            } else {
                System.out.println("My Current loction address: No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("My Current loction address: Can't get Address!");
        }
        return strAdd;
    }

    /*
        Activity Loaders
     */

    private void loadUserProfileActivity() {
        Intent intent = new Intent(this, ViewUserProfileActivity.class);
        intent.putExtra("Uid", viewPostObject.getRequester());

        startActivity(intent);
    }

    private void loadReviewActivity() {
        int result = 0;

        Intent intent = new Intent(this, ReviewActivity.class);
        intent.putExtra(PifApplication.INTENT_EXTRA_MY_POSTOBJECT, viewPostObject);
        startActivityForResult(intent, result);
    }

    private void loadCommentActivity() {
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra("Pid", viewPostObject.getId());
        intent.putExtra(PifApplication.INTENT_EXTRA_MY_POSTOBJECT, viewPostObject);
        startActivity(intent);
    }

    private void loadLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
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
                setupViewPost();
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

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "ViewPost Page", // TODO: Define a title for the content shown.
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
                "ViewPost Page", // TODO: Define a title for the content shown.
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

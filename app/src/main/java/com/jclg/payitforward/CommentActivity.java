package com.jclg.payitforward;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by xinlan on 2/17/2016.
 */
public class CommentActivity extends AppCompatActivity {

    // Toolbar
    private Toolbar myToolbar;

    private String Pid;
    private Posts post;

    private TextView text;

    private EditText commentText;

    private Button submitCommentButton;

    private String comments;

    private String Cusername = null;

    ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment);

        myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        text = (TextView) findViewById(R.id.PostTitle);
        commentText = (EditText) findViewById(R.id.Add_Comment);

        currentUser = ParseUser.getCurrentUser();

        Intent intent = this.getIntent();

        if (intent.getExtras() != null) {
            Pid = intent.getStringExtra("Pid");
            Cusername = intent.getStringExtra("Uid");
            post = intent.getParcelableExtra(PifApplication.INTENT_EXTRA_MY_POSTOBJECT);
        }

        if(Cusername!=null)
            commentText.setHint("Reply to " + Cusername);

        text.setText(post.getTitle());

        submitCommentButton = (Button) findViewById(R.id.Submit_Comment_Button);
        submitCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comments = commentText.getText().toString();
                comments = comments.trim();

                if(Cusername!=null){
                    comments = "Re - " + Cusername +": " + comments;
                }

                if (!comments.isEmpty()){
                    AddComment(comments);
                }
                else
                    System.out.println("nothing entered...");
            }
        });
    }

    /*
        Parse Functions
     */

    void AddComment (String comments){
        String profileId = ParseUser.getCurrentUser().getParseObject("profileId").getObjectId();

        final ParseObject comment = new ParseObject("Comment");
        comment.put("postId", Pid);
        comment.put("description", comments);
        comment.put("profileId", profileId);
        comment.put("username", currentUser.getUsername());

        comment.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(), "Submitted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    // The post failed.
                    Toast.makeText(getApplicationContext(), "Failed to Submit! Try again!", Toast.LENGTH_SHORT).show();
                    Log.d(getClass().getSimpleName(), "User update error: " + e);
                }
            }
        });
    }

    /*
        Activity Loaders
     */

    private void loadLoginView() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void loadProfileView() {
        Intent intent = new Intent(this, ViewProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void loadEditProfileView() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
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
                loadLoginView();
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

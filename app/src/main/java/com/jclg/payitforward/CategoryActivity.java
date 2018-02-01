package com.jclg.payitforward;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import com.parse.ParseUser;

/**
 * Created by graceg on 2016-03-26.
 */
public class CategoryActivity extends AppCompatActivity {

    /* Toolbar */
    private Toolbar myToolbar;

    /* Category Banners in ImageViews */
    private ImageView imageView_clubs;
    private ImageView imageView_courses;
    private ImageView imageView_schoolLife;
    private ImageView imageView_other;

    // Extras
    private int ExtraCategory;
    private Location ExtraLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_category);

         /* Toolbar */
        myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

         /* Extras */
        Intent intent = this.getIntent();
        ExtraLocation = (Location) intent.getParcelableExtra(PifApplication.INTENT_EXTRA_LOCATION);

        /* Category Banners in ImageViews */
        imageView_clubs = (ImageView) findViewById(R.id.category_imageView_clubs);
        imageView_courses = (ImageView) findViewById(R.id.category_imageView_courses);
        imageView_schoolLife = (ImageView) findViewById(R.id.category_imageView_schoolLife);
        imageView_other = (ImageView) findViewById(R.id.category_imageView_other);

        setupImageViews();

        /* Setup OnClick Listeners */

        // Clubs onclick listener
        imageView_clubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExtraCategory = PostCategory.POST_CLUBS;

                loadEditPostActivity();
            }
        });

        // Courses onclick listener
        imageView_courses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExtraCategory = PostCategory.POST_COURSES;

                loadEditPostActivity();
            }
        });

        // School Life onclick listener
        imageView_schoolLife.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExtraCategory = PostCategory.POST_SCHOOLLIFE;

                loadEditPostActivity();
            }
        });

        // Other onclick listener
        imageView_other.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExtraCategory = PostCategory.POST_OTHER;

                loadEditPostActivity();
            }
        });

    }

    /*
        Setup Functions
     */

    private void setupImageViews(){
        Bitmap Bitmap_clubs = BitmapFactory.decodeResource(this.getResources(), R.drawable.app_club);
        Bitmap Bitmap_courses = BitmapFactory.decodeResource(this.getResources(), R.drawable.app_course);
        Bitmap Bitmap_schoolLife = BitmapFactory.decodeResource(this.getResources(), R.drawable.app_student_life);
        Bitmap Bitmap_other = BitmapFactory.decodeResource(this.getResources(), R.drawable.app_other);

        imageView_clubs.setImageBitmap(Bitmap_clubs);
        imageView_courses.setImageBitmap(Bitmap_courses);
        imageView_schoolLife.setImageBitmap(Bitmap_schoolLife);
        imageView_other.setImageBitmap(Bitmap_other);
    }

    /*
        Helper Functions
     */


    /*
        Activity Loaders
     */
    private void loadEditPostActivity() {
        Intent intent = new Intent(this, EditPostActivity.class);

        intent.putExtra(PifApplication.INTENT_EXTRA_LOCATION, ExtraLocation);
        intent.putExtra("INTENT_EXTRA_CATEGORY", ExtraCategory);

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

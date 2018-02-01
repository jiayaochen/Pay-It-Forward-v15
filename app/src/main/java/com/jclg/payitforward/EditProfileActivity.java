package com.jclg.payitforward;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.jclg.payitforward.LoginActivity;
import com.jclg.payitforward.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by xinlan on 1/30/2016.
 */
public class EditProfileActivity extends AppCompatActivity {

    private Toolbar myToolbar;

    private ParseUser currentUser;

    private Button button_save;
    private Button button_select_photo;
//    private Button button_upload_photo;

    private EditText UserNameE;
    private EditText emailE;
    private EditText passwordE;
    private EditText aboutE;

    public  ImageView imageView;
    private ParseFile file;
    private String profileId;
    private String profileAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);

        myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        currentUser = ParseUser.getCurrentUser();
        Intent intent = this.getIntent();

        button_save = (Button)findViewById(R.id.editProfile_button_save);
        button_select_photo = (Button)findViewById(R.id.editProfile_button_select_photo);
//        button_upload_photo = (Button)findViewById(R.id.editProfile_button_upload_photo);

        UserNameE =  (EditText) findViewById(R.id.editProfile_editText_username);
        emailE = (EditText) findViewById(R.id.editProfile_editText_email);
        passwordE = (EditText) findViewById(R.id.editProfile_editText_password);
        aboutE = (EditText) findViewById(R.id.editProfile_editText_about);

        if (intent.getExtras() != null) {
            UserNameE.setText(intent.getStringExtra("myName"));
            emailE.setText(intent.getStringExtra("myEmail"));
        }
        else {
            UserNameE.setText(currentUser.getUsername());
            emailE.setText(currentUser.getEmail());
        }
        getProfileId();

        imageView = (ImageView) findViewById(R.id.editProfile_imageView_user);

//                        // TODO Auto-generated method stub

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = UserNameE.getText().toString();
                String password = passwordE.getText().toString();
                String email = emailE.getText().toString();
                String about = aboutE.getText().toString();

                username = username.trim();
                password = password.trim();
                email = email.trim();
                about = about.trim();

//                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
//                    builder.setMessage(R.string.signup_error_message)
//                            .setTitle(R.string.signup_error_title)
//                            .setPositiveButton(android.R.string.ok, null);
//                    AlertDialog dialog = builder.create();
//                    dialog.show();
//                }
//                else {
                setProgressBarIndeterminateVisibility(true);

                ParseQuery<ParseObject> query_profile = ParseQuery.getQuery("Profile");
                query_profile.whereEqualTo("username", currentUser.getUsername());
                query_profile.whereEqualTo("email", currentUser.getEmail());

                // Retrieve the object by id
                final String finalUsername = username;
                final String finalEmail = email;
                final String finalAbout = about;

                // Find current user's profile in parse
                query_profile.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    // Query finished
                    public void done(List<ParseObject> results, ParseException e) {
                        if (e == null) {
                            // Update current user's Profile with new username and email
                            for (ParseObject profile : results) {

                                if(!finalUsername.isEmpty()){
                                    profile.put("username", finalUsername);
                                    currentUser.setUsername(finalUsername);
                                    currentUser.saveInBackground();
                                }
                                if(!finalEmail.isEmpty()) {
                                    profile.put("email", finalEmail);
                                    currentUser.setEmail(finalEmail);
                                    currentUser.saveInBackground();
                                }
                                if(!finalAbout.isEmpty()){
                                    profile.put("bibliography",finalAbout);
                                }


                                profile.saveInBackground();
                            }
                        }
                    }
                });


                // Update current user's User with new username and email

                if(!password.isEmpty()){
                    currentUser.setPassword(password);
                    currentUser.saveInBackground();
                    //Toast.makeText(EditProfileActivity.this, "saved"+password,Toast.LENGTH_SHORT).show();
                }

                if(!(username.isEmpty() || password.isEmpty() || email.isEmpty()))
                    loadLoginView();
            }
            //}
        });

        button_select_photo.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });

//        button_upload_photo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                try {
//                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//
//                    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
//
//                    if (drawable == null)
//                        Toast.makeText(EditProfileActivity.this, "Unable to Get Profile Picture",Toast.LENGTH_SHORT).show();
//
//                    Bitmap bitmap = drawable.getBitmap();
//
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//
//                    byte[] data = bos.toByteArray();
//
//
//                    // Create the ParseFile
//                    file = new ParseFile("userPic.png", data);
//                    // Upload the image into Parse Cloud
//                    file.saveInBackground();
//
//                    // Setup query for search in Parse database
//                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Profile");
//                    query.whereEqualTo("username", currentUser.getUsername());
//
//                    final ParseObject profile = new ParseObject("Profile");
//
//                    // Run the query
//                    query.findInBackground(new FindCallback<ParseObject>() {
//                        @SuppressWarnings("unchecked")
//                        @Override
//                        // Query finished
//                        public void done(List<ParseObject> result, ParseException e) {
//                            System.out.println("done query");
//
//                            // Query Successful
//                            if (e == null) {
//
//                                // Extract data from Parse
//                                for (ParseObject profile : result) {
//                                    profile.put("profilePicture", file);
//
//                                    // Create the class and the columns
//                                    profile.saveInBackground(new SaveCallback() {
//                                        @Override
//                                        public void done(ParseException e) {
//                                            if (e == null) {
//                                                // Success!
//                                                //Toast.makeText(EditProfileActivity.this, "yes", Toast.LENGTH_SHORT).show();
//                                            } else {
//                                                //Toast.makeText(EditProfileActivity.this, "wrong...", Toast.LENGTH_SHORT).show();
//                                            }
//                                        }
//                                    });
//
//                                    // Show a simple toast message
//                                    Toast.makeText(EditProfileActivity.this, "Image Uploaded",
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                            // Query Unsuccessful
//                            else {
//                                Toast.makeText(getApplicationContext(), "Error: Failed to find User Profile", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                }
//                catch (Exception e) {
//
//                    // handle exception here
//                    e.printStackTrace();
//
//
//                }
//            }
//        });
    }

    protected void getProfileId(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Profile");
        query.whereEqualTo("username", currentUser.getUsername());

        // Run the query
        query.findInBackground(new FindCallback<ParseObject>() {
            @SuppressWarnings("unchecked")
            @Override
            // Query finished
            public void done(List<ParseObject> result, ParseException e) {
                System.out.println("done query");
                // Query Successful
                if (e == null) {
                    // Extract data from Parse
                    for (ParseObject profile : result) {
                        profileId = profile.getObjectId();
                        profileAbout = profile.getString("bibliography");
                        aboutE.setText(profileAbout);
                        //Toast.makeText(EditProfileActivity.this, "about: "+ profileAbout, Toast.LENGTH_SHORT).show();
                    }
                }
                // Query Unsuccessful
                else {
                    Toast.makeText(getApplicationContext(), "Error: Failed to find User Profile", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 10 && resultCode == Activity.RESULT_OK){
            if (data == null)
                //Toast.makeText(EditProfileActivity.this, "datatata",Toast.LENGTH_SHORT).show();
                ;
            else{

                Bitmap bitmap = getPath(data.getData());
                //cursor.close();
                imageView.setImageBitmap(bitmap);

                upload_photo();

            }
        }
    }

    private void upload_photo(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();

            if (drawable == null)
                Toast.makeText(EditProfileActivity.this, "Unable to Get Profile Picture",Toast.LENGTH_SHORT).show();

            Bitmap bitmap = drawable.getBitmap();

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);

            byte[] data = bos.toByteArray();


            // Create the ParseFile
            file = new ParseFile("userPic.png", data);
            // Upload the image into Parse Cloud
            file.saveInBackground();

            // Setup query for search in Parse database
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Profile");
            query.whereEqualTo("username", currentUser.getUsername());

            final ParseObject profile = new ParseObject("Profile");

            // Run the query
            query.findInBackground(new FindCallback<ParseObject>() {
                @SuppressWarnings("unchecked")
                @Override
                // Query finished
                public void done(List<ParseObject> result, ParseException e) {
                    System.out.println("done query");

                    // Query Successful
                    if (e == null) {

                        // Extract data from Parse
                        for (ParseObject profile : result) {
                            profile.put("profilePicture", file);

                            // Create the class and the columns
                            profile.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        // Success!
                                        //Toast.makeText(EditProfileActivity.this, "yes", Toast.LENGTH_SHORT).show();
                                    } else {
                                        //Toast.makeText(EditProfileActivity.this, "wrong...", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            // Show a simple toast message
                            Toast.makeText(EditProfileActivity.this, "Image Uploaded",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    // Query Unsuccessful
                    else {
                        Toast.makeText(getApplicationContext(), "Error: Failed to find User Profile", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        catch (Exception e) {

            // handle exception here
            e.printStackTrace();


        }

    }

    private Bitmap getPath(Uri uri) {
        BitmapFactory.Options options;

        Uri selectedImage = uri;
        String[] filep = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(selectedImage, filep, null, null, null);

        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filep[0]);
        System.out.println("------------------------conindex" + columnIndex);

        String filepath = cursor.getString(columnIndex);

        System.out.println("------------------------path" + filepath);


        System.out.println("------------------------path222" + filepath);

        BitmapFactory.Options bfOptions=new BitmapFactory.Options();
        bfOptions.inDither=false;
        bfOptions.inPurgeable=true;
        bfOptions.inInputShareable=true;
        bfOptions.inTempStorage=new byte[32 * 1024];


        File file;
        FileInputStream fs=null;
        try {
            file =new File(filepath);
            fs = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException ee){
            AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
            builder.setMessage(R.string.EditProfile_error_message)
                    .setTitle(R.string.signup_error_title)
                    .setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
            ee.printStackTrace();
        }
        Bitmap bm = null;

        try {
            if(fs!=null) bm=BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
            return bm;
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(fs!=null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        System.out.println("------------------------sth wrong...");
        return null;
    }


    private void loadLoginView() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
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
                loadLoginView();
                break;
//
//            case R.id.action_settings: {
//                // Do something when user selects Settings from Action Bar overlay
//                break;
//            }
        }
        return super.onOptionsItemSelected(item);
    }

}

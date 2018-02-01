package com.jclg.payitforward;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by graceg on 2016-01-10.
 */
public class EditPostActivity extends AppCompatActivity {

    private Toolbar myToolbar;

    // Post information in string and Date
    private String postTitle;
    private String postContent;
    private int postCategory;
    private Date postDueDate;
    private double lat;
    private double lon;

    // Edit Texts
    private EditText titleEditText;
    private EditText contentEditText;

    // Buttons
    private Button postTaskButton;

    // Original due date handler
//    private EditText timeEditText;
//    private EditText dateEditText;
//    private String time;
//    private String date;
//    private Spinner AM_PM;
//    private String clock;

    // New due date handler
    // Calender
    private Calendar calendar;
    // Date
    private TextView dateView;
    private int year, month, day;
    // Time
    private TextView timeView;
    private int hour, minute;


    // Google Maps and Places API
    private Location location;
    private Geocoder coder;
    private List<Address> address;
    private String autoAddress;

    // Static String values
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    //API- Server Key - Google Places API Web Service Must be Enabled
    private static final String PLACES_API_KEY = "AIzaSyCWCTtaE_la7o0dEXU1k4z-AFmCKLEGQmM";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {

        private ArrayList<String> resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            System.out.println("In googleplacesAutocompleteadapter ----------------------------------------------------------");
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            System.out.println("In getFilter ----------------------------------------------------------");
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    System.out.println("In publishResults ----------------------------------------------------------");
                    if (results != null && results.count > 0) {
                        System.out.println("In result > 0 ----------------------------------------------------------");
                        notifyDataSetChanged();
                    } else {
                        System.out.println("In result < 0 ----------------------------------------------------------");
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        coder = new Geocoder(this);

        setContentView(R.layout.activity_edit_post);

        myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

         /* Extras */
        Intent intent = this.getIntent();
        location = (Location) intent.getParcelableExtra(PifApplication.INTENT_EXTRA_LOCATION);
        postCategory = intent.getIntExtra("INTENT_EXTRA_CATEGORY", PostCategory.POST_INVALID);

        // Setup default date and time
        dateView = (TextView) findViewById(R.id.editPost_textView_setDate);
        timeView = (TextView) findViewById(R.id.editPost_textView_setTime);

        calendar = Calendar.getInstance(); // Calender

        // Date
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month+1, day);

        // Time
        hour = calendar.get(Calendar.HOUR);
        minute = calendar.get(Calendar.MINUTE);
        if (calendar.get(Calendar.AM_PM) == 1){
            hour += 12;
        }
        showTime(hour, minute);

        // Post information
        titleEditText = (EditText) findViewById(R.id.editPost_editText_title);
        contentEditText = (EditText) findViewById(R.id.editPost_editText_content);

        // Old Date and Time handling code
//        // Post due date
//        AM_PM = (Spinner)findViewById(R.id.editpost_spinner_12hr_clock);
//        timeEditText = (EditText)findViewById(R.id.editpost_editText_time);
//        dateEditText = (EditText)findViewById(R.id.editpost_editText_date);
//
//        //Set up the clock spinner
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.editpost_stringArray_clock, android.R.layout.simple_spinner_item);
//
//        // Specify the layout to use when the list of choices appears
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        // Apply the adapter to the spinner
//        AM_PM.setAdapter(adapter);
//
//        AM_PM.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Object s = parent.getItemAtPosition(position);
//                clock = s.toString();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                //System.out.println("nothing selected :");
//            }
//        });

        //Location AutoComplete by Google Places
        AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_autocomplete));
        autoCompView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                autoAddress = (String) parent.getItemAtPosition(position);
                System.out.println("In onClick ----------------------------------------------------------");
            }
        });

        postTaskButton = (Button) findViewById(R.id.editPost_button_post);
        postTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postPinOnMapAndParse();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();

        System.out.println("In onStart ----------------------------------------------------------");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "EditPost Page", // TODO: Define a title for the content shown.
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
        System.out.println("In onStop ----------------------------------------------------------");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "EditPost Page", // TODO: Define a title for the content shown.
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

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this, myDateListener, year, month, day);
        }
        else if (id == 998) {
            return new TimePickerDialog(this, myTimeListener, hour, minute, true);
        }
        return null;
    }

    /*
        Setup Date Picker
     */

    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(999);
//        Toast.makeText(getApplicationContext(), "ca", Toast.LENGTH_SHORT).show();
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            // arg1 = year
            // arg2 = month
            // arg3 = day

            year = arg1;
            month = arg2;
            day = arg3;

            showDate(arg1, arg2+1, arg3);
        }
    };

    private void showDate(int year, int month, int day) {
        dateView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    /*
        Setup Time Picker
     */

    @SuppressWarnings("deprecation")
    public void setTime(View view) {
        showDialog(998);
//        Toast.makeText(getApplicationContext(), "ca", Toast.LENGTH_SHORT).show();
    }

    private TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker arg0, int arg1, int arg2) {
            // TODO Auto-generated method stub
            // arg1 = hour
            // arg2 = minute

            Calendar datetime = Calendar.getInstance();
            datetime.set(Calendar.HOUR_OF_DAY, arg1);
            datetime.set(Calendar.MINUTE, arg2);

            hour = arg1;
            minute = arg2;

            showTime(arg1, arg2);
        }
    };

    private void showTime(int iHour, int iMinute) {
        String hour_String = "0";
        String minute_String = "0";

        if (iHour < 10)
            hour_String = hour_String.concat(Integer.toString(iHour));
        else
            hour_String = Integer.toString(iHour);

        if (iMinute < 10)
            minute_String = minute_String.concat(Integer.toString(iMinute));
        else
            minute_String = Integer.toString(iMinute);

        timeView.setText(new StringBuilder().append(hour_String).append(":").append(minute_String)
                );
    }

    /*
        Parse Functions
     */

    private void postPinOnMapAndParse() {
        //Parse content of EditText to strings
        postTitle = titleEditText.getText().toString();
        postContent = contentEditText.getText().toString();

        postTitle = postTitle.trim();
        postContent = postContent.trim();

        try {
            address = coder.getFromLocationName(autoAddress, 5);
            if (address == null) {
                System.out.println("address list is empty");
            }
            else{
                Address location = address.get(0);
                lat = location.getLatitude();
                lon = location.getLongitude();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!(postTitle.isEmpty() || postContent.isEmpty() || (lat == 0 && lon == 0))) {
            postDueDate = parseStringToDate();

            // create new post
            final ParseObject post = new ParseObject("Posts");

            // Current user
            String profileId = ParseUser.getCurrentUser().getParseObject("profileId").getObjectId();

            post.put("Requester", profileId);

            post.put("Title", postTitle);
            post.put("Description", postContent);
            post.put("DueDate", postDueDate);

            post.put("OffersNum", 0);

            post.put("Latitude", lat);
            post.put("Longitude", lon);

            post.put("PostStatus", PostStatus.POST_OPEN);
            post.put("PostCategory", postCategory);

            post.put("RequesterReview", false);
            post.put("TaskerReview", false);

            setProgressBarIndeterminateVisibility(true);
            post.saveInBackground( new SaveCallback() {
                public void done(ParseException e) {
                    setProgressBarIndeterminateVisibility(false);
                    if (e == null) {
                        Toast.makeText(getApplicationContext(), "Posted", Toast.LENGTH_SHORT).show();
                        loadMainActivity();
                    } else {
                        // The post failed.
                        Toast.makeText(getApplicationContext(), "Failed to Post", Toast.LENGTH_SHORT).show();
                        Log.d(getClass().getSimpleName(), "User update error: " + e);
                    }
                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(), "Failed to Post: Missing Entries", Toast.LENGTH_SHORT).show();
        }

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

    private Date parseStringToDate(){
        //Format string to Date object
        Date retDate = null;

        String yearString;
        String monthString;
        String dayString;

        String hourString;
        String minuteString;
        String secondString;

        // Date Formatter
        SimpleDateFormat formatter = new SimpleDateFormat("MM dd yyyy HH:mm:ss");

        // Construct date in eastern time
        StringBuilder stringB = new StringBuilder()
                .append(month+1).append(" ").append(day).append(" ").append(year).append(" ")
                .append(hour).append(":").append(minute).append(":00");
        String dateInString = stringB.toString();

        System.out.println("Now EST is " + dateInString); //2016 05 07 13 04 00

        // Initialize UTC Date object
        Date dateInObject = null;
        try {
            dateInObject = formatter.parse(dateInString);
        }
        catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        // Convert EST date into UTC time
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(dateInObject);

        // Date
        int year = cal.get(Calendar.YEAR);
        yearString = String.format("%04d", year);
        int month = cal.get(Calendar.MONTH);      // 0 to 11 month
        monthString = String.format("%02d", month+1);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        dayString = String.format("%02d", day);

        // Time
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        hourString = String.format("%02d", hour);
        int minute = cal.get(Calendar.MINUTE);
        minuteString = String.format("%02d", minute);
        int second = cal.get(Calendar.SECOND);
        secondString = String.format("%02d", second);

//        System.out.println("Now is " + yearString  + " " + monthString  + " " + dayString  + " " + hourString  + " " + minuteString  + " " + secondString); //2016 05 07 13 04 00
//        System.out.println("\n**************************************************************\n");

        //Set dueDate string into desired format: "2014-07-04T12:08:56.235-4000"
        String dueDate = yearString + "-" + monthString + "-" + dayString + "T" + hourString + ":" + minuteString + ":" + secondString + ".000-0000";
        System.out.println("Now UTC is " + dueDate); //2016 05 07 13 04 00

        SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        try {
            ft.setTimeZone(TimeZone.getTimeZone("UTC"));
            retDate = ft.parse(dueDate);
//            Toast.makeText(getApplicationContext(), "postDueDate in String: " + retDate.toString(), Toast.LENGTH_SHORT).show();
//            System.out.println("postDueDate in String: " + retDate.toString());
//            System.out.println("\n**************************************************************\n");
        }
        catch (java.text.ParseException e) {
//            System.out.println("Parse String to Date Exception");
//            System.out.println("\n**************************************************************\n");
            e.printStackTrace();
        }
        return retDate;
    }

    /*
        Activity Loaders
     */

    private void loadLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void loadMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void loadProfileView() {
        Intent intent = new Intent(this, ViewProfileActivity.class);
        startActivity(intent);
    }

    /*
        Google Places - Auto Complete
     */

    public static ArrayList<String> autocomplete(String input) {

        ArrayList<String> placesList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + PLACES_API_KEY);
            sb.append("&components=country:ca");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());

            System.out.println("URL: " + url);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            //Log.e(LOG_TAG, "Error processing Places API URL", e);
            return placesList;
        } catch (IOException e) {
            //Log.e(LOG_TAG, "Error connecting to Places API", e);
            return placesList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            placesList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                placesList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            //Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return placesList;
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

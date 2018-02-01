package com.jclg.payitforward;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity implements LocationListener,
		GoogleApiClient.ConnectionCallbacks, OnConnectionFailedListener{
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	/*
       * Constants for location update parameters
       */
	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;

	// The update interval
	private static final int UPDATE_INTERVAL_IN_SECONDS = 5;

	// A fast interval ceiling
	private static final int FAST_CEILING_IN_SECONDS = 1;

	// Update interval in milliseconds
	private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;

	// A fast ceiling of update intervals, used when the app is visible
	private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
			* FAST_CEILING_IN_SECONDS;

    private Toolbar myToolbar;

	private Location lastLocation;
	private Location currentLocation;
	private LocationRequest locationRequest;
	private GoogleApiClient locationClient;
	private boolean hasSetUpInitialLocation;

	private Button button_post;
    private Button button_search;
    private Button button_manage;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);

		myToolbar = (Toolbar) findViewById(R.id.app_toolbar);
		setSupportActionBar(myToolbar);

		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser == null) {
			loadLoginView();
		}
		else{}


		locationRequest = LocationRequest.create();
		locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);

		locationClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

        button_post = (Button)findViewById(R.id.main_button_editPost);
        button_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadEditPostView();
            }
        });

        button_search = (Button)findViewById(R.id.main_button_search);
        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSearchActivity();
            }
        });

		button_manage = (Button)findViewById(R.id.main_button_managePosts);
		button_manage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadManagePostsActivity();
			}
		});
	}

	@Override
	public void onStop() {
		// If the client is connected
		if (locationClient.isConnected()) {
			stopPeriodicUpdates();
		}

		// After disconnect() is called, the client is considered "dead".
		locationClient.disconnect();

		super.onStop();
	}

	@Override
	public void onStart() {
		super.onStart();

		// Connect to the location services client
		locationClient.connect();
	}

	private void loadEditPostView() {
		Intent intent = new Intent(MainActivity.this, CategoryActivity.class);

		// Only allow posts if we have a location
		Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
		if (myLoc == null) {
			Toast.makeText(MainActivity.this,
					"Can't get current location, please enable location on your device.",
                    Toast.LENGTH_LONG).show();
			return;
		}

		intent.putExtra(PifApplication.INTENT_EXTRA_LOCATION, myLoc);
		startActivity(intent);
	}

	private void loadSearchActivity() {
		Intent intent = new Intent(this, SearchActivity.class);
		startActivity(intent);
	}

	private void loadManagePostsActivity() {
		Intent intent = new Intent(this, ManagePostsActivity.class);
		startActivity(intent);
	}

	private void loadLoginView() {
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
				loadLoginView();
				break;

			case R.id.action_viewProfile: {
				// Do something when user selects Settings from Action Bar overlay
				//loadProfileView();
				loadEditProfileView();
				break;
			}

//			case R.id.action_settings: {
//				// Do something when user selects Settings from Action Bar overlay
//				break;
//			}
		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onConnected(Bundle bundle) {
		if (PifApplication.APPDEBUG) {
			Log.d(PifApplication.APPTAG, "Connected to location services");
		}
		currentLocation = getLocation();
		if (currentLocation == null)
			System.out.println("current location is null!!!!!!!!!!!!!!");
		else {
			System.out.println("current location: " + currentLocation.getLatitude() + " " + currentLocation.getLongitude());
			startPeriodicUpdates();
		}
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onLocationChanged(Location location) {
		System.out.println("current location in on location changed!: " + currentLocation.getLatitude() + " " + currentLocation.getLongitude());
		currentLocation = location;
		if (lastLocation != null
				&& geoPointFromLocation(location)
				.distanceInKilometersTo(geoPointFromLocation(lastLocation)) < 0.01) {
			// If the location hasn't changed by more than 10 meters, ignore it.
			return;
		}
		lastLocation = location;
		LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
		if (!hasSetUpInitialLocation) {
			System.out.println("in has set up init: " + myLatLng.latitude + " " + myLatLng.longitude);
			// Zoom to the current location.
			//updateZoom(myLatLng);
			hasSetUpInitialLocation = true;
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

			} catch (IntentSender.SendIntentException e) {

				if (PifApplication.APPDEBUG) {
					// Thrown if Google Play services canceled the original PendingIntent
					Log.d(PifApplication.APPTAG, "An error occurred when connecting to location services.", e);
				}
			}
		} else {
			// If no resolution is available, display a dialog to the user with the error.
			System.out.println("on connection failed...");
		}
	}

	private void stopPeriodicUpdates() {
		locationClient.disconnect();
	}

	private ParseGeoPoint geoPointFromLocation(Location loc) {
		return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
	}

//	private Location getLocation() {
//		return LocationServices.FusedLocationApi.getLastLocation(locationClient);
//	}
	private Location getLocation() {
		// If Google Play Services is available
		if (servicesConnected()) {
			// Get the current location
			return LocationServices.FusedLocationApi.getLastLocation(locationClient);
		} else {
			return null;
		}
	}

	private void startPeriodicUpdates() {
		LocationServices.FusedLocationApi.requestLocationUpdates(
				locationClient, locationRequest, this);
	}

	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			if (PifApplication.APPDEBUG) {
				// In debug mode, log the status
				Log.d(PifApplication.APPTAG, "Google play services available");
			}
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			// Display an error message
			Toast.makeText(MainActivity.this,
					"Error: Google Play services is not available.",
					Toast.LENGTH_LONG).show();
			return false;
		}
	}

}


package com.jclg.payitforward;

import android.app.Application;

import com.parse.Parse;

import org.json.JSONArray;
import org.json.JSONException;

public class PifApplication extends Application {

    // Debug constants
	public static final boolean APPDEBUG = false;
	public static final String APPTAG = "App";

    // Post constants
    public static final int OFFERS_MAX = 100;
    public static final int OFFERS_MIN = 0;

    // Intent extras
	public static final String INTENT_EXTRA_LOCATION = "location";
    public static final String INTENT_EXTRA_MY_POSTOBJECT = "myPostObject";
    public static final String INTENT_EXTRA_SEARCH_POSTLIST = "myPostList";

    // Map default UofT location
    public static final double LAT_CENTRE = 43.663961;
	public static final double LNG_CENTRE = -79.396689;
	public static final float ZOOM_DEFAULT = 14.5f;

	// Key for saving the search distance preference

	private static final float DEFAULT_SEARCH_DISTANCE = 250.0f;

	@Override
	public void onCreate() {
		super.onCreate();

		Parse.initialize(this, "1VqqhUFwmhhBG2dAJV6R6HOIQ09fYBsorwy1doDr", "jJzjx9obNWVJTr7EMuk0aLpPfuXVp5PvKwVOLptW");

	}


}

package com.imaginat.locationservices;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by nat on 5/7/16.
 */
public final class Constants {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "com.imaginat.locationservices";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";

    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";
    //========geo fence stuff
    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";
    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 400; // 1609 1 mile, 1.6 km

    /**
     * Map for storing information about airports in the San Francisco bay area.
     */
    public static final HashMap<String, LatLng> MY_LANDMARKS = new HashMap<String, LatLng>();
    static {
        //CRESTWOOD
        MY_LANDMARKS.put("CRESTWOOD TRAIN STATION", new LatLng(40.958997,-73.820564));

        // WARREN.
        MY_LANDMARKS.put("WARREN AVENUE", new LatLng(40.9618839,-73.8154516));

        //EASTCHESTER HIGH SCHOOL
        MY_LANDMARKS.put("WARREN AVENUE", new LatLng(40.961959, -73.817088));

        //LORD & TAYLORS
        MY_LANDMARKS.put("LORD&TAYLORS", new LatLng(40.972252, -73.803934));

        //KENSICO DAM
        MY_LANDMARKS.put("KENSICO DAM",new LatLng(41.073794, -73.766287));
    }
}
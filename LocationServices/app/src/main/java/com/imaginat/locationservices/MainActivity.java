package com.imaginat.locationservices;

import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity
 implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener,ResultCallback<Status>, OnMapReadyCallback {

    public static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_FINE_LOCATION = 0;
    final static int REQUEST_LOCATION = 199;
    static String mAddressOutput;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private AddressResultReceiver mResultReceiver;


    ///fence=========================================================
    /**
     * The list of geofences used in this sample.
     */
    protected ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    /**
     * Used to keep track of whether geofences were added.
     */
    private boolean mGeofencesAdded;

    /**
     * Used to persist application state about whether geofences were added.
     */
    private SharedPreferences mSharedPreferences;

    ////////==========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mMapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.my_container, mMapFragment,"THEMAP");
        fragmentTransaction.commit();

        getFragmentManager().executePendingTransactions();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentByTag("THEMAP");
        if(mapFragment==null){
            Log.d(TAG,"mapFragment is NULL");
            mMapFragment.getMapAsync(this);
        }else{
            Log.d(TAG,"mapFragment is NOT NULL");
            // mapFragment.getMapAsync(this);
        }


        Log.d(TAG, "onCreate about to call mGoogleApiClient.Builder");
         mGoogleApiClient = new GoogleApiClient.Builder(this)
        .enableAutoManage(this /* FragmentActivity */,
        this /* OnConnectionFailedListener */)
                 .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();


        // Retrieve an instance of the SharedPreferences object.
        mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<Geofence>();
        // Get the geofences used. Geofence data is hard coded in this sample.
        populateGeofenceList();


        Button addressButton = (Button)findViewById(R.id.getAddressButton);
        addressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick");
                if (mGoogleApiClient.isConnected()){// && mLastLocation != null) {
                    Log.d(TAG,"onIntentService about to be called");
                    startIntentService();
                }else{
                    Log.d(TAG,"Nothing being called on click");
                }
            }
        });

        Button setGeoFenceButton = (Button)findViewById(R.id.addGeoFence);
        setGeoFenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGoogleApiClient.isConnected()) {
                    Toast.makeText(MainActivity.this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    LocationServices.GeofencingApi.addGeofences(
                            mGoogleApiClient,
                            // The GeofenceRequest object.
                            getGeofencingRequest(),
                            // A pending intent that that is reused when calling removeGeofences(). This
                            // pending intent is used to generate an intent when a matched geofence
                            // transition is observed.
                            getGeofencePendingIntent()
                    ).setResultCallback(MainActivity.this); // Result processed in onResult().
                } catch (SecurityException securityException) {
                    // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                    logSecurityException(securityException);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if (mGoogleApiClient != null) {
            Log.d(TAG,"googleApiClient is not null, attempting to connect");
            mGoogleApiClient.connect();
        }else{
            Log.d(TAG,"googleAPIClient is NULL");
        }
        Log.d(TAG, "Leaving onStart");
    }

    @Override
    protected void onStop() {

        if(mGoogleApiClient!=null){
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();

        //re-factor this code, identical to below
        final LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try {
            if (mGoogleApiClient.isConnected()) {// && !mRequestingLocationUpdates) {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, MainActivity.this);
                //startLocationUpdates();
            }
        }catch(SecurityException se){

        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "ConnectionFailed");
        Toast.makeText(this,"Connection failed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        loadPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION);
//        LocationRequest request = LocationRequest.create();
//        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        request.setNumUpdates(1);
//        request.setInterval(0);


        try{
            Log.d(TAG,"onConnected reached");

            createLocationRequest();
            /*
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {

                Toast.makeText(this, "Latitude:" + mLastLocation.getLatitude() + ", Longitude:" + mLastLocation.getLongitude(), Toast.LENGTH_LONG).show();
                Log.d(TAG, "Latitude:" + mLastLocation.getLatitude() + ", Longitude:" + mLastLocation.getLongitude());
            }else{
                Log.d(TAG,"mLastLocation is null");

            }*/
        }catch(SecurityException ex){
            Log.d(TAG,"SecurityException ");
            ex.printStackTrace();
        }catch(Exception ex){
            Log.d(TAG,"Exception");
            ex.printStackTrace();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }



    protected void createLocationRequest() throws SecurityException{
        final LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) throws SecurityException {
                final Status status = result.getStatus();
                //final LocationSettingsStates h = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        Log.d(TAG, "Location Setting Requst status code is success");
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                mGoogleApiClient, mLocationRequest, MainActivity.this);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        Log.d(TAG, "Location Setting Requst status code is resolution required");
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(TAG, "Location Setting Requst status code is setting change unavaible");
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

                        break;
                }
            }
        });

    }

    private void loadPermissions(String perm,int requestCode) {
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                ActivityCompat.requestPermissions(this, new String[]{perm},requestCode);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        Toast.makeText(this, "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude(), Toast.LENGTH_LONG).show();
        Log.d(TAG, "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }



    private static void displayAddressOutput(){
        Log.d(TAG,"the address output "+mAddressOutput);
    }



    //======================GEO FENCE====================================================
    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    public void populateGeofenceList() {

        for (Map.Entry<String, LatLng> entry : Constants.MY_LANDMARKS.entrySet()) {

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                            // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                            // Set the expiration duration of the geofence. This geofence gets automatically
                            // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                            // Set the transition types of interest. Alerts are only generated for these
                            // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                            // Create the geofence.
                    .build());
        }
    }


    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();

            // Update the UI. Adding geofences enables the Remove Geofences button, and removing
            // geofences enables the Add Geofences button.
            //setButtonsEnabledState();

            Toast.makeText(
                    this,
                    getString(mGeofencesAdded ? R.string.geofences_added :
                            R.string.geofences_removed),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG,"Inside onMapReady");
        GoogleMap map = googleMap;
        for (Map.Entry<String, LatLng> entry : Constants.MY_LANDMARKS.entrySet()) {
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(entry.getValue().latitude, entry.getValue().longitude))
                    .title("Marker"));

        }

    }

    //====================================================================================
    public static class AddressResultReceiver extends ResultReceiver {

        String CREATOR="me";

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }



        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            displayAddressOutput();

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                //Toast.makeText(MainActivity.this,getString(R.string.address_found),Toast.LENGTH_SHORT).show();
                Log.d(TAG,"address found");
            }

        }
    }//end of inner class


}

package com.imaginat.locationservices;

import android.content.Intent;
import android.content.IntentSender;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class MainActivity extends AppCompatActivity
 implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {

    public static final String TAG = MainActivity.class.getName();
    private static final int REQUEST_FINE_LOCATION = 0;
    final static int REQUEST_LOCATION = 199;
    static String mAddressOutput;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private AddressResultReceiver mResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate about to call mGoogleApiClient.Builder");
         mGoogleApiClient = new GoogleApiClient.Builder(this)
        .enableAutoManage(this /* FragmentActivity */,
        this /* OnConnectionFailedListener */)
                 .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

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

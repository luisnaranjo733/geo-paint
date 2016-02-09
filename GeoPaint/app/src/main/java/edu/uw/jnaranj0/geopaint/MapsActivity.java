package edu.uw.jnaranj0.geopaint;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.Manifest;
import android.widget.Toast;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    // Start of GoogleApiClient.ConnectionCallbacks methods
    private final int PERMISSION_REQUEST_CODE = 19;
    //private PolylineOptions polylineOptions;
    private Polyline polyline;

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "On connected");
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        LocationRequest request = new LocationRequest();
        request.setInterval(10000);
        request.setFastestInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Location permission granted!!");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);

        } else {
            Log.v(TAG, "Location permission NOT granted :(");
            String[] permissions = new String[1];
            permissions[0] = Manifest.permission.ACCESS_FINE_LOCATION;
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.v(TAG, "Permission request result: " + grantResults.toString());
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    onConnected(new Bundle());

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG, "On connection suspended: " + i);
    }

    // End of GoogleApiClient.ConnectionCallbacks methods

    public static final String TAG = "GeoPaint";

    private Menu menu;
    private boolean penActive = true;
    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            Log.v(TAG,  "Instantiated google api client");
        }

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {

        mGoogleApiClient.disconnect();
        super.onStop();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng seattle = new LatLng(47.6633296,-122.4998691);
        mMap.addMarker(new MarkerOptions().position(seattle).title("Marker in Seattle"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(seattle));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        this.menu = menu;
        return true;
    }

    public void togglePen(MenuItem item) {
        Log.v(TAG, "Toggle pen");

        //Drawable true_icon = Drawable.createFromPath("@android:drawable/ic_menu_edit");
        //Drawable false_icon = Drawable.createFromPath("@android:drawable/ic_menu_close_clear_cancel");

        MenuItem togglePenButton = menu.getItem(0);
        if (penActive) {
            // deactivate pen
            togglePenButton.setIcon(R.drawable.ic_menu_block);
        } else {
            // activate pen
            togglePenButton.setIcon(R.drawable.ic_menu_edit);
        }
        penActive = !penActive;

    }

    public void pickColor(MenuItem item) {
        Log.v(TAG, "Pick a color!");
    }

    public void shareImage(MenuItem item) {
        Log.v(TAG, "Share the image!");
    }

    // GoogleApiClient.OnConnectionFailedListener callback
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.toString());
    }

    // LocationListener method
    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(this, "asdfa", Toast.LENGTH_SHORT).show();
        Log.v(TAG, "Location changed: " + location.getLatitude() + ", " + location.getLongitude());

        if (penActive) {
            Log.v(TAG, "PEN ACTIVE");
            LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());
            if (polyline == null) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.add(coordinate);
                polyline = mMap.addPolyline(polylineOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 18));
            } else {
                List<LatLng> points = polyline.getPoints();
                points.add(coordinate);
                polyline.setPoints(points);

            }
        } else {
            Log.v(TAG, "Pen not active");
        }

    }
}

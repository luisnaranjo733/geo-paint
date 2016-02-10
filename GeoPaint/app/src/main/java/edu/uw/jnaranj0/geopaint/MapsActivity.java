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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String TAG = "**GeoPaint**";

    private Menu menu;
    private boolean penActive = false;
    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;

    // Start of GoogleApiClient.ConnectionCallbacks methods
    private final int PERMISSION_REQUEST_CODE = 19;
    private Polyline polyline;
    private int currentColor = -1;

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "On connected");
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        LocationRequest request = new LocationRequest();
        request.setInterval(30000);
        request.setFastestInterval(1000);
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
        LatLng seattle = new LatLng(47.6628344,-122.305184);
        //mMap.addMarker(new MarkerOptions().position(seattle).title("Marker in Seattle"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seattle, 20));
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
            Toast.makeText(this, "Pen deactivated!", Toast.LENGTH_SHORT).show();
            // deactivate pen
            togglePenButton.setIcon(R.drawable.ic_menu_block);

        } else {
            Toast.makeText(this, "Pen activated!", Toast.LENGTH_SHORT).show();
            // activate pen
            togglePenButton.setIcon(R.drawable.ic_menu_edit);
            removePolyLine();
        }
        penActive = !penActive;

    }

    public void pickColor(MenuItem item) {
        Log.v(TAG, "Pick a color!");
        // erase current polyline
        // change color
        removePolyLine();

        currentColor = ContextCompat.getColor(this, R.color.flamingo);

        //textView = (TextView) findViewById(R.id.text);

        int[] mColors = getResources().getIntArray(R.array.default_rainbow);

        ColorPickerDialog dialog = ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                mColors,
                currentColor,
                5, // Number of columns
                ColorPickerDialog.SIZE_SMALL);

        dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

            @Override
            public void onColorSelected(int color) {
                currentColor = color;
                Log.v(TAG, "Selected color: " + currentColor);
                Toast.makeText(MapsActivity.this, "Selected color: " + currentColor, Toast.LENGTH_SHORT).show();
            }

        });

        dialog.show(getFragmentManager(), "color_dialog_test");
    }

    public void removePolyLine() {
        if (polyline != null) {
            polyline.remove();
            polyline = null;
        }

    }

    public void saveDrawing(MenuItem item) {
        Log.v(TAG, "Save the drawing!");

        try {
            JSONObject root = new JSONObject();
            root.put("type", "FeatureCollection");
            JSONArray features = new JSONArray();
            // populate features
            JSONObject lineString = new JSONObject();

            lineString.put("type", "Feature");

            JSONObject lineStringGeometry = new JSONObject();
            lineStringGeometry.put("type", "LineString");
            JSONArray coordinates = new JSONArray();
            // populate array with coordinates
            List<LatLng> points = polyline.getPoints();
            for (LatLng point: points) {
                //coordinates.put("[" + point.latitude + "," + point.longitude + "]");
                coordinates.put(point);
            }

            lineStringGeometry.put("coordinates",  coordinates);

            JSONObject lineStringProperties = new JSONObject();
            lineStringProperties.put("prop0", "value0");
            lineStringProperties.put("prop1", 0.0);

            lineString.put("geometry", lineStringGeometry);
            lineString.put("properties", lineStringProperties);
            features.put(lineString);
            root.put("features", features);

            Log.v(TAG, root.toString());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }



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
                Log.v(TAG, "Instantiating polyline");
                PolylineOptions polylineOptions = new PolylineOptions().add(coordinate);
                if (currentColor != -1) {
                    Log.v(TAG, "Updating color of polyline");
                    polylineOptions.color(currentColor);
                }
                polyline = mMap.addPolyline(polylineOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 18));
            } else {
                Log.v(TAG, "Updating polyline");
                List<LatLng> points = polyline.getPoints();
                points.add(coordinate);
                polyline.setPoints(points);
                //polyline.setColor(currentColor);

            }
        } else {
            Log.v(TAG, "Pen not active");
        }

    }
}

package edu.uw.jnaranj0.geopaint;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ShareActionProvider;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String TAG = "**GeoPaint**";

    private Menu menu;
    private boolean penActive = false;
    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;

    private ArrayList<Polyline> lines;
    private Polyline currentPolyline;
    private Intent shareIntent;
    private ShareActionProvider myShareActionProvider;

    // Start of GoogleApiClient.ConnectionCallbacks methods
    private final int PERMISSION_REQUEST_CODE = 19;
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

        lines = new ArrayList<Polyline>();

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

        MenuItem shareItem = menu.findItem(R.id.action_share);
        myShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");



        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.EMPTY);
        myShareActionProvider.setShareIntent(shareIntent);

        return true;
    }

    public void togglePen(MenuItem item) {
        Log.v(TAG, "Toggle pen");

        MenuItem togglePenButton = menu.getItem(0);
        if (penActive) {
            Toast.makeText(this, "Pen deactivated!", Toast.LENGTH_SHORT).show();
            // deactivate pen
            togglePenButton.setIcon(R.drawable.ic_menu_block);
            currentPolyline = null;

        } else {
            Toast.makeText(this, "Pen activated!", Toast.LENGTH_SHORT).show();
            // activate pen
            togglePenButton.setIcon(R.drawable.ic_menu_edit);
        }
        penActive = !penActive;

    }

    public void pickColor(MenuItem item) {
        Log.v(TAG, "Pick a color!");
        // erase current polyline
        currentPolyline = null;
        // change color

        if (currentColor == -1) {
            currentColor = ContextCompat.getColor(this, R.color.flamingo);
        }

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

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void saveDrawing(MenuItem item) {
        Log.v(TAG, "Save the drawing!");
        if (!lines.isEmpty()) {
            Toast.makeText(this, "Saving drawing", Toast.LENGTH_LONG).show();
            String json = GeoJsonConverter.convertToGeoJson(lines);

            if (isExternalStorageWritable()) {
                File file = new File(this.getExternalFilesDir(null), "drawing.geojson");
                Log.v(TAG, "External storage IS available at: " + file.getAbsolutePath());
                //Toast.makeText(this, file.getAbsolutePath().toString(), Toast.LENGTH_LONG).show();

                try {
                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(json.getBytes()); //write the string to the file
                    outputStream.close(); //close the stream

                    shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    myShareActionProvider.setShareIntent(shareIntent);
                    Log.v(TAG, Uri.fromFile(file).toString());
                    Toast.makeText(this,  Uri.fromFile(file).toString(), Toast.LENGTH_SHORT).show();
                } catch (java.io.IOException exception) {
                    Log.e(TAG, exception.toString());
                    Toast.makeText(this, "IO error", Toast.LENGTH_SHORT).show();

                }

            } else {
                Log.v(TAG, "External storage is NOT available");
                Toast.makeText(this, "External storage is NOT available", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Sorry, no lines drawn yet!", Toast.LENGTH_LONG).show();
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
            LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());

            // init, change color and raise pen all nullify polyline variable
            if (currentPolyline == null) {
                // add polyline to list
                PolylineOptions options = new PolylineOptions().color(currentColor);
                currentPolyline = mMap.addPolyline(options);
                lines.add(currentPolyline);
                // set that to be the  "current" polyline
            }
            // we have a current polyline here
            List<LatLng> points = currentPolyline.getPoints();
            points.add(coordinate);
            currentPolyline.setPoints(points);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 18));

            shareIntent.putExtra(Intent.EXTRA_STREAM, "");
            myShareActionProvider.setShareIntent(shareIntent);
            // draw a point on this line, with the current color
        }
    }
}

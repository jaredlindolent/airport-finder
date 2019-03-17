package com.jared.airportfinder.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jared.airportfinder.Interfaces.NearbyAirportListener;
import com.jared.airportfinder.R;
import com.jared.airportfinder.Sync.NearbyAirportTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NearbyAirportListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    LocationListener locationListener;
    FloatingActionButton fab;
    GoogleMap gMap;
    Marker marker;

    String result;
    JSONArray jsonArray;
    JSONObject jsonResult;

    String first_nameAirport, iataCode, timezone,
            latitude, longitude;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (iataCode != null){
                    Intent intent = new Intent(MapsActivity.this, NearbyAirportActivity.class);
                    intent.putExtra("nameAirport", first_nameAirport);
                    intent.putExtra("timezone", timezone);
                    intent.putExtra("codeIataAirport", iataCode);
                    startActivity(intent);
                    finish();
                    return;
                } else {
                    findLocation(gMap);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Criteria criteria = new Criteria();
        checkLocationPermissions();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        mMap = googleMap;
        LatLng you = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(you));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(you, 17f));
        findNearbyAirports(you);

        gMap = googleMap;
        findLocation(googleMap);
    }

    public void findLocation(final GoogleMap gMap) {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Location onlyOneLocation = location;
                locationManager.removeUpdates(this);
                mMap = gMap;
                LatLng you = new LatLng(onlyOneLocation.getLatitude(), onlyOneLocation.getLongitude());
                if (marker != null) {
                    marker.remove();
                    marker = mMap.addMarker(new MarkerOptions().position(you));
                } else {
                    marker = mMap.addMarker(new MarkerOptions().position(you));
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(you, 17f));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        checkLocationPermissions();
    }

    public void manageNearbyAirports(String result){
        try {
            jsonArray = new JSONArray(result);
            jsonResult = jsonArray.getJSONObject(0);

            first_nameAirport = jsonResult.getString("nameAirport");
            timezone = jsonResult.getString("timezone");
            iataCode = jsonResult.getString("codeIataAirport");


            for (int i=0; i<jsonArray.length(); i++){
                jsonResult = jsonArray.getJSONObject(i);

                latitude = jsonResult.getString("latitudeAirport");
                longitude = jsonResult.getString("longitudeAirport");
                setAirportMarker(latitude, longitude);
            }


        } catch (JSONException e){
            e.printStackTrace();
            showAlertDialogWithSettings("Find Nearby Airports", "Could not find nearby airports");
        }
    }

    /*
     * Alert dialog control
     */
    private void showAlertDialogWithSettings(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton("OK", null);
        builder.show();

    }

    public void setAirportMarker(String latitude, String longitude){
        double _latitude = convertStringToDouble(latitude);
        double _longitude = convertStringToDouble(longitude);

        LatLng airpirtCoords = new LatLng(_latitude, _longitude);
        mMap.addMarker(new MarkerOptions().position(airpirtCoords));
    }

    public double convertStringToDouble(String value){
        return Double.parseDouble(value);
    }

    public void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    public void findNearbyAirports(LatLng coords){
        NearbyAirportTask nearbyAirportTask = new NearbyAirportTask(MapsActivity.this, MapsActivity.this, coords);
        nearbyAirportTask.execute("http://aviation-edge.com/v2/public/nearby?key=350d9b-233c83&lat=" + coords.latitude + "&lng=" + coords.longitude + "&distance=100");
    }

    @Override
    public void onNearbyAirportListener(String airports) {
        if (airports != ""){
            result = "";
            result = airports;
            manageNearbyAirports(result);
        }
    }
}

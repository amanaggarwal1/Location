package com.amanaggarwal1.location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amanaggarwal1.location.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.*;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final int MIN_REQUEST_TIME = 5000;
    private static final int MIN_REQUEST_DISTANCE = 0;

    private  SupportMapFragment mapFragment;
    private GoogleMap myMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private String getAddressForLocation(LatLng point){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String address = "";

        try{
            List<Address> addressesList = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            address += addressesList.get(0).getAddressLine(0);
        }catch (Exception e){
            Log.d("LOGCAT", e.getMessage());
        }

        if(address.isEmpty()) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            address += simpleDateFormat.format(new Date());
        }

        return address;
    }

    private void addMarkerOnMap(LatLng latLng, String title, float markerColor){

        //myMap.clear();
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor));
        myMap.addMarker (markerOptions);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
    }

    private void setupOnLocationListener(final GoogleMap myMap){
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("LOGCAT", "onLocationChanged called, lat = " +
                        location.getLatitude() + " long = " + location.getLongitude() +
                        " accuracy = " + location.getAccuracy() + " alt = " + location.getAltitude());

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                addMarkerOnMap(latLng, "You are here", HUE_RED);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(getApplicationContext(), "Enabled new provider " + provider,
                        Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(getApplicationContext(), "Disabled provider " + provider,
                        Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void requestUserLocation(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            assert locationManager != null;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_REQUEST_TIME, MIN_REQUEST_DISTANCE, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_REQUEST_TIME, MIN_REQUEST_DISTANCE, locationListener);

        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("LOGCAT", "permission granted");
                mapFragment.getMapAsync(this);
            }
        }
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {

        myMap = googleMap;
        myMap.setOnMapLongClickListener(this);
        myMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        setupOnLocationListener(myMap);

        Intent intent = getIntent();
        int locationNumberToVisit = intent.getIntExtra("locationNumber", 0);

        if(locationNumberToVisit == 0) {
            requestUserLocation();
        }else{
            LatLng placeToVisit = MainActivity.latLngList.get(locationNumberToVisit);
            String title = MainActivity.placesList.get(locationNumberToVisit);
            addMarkerOnMap(placeToVisit, title, HUE_CYAN);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapLongClick(LatLng point) {

        String address = getAddressForLocation(point);

        myMap.addMarker(new MarkerOptions()
                .position(point)
                .title(address)
                .icon(BitmapDescriptorFactory.defaultMarker(HUE_CYAN)));

        MainActivity.placesList.add(address);
        MainActivity.latLngList.add(point);
        MainActivity.arrayAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Location saved", Toast.LENGTH_SHORT).show();

    }

}

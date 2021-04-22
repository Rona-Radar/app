package com.example.ronaradar;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentUserLocationMark;
    private static final int Request_User_Location_Code = 99;
    private double latitude, longitude;
    private int proximityRadius = 10000;
    LatLng SearchedlatLng; // latitude and longitude for searched location
                

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.maps);
        bottomNavigationView.setOnNavigationItemSelectedListener((item) -> {
            switch(item.getItemId()) {
                case R.id.feed:
                    startActivity(new Intent(this, FeedActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.maps:
                    return true;
            }
            return false;
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkUserLocationPermission();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }

    public void onClick(View v) {
        // nearby doctors + hospitals in current location
        Object transferData[] = new Object[2];
        FindNearbySites getNearbyPlaces = new FindNearbySites();
        Object transferData_doctor[] = new Object[2];
        FindNearbySites getNearbyPlacesDoctor = new FindNearbySites();
               
        // nearby doctors + hospitals in requested search location
        Object transferDataSearch[] = new Object[2];
        FindNearbySites getNearbyPlacesSearch = new FindNearbySites();
        Object transferData_doctorSearch[] = new Object[2];
        FindNearbySites getNearbyPlacesDoctorSearch = new FindNearbySites();

        switch(v.getId())
        {
            // search bar
            case R.id.search:
                EditText addressField = (EditText) findViewById(R.id.location_search);
                String address = addressField.getText().toString();
                List<Address> addressList = null;
                MarkerOptions userMarkerOptions = new MarkerOptions();

                if(!TextUtils.isEmpty(address))
                {
                    Geocoder geocoder = new Geocoder(this);
                    try
                    {
                        addressList = geocoder.getFromLocationName(address, 6);
                        if (addressList != null)
                        {
                            for(int i = 0; i < addressList.size(); ++i)
                            {
                                Address userAddress = addressList.get(i);
                                SearchedlatLng = new LatLng(userAddress.getLatitude(), userAddress.getLongitude());
                                userMarkerOptions.position(SearchedlatLng);
                                userMarkerOptions.title(address);
                                userMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                                mMap.addMarker(userMarkerOptions);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(SearchedlatLng));
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
                }
                break;
            // plotting locations with refresh button
            case R.id.refresh:
                // current location
                mMap.clear();
                String url = getUrl(latitude, longitude, "hospital");
                transferData[0] = mMap;
                transferData[1] = url;
                getNearbyPlaces.execute(transferData);
                Toast.makeText(this, "Searching for nearby hospitals and doctors", Toast.LENGTH_SHORT).show();

                String doctor_url = getUrl(latitude, longitude, "doctor");
                transferData_doctor[0] = mMap;
                transferData_doctor[1] = doctor_url;
                getNearbyPlacesDoctor.execute(transferData_doctor);
                
                // requested search location 
                Log.d("isNull", String.valueOf(SearchedlatLng == null));
                if(SearchedlatLng != null)
                {
                    String urlSearch = getUrl(SearchedlatLng.latitude, SearchedlatLng.longitude, "hospital");
                    transferDataSearch[0] = mMap;
                    transferDataSearch[1] = urlSearch;
                    getNearbyPlacesSearch.execute(transferDataSearch);

                    String doctorUrlSearch = getUrl(SearchedlatLng.latitude, SearchedlatLng.longitude, "doctor");
                    transferData_doctorSearch[0] = mMap;
                    transferData_doctorSearch[1] = doctorUrlSearch;
                    getNearbyPlacesDoctorSearch.execute(transferData_doctorSearch);

                }

                break;


        }
    }
       
    // gets google places api link
    private String getUrl(double latitude, double longitude, String place)
    {
        StringBuilder googleUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleUrl.append("location=" + latitude + "," + longitude);
        googleUrl.append("&radius=" + proximityRadius);
        googleUrl.append("&type=" + place);
        googleUrl.append("&key=" + "");

        Log.d("Link", "url=" + googleUrl.toString());

        return googleUrl.toString();
    }

    public boolean checkUserLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            return false;
        }
        else return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case Request_User_Location_Code:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if (googleApiClient == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    protected synchronized void buildGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    
                
    // zooms camera over to the user's current location
    @Override
    public void onLocationChanged(@NonNull Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();


        lastLocation = location;

        if(currentUserLocationMark != null)
        {
            currentUserLocationMark.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("You are here");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        currentUserLocationMark = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(14));

        if(googleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }
}

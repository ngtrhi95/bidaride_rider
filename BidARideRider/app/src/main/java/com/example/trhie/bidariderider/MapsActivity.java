package com.example.trhie.bidariderider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import AutoComplete.AT_Adapter;
import AutoComplete.Constants;
import AutoComplete.RecyclerItemClickListener;
import Modules.DirectionFinderListener;
import Modules.PlaceInfo;
import Modules.Route;

/**
 * Created by trhie on 4/30/2017.
 */

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, LocationListener {
    private GoogleMap mMap;
    private EditText etAddress;
    private ImageView clearText;
    private String address;
    private PlaceInfo lastLocation = new PlaceInfo();;

    private AT_Adapter mATAdapter;
    private RecyclerView myRecyclerView;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    Location mLastLocation;
    Marker mCurrLocationMarker;
    private int idSrcmap = 0;
    private int idDesmap = 0;
    protected LocationManager locationManager;
    private LinearLayoutManager  myLinearLayoutManager;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 3000;

    protected GoogleApiClient myGoogleAppClient;

    private static  final LatLngBounds myBounds = new LatLngBounds(
            new LatLng(-0, 0), new LatLng(0, 0));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //buildGoogleApiClient();
        setContentView(R.layout.activity_maps);
        if (checkPlayServices()) {

            buildGoogleApiClient();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        etAddress = (EditText) findViewById(R.id.etAddress);
        clearText = (ImageView) findViewById(R.id.cleartext);
        mATAdapter = new AT_Adapter(this, R.layout.search_row, myGoogleAppClient, myBounds, null);
        myRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        myLinearLayoutManager = new LinearLayoutManager(this);
        myRecyclerView.setLayoutManager(myLinearLayoutManager);
        myRecyclerView.setAdapter(mATAdapter);

        etAddress.addTextChangedListener(new TextWatcher() {

            @SuppressLint("LongLogTag")
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.toString().equals("") && myGoogleAppClient.isConnected()) {
                    mATAdapter.getFilter().filter(s.toString());
                }
                else {
                    mATAdapter.removeData();
                }


            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });
        myRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        final AT_Adapter.PlaceAutocomplete item = mATAdapter.getItem(position);
                        final String placeId = String.valueOf(item.placeId);
                        Log.i("TAG", "Autocomplete item selected: " + item.description);
                        /*
                             Issue a request to the Places Geo Data API to retrieve a Place object with additional details about the place.
                         */

                        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                                .getPlaceById(myGoogleAppClient, placeId);
                        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if(places.getCount()==1){
                                    mATAdapter.removeData();
                                    lastLocation.setAddress(places.get(0).getAddress().toString());
                                    lastLocation.setLocation(places.get(0).getLatLng());
                                    etAddress.setText(places.get(0).getAddress());

                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(places.get(0).getLatLng());
                                    markerOptions.title("Your Position");
                                    mCurrLocationMarker = mMap.addMarker(markerOptions);

                                    //move map camera
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(places.get(0).getLatLng(),16));


                                    //Toast.makeText(getApplicationContext(),String.valueOf(places.get(0).getLatLng()),Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(getApplicationContext(),Constants.SOMETHING_WENT_WRONG,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        Log.i("TAG", "Clicked: " + item.description);
                        Log.i("TAG", "Called getPlaceById to get Place details for " + item.placeId);
                    }
                })
        );

        Intent it = getIntent();
        idSrcmap = 0;
        idDesmap = 0;

        idSrcmap= it.getIntExtra("SrcMap", 0);
        idDesmap= it.getIntExtra("DesMap", 0);

        if (idSrcmap == 0) {
            lastLocation.setIdMap(idDesmap);
        }
        else {
            lastLocation.setIdMap(idSrcmap);
        }
    }
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        myGoogleAppClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API).build();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng hcmus = new LatLng(10.762963, 106.682394);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcmus, 16));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                // TODO Auto-generated method stub
                //lstLatLngs.add(point);
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(point));


                getAddressFormLatLng(point);
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mMap.clear();
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return false;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, MapsActivity.this);
                if (locationManager != null) {
                    Location loc = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                    getAddressFormLatLng(latLng);
                }
                return true;
            }
        });
    }

    private void getAddressFormLatLng(LatLng point) {
        Geocoder geocoder;
        List<Address> lst_addresses;
        geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());

        try {
            lst_addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            Address location = lst_addresses.get(0);
            setEtAddress(location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));
        getAddressFormLatLng(latLng);
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

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void setEtAddress (Address location) {
        String addresslines = location.getAddressLine(0);
        int i = 1;
        while (location.getAddressLine(i) != null) {
            addresslines += ", " + location.getAddressLine(i);
            i++;
        }

        lastLocation.setAddress(addresslines);
        lastLocation.setLocation(new LatLng(location.getLatitude(), location.getLongitude()));
        etAddress.setText(addresslines);

    }

    public void selectLocation(View view) {
        if (lastLocation.getAddress() == null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Please choose a location!");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }
        Intent i = new Intent(MapsActivity.this, DirectionActivity.class);
        i.putExtra("lastLocation", lastLocation);
        startActivity(i);
    }

    @Override
    public void onClick(View v) {
        if(v==clearText){
            etAddress.setText("");
            mATAdapter.removeData();
        }
    }

    public void clearTextAddress(View view) {
        etAddress.setText("");

        //myRecyclerView.setAdapter(null);
        /*RecyclerView.ItemAnimator animator = myRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }*/
        mATAdapter.removeData();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!myGoogleAppClient.isConnected() && !myGoogleAppClient.isConnecting()){
            Log.v("Google API","Connecting");
            myGoogleAppClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(myGoogleAppClient.isConnected()){
            Log.v("Google API","Dis-Connecting");
            myGoogleAppClient.disconnect();
        }
    }
}

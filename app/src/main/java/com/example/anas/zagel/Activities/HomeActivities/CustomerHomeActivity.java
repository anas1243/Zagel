package com.example.anas.zagel.Activities.HomeActivities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.Activities.OrderPackageActivities.OrderPackageDetailsActivity;
import com.example.anas.zagel.Adaptors.PlaceAutocompleteAdapter;
import com.example.anas.zagel.Models.OnlineDeliveryMan;
import com.example.anas.zagel.Models.User;
import com.example.anas.zagel.Modules.DirectionFinderListener;
import com.example.anas.zagel.Modules.Route;
import com.example.anas.zagel.R;
import com.example.anas.zagel.Utilities.DrawerUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.anas.zagel.Activities.MainActivity.currentUser;
import static com.example.anas.zagel.Activities.MainActivity.mDatabaseOnlineDeliverymenReference;


public class CustomerHomeActivity extends AppCompatActivity implements DirectionFinderListener, OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {
    //Map
    private static final String TAG = "CustomerHomeActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMESSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168),
            new LatLng(71, 136)
    );
    private static final int ERROR_DIALOG_REQUEST = 9001;
    public static String vehicle = "driving";

    private int setLocation = 0;

    //Firebase
    public static String userId, name, email;
    public static ArrayList<OnlineDeliveryMan> onlineDeliveryMenList = new ArrayList();
    DatabaseReference mDatabasePackRef = MainActivity.mDatabasePackRef;
    FirebaseDatabase mDatabase = MainActivity.mDatabase;
    DatabaseReference mDatabaseReference = MainActivity.mDatabaseUsersReference;

    //Widgets
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    DrawerUtil drawer;
    SimpleRatingBar ratingBar;

    private ImageView mGps;  // get location
    private ImageView mGpss;  // set location to source textbox
    private ImageView mGpsd;  // set location to destination textbox

    // private image view for marker

    //Vars
    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutoCompleteAdapter;
    //direction vars
    private Button btnFindPath;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private Button orderButton;
    private LatLngBounds customerBounds;
    private User user = null;
//

    //private GoogleApiClient mGoogleApiClient;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sliding_up_panel);

        mGps = findViewById(R.id.ic_gps);
        mGpss = findViewById(R.id.ic_gps);
        mGpsd = findViewById(R.id.ic_gps);

        orderButton = findViewById(R.id.btn_order_sliding_up_panel);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerHomeActivity.this, OrderPackageDetailsActivity.class);
                startActivity(intent);
            }
        });

        ButterKnife.bind(this);
        // toolbar.setLogo(R.drawable.logo);
        setSupportActionBar(toolbar);

        drawer = new DrawerUtil(MainActivity.currentUser.getName(), MainActivity.currentUser.getEmail());
        drawer.getDrawer(CustomerHomeActivity.this, toolbar);

        //initializing package database reference
        mDatabasePackRef = mDatabase.getReference().child("orders");

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        getLocationPermission();

        removeMeFromOnlineDeliverymen();
    }

    private void removeMeFromOnlineDeliverymen() {
        if (currentUser.getUserType().equals("b")) {
            MainActivity.mDatabaseOnlineDeliverymenReference.child(currentUser.getId()).removeValue();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public String getVehicle() {
        return vehicle;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "location bitch !", Toast.LENGTH_SHORT).show();
                return;
            }
            //the location button on the upper right ^^
            mMap.setMyLocationEnabled(true);
            //if you want to disable this button
            // mMap.getUiSettings().setMyLocationButtonEnabled(true);
            //also you can play around with UiSettings
            //mMap.getUiSettings().setMapToolbarEnabled(true);
            Toast.makeText(this, "halla walahhay !", Toast.LENGTH_SHORT).show();


            init();


        }
    }

    private void retrieveAllOnlineDeliveryMen() {

        mDatabaseOnlineDeliverymenReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                onlineDeliveryMenList.clear();

                // Result will be holded Here
                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (customerBounds.contains(
                            new LatLng(
                                    Double.parseDouble(ds.getValue(OnlineDeliveryMan.class).getLocation().getLat())
                                    , Double.parseDouble(ds.getValue(OnlineDeliveryMan.class).getLocation().getLng())
                            )
                    )
                            ) {
                        // 1st we want his ID :(
                        OnlineDeliveryMan currentDeliveryman = ds.getValue(OnlineDeliveryMan.class);

//                                int currentrate = MainActivity.currentUser.getRatingsSum();
//                                int currentratecount = MainActivity.currentUser.getOrdersCounter();
//                                int rate = (currentrate / currentratecount);

                        //get deliveryman data
                        MainActivity.mDatabaseUsersReference.child(currentDeliveryman.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                user = dataSnapshot.getValue(User.class);
                                Log.e(TAG, "deliveryman name" + user.getName());
                                String vechile=user.getDeliveryMode().getVehicle();


                                MarkerOptions options = new MarkerOptions();
                                options.position(new LatLng(
                                        Double.parseDouble(ds.getValue(OnlineDeliveryMan.class).getLocation().getLat())
                                        , Double.parseDouble(ds.getValue(OnlineDeliveryMan.class).getLocation().getLng())
                                ));
                                options.title(user.getName());
                                options.snippet(String.valueOf(user.getRating()));

                                if (vechile.equals("w")) {
                                    vechile = "walkink";
                                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_maker_walking));

                                } else {
                                    vechile = "driving";
                                    options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_maker_driving));


                                }

                                mMap.addMarker(options);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                        //insert him to the list
                        onlineDeliveryMenList.add(currentDeliveryman); //add result into array list

                    } else {
                        Log.e(TAG, "onDataChange: " + Double.parseDouble(ds.getValue(OnlineDeliveryMan.class).getLocation().getLat()) + " is not in range");
                    }

                }
                TextView t = (TextView) findViewById(R.id.deliverymenCounter);
                t.setText(onlineDeliveryMenList.size() + " online deliverymen");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }


    public LatLngBounds toBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        Log.e(TAG, "toBounds: " + southwestCorner + " " + northeastCorner);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }


    private void init() {
        Log.e(TAG, "init: intializing");

        /*mSearchText1.setOnItemClickListener(mAutoCompleteClickListener);
        mSearchText2.setOnItemClickListener(mAutoCompleteClickListener);*/


        // Create a GoogleApiClient instance

        mPlaceAutoCompleteAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient, LAT_LNG_BOUNDS, null);

        //auto complete adapter takeCare of all those parametars :/



       /* mSearchText1.setAdapter(mPlaceAutoCompleteAdapter);
        mSearchText2.setAdapter(mPlaceAutoCompleteAdapter);*/


        mGpss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //going to map activity ^^
                setLocation = 0;
                getDeviceLocation("Trivial Guess", setLocation);


                Log.e(TAG, "onClick: Your Location Is The Source ^^ !!");
            }
        });

        mGpsd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //going to map activity ^^
                setLocation = 1;
                getDeviceLocation("Trivial Guess", setLocation);
                Log.e(TAG, "onClick: Your Location Is The destination ^^ !!");
            }
        });


        mGps.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.e(TAG, "onClick: Clicked gps icon");
                getDeviceLocation();
            }
        });
        hideSoftKeyboard();


        if (mMap != null) {

            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {

                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    View v = getLayoutInflater().inflate(R.layout.infowindow_layout, null);

                    TextView one = v.findViewById(R.id.gender_marker);
                    ratingBar = v.findViewById(R.id.rb_rating_dialog);

                    one.setText(marker.getTitle());

                    ratingBar.setRating(Float.parseFloat(marker.getSnippet()));

                    return v;


                }
            });


            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    Geocoder geocoder = new Geocoder(CustomerHomeActivity.this);
                    List<Address> list = null;


                    try {

                        double lat = marker.getPosition().latitude;
                        double lng = marker.getPosition().longitude;
                        list = geocoder.getFromLocation(lat, lng, 1);
                        Address add = list.get(0);
                        marker.setTitle(add.getLocality());
                        marker.showInfoWindow();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            });

        }


    }

    private void geoLocate(AutoCompleteTextView tv) {
        Log.e(TAG, "geoLocate: GeoLocating");
        String searchSting = tv.getText().toString();
        Geocoder geocoder = new Geocoder(CustomerHomeActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchSting, 1);

        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOExeception:" + e.getMessage());
        }
        if (list.size() > 0) {
            Address address = list.get(0);
            Log.e(TAG, "geoLocate: found a location " + address.toString());
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
            hideSoftKeyboard();
        }

    }

    private void getDeviceLocation() {
        Log.e(TAG, "getDeviceLocation: getting the devices current location");
        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                final Task location = mfusedLocationProviderClient.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.e(TAG, "onComplete: location found !");
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {
                                Log.e(TAG, "lat:" + currentLocation.getLatitude() + " lng:" + currentLocation.getLongitude());
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                        DEFAULT_ZOOM, "my location");
                                //even if locationCentered button clicked
                                customerBounds = toBounds(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 10000);
                                retrieveAllOnlineDeliveryMen();
                            }
                        } else {
                            Log.e(TAG, "onComplete: current location is null !");
                            Toast.makeText(CustomerHomeActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException" + e.getMessage());
        }
    }

    private void getDeviceLocation(final String TrivialGuess, final int flagLocation) {

        Log.e(TAG, "getDeviceLocation: getting the devices current location");
        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                final Task location = mfusedLocationProviderClient.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.e(TAG, "onComplete: location found ,your Location is the Source!");
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {

                                String LatLongToAdress = getCompleteAddressString(currentLocation.getLatitude(), currentLocation.getLongitude());
                                if (flagLocation == 0) {
                                    Log.e(TAG, "onComplete: location found ,your Location is the Source!");
                                    //mSearchText1.setText(LatLongToAdress);
                                    moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                            DEFAULT_ZOOM, TrivialGuess);
                                } else {

                                    Log.e(TAG, "onComplete: location found ,your Location is the destination!");
                                    //mSearchText2.setText(LatLongToAdress);
                                    moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                            DEFAULT_ZOOM, TrivialGuess);
                                }
                            }

                        } else {
                            Log.e(TAG, "onComplete: current location is null !");
                            Toast.makeText(CustomerHomeActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException" + e.getMessage());
        }
    }

    private void moveCamera(LatLng latlng, float zoom, String title) {
        Log.e(TAG, "moveCamera: moving the camera to Lat" + latlng.latitude + " ,Lon" + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));

        if (!title.equals("my location")) {
            MarkerOptions options = new MarkerOptions().position(latlng).title(title);
            mMap.addMarker(options);
        }
        hideSoftKeyboard();

    }

    private void initMap() {
        Log.e(TAG, "initMap: initilizing map");
        SupportMapFragment MapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        MapFragment.getMapAsync(CustomerHomeActivity.this);
    }

    private void getLocationPermission() {
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};
        Log.e(TAG, "getLocationPermission: getting location permission");

        //now we want to check whether or not permission is granted

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                //showSettingsAlert();
                if (isServicesOk()) {
                    initMap();
                }

            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMESSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMESSION_REQUEST_CODE);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.e(TAG, "onRequestPermissionsResult: Called!.");
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMESSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission faild");
                            return;
                        }
                    }
                    Log.e(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted = true;
                    //showSettingsAlert();
                    //initialize our map
                    if (isServicesOk()) {
                        initMap();
                    }
                }
            }
        }
    }

    private void geoLocateById(String placeID) {
        mGeoDataClient.getPlaceById(placeID).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    @SuppressLint("RestrictedApi") Place myPlace = places.get(0);
                    Log.e(TAG, "Place found: " + myPlace.getName());
                    moveCamera(myPlace.getLatLng(), DEFAULT_ZOOM, myPlace.getName().toString());
                    places.release();
                } else {
                    Log.e(TAG, "Place not found.");
                }
            }
        });
    }

    private void hideSoftKeyboard() {
        Log.e(TAG, "hideSoftKeyboard: hiding keyboard");
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    //fuctions for directions and durations

    private void sendRequest() {
        /*String origin = mSearchText1.getText().toString();
        String destination = mSearchText2.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(CustomerHomeActivity.this, origin, destination,vehicle).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> route) {

    }


//    public void onDirectionFinderSuccess(List<Route> routes) {
//        progressDialog.dismiss();
//        polylinePaths = new ArrayList<>();
//        originMarkers = new ArrayList<>();
//        destinationMarkers = new ArrayList<>();
//        int j = routes.size() - 1;
//        for (int k = routes.size() - 1; k != -1; k--) {
//            if (j != 0) {
//                PolylineOptions polylineOptions = new PolylineOptions().
//                        geodesic(true).
//                        color(Color.GRAY).
//                        width(10);
//                for (int i = 0; i < routes.get(k).points.size(); i++) {
//
//                    polylineOptions.add(routes.get(k).points.get(i));
//                }
//
//                polylinePaths.add(mMap.addPolyline(polylineOptions));
//                j--;
//                continue;
//            }
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routes.get(k).startLocation, 17));
//            /*((TextView) findViewById(R.id.tvDuration)).setText(routes.get(k).duration.text);
//            ((TextView) findViewById(R.id.tvDistance)).setText(routes.get(k).distance.text);
//*/
//            originMarkers.add(mMap.addMarker(new MarkerOptions()
//                    .title(routes.get(k).startAddress)
//                    .position(routes.get(k).startLocation).draggable(true)));
//            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
//
//                    .title(routes.get(k).endAddress)
//                    .position(routes.get(k).endLocation)));
//
//            PolylineOptions polylineOptions = new PolylineOptions().
//                    geodesic(true).
//                    color(Color.BLUE).
//                    width(10);
//
//            for (int i = 0; i < routes.get(k).points.size(); i++) {
//                polylineOptions.add(routes.get(k).points.get(i));
//            }
//
//
//            polylinePaths.add(mMap.addPolyline(polylineOptions));
//
//        }
//    }

    //return full address by knowing Latlong "when we clicked on Mylocation is the source"

    @SuppressLint("LongLogTag")
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(CustomerHomeActivity.this, Locale.getDefault());
        Log.e(TAG, "getCompleteAddressString: " + LONGITUDE + " " + LATITUDE);
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);

            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                Log.e(TAG, "getCompleteAddressString: Is this an ID ????????????? " + returnedAddress);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.e("My Current loction address", strReturnedAddress.toString());
            } else {
                Log.e("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("My Current loction address", "Canont get Address!" + e.getMessage());
        }
        return strAdd;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @SuppressLint("ShowToast")
    public boolean isServicesOk() {
        Log.d(TAG, "isServicesOk: checking google services ");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(CustomerHomeActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            //user can make map requests
            Log.d(TAG, "isServicesOk: Google play Services are working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOk: An Error accoured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(CustomerHomeActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
            return false;
        } else {
            Toast.makeText(this, "you can not make map request", Toast.LENGTH_SHORT);
            return false;
        }


    }
}


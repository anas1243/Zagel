package com.example.anas.zagel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
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
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anas.zagel.Activities.HomeActivities.DeliveryHomeActivity;
import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.Adaptors.PlaceAutocompleteAdapter;
import com.example.anas.zagel.Models.OnlineDeliveryMan;
import com.example.anas.zagel.Models.Package;
import com.example.anas.zagel.Models.User;
import com.example.anas.zagel.Modules.DirectionFinder;
import com.example.anas.zagel.Modules.DirectionFinderListener;
import com.example.anas.zagel.Modules.Route;
import com.example.anas.zagel.Utilities.DrawerUtil;
import com.example.anas.zagel.Utilities.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;

public class FromSourceToDestinationActivity extends AppCompatActivity implements DirectionFinderListener, OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LocationToSource";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMESSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168),
            new LatLng(71, 136)
    );
    private static final int ERROR_DIALOG_REQUEST = 9001;
    public static String locationforrequest;
    public static String vehicle = "driving";
    //firebase
    public static String userId, name, email;
    public static String LatLongToAdress;
    SimpleRatingBar ratingBar;
    String customerID;
    int deliveryToCustomerRating;
    DatabaseReference mDatabasePackRef = MainActivity.mDatabasePackRef;
    FirebaseDatabase mDatabase = MainActivity.mDatabase;
    DatabaseReference mDatabaseReference = MainActivity.mDatabaseUsersReference;
    Toolbar toolbar;
    DrawerUtil drawer;
    Package mPackage;
    //private View mapView =getLayoutInflater().inflate(R.layout.activity_map ,null);
    // private View yallaPackageOrder =getLayoutInflater().inflate(R.layout.order_package_details ,null);
    SlidingUpPanelLayout mSlidingLayout;
    int setLocation = 0;
    ArrayList<OnlineDeliveryMan> onlineDeliveryMenList = new ArrayList();
    Location currentLocation;
    Point p;
    private Route currentRoute;
    private Button toDestinationButton;
    private AutoCompleteTextView mSearchText1;
    private AutoCompleteTextView mSearchText2;
    private Button driving;
    private Button walking;
    private Button endTripButton;
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
    private User currentDeliferyman;
    //private GoogleApiClient mGoogleApiClient;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private AdapterView.OnItemClickListener mAutoCompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();
            final AutocompletePrediction item = mPlaceAutoCompleteAdapter.getItem(i);
            Log.e(TAG, "onItemClick: heyyyyyyyyyyyyyyyyyyyyy1111y");

            final String placeId = item.getPlaceId();

            //but this method  is not effective :/
            //final String PlaceName = item.getPrimaryText(null).toString();
            //geoLocate(PlaceName);
            geoLocateById(placeId);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_from_source_to_destinition);

        mPackage = (Package) getIntent().getSerializableExtra("package");
        currentLocation = (Location) getIntent().getSerializableExtra("currentLocation");
        final User customer = (User) getIntent().getSerializableExtra("customer");


        //  .getsourcse()


        ImageButton popupButton = findViewById(R.id.btn_info_trip);
        popupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                //Open popup window
                if (p != null)
                    Utility.showPopup(FromSourceToDestinationActivity.this, p, customer, mPackage);
            }
        });



        ButterKnife.bind(this);
        // toolbar.setLogo(R.drawable.logo);
        setSupportActionBar(toolbar);

        drawer = new DrawerUtil(MainActivity.currentUser.getName(), MainActivity.currentUser.getEmail());
        drawer.getDrawer(FromSourceToDestinationActivity.this, toolbar);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        getLocationPermission();
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

            Toast.makeText(this, "halla walahhay !", Toast.LENGTH_SHORT).show();
            init();


        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        int[] location = new int[2];
        ImageButton button = findViewById(R.id.btn_info_trip);

        // Get the x, y location and store it in the location[] array
        // location[0] = x, location[1] = y.
        button.getLocationOnScreen(location);

        //Initialize the Point with x, and y positions
        p = new Point();
        p.x = location[0];
        p.y = location[1];
    }

    private void init() {
        Log.e(TAG, "init: intializing");

        mPlaceAutoCompleteAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient, LAT_LNG_BOUNDS, null);


        if (mMap != null) {

            endTripButton = findViewById(R.id.btn_end_trip);
            endTripButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(FromSourceToDestinationActivity.this);
                    dialog.setContentView(R.layout.price_rating_dialog);
                    dialog.setCancelable(false);
                    TextView priceTextview = (TextView) dialog.findViewById(R.id.tv_price_dialog);
                    ratingBar = dialog.findViewById(R.id.rb_rating_dialog);
                    priceTextview.setText(mPackage.getPrice() + " EGP");

                    Button okaybtn = dialog.findViewById(R.id.btn_order_okay);
                    okaybtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.e(TAG,"Entered okaybtn clicklistener");
                            deliveryToCustomerRating = Math.round(ratingBar.getRating());
                            Log.e(TAG, "getting the ratingsSum");
                            Log.e(TAG, String.valueOf(deliveryToCustomerRating));

                            endTrip();
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                }
            });

            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {

                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.infowindow_place_layout, null);
                    TextView one = v.findViewById(R.id.addres_marker);
                    one.setText(marker.getTitle());

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
                    Geocoder geocoder = new Geocoder(FromSourceToDestinationActivity.this);
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

    private void endTrip() {
        String userId = MainActivity.currentUser.getId();
        DatabaseReference currentOnlineDeliverymanReference = MainActivity.mDatabaseOnlineDeliverymenReference.child(userId);

        // String onlineDeliveryID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.e(TAG,userId);

        currentOnlineDeliverymanReference.child("endTrip").setValue(true);
        currentOnlineDeliverymanReference.child("currentPackageID").setValue("");
        currentOnlineDeliverymanReference.child("acceptedOrder").setValue(false);
        final DatabaseReference customerReference = MainActivity.mDatabaseUsersReference.child(mPackage.getUid_sender());
        customerReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG,"ENTERED THE DATASNAPSHOT RATING CUSTOMER");
                User customer = dataSnapshot.getValue(User.class);
                int ratingsSum = customer.getRatingsSum();
                int counter = customer.getRatingCounter();
                counter++;
                int rating = Utility.calculateRating(counter, ratingsSum,
                        deliveryToCustomerRating);
                Log.e(TAG, "finalRating=" + rating);
                customerReference.child("ratingCounter").setValue(counter);
                customerReference.child("ratingsSum").setValue(ratingsSum + deliveryToCustomerRating);
                customerReference.child("rating").setValue(rating);
                Log.e(TAG, "rating" + rating);
                Intent intent = new Intent(FromSourceToDestinationActivity.this, DeliveryHomeActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    @SuppressLint("LongLogTag")
    public void getDeviceLocation() {
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
                                locationforrequest = getCompleteAddressString(currentLocation.getLatitude(), currentLocation.getLongitude());

                                Toast.makeText(FromSourceToDestinationActivity.this, "location in string is " + locationforrequest, Toast.LENGTH_SHORT).show();
                                if (locationforrequest != null) {
                                    Toast.makeText(FromSourceToDestinationActivity.this, "now in condition  ", Toast.LENGTH_SHORT).show();
                                    sendRequest();

                                }
                            }
                        } else {
                            Log.e(TAG, "onComplete: current location is null !");
                            Toast.makeText(FromSourceToDestinationActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
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

        MapFragment.getMapAsync(FromSourceToDestinationActivity.this);
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
        String origin = mPackage.getSource();
        String destination = mPackage.getDestination();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(FromSourceToDestinationActivity.this, origin, destination, vehicle).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();
        int j = routes.size() - 1;
        for (int k = routes.size() - 1; k != -1; k--) {
            if (j != 0) {
                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.GRAY).
                        width(10);
                for (int i = 0; i < routes.get(k).points.size(); i++) {

                    polylineOptions.add(routes.get(k).points.get(i));
                }

                polylinePaths.add(mMap.addPolyline(polylineOptions));
                j--;
                continue;
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routes.get(k).startLocation, 17));
            /*((TextView) findViewById(R.id.tvDuration)).setText(routes.get(k).duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(routes.get(k).distance.text);
*/
            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(routes.get(k).startAddress)
                    .position(routes.get(k).startLocation).draggable(true)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()

                    .title(routes.get(k).endAddress)
                    .position(routes.get(k).endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < routes.get(k).points.size(); i++) {
                polylineOptions.add(routes.get(k).points.get(i));
            }


            polylinePaths.add(mMap.addPolyline(polylineOptions));

        }
    }

    //return full address by knowing Latlong "when we clicked on Mylocation is the source"

    @SuppressLint("LongLogTag")
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(FromSourceToDestinationActivity.this, Locale.getDefault());
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
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(FromSourceToDestinationActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            //user can make map requests
            Log.d(TAG, "isServicesOk: Google play Services are working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOk: An Error accoured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(FromSourceToDestinationActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
            return false;
        } else {
            Toast.makeText(this, "you can not make map request", Toast.LENGTH_SHORT);
            return false;
        }


    }
}
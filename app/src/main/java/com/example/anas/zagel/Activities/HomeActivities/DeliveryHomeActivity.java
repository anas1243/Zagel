package com.example.anas.zagel.Activities.HomeActivities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.Adaptors.PlaceAutocompleteAdapter;
import com.example.anas.zagel.Models.OnlineDeliveryMan;
import com.example.anas.zagel.Models.Package;
import com.example.anas.zagel.Modules.DirectionFinder;
import com.example.anas.zagel.Modules.DirectionFinderListener;
import com.example.anas.zagel.Modules.Route;
import com.example.anas.zagel.NewOrderedPackageActivity;
import com.example.anas.zagel.R;
import com.example.anas.zagel.Utilities.DrawerUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DeliveryHomeActivity extends AppCompatActivity implements DirectionFinderListener, OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
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
    public static String userId, name, email;
    //Map
    public static Location currentLocation;
    //Firebase
    //public static DatabaseReference mDatabasePackRef;
    Package currentOrder = MainActivity.currentOrder;
    DatabaseReference mDatabaseUsersReference = MainActivity.mDatabaseUsersReference;
    DatabaseReference mDatabasePackRef = MainActivity.mDatabasePackRef;
    DatabaseReference currentOnlineDeliverymanReference;
    FirebaseDatabase mDatabase = MainActivity.mDatabase;
    //Widgets
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    DrawerUtil drawer;
    //private View mapView =getLayoutInflater().inflate(R.layout.activity_map ,null);
    // private View yallaPackageOrder =getLayoutInflater().inflate(R.layout.order_package_details ,null);
    SlidingUpPanelLayout mSlidingLayout;
    //Vars
    private ImageView mGps;  // get location
    private Button driving;
    private Button walking;
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
    private Package mPackage;

    private LocationManager locationManager;
    private String provider;


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

            Log.e(TAG, "onItemClick: heyyyyyyyyyyyyyyyyyyyyyy");
            Log.e(TAG, "onItemClick: you have clicked on this location" + placeId);

            geoLocateById(placeId);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivary_home);
        mGps = findViewById(R.id.ic_gps);
        //  View yallaPackageOrder =getLayoutInflater().inflate(R.layout.order_package_details ,null);
        //btnFindPath = findViewById(R.id.btnFindPath);


        //initializing package database reference
        //mDatabasePackRef = mDatabase.getReference().child("orders");

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        ButterKnife.bind(this);
        // toolbar.setLogo(R.drawable.logo);
        setSupportActionBar(toolbar);

        drawer = new DrawerUtil(MainActivity.currentUser.getName(), MainActivity.currentUser.getEmail());
        drawer.getDrawer(DeliveryHomeActivity.this, toolbar);
 
        getLocationPermission();
        selectVehicle();
        listenToOrderedPackage();

    }


    private void selectVehicle() {
        final Dialog dialog = new Dialog(DeliveryHomeActivity.this);
        dialog.setContentView(R.layout.vehicle_dialog);
        dialog.setCancelable(false);
        ImageButton walkingButton = dialog.findViewById(R.id.btn_walking);
        walkingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.currentUser.deliveryMode.setVehicle("w");
                MainActivity.mDatabaseUsersReference.child(MainActivity.currentUser.getId()).child("deliveryMode").child("vehicle").setValue("w");
                dialog.cancel();
            }
        });
        ImageButton drivingButton = dialog.findViewById(R.id.btn_driving);
        drivingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.currentUser.deliveryMode.setVehicle("d");
                MainActivity.mDatabaseUsersReference.child(MainActivity.currentUser.getId()).child("deliveryMode").child("vehicle").setValue("d");
                dialog.cancel();
            }
        });
        dialog.show();
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

    private void initMap() {
        Log.e(TAG, "initMap: initilizing map");
        SupportMapFragment MapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        MapFragment.getMapAsync(DeliveryHomeActivity.this);

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
                    // showSettingsAlert();
                    //initialize our map
                    if (isServicesOk()) {
                        initMap();
                    }
                }
            }
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
                            currentLocation = (Location) task.getResult();
                            if (currentLocation != null) {
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                        DEFAULT_ZOOM, "my location");
                                com.example.anas.zagel.Models.Location delivaryManLocation = new com.example.anas.zagel.Models.Location(currentLocation.getLatitude() + "", currentLocation.getLongitude() + "");
                                OnlineDeliveryMan onlineDeliveryMan = new OnlineDeliveryMan();
                                onlineDeliveryMan.setId(MainActivity.currentUser.getId());
                                onlineDeliveryMan.setLocation(delivaryManLocation);

                                MainActivity.mDatabaseOnlineDeliverymenReference.child(onlineDeliveryMan.getId()).setValue(onlineDeliveryMan);
                                Log.e(TAG, "Latlng " + delivaryManLocation.getLat() + " " + delivaryManLocation.getLng() + "");
                                //Calling PackageListener Function inorder to check if the online deliveryman has a package or not

                            }
                        } else {
                            Log.e(TAG, "onComplete: current location is null !");
                            Toast.makeText(DeliveryHomeActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                this.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

//Choosing the best criteria depending on what is available.
                Criteria criteria = new Criteria();
                provider = locationManager.getBestProvider(criteria, false);
                //provider = LocationManager.GPS_PROVIDER; // We want to use the GPS

// Initialize the location fields
                Location llocation = locationManager.getLastKnownLocation(provider);




            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException" + e.getMessage());
        }
    }

    private void listenToOrderedPackage() {
        currentOnlineDeliverymanReference = MainActivity.mDatabaseOnlineDeliverymenReference.child(MainActivity.currentUser.getId());
        DatabaseReference currentPackageReference = currentOnlineDeliverymanReference.child("currentPackageID");
        currentPackageReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String packageId = (String) dataSnapshot.getValue();
                    if (!packageId.isEmpty() && !packageId.equals("")) {
                        sendNotification(packageId);
                        showNewOrderDialog(packageId);
                    }
                    //MainActivity.mDatabaseOnlineDeliverymenReference.child(onlineDeliveryMan.getId()).setValue(onlineDeliveryMan);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void showNewOrderDialog(final String packageId) {
        final Dialog dialog = new Dialog(DeliveryHomeActivity.this);
        dialog.setContentView(R.layout.new_order_dialog);
        dialog.setCancelable(false);
        Button checkoutButton = dialog.findViewById(R.id.btn_check_out_new_order_dialog);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliveryHomeActivity.this, NewOrderedPackageActivity.class);
                intent.putExtra("packageID", packageId);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    public void sendNotification(String packageID) {
        Intent intent = new Intent(getApplicationContext(), NewOrderedPackageActivity.class);
        intent.putExtra("location", currentLocation);
        intent.putExtra("packageID", packageID);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, 0);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("New package to pick up")
                .setContentText("Hurry Up!...")
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.zagel_logo_gray, "Chat", pendingIntent)
                .setSmallIcon(R.drawable.zagel_logo_gray)
                .setSound(defaultSoundUri)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(1, notification);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private void moveCamera(LatLng latlng, float zoom, String title) {
        Log.e(TAG, "moveCamera: moving the camera to Lat" + latlng.latitude + " ,Lon" + latlng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));


    }



    private void init() {
        Log.e(TAG, "init: intializing");

        mGps.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.e(TAG, "onClick: Clicked gps icon");
                getDeviceLocation();
            }
        });
        hideSoftKeyboard();



    }

    private void geoLocate(AutoCompleteTextView tv) {
        Log.e(TAG, "geoLocate: GeoLocating");
        String searchSting = tv.getText().toString();
        Geocoder geocoder = new Geocoder(DeliveryHomeActivity.this);
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
        String origin = "";  // from firebase //
        String destination = ""; // from firebase //
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(DeliveryHomeActivity.this, origin, destination, vehicle).execute();
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
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
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
            Log.e("My Current loction address", "Canont get Address!");
        }
        return strAdd;
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @SuppressLint("ShowToast")
    public boolean isServicesOk() {
        Log.d(TAG, "isServicesOk: checking google services ");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(DeliveryHomeActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            //user can make map requests
            Log.d(TAG, "isServicesOk: Google play Services are working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOk: An Error accoured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(DeliveryHomeActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
            return false;
        } else {
            Toast.makeText(this, "you can not make map request", Toast.LENGTH_SHORT);
            return false;
        }
    }

    public String getVehicle() {
        return vehicle;
    }


    @Override
    public void onLocationChanged(Location location) {


        Log.d(TAG, "GPS LocationChanged");
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        Log.d(TAG, "Received GPS request for " + String.valueOf(lat) + "," + String.valueOf(lng) + " , ready to rumble!");

        if (location != null) {
            moveCamera(new LatLng(location.getLatitude(), location.getLongitude()),
                    DEFAULT_ZOOM, "my location");
            com.example.anas.zagel.Models.Location delivaryManLocation = new com.example.anas.zagel.Models.Location(location.getLatitude() + "", location.getLongitude() + "");
            OnlineDeliveryMan onlineDeliveryMan = new OnlineDeliveryMan();
            onlineDeliveryMan.setId(MainActivity.currentUser.getId());
            onlineDeliveryMan.setLocation(delivaryManLocation);

            MainActivity.mDatabaseOnlineDeliverymenReference.child(onlineDeliveryMan.getId()).setValue(onlineDeliveryMan);
            Log.e(TAG, "Latlng " + delivaryManLocation.getLat() + " " + delivaryManLocation.getLng() + "");
            //Calling PackageListener Function inorder to check if the online deliveryman has a package or not

        }
        MainActivity.mDatabaseOnlineDeliverymenReference.child(MainActivity.currentUser.getId()).removeValue();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "Resuming");
        //locationManager.requestLocationUpdates(provider,400,1,this.);
    }

}
package com.example.anas.zagel.Activities.OrderPackageActivities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.Adaptors.PlaceAutocompleteAdapter;
import com.example.anas.zagel.Models.OnlineDeliveryMan;
import com.example.anas.zagel.Models.Package;
import com.example.anas.zagel.Models.User;
import com.example.anas.zagel.Modules.DirectionFinder;
import com.example.anas.zagel.Modules.DirectionFinderListener;
import com.example.anas.zagel.Modules.Route;
import com.example.anas.zagel.R;
import com.example.anas.zagel.Utilities.DrawerUtil;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.anas.zagel.Activities.MainActivity.mDatabaseOnlineDeliverymenReference;
import static com.example.anas.zagel.Activities.MainActivity.mDatabaseUsersReference;


public class OrderPackageMapActivity extends AppCompatActivity implements DirectionFinderListener, OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {
    //Map
    private static final String TAG = "OrderPackageMapActivity";
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
    //public static DatabaseReference mDatabasePackRef;
    public static String userId, name, email;
    public static ArrayList<OnlineDeliveryMan> onlineDeliveryMenList = new ArrayList();
    public DatabaseReference acceptedReference;
    //iterator
    int i = 0;
    //Firebase
    DatabaseReference onlineDeliverymanReference = mDatabaseOnlineDeliverymenReference;
    DatabaseReference mDatabasePackRef = MainActivity.mDatabasePackRef;
    FirebaseDatabase mDatabase = MainActivity.mDatabase;
    User currentUser = MainActivity.currentUser;
    DatabaseReference mDatabaseReference = MainActivity.mDatabaseUsersReference;
    //Widgets
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    DrawerUtil drawer;
    //private View mapView =getLayoutInflater().inflate(R.layout.activity_map ,null);
    // private View yallaPackageOrder =getLayoutInflater().inflate(R.layout.order_package_details ,null);
    SlidingUpPanelLayout mSlidingLayout;
    int setLocation = 0;
    String origin;
    String destination;
    String id;
    String onlineSetPackage = "";
    DatabaseReference currentOnlineDeliverymanReference;
    private AutoCompleteTextView mSearchText1;
    private AutoCompleteTextView mSearchText2;
    private ImageView mGps;  // get location
    private ImageView mGpss;  // set location to source textbox
    private ImageView mGpsd;  // set location to destination textbox
    private Button driving;
    private Button walking;
    private TextView estimated_price;
    private double EstimatedPrice;
    //Vars
    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutoCompleteAdapter;
    //direction vars
    private Button btn_estimate_price;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private int duration;
    private int distance;
    private Package mPackage;
    private int weightCategory;
    private boolean PackageIsBreakable;
    private LatLng packageSource;
    private LatLngBounds PackageBounds;
    private User currentDeliveryman;
    private FirebaseUser firebaseUser;
    private Timer myTimer;
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
    private AdapterView.OnItemClickListener mAutoCompleteClickListeners1 = new AdapterView.OnItemClickListener() {
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

            geoLocateById(placeId, "source");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_package_map);
        mGps = findViewById(R.id.ic_gps);
        //  View yallaPackageOrder =getLayoutInflater().inflate(R.layout.order_package_details ,null);


        mGpss = findViewById(R.id.ic_gpss);
        mGpsd = findViewById(R.id.ic_gpsd);
        mSearchText1 = findViewById(R.id.input_search1);
        mSearchText2 = findViewById(R.id.input_search2);
        btn_estimate_price = findViewById(R.id.btn_estimate_price);
        /*driving = findViewById(R.id.driving);
        walking = findViewById(R.id.walking);*/

        ButterKnife.bind(this);
        // toolbar.setLogo(R.drawable.logo);
        setSupportActionBar(toolbar);


        drawer = new DrawerUtil(currentUser.getName(), currentUser.getEmail());
        drawer.getDrawer(OrderPackageMapActivity.this, toolbar);

        //initializing package database reference
        mDatabasePackRef = mDatabase.getReference().child("orders");

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);
        getLocationPermission();
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

    private void estimatePrice() {
        final Dialog dialog = new Dialog(OrderPackageMapActivity.this);
        dialog.setContentView(R.layout.price_dialog);
        dialog.setCancelable(true);
        mPackage = (Package) getIntent().getSerializableExtra("package");
        weightCategory = mPackage.getWeight();
        PackageIsBreakable = mPackage.isBreakable();
        int breakablePrice;
        if (PackageIsBreakable)
            breakablePrice = 2;
        else
            breakablePrice = 0;

        EstimatedPrice = Math.ceil(duration / 60 * 0.2 + distance / 1000 * 0.5 + weightCategory * 3 + breakablePrice);
        Log.e(TAG, "estimatePrice: " + duration / 60 * 0.2 + " " + distance / 1000 * 0.2 + " " + weightCategory + " " + PackageIsBreakable);
        estimated_price = dialog.findViewById(R.id.tv_price_dialog);
        estimated_price.setText(EstimatedPrice + "EGP");

        Button cancelButton = dialog.findViewById(R.id.btn_cancel_price_dialog);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button orderButton = dialog.findViewById(R.id.btn_order_dialog);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchText1.getText().toString() != "" && mSearchText2.getText().toString() != "") {
                    dialog.findViewById(R.id.linearLayout2).setVisibility(View.GONE);
                    dialog.findViewById(R.id.progressBar2).setVisibility(View.VISIBLE);
                    retrieveAllOnlineDeliveryMen();
                    setPackageData();
                    placePackage();
                    dialog.dismiss();
                    //sortNearDeliveryman();
                    //findDeliveryman();
                    //dialog.findViewById(R.id.progressBar2).setVisibility(View.GONE);
                    //Intent intent = new Intent(OrderPackageMapActivity.this, PackageOnTheWayActivity.class);
                    // startActivity(intent);
                } else {
                    Toast.makeText(OrderPackageMapActivity.this, "Please enter source and destination for your package ! ", Toast.LENGTH_SHORT).show();
                }
            }
        });


        dialog.show();
    }

    private void setPackageData() {
        mPackage.setUid_sender(currentUser.getId());
        mPackage.setDestination(destination);
        mPackage.setSource(origin);
        mPackage.setId(currentUser.getId() + currentUser.getOrdersCounter());
        mPackage.setPrice(EstimatedPrice + "");
    }

    /*private boolean isAcceptedByDeliveryman(int i) {

    }*/


    private void sortNearDeliveryman() {
        //TODO replace with sherif's algorithm

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public String getVehicle() {
        return vehicle;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //the location button on the upper right ^^
            mMap.setMyLocationEnabled(true);
            //if you want to disable this button
            // mMap.getUiSettings().setMyLocationButtonEnabled(true);
            //also you can play around with UiSettings
            //mMap.getUiSettings().setMapToolbarEnabled(true);
            init();

        }
    }


    private double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2 - lat1);  // deg2rad below
        double dLon = deg2rad(lon2 - lon1);
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c; // Distance in km
        return d;
    }

    double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }


    private void retrieveAllOnlineDeliveryMen() {
        mDatabaseOnlineDeliverymenReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double test;
                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    OnlineDeliveryMan deliveryMan = ds.getValue(OnlineDeliveryMan.class);
                    if (PackageBounds.contains(
                            new LatLng(
                                    Double.parseDouble(deliveryMan.getLocation().getLat())
                                    , Double.parseDouble(deliveryMan.getLocation().getLng())
                            )
                    )
                            ) {
                        String id = deliveryMan.getId();
                        DatabaseReference currentDeliverymanRef = MainActivity.mDatabaseUsersReference.child(id);
                        currentDeliverymanRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                currentDeliveryman = dataSnapshot.getValue(User.class);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        test = getDistanceFromLatLonInKm(packageSource.latitude
                                , packageSource.longitude
                                , Double.parseDouble(deliveryMan.getLocation().getLat())
                                , Double.parseDouble(deliveryMan.getLocation().getLng()));

                        deliveryMan.setKilometers(test);
                        onlineDeliveryMenList.add(deliveryMan); //add result into array list
                    } else {
                        Log.e(TAG, "onDataChange: " + Double.parseDouble(deliveryMan.getLocation().getLat()) + " is not in range");
                    }
                }
                if (onlineDeliveryMenList.size() > 1) {
                    Collections.sort(onlineDeliveryMenList, new Comparator<OnlineDeliveryMan>() {
                        public int compare(OnlineDeliveryMan s1, OnlineDeliveryMan s2) {
                            if (s1.getKilometers() > s2.getKilometers())
                                return 1;
                            else if (s1.getKilometers() == s2.getKilometers())
                                return 0;
                            else return -1;
                        }
                    });
                }
                Log.e(TAG, "onDataChange: we have found " + onlineDeliveryMenList.size() + " for you !");

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public void placePackage() {
        int incrementedCounter = currentUser.increaseCounterByOne();
        mPackage.setId(currentUser.getId() + incrementedCounter);
        mDatabaseUsersReference.child(currentUser.getId()).child("ordersCounter").setValue(incrementedCounter);

        Uri filePath = Uri.parse((String) getIntent().getSerializableExtra("imagePath"));

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

       final StorageReference  ref = MainActivity.storageReference.child("package_images/" + UUID.randomUUID().toString());
        ref.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        mPackage.setPhotoUrl(ref.getDownloadUrl().toString());
                        mDatabasePackRef.child(mPackage.getId()).setValue(mPackage);
                        mDatabasePackRef.child(mPackage.getId()).child("photoUrl").setValue(ref.getDownloadUrl().toString());
                        Intent intent = new Intent(OrderPackageMapActivity.this, WaitingForDeliverymanAcceptanceActivity.class);
                        intent.putExtra("package", mPackage);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(OrderPackageMapActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        progressDialog.setMessage((int) progress + "%" + "uploaded");
                    }
                });

        //here you can call variables named "origin" and "destination"
        Log.e(TAG, "placePackage: " + origin + destination);

    }
    private void init() {
        Log.e(TAG, "init: intializing");

        mSearchText1.setOnItemClickListener(mAutoCompleteClickListeners1);
        mSearchText2.setOnItemClickListener(mAutoCompleteClickListener);


        // Create a GoogleApiClient instance

        mPlaceAutoCompleteAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient, LAT_LNG_BOUNDS, null);

        //auto complete adapter takeCare of all those parametars :/


        mSearchText1.setAdapter(mPlaceAutoCompleteAdapter);
        mSearchText2.setAdapter(mPlaceAutoCompleteAdapter);


        mGpss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //going to map activity ^^
                setLocation = 0;
                getDeviceLocation("SetBounds", setLocation);


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

        mSearchText1.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        keyEvent.getKeyCode() == KeyEvent.ACTION_DOWN ||
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        ) {
                    Log.e(TAG, "init: intializing succcccccccccc");
                    //start searching ^^

                    Log.e(TAG, "onEditorAction: the Selected item is " + mPlaceAutoCompleteAdapter.getmResultList().get(0).toString());
                    mSearchText1.dismissDropDown();
                    mSearchText1.setText(mPlaceAutoCompleteAdapter.getmResultList().get(0).getFullText(null));
                    hideSoftKeyboard();
                    geoLocateById(mPlaceAutoCompleteAdapter.getmResultList().get(0).getPlaceId(), "source");
                }
                return false;
            }
        });

        mSearchText2.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        keyEvent.getKeyCode() == KeyEvent.ACTION_DOWN ||
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        ) {
                    Log.e(TAG, "init: intializing ");
                    //start searching ^^
                    Log.e(TAG, "onEditorAction: the Selected item is " + mPlaceAutoCompleteAdapter.getmResultList().get(0).toString());
                    mSearchText2.dismissDropDown();
                    mSearchText2.setText(mPlaceAutoCompleteAdapter.getmResultList().get(0).getFullText(null));
                    hideSoftKeyboard();
                    geoLocateById(mPlaceAutoCompleteAdapter.getmResultList().get(0).getPlaceId(), "destination");
                }
                return false;
            }
        });

        btn_estimate_price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mMap.clear();
                Log.e(TAG, "onClick: yatara eiiiiiiiiiiiiih" + getVehicle());
                sendRequest();
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.e(TAG, "onClick: Clicked gps icon");
                getDeviceLocation();
            }
        });
        hideSoftKeyboard();

        /*driving.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.e(TAG, "onClick: Clicked driving icon");
                vehicle = "driving";

            }
        });
        walking.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.e(TAG, "onClick: Clicked walking icon");
                vehicle = "walking";

            }
        });*/

        if (mMap != null) {
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
                    Geocoder geocoder = new Geocoder(OrderPackageMapActivity.this);
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
        Geocoder geocoder = new Geocoder(OrderPackageMapActivity.this);
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
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                        DEFAULT_ZOOM, "my location");
                            }
                        } else {
                            Log.e(TAG, "onComplete: current location is null !");
                            Toast.makeText(OrderPackageMapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
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
                                    mSearchText1.setText(LatLongToAdress);
                                    moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                            DEFAULT_ZOOM, TrivialGuess);
                                    if (TrivialGuess.equals("SetBounds")) {
                                        packageSource = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                        PackageBounds = toBounds(packageSource, 10000);
                                    }
                                } else {
                                    Log.e(TAG, "onComplete: location found ,your Location is the destination!");
                                    mSearchText2.setText(LatLongToAdress);
                                    moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                            DEFAULT_ZOOM, TrivialGuess);
                                }
                            }
                        } else {
                            Log.e(TAG, "onComplete: current location is null !");
                            Toast.makeText(OrderPackageMapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
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

        MapFragment.getMapAsync(OrderPackageMapActivity.this);
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

    private void geoLocateById(String placeID, final String source) {
        mGeoDataClient.getPlaceById(placeID).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse places = task.getResult();
                    @SuppressLint("RestrictedApi") Place myPlace = places.get(0);
                    Log.e(TAG, "Place found: " + myPlace.getName());
                    moveCamera(myPlace.getLatLng(), DEFAULT_ZOOM, myPlace.getName().toString());
                    if (source.equals("source")) {
                        packageSource = myPlace.getLatLng();
                        PackageBounds = toBounds(packageSource, 10000);
                    }
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
        origin = mSearchText1.getText().toString();
        destination = mSearchText2.getText().toString();
        if (origin.isEmpty()) {
            mSearchText1.setError("Please enter origin address!");
            return;
        }
        if (destination.isEmpty()) {
            mSearchText2.setError("Please enter destination address!");
            return;
        }

        try {
            new DirectionFinder(OrderPackageMapActivity.this, origin, destination, vehicle).execute();
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
            duration = routes.get(k).duration.value;
            distance = routes.get(k).distance.value;
            Log.e(TAG, "onDirectionFinderSuccess: " + distance + " " + duration);
            estimatePrice();

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
        Geocoder geocoder = new Geocoder(OrderPackageMapActivity.this, Locale.getDefault());
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
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(OrderPackageMapActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            //user can make map requests
            Log.d(TAG, "isServicesOk: Google play Services are working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOk: An Error accoured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(OrderPackageMapActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
            return false;
        } else {
            Toast.makeText(this, "you can not make map request", Toast.LENGTH_SHORT);
            return false;
        }


    }
}
package com.example.anas.zagel.Activities.OrderPackageActivities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.anas.zagel.Activities.HomeActivities.CustomerHomeActivity;
import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.Models.Package;
import com.example.anas.zagel.Models.User;
import com.example.anas.zagel.R;
import com.example.anas.zagel.Utilities.DrawerUtil;
import com.example.anas.zagel.Utilities.Utility;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import butterknife.BindView;

import static com.example.anas.zagel.Activities.MainActivity.currentUser;
import static com.example.anas.zagel.Activities.MainActivity.mDatabaseOnlineDeliverymenReference;

public class PackageOnTheWayActivity extends AppCompatActivity {
    private static final String TAG ="PackageOnTheWayActivity";
    Package mPackage;

    //Widgets
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    DrawerUtil drawer;
    SimpleRatingBar ratingBar;
    String customerID;
    int customerToDeliveryRating;
    User customer;
    double finalRating;
    int totalRating, ratingsSum;
    private ValueEventListener valueEventListener;
    private DatabaseReference endTripReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_on_the_way);
        mPackage = (Package)getIntent().getSerializableExtra("package");

        //ButterKnife.bind(this);
        // toolbar.setLogo(R.drawable.logo);
        //setSupportActionBar(toolbar);
        //drawer = new DrawerUtil(MainActivity.currentUser.getName(), MainActivity.currentUser.getEmail());
        //drawer.getDrawer(PackageOnTheWayActivity.this, toolbar);

        endTripReference = mDatabaseOnlineDeliverymenReference.child(currentUser.getAttachedDeliveryId()).child("endTrip");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((boolean) dataSnapshot.getValue()) {

                    final Dialog dialog = new Dialog(PackageOnTheWayActivity.this);
                    dialog.setContentView(R.layout.price_rating_dialog);
                    dialog.setCancelable(false);

                    TextView priceTextView = dialog.findViewById(R.id.tv_price_dialog);
                    priceTextView.setText(mPackage.getPrice() + " EGP");

                    Button okaybtn = dialog.findViewById(R.id.btn_order_okay);
                    okaybtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.e(TAG,"Entered okaybtn clicklistener");
                            ratingBar = dialog.findViewById(R.id.rb_rating_dialog);
                            customerToDeliveryRating = Math.round(ratingBar.getRating());
                            Log.e(TAG, "getting the ratingsSum");
                            Log.e(TAG, String.valueOf(customerToDeliveryRating));

                            endTrip();

                        }
                    });

                    dialog.show();
                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        endTripReference.addValueEventListener(valueEventListener);
    }

    private void endTrip() {
        String userId = MainActivity.currentUser.getId();
        DatabaseReference currentCustomerReference = MainActivity.mDatabaseUsersReference.child(userId);
        // FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //String onlineDeliveryID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.e(TAG,userId);
        //  Log.e(TAG, String.valueOf(mPackage));
        final DatabaseReference deliveryReference = MainActivity.mDatabaseUsersReference.child(MainActivity.currentUser.getAttachedDeliveryId());
//attachedDeliveryId
        currentCustomerReference.child("attachedDeliveryId").setValue("");
        //currentCustomerReference.child("currentPackageID").setValue("");
        //currentCustomerReference.child("acceptedOrder").setValue(false);

        deliveryReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG,"ENTERED THE DATASNAPSHOT RATING CUSTOMER");
                customer = dataSnapshot.getValue(User.class);
                int ratingsSum = customer.getRatingsSum();
                int counter = customer.getRatingCounter();
                counter++;
                int rating = Utility.calculateRating(counter, ratingsSum,
                        customerToDeliveryRating);
                Log.e(TAG, "finalRating=" + rating);
                deliveryReference.child("ratingsSum").setValue(ratingsSum + customerToDeliveryRating);
                deliveryReference.child("ratingCounter").setValue(counter);
                deliveryReference.child("rating").setValue(rating);
                Log.e(TAG, "rating" + rating);
                Intent intent = new Intent(PackageOnTheWayActivity.this, CustomerHomeActivity.class);
                startActivity(intent);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endTripReference.removeEventListener(valueEventListener);
    }
}

package com.example.anas.zagel;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.Models.Package;
import com.example.anas.zagel.Models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewOrderedPackageActivity extends AppCompatActivity {
    public static DatabaseReference currentOnlineDeliverymanReference;
    Package mPackage;
    User customer;
    TextView packageNameTextView, packageDescriptionTextView, packageBreakableTextView, packageWeightTextView, packagePayPointTextView, packageSourceTextView, packageDestinationTextView, customerNameTextView;
    CircleImageView packageImageView, customerImageView;
    SimpleRatingBar customerRatingBar;
    String packageID = "";
    Location currentLocation;
    Button yallaButton, declineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_ordered_package);

        packageID = (String) getIntent().getSerializableExtra("packageID");
        currentLocation = (Location) getIntent().getSerializableExtra("currentLocation");
        packageNameTextView = findViewById(R.id.tv_package_name_popup);
        packageDescriptionTextView = findViewById(R.id.tv_package_description_popup);
        packageBreakableTextView = findViewById(R.id.tv_package_breakable_new_order);
        packageWeightTextView = findViewById(R.id.tv_package_weight_new_order);
        packagePayPointTextView = findViewById(R.id.tv_package_pay_point_new_order);
        packageSourceTextView = findViewById(R.id.tv_package_source_new_order);
        packageDestinationTextView = findViewById(R.id.tv_package_destination_new_order);
        packageImageView = findViewById(R.id.iv_package_photo_new_order);

        customerNameTextView = findViewById(R.id.tv_customer_name_popup);
        customerImageView = findViewById(R.id.iv_customer_photo_new_order);
        customerRatingBar = findViewById(R.id.rb_customer_rating_new_order);

        yallaButton = findViewById(R.id.btn_yalla_new_order);
        //declineButton = findViewById(R.id.btn_decline_new_order);

        getPackage();


        //customerNameTextView.setText(mPackage.getUid_sender());


        //Set source desination


    }

    private void getPackage() {
        DatabaseReference myPackageReference = MainActivity.mDatabasePackRef.child(packageID);
        myPackageReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mPackage = dataSnapshot.getValue(Package.class);

                setPackageInfo();
                getCustomer();

                setButtonsListener();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getCustomer() {
        DatabaseReference customerReference = MainActivity.mDatabaseUsersReference.child(mPackage.getUid_sender());
        customerReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                customer = dataSnapshot.getValue(User.class);
                setCustomerInfo();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void setButtonsListener() {


        yallaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = MainActivity.currentUser.getId();
                final DatabaseReference currentOnlineDeliverymanReference = MainActivity.mDatabaseOnlineDeliverymenReference.child(userId);
                currentOnlineDeliverymanReference.child("acceptedOrder").setValue(true);
                Intent intent = new Intent(NewOrderedPackageActivity.this, FromLocationToSourceActivity.class);
                intent.putExtra("package", mPackage);
                intent.putExtra("location", currentLocation);
                intent.putExtra("customer", customer);
                startActivity(intent);
            }
        });

        /*declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declinePackage();
            }
        });*/

    }

    /*private void declinePackage() {
        String userId = MainActivity.currentUser.getId();
        currentOnlineDeliverymanReference = MainActivity.mDatabaseOnlineDeliverymenReference.child(userId);
        currentOnlineDeliverymanReference.child("acceptedOrder").setValue(false);
        currentOnlineDeliverymanReference.child("currentPackageID").setValue("");
        Intent intent = new Intent(NewOrderedPackageActivity.this, DeliveryHomeActivity.class);
        startActivity(intent);
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //declinePackage();
        //TODO waring eno et7tlo el package 5las
    }

    public void setPackageInfo() {
        packageNameTextView.setText(mPackage.getName());
        packageDescriptionTextView.setText(mPackage.getDescription());

        if (mPackage.isBreakable())
            packageBreakableTextView.setText("breakable");
        else
            packageBreakableTextView.setText("Not breakable");

        switch (mPackage.getWeight()) {
            case 1:
                packageWeightTextView.setText("Less Than 1 kilo");
                break;
            case 2:
                packageWeightTextView.setText("1-5 Kilos");
                break;
            case 3:
                packageWeightTextView.setText("More Than 5 kilos");
                break;
        }

        switch (mPackage.getPayPoint().charAt(0)) {
            case 's':
                packagePayPointTextView.setText("Payment at source");
                break;
            case 'd':
                packagePayPointTextView.setText("Payment at destination");
                break;

        }
        packageSourceTextView.setText(mPackage.getSource());
        packageDestinationTextView.setText(mPackage.getDestination());
        Glide.with(getApplicationContext()).load(mPackage.getPhotoUrl()).into(packageImageView);

    }

    private void setCustomerInfo() {
        customerNameTextView.setText(customer.getName());
        customerRatingBar.setRating(customer.getRating());
        Glide.with(getApplicationContext()).load(customer.getPhotoUrl()).into(customerImageView);
    }
}
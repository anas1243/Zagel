package com.example.anas.zagel.Activities.UserInfoActivites;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.anas.zagel.Activities.HomeActivities.CustomerHomeActivity;
import com.example.anas.zagel.Activities.HomeActivities.DeliveryHomeActivity;
import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.Models.User;
import com.example.anas.zagel.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserTypeActivity extends AppCompatActivity {
    User currentUser = MainActivity.currentUser;
    DatabaseReference mDatabaseReference = MainActivity.mDatabaseUsersReference;
    FirebaseDatabase mDatabase = MainActivity.mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acivity_user_type);
        isGPSopened();
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        RelativeLayout customerButton = findViewById(R.id.btn_customer);
        customerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser.getUserType().equals("")) {
                    currentUser.setUserType("c");
                    mDatabaseReference.child(firebaseUser.getUid()).child("userType").setValue("c");
                }
                currentUser.setCurrentUserType("c");
                mDatabaseReference.child(firebaseUser.getUid()).child("currentUserType").setValue("c");

                //Go to home screen
                Intent intent = new Intent(UserTypeActivity.this, CustomerHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        RelativeLayout deliveryButton = findViewById(R.id.btn_deliveryman);
        deliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser.getUserType().equals("")) {
                    currentUser.setUserType("d");
                    mDatabaseReference.child(firebaseUser.getUid()).child("userType").setValue("d");
                }
                currentUser.setCurrentUserType("d");
                mDatabaseReference.child(firebaseUser.getUid()).child("currentUserType").setValue("d");


                Intent intent;
                if (currentUser.getDeliveryMode().getNationalIdUrl().equals("")) {
                    intent = new Intent(UserTypeActivity.this, DeliveryManExtraInfoActivity.class);
                    startActivity(intent);
                } else {
                    intent = new Intent(UserTypeActivity.this, DeliveryHomeActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });
    }

    private void isGPSopened() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled) {
            //TODO ckeck for the connectivity
            showSettingsAlert();
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserTypeActivity.this);

        alertDialog.setTitle("GPS is settings");

        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                UserTypeActivity.this.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

}
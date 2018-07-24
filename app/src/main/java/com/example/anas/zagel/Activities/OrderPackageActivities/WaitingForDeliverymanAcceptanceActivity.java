package com.example.anas.zagel.Activities.OrderPackageActivities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.Models.Package;
import com.example.anas.zagel.R;
import com.google.firebase.database.DatabaseReference;

import static com.example.anas.zagel.Activities.MainActivity.currentUser;
import static com.example.anas.zagel.Activities.MainActivity.mDatabaseUsersReference;

public class WaitingForDeliverymanAcceptanceActivity extends AppCompatActivity {
    private static final String LOG_TAG = WaitingForDeliverymanAcceptanceActivity.class.getSimpleName();
    final Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable = null;
    boolean isAccepted;
    private int i = 0;
    private String id;
    private DatabaseReference acceptedReference;
    private String onlineSetPackage = "";
    private Package mPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_for_deliveryman_acceptance);
        mPackage = (Package) getIntent().getSerializableExtra("package");

        findDeliveryman();
    }

    private void findDeliveryman() {
        Log.e("OrderPackageMapActivity", "entered find delivery man function");
        //TODO work with sorted list

        boolean foundDeliveryman = false;


        while (i < OrderPackageMapActivity.onlineDeliveryMenList.size()) {
            id = OrderPackageMapActivity.onlineDeliveryMenList.get(i).getId();

            onlineSetPackage = OrderPackageMapActivity.onlineDeliveryMenList.get(i).getCurrentPackageID();
            String packageID = mPackage.getId();
            Log.e("OrderPackageMapActivity", "entered while loop");
            Log.e("i =", String.valueOf(i));
            Log.e("find afterid ", id);
            Log.e("find deliverymanPackId", packageID);

            DatabaseReference onlineCurrentPackageRef = MainActivity.mDatabaseOnlineDeliverymenReference.child(id).child("currentPackageID");

            Log.e("onlineSetPackage", "NIHILIST CODE");
            Log.e("onlineSetPackage", onlineSetPackage);

            if ((onlineSetPackage.equals("")) || (onlineSetPackage == null) || (onlineSetPackage.isEmpty())) {
                Log.e(LOG_TAG, "package is set to:" + id);

                onlineCurrentPackageRef.setValue(packageID);
                MainActivity.currentUser.setAttachedDeliveryId(id);
                mDatabaseUsersReference.child(currentUser.getId()).child("attachedDeliveryId").setValue(id);
                foundDeliveryman = true;

                Intent intent = new Intent(WaitingForDeliverymanAcceptanceActivity.this, PackageOnTheWayActivity.class);
                intent.putExtra("package", mPackage);
                startActivity(intent);
                finish();
                break;
            } else {
                i++;
                Log.e("i =", String.valueOf(i));
            }

        }
        if (!foundDeliveryman)
            Toast.makeText(this, "No near online deliveryman available", Toast.LENGTH_LONG).show();


    }

}

package com.example.anas.zagel.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.anas.zagel.Activities.HomeActivities.CustomerHomeActivity;
import com.example.anas.zagel.Activities.HomeActivities.DeliveryHomeActivity;
import com.example.anas.zagel.Activities.UserInfoActivites.DeliveryManExtraInfoActivity;
import com.example.anas.zagel.Activities.UserInfoActivites.FirstBasicInfoActivity;
import com.example.anas.zagel.Activities.UserInfoActivites.UserTypeActivity;
import com.example.anas.zagel.Models.Package;
import com.example.anas.zagel.Models.User;
import com.example.anas.zagel.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String ANONYMOUS = "Anonymous";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 1;
    public static DatabaseReference mDatabaseUsersReference;
    public static DatabaseReference mDatabasePackRef;
    public static DatabaseReference mDatabaseOnlineDeliverymenReference;
    public static FirebaseDatabase mDatabase;
    public static FirebaseStorage storage;
    public static StorageReference storageReference;
    public static User currentUser;
    public static Package currentOrder;
    DatabaseReference currentUserDatabaseReference;
    ValueEventListener valueEventListener;
    // Firebase
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(MainActivity.this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        currentUser = null;
        currentOrder = null;
        mDatabaseUsersReference = mDatabase.getReference().child("users");
        mDatabasePackRef = mDatabase.getReference().child("orders");
        mDatabaseOnlineDeliverymenReference = mDatabase.getReference().child("online_deliverymen");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                Log.e("First  ", String.valueOf(currentUser));
                if (firebaseUser != null) {
                    // User is signed in
                    currentUserDatabaseReference = mDatabaseUsersReference.child(firebaseUser.getUid());
                    //DatabaseReference orderDatabaseReference = mDatabaseUsersReference.child(firebaseUser.getUid());
                   //TODO : leh m3mola mrteen ?!
                    valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            currentUser = dataSnapshot.getValue(User.class);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(MainActivity.this, "cancelled", Toast.LENGTH_SHORT).show();
                            Log.e(LOG_TAG, databaseError.toString());
                        }
                    };
                    currentUserDatabaseReference.addValueEventListener(valueEventListener);

                    currentUserDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            userInfoAction();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    // User is signed out
                    authenticateUser();
                    /*SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    String phoneNumber=pref.getString("phone",null);
                    if(!phoneNumber.isEmpty()) {
                        editor.putString("phone", firebaseUser.getPhoneNumber());  // Saving string
                        editor.commit();
                    }*/


                }
            }
        };
    }

    private void userInfoAction() {
        //currentUserDatabaseReference.removeEventListener(valueEventListener);
        //TODO el loop
        if (currentUser != null) {
            if (currentUser.isBasicInfo()) {
                if (currentUser.getUserType().equals("")) {
                    Intent intent = new Intent(MainActivity.this, UserTypeActivity.class);
                    Log.e(LOG_TAG, "GO to userTypeActivity");
                    startActivity(intent);

                    finish();
                } else if (currentUser.getUserType().equals("d")) {
                    if (currentUser.getDeliveryMode().getNationalIdUrl().isEmpty()) {
                        Intent intent = new Intent(MainActivity.this, DeliveryManExtraInfoActivity.class);
                        Log.e(LOG_TAG, "GO to DeliveryManExtraInfoActivity");
                        startActivity(intent);
                        finish();
                    } else if (currentUser.getDeliveryMode().getElectricityResit().isEmpty()) {
                        Intent intent = new Intent(MainActivity.this, DeliveryManExtraInfoActivity.class);
                        Log.e(LOG_TAG, "GO to DeliveryManExtraInfoActivity");
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(MainActivity.this, DeliveryHomeActivity.class);
                        Log.e(LOG_TAG, "GO to DeliveryHomeActivity");
                        startActivity(intent);
                        finish();
                    }
                } else if (currentUser.getUserType().equals("c")) {
                    Intent intent = new Intent(MainActivity.this, CustomerHomeActivity.class);
                    Log.e(LOG_TAG, "GO to CustomerHomeActivity");
                    startActivity(intent);
                    finish();
                } else if (currentUser.getUserType().equals("b")) {
                    Intent intent = new Intent(MainActivity.this, UserTypeActivity.class);
                    Log.e(LOG_TAG, "GO to UserTypeActivity w ana b");
                    startActivity(intent);
                    finish();
                }
            } else {
                Intent intent = new Intent(MainActivity.this, FirstBasicInfoActivity.class);
                Log.e(LOG_TAG, "GO to FirstBasicInfoActivity");
                startActivity(intent);
                finish();
            }
        } else {
            Intent intent = new Intent(MainActivity.this, FirstBasicInfoActivity.class);
            startActivity(intent);
            Log.e(LOG_TAG, "GO to FirstBasicInfoActivity");
            Log.e(LOG_TAG, "current user equal null");
            finish();
        }
    }


    private void authenticateUser() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build());

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                        finish();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
}

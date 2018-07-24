package com.example.anas.zagel.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.anas.zagel.Activities.HomeActivities.CustomerHomeActivity;
import com.example.anas.zagel.Activities.HomeActivities.DeliveryHomeActivity;
import com.example.anas.zagel.Activities.UserInfoActivites.DeliveryManExtraInfoActivity;
import com.example.anas.zagel.R;
import com.example.anas.zagel.Utilities.DrawerUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.anas.zagel.Activities.MainActivity.currentUser;
import static com.example.anas.zagel.Activities.MainActivity.mDatabaseUsersReference;

public class AccountSettingsActivity extends AppCompatActivity {

    private static final String TAG = AccountSettingsActivity.class.getSimpleName();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    DrawerUtil drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        String name = currentUser.getName();
        String email = currentUser.getEmail();
        Log.e("name me me ", name);
        drawer = new DrawerUtil(name, email);
        drawer.getDrawer(AccountSettingsActivity.this, toolbar);

        /*MultiStateToggleButton modesToggleButton = (MultiStateToggleButton) this.findViewById(R.id.mstb_multi_id);

        if (currentUser.getCurrentUserType().equals("d"))
            modesToggleButton.setValue(0);
        else
            modesToggleButton.setValue(1);

        modesToggleButton.setOnValueChangedListener(new ToggleButton.OnValueChangedListener() {
            @Override
            public void onValueChanged(int position) {
                if (position == 0) { //deliveryman
                    if (currentUser.getCurrentUserType().equals("d"))
                        Toast.makeText(AccountSettingsActivity.this, "You are already a deliveryman!", Toast.LENGTH_SHORT).show();
                    else {
                        showSwitchDialog("d");
                    }
                } else {
                    if (currentUser.getCurrentUserType().equals("c"))
                        Toast.makeText(AccountSettingsActivity.this, "You are already a customer!", Toast.LENGTH_SHORT).show();
                    else {
                        showSwitchDialog("c");
                    }
                }

            }
        });*/

        mDatabaseUsersReference.child(currentUser.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                initializeFields();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ConstraintLayout nameLayout = findViewById(R.id.layout_name_settings);
        nameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNameDialog();
            }
        });

        final ConstraintLayout mobileLayout = findViewById(R.id.layout_mobile_settings);
        mobileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(mobileLayout, "You can't change your registered mobile number.",
                        Snackbar.LENGTH_SHORT).show();
            }
        });

        ConstraintLayout userTypeLayout = findViewById(R.id.layout_account_type_settings);
        userTypeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserTypeDialog();
            }
        });
        ConstraintLayout vehicleLayout = findViewById(R.id.layout_vehicle_settings);
        vehicleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVehicleDialog();
            }
        });

        /*final ConstraintLayout deactivateLayout = findViewById(R.id.layout_deactivate_settings);
        deactivateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deactivateAccount();
            }
        });*/

    }

    private void deactivateAccount() {
        Log.d(TAG, "ingreso a deleteAccount");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    startActivity(new Intent(AccountSettingsActivity.this, MainActivity.class));
                    finish();
                } else {
                    Log.e(TAG, "Something is wrong!");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, e.toString());

            }
        });
    }

    private void showVehicleDialog() {
        final Dialog dialog = new Dialog(AccountSettingsActivity.this);
        dialog.setContentView(R.layout.vehicle_dialog);
        dialog.setCancelable(true);
        ImageButton walkingButton = dialog.findViewById(R.id.btn_walking);
        walkingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser.deliveryMode.setVehicle("w");
                MainActivity.mDatabaseUsersReference.child(currentUser.getId()).child("deliveryMode").child("vehicle").setValue("w");
                dialog.dismiss();
            }
        });
        ImageButton drivingButton = dialog.findViewById(R.id.btn_driving);
        drivingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser.deliveryMode.setVehicle("d");
                MainActivity.mDatabaseUsersReference.child(currentUser.getId()).child("deliveryMode").child("vehicle").setValue("d");
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void initializeFields() {
        TextView nameTextView = findViewById(R.id.tv_name_settings);
        nameTextView.setText(currentUser.getName());

        TextView mobileTextView = findViewById(R.id.tv_mobile_settings);
        mobileTextView.setText(currentUser.getPhone());

        TextView currentUserTypeTextView = findViewById(R.id.tv_user_type_settings);
        switch (currentUser.getCurrentUserType().charAt(0)) {
            case 'c':
                currentUserTypeTextView.setText("Customer");
                break;
            case 'd':
                currentUserTypeTextView.setText("Deliveryman");
                break;
        }

        TextView vehicleLabelTextView = findViewById(R.id.tv_vehicle_label_settings);
        ImageView vehicleIcon = findViewById(R.id.iv_vehicle_settings);
        TextView vehicleTextView = findViewById(R.id.tv_vehicle_settings);
        View line = findViewById(R.id.view2);

        if (currentUser.getCurrentUserType().equals("c")) {
            vehicleIcon.setVisibility(View.GONE);
            vehicleLabelTextView.setVisibility(View.GONE);
            vehicleTextView.setVisibility(View.GONE);
            line.setVisibility(View.GONE);
        } else {
            vehicleIcon.setVisibility(View.VISIBLE);
            vehicleLabelTextView.setVisibility(View.VISIBLE);
            vehicleTextView.setVisibility(View.VISIBLE);
            line.setVisibility(View.VISIBLE);

            switch (currentUser.getDeliveryMode().getVehicle().charAt(0)) {
                case 'w':
                    vehicleTextView.setText("On foot");
                    break;
                case 'd':
                    vehicleTextView.setText("Driving");
                    break;
            }

        }

    }

    private void showNameDialog() {
        final Dialog dialog = new Dialog(AccountSettingsActivity.this);
        dialog.setContentView(R.layout.change_name_dialog);
        dialog.setCancelable(true);
        TextView titleTextView = dialog.findViewById(R.id.tv_title_settings);
        titleTextView.setText("Name");

        final EditText nameEditText = dialog.findViewById(R.id.et_value_settings);
        nameEditText.setText(MainActivity.currentUser.getName());
        nameEditText.setFocusable(true);

        final Button save = dialog.findViewById(R.id.btn_save_settings);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser.setName(nameEditText.getText().toString());
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                MainActivity.mDatabaseUsersReference.child(firebaseUser.getUid()).setValue(currentUser);
                Snackbar.make(save, "You can't change your registered mobile number.",
                        Snackbar.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        Button cancel = dialog.findViewById(R.id.btn_cancel_settings);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showUserTypeDialog() {
        final Dialog dialog = new Dialog(AccountSettingsActivity.this);
        dialog.setContentView(R.layout.user_type_dialog);
        dialog.setCancelable(true);
        Button deliverymanButton = dialog.findViewById(R.id.btn_deliveryman_dialog);
        deliverymanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser.setCurrentUserType("d");
                MainActivity.mDatabaseUsersReference.child(currentUser.getId()).child("currentUserType").setValue("d");

                currentUser.setUserType("b");
                MainActivity.mDatabaseUsersReference.child(currentUser.getId()).child("userType").setValue("b");

                Intent intent;
                if (currentUser.getDeliveryMode().getNationalIdUrl().isEmpty()) {
                    intent = new Intent(AccountSettingsActivity.this, DeliveryManExtraInfoActivity.class);
                } else if (currentUser.getDeliveryMode().getElectricityResit().isEmpty()) {
                    intent = new Intent(AccountSettingsActivity.this, DeliveryManExtraInfoActivity.class);
                } else
                    intent = new Intent(AccountSettingsActivity.this, DeliveryHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        Button customerButton = dialog.findViewById(R.id.btn_customer_dialog);
        customerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser.setCurrentUserType("c");
                MainActivity.mDatabaseUsersReference.child(currentUser.getId()).child("currentUserType").setValue("c");

                currentUser.setUserType("b");
                MainActivity.mDatabaseUsersReference.child(currentUser.getId()).child("userType").setValue("b");

                Intent intent = new Intent(AccountSettingsActivity.this, CustomerHomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        dialog.show();
    }

    private void showSwitchDialog(final String newType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountSettingsActivity.this);
        if (newType.equals("c"))
            builder.setMessage("Switch to customer mode?");
        else
            builder.setMessage("Switch to deliveryman mode?");

        builder.setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        currentUser.setCurrentUserType(newType);
                        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        MainActivity.mDatabaseUsersReference.child(firebaseUser.getUid()).child("currentUserType").setValue(newType);

                        if (!currentUser.getUserType().equals("b")) {
                            currentUser.setUserType("b");
                            MainActivity.mDatabaseUsersReference.child(firebaseUser.getUid()).child("userType").setValue("b");
                        }
                        Intent intent;
                        //TODO a4of el
                        if (newType.equals("c")) {
                            intent = new Intent(AccountSettingsActivity.this, CustomerHomeActivity.class);
                        } else {
                            if (currentUser.getDeliveryMode().getNationalIdUrl().isEmpty()) {
                                intent = new Intent(AccountSettingsActivity.this, DeliveryManExtraInfoActivity.class);
                            } else if (currentUser.getDeliveryMode().getElectricityResit().isEmpty()) {
                                intent = new Intent(AccountSettingsActivity.this, DeliveryManExtraInfoActivity.class);
                            } else
                                intent = new Intent(AccountSettingsActivity.this, DeliveryHomeActivity.class);

                        }
                        startActivity(intent);
                        finish();
                    }
                }).

                setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

}

package com.example.anas.zagel.Activities.UserProfileActivites;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.Models.Birthday;
import com.example.anas.zagel.Models.User;
import com.example.anas.zagel.R;
import com.example.anas.zagel.Utilities.DrawerUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Eltobgy on 27-Mar-18.
 */

public class EditProfileActivity extends ProfileActivity {
    //Initializing Views
    EditText nameEditText, mobileEditText, emailEditText;
    CircleImageView profileImageView;
    ImageButton editImageButton;
    Button saveButton, cancelButton;
    Drawable originalEditTextStyle;
    Spinner genderSpinner;
    DatePicker datePicker;
    ImageView imageView;

    User currentUser = MainActivity.currentUser;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    DrawerUtil drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        drawer = new DrawerUtil(currentUser.getName(), currentUser.getEmail());
        drawer.getDrawer(EditProfileActivity.this, toolbar);


        emailEditText = findViewById(R.id.et_email_edit_profile);
        genderSpinner = findViewById(R.id.spinner_gender_edit_profile);
        datePicker = findViewById(R.id.dp_birthday_edit_profile);

        saveButton = findViewById(R.id.btn_save);
        cancelButton = findViewById(R.id.btn_cancel);

        /**  mUsersDatabaseReference.addValueEventListener(new ValueEventListener() {
        @Override public void onDataChange(DataSnapshot dataSnapshot) {
        showData(dataSnapshot);
        currentUser = dataSnapshot.getValue(User.class);
        if (currentUser != null) {
        name = currentUser.getName();
        email = currentUser.getEmail();
        drawer = new DrawerUtil(name, email);
        drawer.getDrawer(EditProfileActivity.this, toolbar);
        }
        }

        @Override public void onCancelled(DatabaseError databaseError) {

        }
        });**/
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateInfo();
                startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiscardDialog();
            }
        });


    }

    private void updateInfo() {
        //currentUser.setName(nameEditText.getText().toString());
        //currentUser.setPhone(mobileView.getText().toString());
        currentUser.setEmail(emailEditText.getText().toString());

        Spinner genderSpinner = findViewById(R.id.spinner_gender_edit_profile);
        int selectedGenderPosition = genderSpinner.getSelectedItemPosition();
        switch (selectedGenderPosition) {
            case 0: //unknown
                currentUser.setGender("u");
                break;
            case 1:
                currentUser.setGender("m");
                break;
            case 2:
                currentUser.setGender("f");
                break;
            default:
                currentUser.setGender("");
        }

        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();
        MainActivity.currentUser.setBirthday(new Birthday(day, month, year));

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseUserReference.child(firebaseUser.getUid()).setValue(currentUser);
    }

    //TODO Activate this method.
    private void showData(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            // User user = ds.getValue(User.class);

            //TODO change mobile
            //mobileTextView.setText(ds.child(userId).getValue(User.class).getPhone()); //set phone
            emailEditText.setText(currentUser.getEmail());
            int pos = 0;
            String gender = currentUser.getGender();
            switch (gender) {
                case "u": //unknown
                    pos = 0;
                    break;
                case "m":
                    pos = 1;
                    break;
                case "f":
                    pos = 2;
                    break;

            }
            genderSpinner.setSelection(pos);
            //genderView.setText(ds.child(userId).getValue(User.class).getGender()); //set gender

            int day = currentUser.getBirthday().getDay();
            int month = currentUser.getBirthday().getMonth();
            int year = currentUser.getBirthday().getYear();
            datePicker.updateDate(year, month, day);
            // profileImageView.
            /*String photoUrl = (currentUser.getPhotoUrl());
            if (photoUrl.isEmpty())
                Glide.with(getApplicationContext()).load(R.drawable.profile_placeholder).into(profileImageView);
            else
                Glide.with(getApplicationContext()).load(photoUrl).into(profileImageView);*/
        }
    }

    private void showDiscardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard changes?")
                .setCancelable(false)
                .setPositiveButton("Discard", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onBackPressed() {
        showDiscardDialog();
    }
}

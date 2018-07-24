package com.example.anas.zagel.Activities.UserProfileActivites;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.Models.User;
import com.example.anas.zagel.R;
import com.example.anas.zagel.Utilities.DrawerUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileActivity extends AppCompatActivity {
    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();
    public static FirebaseDatabase mFirebaseDatabase;
    public static String userId, name, email;
    private final int PICK_IMAGE_REQUEST = 70;
    // protected DatabaseReference mDatabase;
    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    DatabaseReference mDatabaseUserReference = MainActivity.mDatabaseUsersReference;
    DatabaseReference mUsersDatabaseReference;
    //  DatabaseReference mDatabasePackRef = MainActivity.mDatabasePackRef;
    // FirebaseDatabase mDatabase = MainActivity.mDatabase;
    User currentUser = MainActivity.currentUser;
    //Widgets
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    DrawerUtil drawer;
    //Initializing Views
    TextView birthTextView, mobileTextView, nameTextView, genderTextView, emailTextView;
    RelativeLayout profileImageLayout;
    CircleImageView profileImageView;
    ImageButton editImageButton;
    SimpleRatingBar ratingBar;
    ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");
        // mDatabase = mFirebaseDatabase.getReference();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        //Views
        nameTextView = findViewById(R.id.et_name_edit_profile);
        // mobileTextView = findViewById(R.id.tv_mobile);
        emailTextView = findViewById(R.id.tv_email);
        genderTextView = findViewById(R.id.tv_gender);
        birthTextView = findViewById(R.id.tv_birth);
        ratingBar = findViewById(R.id.rb_rating_profile);
        profileImageLayout = findViewById(R.id.layout_profile_image_profile);
        profileImageView = findViewById(R.id.iv_profile_image);
        editImageButton = findViewById(R.id.update_profile);

        ButterKnife.bind(this);
        // toolbar.setLogo(R.drawable.logo);
        setSupportActionBar(toolbar);


        drawer = new DrawerUtil(currentUser.getName(), currentUser.getEmail());
        drawer.getDrawer(ProfileActivity.this, toolbar);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mUsersDatabaseReference.addValueEventListener(valueEventListener);

        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });

        //TODO fix el relativelayout listener
        profileImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
    }

    private void showData() {

        nameTextView.setText(currentUser.getName()); //set the name
        //set gender
        switch (currentUser.getGender().charAt(0)) {
            case 'f':
                genderTextView.setText("female");
                break;
            case 'u':
                genderTextView.setText("unknown");
                break;
            case 'm':
                genderTextView.setText("male");
                break;
        }
        //TODO move mobile to account settings
        //mobileTextView.setText(currentUser.getPhone()); //set phone
        emailTextView.setText(currentUser.getEmail()); //set email
        birthTextView.setText(currentUser.getBirthday().getYear() + "/"
                + currentUser.getBirthday().getMonth() + "/" +
                currentUser.getBirthday().getDay());
        // profileImageView
        String photoUrl = (currentUser.getPhotoUrl());
        if (photoUrl.isEmpty())
            Glide.with(getApplicationContext())
                    .load(R.drawable.profile_placeholder).into(profileImageView);
        else
            Glide.with(getApplicationContext()).load(photoUrl).into(profileImageView);

        //rating
        ratingBar.setRating(currentUser.getRating());
    }


    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            uploadImage(filePath);

        }
    }

    private void uploadImage(final Uri filePath) {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            mDatabaseUserReference.child(userId).child("photoUrl").setValue(ref.getDownloadUrl().toString());
                            Glide.with(getApplicationContext()).load(filePath).into(profileImageView);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUsersDatabaseReference.removeEventListener(valueEventListener);
    }
}


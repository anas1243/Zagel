package com.example.anas.zagel.Activities.UserInfoActivites;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.anas.zagel.Activities.HomeActivities.DeliveryHomeActivity;
import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class DeliveryManExtraInfoActivity extends AppCompatActivity {
    //Firebase
    public static FirebaseStorage storage;
    public static StorageReference storageReference;
    //Electricity
    private final int PICK_IMAGE_REQUEST_E = 70;
    //National id request
    private final int PICK_IMAGE_REQUEST_ID = 80;

    String userId;
    StorageReference ref;
    int successfulUploads = 0;
    ImageButton choosePhotoButtonElectricity, choosePhotoButtonID;
    private Uri electricityResitPath, nationalIDdPath;
    private DatabaseReference mDatabase = MainActivity.mDatabaseUsersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_man_extra_info_upload);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = firebaseUser.getUid();
        choosePhotoButtonElectricity = findViewById(R.id.btn_choose_photo_electricity);
        choosePhotoButtonElectricity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(PICK_IMAGE_REQUEST_E);

            }
        });

        choosePhotoButtonID = findViewById(R.id.btn_choose_photo_id);
        choosePhotoButtonID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(PICK_IMAGE_REQUEST_ID);

            }
        });

        Button nextButton = findViewById(R.id.btn_next_delivery_man_extra_info);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (electricityResitPath == null) {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.constraint_layout_delivery_extra_info), "Upload resent electricity resit photo", Snackbar.LENGTH_LONG);

                    snackbar.show();
                } else if (nationalIDdPath == null) {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.constraint_layout_delivery_extra_info), "Upload your national ID photo", Snackbar.LENGTH_LONG);

                    snackbar.show();

                } else
                    uploadImage();
            }
        });
    }

    private void chooseImage(int PICK_IMAGE_REQUEST) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST_ID && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            nationalIDdPath = data.getData();
            Glide.with(getApplicationContext()).load(nationalIDdPath).into(choosePhotoButtonID);


        } else if (requestCode == PICK_IMAGE_REQUEST_E && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            electricityResitPath = data.getData();
            Glide.with(getApplicationContext()).load(electricityResitPath).into(choosePhotoButtonElectricity);

        }
    }

    private void uploadImage() {

        if (nationalIDdPath != null && electricityResitPath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            ref = storageReference.child("electricity_resits/" + UUID.randomUUID().toString());
            ref.putFile(electricityResitPath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            mDatabase.child(userId).child("deliveryMode").child("electricityResit").setValue(ref.getDownloadUrl().toString());
                            successfulUploads++;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(DeliveryManExtraInfoActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

            ref = storageReference.child("national_IDs/" + UUID.randomUUID().toString());
            ref.putFile(nationalIDdPath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDatabase.child(userId).child("deliveryMode").child("nationalIdUrl").setValue(ref.getDownloadUrl().toString());
                            successfulUploads++;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(DeliveryManExtraInfoActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

            if (successfulUploads == 2) {
                progressDialog.dismiss();
                Toast.makeText(DeliveryManExtraInfoActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DeliveryManExtraInfoActivity.this, DeliveryHomeActivity.class);
                startActivity(intent);
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
                        startActivity(new Intent(DeliveryManExtraInfoActivity.this, MainActivity.class));
                        finish();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
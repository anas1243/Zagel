package com.example.anas.zagel.Activities.UserInfoActivites;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.Glide;
import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.Models.User;
import com.example.anas.zagel.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.anas.zagel.Activities.MainActivity.currentUser;

public class FirstBasicInfoActivity extends AppCompatActivity {
    private final int PICK_IMAGE_REQUEST = 71;
    private Uri filePath;
    private EditText nameEditText;
    private CircleImageView imageView;
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_basic_info);
        currentUser = new User();
        imageView = findViewById(R.id.iv_image_first_basic_info);
        nameEditText = findViewById(R.id.tv_name_first_basic_info);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });


        Button nextButton = findViewById(R.id.btn_next_first_basic_info);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameEditText.getText().toString().equals("")) {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.constraint_layout_first_basic_profile), "Enter your name", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    uploadData();
                }
            }
        });
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
            filePath = data.getData();
            Glide.with(getApplicationContext()).load(filePath).into(imageView);
        }
    }

    private void uploadData() {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            String name = nameEditText.getText().toString();
            currentUser.setName(name);

            final StorageReference ref = MainActivity.storageReference.child("profile_images/" + UUID.randomUUID().toString());
            uploadTask = ref.putFile(filePath);

            Task<Uri> urlTask = uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                            .getTotalByteCount());
                    progressDialog.setMessage((int) progress + "%" + "uploaded");
                }
            }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        currentUser.setPhotoUrl(downloadUri.toString());
                        Intent intent = new Intent(FirstBasicInfoActivity.this, BasicInfoActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Handle failures
                        // ...
                    }

                }
            });
        } else {

            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.constraint_layout_first_basic_profile), "Choose your photo", Snackbar.LENGTH_LONG);

            snackbar.show();
        }
    }

}

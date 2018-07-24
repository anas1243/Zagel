package com.example.anas.zagel.Utilities;

import com.example.anas.zagel.Activities.MainActivity;
import com.google.firebase.storage.StorageReference;

/**
 * Created by Toka on 2018-04-21.
 */

public class UploadImageUtility {
    private StorageReference storageReference = MainActivity.storageReference;

    /*public static Intent chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        return intent;
    }

    public boolean uploadImage(Uri filePath) {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog();
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(UploadNationalIdActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            mDatabase.child(userId).child("photoUrl").setValue(taskSnapshot.getDownloadUrl().toString());

                            Intent intent = new Intent(UploadNationalIdActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UploadNationalIdActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        }
    }*/
}

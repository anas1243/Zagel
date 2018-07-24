package com.example.anas.zagel.Activities.UserInfoActivites;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.Models.Birthday;
import com.example.anas.zagel.Models.User;
import com.example.anas.zagel.R;
import com.example.anas.zagel.SharedPrefManager;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class BasicInfoActivity extends AppCompatActivity {
    User currentUser = MainActivity.currentUser;
    DatabaseReference mDatabaseReference = MainActivity.mDatabaseUsersReference;
    FirebaseUser firebaseUser;

    Button nextButton;
    EditText emailEditText;
    Spinner genderSpinner;
    DatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_info);
        nextButton = findViewById(R.id.btn_to_user_type);
        emailEditText = findViewById(R.id.et_email);
        genderSpinner = findViewById(R.id.spinner_gender_basic_info);
        datePicker = findViewById(R.id.dp_birthday_basic_info);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserData();
            }
        });
    }


    private void getUserData() {
        currentUser.setEmail(emailEditText.getText().toString());

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
        currentUser.setBirthday(new Birthday(day, month, year));

        currentUser.setBasicInfo(true);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUser.setId(firebaseUser.getUid());
        currentUser.setPhone(firebaseUser.getPhoneNumber());

        String token = SharedPrefManager.getInstance(this).getDeviceToken();//add to fire base
        currentUser.setToken(token);

        mDatabaseReference.child(firebaseUser.getUid()).setValue(currentUser);
        Intent intent = new Intent(BasicInfoActivity.this, UserTypeActivity.class);
        startActivity(intent);

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
                        startActivity(new Intent(BasicInfoActivity.this, MainActivity.class));
                        finish();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

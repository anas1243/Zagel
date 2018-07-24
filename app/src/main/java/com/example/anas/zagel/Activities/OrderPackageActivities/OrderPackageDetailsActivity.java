package com.example.anas.zagel.Activities.OrderPackageActivities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.Models.Package;
import com.example.anas.zagel.Models.User;
import com.example.anas.zagel.R;
import com.example.anas.zagel.Utilities.DrawerUtil;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;


public class OrderPackageDetailsActivity extends AppCompatActivity {
    //Map
    private static final String TAG = OrderPackageDetailsActivity.class.getSimpleName();
    private static final int PICK_IMAGE_REQUEST = 70;
    //Firebase
    //public static DatabaseReference mDatabasePackRef;
    public static String userId, name, email;
    Uri filePath;
    DatabaseReference mDatabasePackRef = MainActivity.mDatabasePackRef;
    FirebaseDatabase mDatabase = MainActivity.mDatabase;
    User currentUser = MainActivity.currentUser;
    DatabaseReference mDatabaseReference = MainActivity.mDatabaseUsersReference;
    //Widgets
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    DrawerUtil drawer;

    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_package_details);
        nextButton = findViewById(R.id.btn_next_order_package_details);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPackage();
            }
        });

        ButterKnife.bind(this);
        // toolbar.setLogo(R.drawable.logo);
        setSupportActionBar(toolbar);

        drawer = new DrawerUtil(MainActivity.currentUser.getName(), MainActivity.currentUser.getEmail());
        drawer.getDrawer(OrderPackageDetailsActivity.this, toolbar);

        //initializing package database reference
        mDatabasePackRef = mDatabase.getReference().child("orders");

        LinearLayout uploadPhotoLayout = findViewById(R.id.layout_upload_package_photo);
        uploadPhotoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
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
            Glide.with(getApplicationContext()).load(filePath).into((ImageView) findViewById(R.id.iv_package_photo));
        }
    }

    public void createPackage() {
        final Package mPackage = new Package();
        //declaring Views
        final EditText nameEditText = findViewById(R.id.package_name);
        final EditText descEditText = findViewById(R.id.package_description);
        final Spinner weightSpinner = findViewById(R.id.spinner_weight);
        final CheckBox breakableCheckBox = findViewById(R.id.cb_breakable);

        String name = nameEditText.getText().toString();
        int selectedWeightPosition = weightSpinner.getSelectedItemPosition();
        int weightLevel;
        switch (selectedWeightPosition) {
            case 0: //unknown
                weightLevel = 1;
                break;
            case 1:
                weightLevel = 2;
                break;
            case 2:
                weightLevel = 3;
                break;
            default:
                weightLevel = 0;
        }
        String packDescription = (descEditText.getText().toString());

        mPackage.setName(name);
        mPackage.setBreakable(breakableCheckBox.isChecked());
        mPackage.setDescription(packDescription);
        mPackage.setWeight(weightLevel);

        RadioGroup rg = findViewById(R.id.radio_group);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_source:
                        mPackage.setPayPoint("s");
                        break;
                    case R.id.rb_destination:
                        mPackage.setPayPoint("d");
                        break;
                }
            }
        });

        if (name.equals("")) {
            nameEditText.setError("Set a name for your package");
        } else {
            if (packDescription.equals("")) {
                descEditText.setError("Set a description for your package");
            } else {
                if (filePath == null) {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.relativeLayout3), "Set a description for your package", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    //mPackage.setPhotoUrl(filePath.toString());
                    Glide.with(getApplicationContext()).load(filePath).into((ImageView) findViewById(R.id.iv_package_photo));
                    Intent intent = new Intent(OrderPackageDetailsActivity.this, OrderPackageMapActivity.class);
                    intent.putExtra("package", mPackage);
                    intent.putExtra("imagePath", filePath.toString());
                    Log.e(TAG, "filepath: " + filePath);
                    startActivity(intent);
                }
            }
        }

    }


}
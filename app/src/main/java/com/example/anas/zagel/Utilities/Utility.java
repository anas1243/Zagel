package com.example.anas.zagel.Utilities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.anas.zagel.Models.Package;
import com.example.anas.zagel.Models.User;
import com.example.anas.zagel.R;

public class Utility {
    private static String phone;
 /*   public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }*/

    public static int calculateRating(int counter, int ratingSummation, int newRating) {
        if (counter == 0)
            return 0;
        else
            return Math.round(ratingSummation + newRating) / counter;
    }


    public static void setCustomerAndPackageData(User customer, Package mPackage, View layout) {
        TextView customerNameTextView = layout.findViewById(R.id.tv_customer_name_popup);
        TextView customerPhoneTextView = layout.findViewById(R.id.tv_customer_phone_popup);

        customerNameTextView.setText(customer.getName());
        customerPhoneTextView.setText(customer.getPhone());

        TextView packageNameTextView = layout.findViewById(R.id.tv_package_name_popup);
        TextView packageDescriptionTextView = layout.findViewById(R.id.tv_package_description_popup);

        packageNameTextView.setText(mPackage.getName());
        packageDescriptionTextView.setText(mPackage.getDescription());

    }

    public static void showPopup(final Activity context, Point p, User customer, Package mPackage) {
        int popupWidth = 500;
        int popupHeight = 700;

        // Inflate the popup_layout.xml
        ConstraintLayout viewGroup = context.findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.second, viewGroup);
        Button callButton = layout.findViewById(R.id.imageCallButton);

        Log.e("UTILITY", String.valueOf(callButton));
        phone = customer.getPhone();
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String firebaseUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();


                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                context.startActivity(intent);

            }
        });


        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(context);
        popup.setContentView(layout);
       /* popup.setWidth(popupWidth);
        popup.setHeight(popupHeight);*/
        popup.setFocusable(true);


        // Some offset to align the popup a bit to the right, and a bit down, relative to button's position.
        int OFFSET_X = 30;
        int OFFSET_Y = 30;

        // Clear the default translucent background
        popup.setBackgroundDrawable(new BitmapDrawable());

        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);


        Utility.setCustomerAndPackageData(customer, mPackage, layout);
    }


}

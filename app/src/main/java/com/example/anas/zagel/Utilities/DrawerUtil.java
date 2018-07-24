package com.example.anas.zagel.Utilities;


import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.anas.zagel.Activities.AccountSettingsActivity;
import com.example.anas.zagel.Activities.HomeActivities.CustomerHomeActivity;
import com.example.anas.zagel.Activities.HomeActivities.DeliveryHomeActivity;
import com.example.anas.zagel.Activities.MainActivity;
import com.example.anas.zagel.Activities.UserProfileActivites.ProfileActivity;
import com.example.anas.zagel.Models.OnlineDeliveryMan;
import com.example.anas.zagel.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondarySwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;

import static com.example.anas.zagel.Activities.HomeActivities.DeliveryHomeActivity.currentLocation;
import static com.example.anas.zagel.Activities.MainActivity.currentUser;


public class DrawerUtil extends Activity {
    String userId, photoUrl, name, email, uId;

    public DrawerUtil(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void getDrawer(final Activity activity, Toolbar toolbar) {
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder, String tag) {
                Glide.with(activity).load(MainActivity.currentUser.getPhotoUrl()).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }
        });
        // Create the AccountHeader
        DrawerUtil drawer = new DrawerUtil(name, email);

        PrimaryDrawerItem drawerItemHome = new PrimaryDrawerItem().withIdentifier(1)
                .withName(R.string.nav_home).withIcon(R.drawable.ic_home);
        PrimaryDrawerItem drawerItemProfile = new PrimaryDrawerItem()
                .withIdentifier(2).withName(R.string.nav_profile).withIcon(R.drawable.ic_profile);


        SecondaryDrawerItem drawerItemNotify = new SecondaryDrawerItem().withIdentifier(3)
                .withName(R.string.nav_listen).withIcon(R.drawable.ic_notification);

        SecondaryDrawerItem drawerItemSettings = new SecondaryDrawerItem().withIdentifier(4)
                .withName(R.string.nav_settings).withIcon(R.drawable.ic_settings);
        SecondaryDrawerItem drawerItemLogOut = new SecondaryDrawerItem().withIdentifier(5)
                .withName(R.string.nav_logout).withIcon(R.drawable.ic_logout);
        SecondaryDrawerItem drawerItemAboutUs = new SecondaryDrawerItem().withIdentifier(6)
                .withName(R.string.nav_about_us).withIcon(R.drawable.ic_about_us);

        final SecondarySwitchDrawerItem onlineToggle = new SecondarySwitchDrawerItem();
        onlineToggle.withIdentifier(7);
        onlineToggle.withName("online");

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.drawable.clouds_background)
                .addProfiles(
                        new ProfileDrawerItem().withName(name).withEmail(email)
                                .withIcon(MainActivity.currentUser.getPhotoUrl())
                        //.withTextColor(R.color.colorPrimaryDark)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })//.withTextColor(getResources().getColor(R.color.colorPrimaryDark))
                .build();


        //create the drawer and remember the `Drawer` result object
        final Drawer result = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withCloseOnClick(true)
                .withSelectedItem(-1)
                .addDrawerItems(
                        drawerItemHome,
                        drawerItemProfile,
                        //onlineToggle,
                        new DividerDrawerItem(),
                        //drawerItemNotify,
                        drawerItemSettings,
                        new DividerDrawerItem(),
                        //drawerItemAboutUs,
                        drawerItemLogOut
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem.getIdentifier() == 1 && (!(activity instanceof CustomerHomeActivity) ||
                                !(activity instanceof DeliveryHomeActivity))) {
                            // load home screen.
                            Intent intent;
                            if (MainActivity.currentUser.getCurrentUserType().equals("c"))
                                intent = new Intent(activity, CustomerHomeActivity.class);
                            else
                                intent = new Intent(activity, DeliveryHomeActivity.class);
                            view.getContext().startActivity(intent);
                        } else if (drawerItem.getIdentifier() == 2 && !(activity instanceof ProfileActivity)) {
                            // load profile/user screen.
                            Intent intent = new Intent(activity, ProfileActivity.class);
                            view.getContext().startActivity(intent);
                        } else if (drawerItem.getIdentifier() == 3 && !(activity instanceof MainActivity)) {
                            // load notifications/newsfeed screen.
                            //TODO replace the activity.
                            Intent intent = new Intent(activity, MainActivity.class);
                            view.getContext().startActivity(intent);
                        } else if (drawerItem.getIdentifier() == 4 && !(activity instanceof AccountSettingsActivity)) {
                            // load settings screen
                            Intent intent = new Intent(activity, AccountSettingsActivity.class);
                            view.getContext().startActivity(intent);
                        } else if (drawerItem.getIdentifier() == 5) {
                            // ToDo replace with logout.
                            AuthUI.getInstance().signOut(activity).addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    // user is now signed out

                                }
                            });
                            Intent intent = new Intent(activity, MainActivity.class);
                            view.getContext().startActivity(intent);
                        } /*else if (drawerItem.getIdentifier() == 6 && !(activity instanceof AboutUsActivity)) {
                            // load aboutus screen
                            Intent intent = new Intent(activity, AboutUsActivity.class);
                            view.getContext().startActivity(intent);
                        }*/ else if (drawerItem.getIdentifier() == 7) {
                            Log.e("hey", "**************");

                            if (onlineToggle.isChecked()) {
                                Log.e("", "toggle is checked");

                                com.example.anas.zagel.Models.Location delivaryManLocation = new com.example.anas.zagel.Models.Location(currentLocation.getLatitude() + "", currentLocation.getLongitude() + "");
                                OnlineDeliveryMan onlineDeliveryMan = new OnlineDeliveryMan();
                                onlineDeliveryMan.setId(MainActivity.currentUser.getId());
                                onlineDeliveryMan.setLocation(delivaryManLocation);

                                MainActivity.mDatabaseOnlineDeliverymenReference.child(onlineDeliveryMan.getId()).setValue(onlineDeliveryMan);
                            } else {
                                Log.e("", "toggle is not checked");
                                //TODO functionality of toggle button
                                MainActivity.mDatabaseOnlineDeliverymenReference.child(currentUser.getId()).removeValue();

                            }
                        }
                        return true;
                    }
                })
                .build();
        //TODO ash8l el online ll deliveryman
        if (MainActivity.currentUser.getCurrentUserType().equals("d"))
            result.addItemAtPosition(onlineToggle, 1);


    }

}